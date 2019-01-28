package io.github.chrislo27.rhre3.modding

import com.badlogic.gdx.files.FileHandle
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import java.util.*


class ModdingMetadata(private val registryData: GameRegistry.RegistryData,
                      private val sourceFolder: FileHandle, private val customFolder: FileHandle) {

    class BadModdingMetadataException(data: Data, fh: FileHandle, message: String)
        : RuntimeException("Error in parsing modding metadata ${data.game.id}/${fh.name()}: $message")

    inner class Data(val game: ModdingGame) {
        val mappedData: Map<Any, Map<MetadataField, MetadataValue>> = mutableMapOf()

        fun joinToString(map: Map<MetadataField, MetadataValue>, entity: Entity?, keyColor: String = "LIGHT_GRAY", unknownColor: String = "ORANGE"): String {
            return map.entries.joinToString(separator = "\n") { (key, value) ->
                "[${if (key.unknown) unknownColor else keyColor}]${key.name}:[] ${value.getValue(entity)}"
            }
        }

        fun joinToStringFromData(any: Any, entity: Entity?, keyColor: String = "LIGHT_GRAY", unknownColor: String = "ORANGE"): String {
            return mappedData[any]?.let { joinToString(it, entity, keyColor, unknownColor) } ?: ""
        }
    }

    private val dataMap: MutableMap<ModdingGame, Data> = mutableMapOf()
    val currentData: Data get() = get(ModdingUtils.currentGame)

    init {
        var totalLoaded = 0
        ModdingGame.VALUES.forEach { moddingGame ->
            val fileMap: MutableMap<String, FileHandle> = mutableMapOf()
            // Search folders
            val sourceFdr = sourceFolder.child("${moddingGame.id}/")
            if (sourceFdr.exists()) {
                sourceFdr.list(".json").forEach { fh ->
                    fileMap[fh.name()] = fh
                }
            }
            val customFdr = customFolder.child("${moddingGame.id}/")
            if (customFdr.exists()) {
                customFdr.list(".json").forEach { fh ->
                    val name = fh.name()
                    if (fileMap.containsKey(name)) {
                        Toolboks.LOGGER.info("Overwriting modding metadata ${moddingGame.id}/$name with custom one")
                    }
                    fileMap[name] = fh
                }
            } else {
                customFdr.mkdirs()
            }

            val data: Data = get(moddingGame)
            fileMap.values.forEach { fh ->
                try {
                    load(data, fh)
                    totalLoaded++
                } catch (be: BadModdingMetadataException) {
                    Toolboks.LOGGER.error(be.message ?: "Failed to load modding metadata ${moddingGame.id}/${fh.name()}")
                } catch (e: Exception) {
                    Toolboks.LOGGER.error("Failed to load modding metadata ${moddingGame.id}/${fh.name()}")
                    e.printStackTrace()
                }
            }
        }
        Toolboks.LOGGER.info("Loaded modding metadata; $totalLoaded total files loaded")
    }

    operator fun get(game: ModdingGame): Data = dataMap.getOrPut(game) { Data(game) }

    private fun getID(any: Any): String = when (any) {
        is Datamodel -> any.id
        is Game -> any.id
        else -> error("getID failed for $any (${any::class.java.canonicalName})")
    }

    private fun load(data: Data, fileHandle: FileHandle) {
        fun badMetadata(msg: String): Nothing = throw BadModdingMetadataException(data, fileHandle, msg)
        data.mappedData as MutableMap

        val dataName = "${data.game.id}/${fileHandle.name()}"
        val root: JsonNode = JsonHandler.OBJECT_MAPPER.readTree(fileHandle.file())
        // The tree must be rooted with an array
        if (!root.isArray) {
            badMetadata("The root of the json file should be an array.${if (root.isObject) " It seems it is an object, so please surround it with []." else ""}")
        }

        root.forEachIndexed { arrayIndex, tree ->
            if (!tree.has("applyTo"))
                badMetadata("[$arrayIndex].applyTo string array required.")
            val applyToNode: ArrayNode = (tree["applyTo"]?.takeUnless { !it.isArray } as? ArrayNode)
                    ?: badMetadata("[$arrayIndex].applyTo must be a string array.")
            val applyToIDs: List<String> = applyToNode.map { subnode ->
                if (!subnode.isTextual) badMetadata("[$arrayIndex].applyTo string array should be only strings, found ${subnode.nodeType}=${subnode.asText()}.")
                if (subnode.asText().isBlank()) badMetadata("[$arrayIndex].applyTo string array cannot have blank entries.")
                subnode.asText()
            }.distinct()
            if (applyToIDs.size < applyToNode.size()) {
                badMetadata("[$arrayIndex].applyTo string array cannot have duplicates ().")
            }
            val mappedApplyTo: Map<Any, IDType> = applyToIDs.associate {
                // Attempt to find game, then no deprecations, then deprecations (with warning), then error
                registryData.gameMap[it]?.to(IDType.GAME)
                        ?: registryData.noDeprecationsObjectMap[it]?.to(IDType.DATAMODEL)
                        ?: registryData.objectMap[it]?.also { dm ->
                            Toolboks.LOGGER.warn("Warning in $dataName[$arrayIndex].applyTo: $it refers to a deprecated ID, use ${dm.id} instead")
                        }?.to(IDType.DATAMODEL)
                        ?: badMetadata("Error in [$arrayIndex].applyTo: $it does not exist as a game or datamodel.")
            }

            tree.fieldNames().asSequence().filter { it != "applyTo" }.forEach { fieldName ->
                val node = tree[fieldName]!!
                val metadataField: MetadataField = MetadataField.GLOBAL_FIELDS[fieldName]
                        ?: MetadataField.GAME_FIELDS[data.game]?.get(fieldName)
                        ?: run {
                            Toolboks.LOGGER.warn("Unrecognized metadata field name $dataName[$arrayIndex].\"$fieldName\"")
                            MetadataField(fieldName, fieldName, EnumSet.allOf(IDType::class.java), true)
                        }

                val metadataValue: MetadataValue = when {
                    node.isTextual -> StaticValue(node.asText())
                    node.isObject && node["function"]?.takeIf { it.isTextual }?.asText() == "widthRange" ->
                        WidthRangeValue().also { widthRange ->
                            node.fieldNames().forEach { name ->
                                val subnode = node[name]!!
                                if (subnode.isTextual) {
                                    val textValue = subnode.asText()
                                    if (name == "else") {
                                        widthRange.elseValue = textValue
                                    } else if (name.toFloatOrNull() != null) {
                                        val v = name.toFloat()
                                        widthRange.exactValues[v..v] = textValue
                                    } else {
                                        val matchResult = RangeValue.REGEX.matchEntire(name)
                                        if (matchResult != null) {
                                            val range: ClosedRange<Float> = matchResult.groupValues[1].toFloat()..matchResult.groupValues[2].toFloat()
                                            if (range.isEmpty()) {
                                                Toolboks.LOGGER.warn("$dataName[$arrayIndex].\"$fieldName\".\"name\" is an empty range")
                                            }
                                            widthRange.exactValues[range] = textValue
                                        } else {
                                            badMetadata("Unrecognized field format for width range function $dataName[$arrayIndex].\"$fieldName\" \"$name\"")
                                        }
                                    }
                                }
                            }
                        }
                    else -> badMetadata("Unable to parse value as string or function object for [$arrayIndex].$metadataField.")
                }
                if (metadataValue.needsEntity && IDType.GAME in mappedApplyTo.values) {
                    Toolboks.LOGGER.warn("Warning for $dataName[$arrayIndex].$metadataField: Function declared but there are game IDs in applyTo")
                }

                // Map this data to each datamodel/game in mappedApplyTo
                mappedApplyTo.forEach { any, type ->
                    val newMap = data.mappedData.getOrPut(any) { linkedMapOf() } as MutableMap
                    if (newMap[metadataField] != null) {
                        Toolboks.LOGGER.warn("Duplicate metadata field $dataName[$arrayIndex].$metadataField for ${getID(any)}")
                    } else {
                        // Provide warnings if the metadataField is incompatible with the IDType of items in mappedApplyTo
                        if (type !in metadataField.idTypes) {
                            Toolboks.LOGGER.warn("Warning for $dataName[$arrayIndex].$metadataField: Field supports ${metadataField.idTypes} but applyTo has a $type")
                        }
                    }

                    newMap[metadataField] = metadataValue
                }
            }
        }

    }
}

enum class IDType {
    GAME, DATAMODEL
}

data class MetadataField(val jsonField: String, val name: String, val idTypes: EnumSet<IDType>, val unknown: Boolean = false) {
    companion object {
        val GLOBAL_FIELDS: Map<String, MetadataField> =
                linkedMapOf(MetadataField("note", "Note", EnumSet.allOf(IDType::class.java)).toPair())
        val GAME_FIELDS: Map<ModdingGame, Map<String, MetadataField>> =
                mapOf(ModdingGame.MEGAMIX_NA to linkedMapOf(
                        MetadataField("sub", "Sub", EnumSet.of(IDType.DATAMODEL)).toPair(),
                        MetadataField("name", "Name", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("engine", "Engine", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("tempoFile", "Tempo File", EnumSet.of(IDType.GAME)).toPair(),
                        MetadataField("index", "Index", EnumSet.of(IDType.GAME)).toPair()
                                                           ))
    }

    private fun toPair(): Pair<String, MetadataField> = jsonField to this
}
