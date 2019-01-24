package io.github.chrislo27.rhre3.modding

import com.badlogic.gdx.files.FileHandle
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import java.util.*


class ModdingMetadata(private val registryData: GameRegistry.RegistryData,
                      private val sourceFolder: FileHandle, private val customFolder: FileHandle) {

    class BadModdingMetadataException(data: Data, fh: FileHandle, message: String)
        : RuntimeException("Error in parsing modding metadata ${data.game.id}/${fh.name()}: $message")

    inner class Data(val game: ModdingGame) {

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
            }

            val data: Data = get(moddingGame)
            fileMap.values.forEach { fh ->
                try {
                    load(data, fh)
                    totalLoaded++
                } catch (be: BadModdingMetadataException){
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

    private fun load(data: Data, fileHandle: FileHandle) {
        fun badMetadata(msg: String): Nothing = throw BadModdingMetadataException(data, fileHandle, msg)

        val dataName = "${data.game.id}/${fileHandle.name()}"
        val root: JsonNode = JsonHandler.OBJECT_MAPPER.readTree(fileHandle.file())
        // The tree must be rooted with an array
        if (!root.isArray) {
            badMetadata("The root of the json file should be an array.${if (root.isObject) " It seems it is an object, so please surround it with []." else ""}")
        }

        root.forEach { tree ->
            if (!tree.has("applyTo"))
                badMetadata("applyTo string array required.")
            val applyToNode: ArrayNode = (tree["applyTo"]?.takeUnless { !it.isArray } as? ArrayNode) ?: badMetadata("applyTo must be a string array.")
            val applyTo: List<String> = applyToNode.map { subnode ->
                if (!subnode.isTextual) badMetadata("applyTo string array should be only strings, found ${subnode.nodeType}=${subnode.asText()}.")
                if (subnode.asText().isBlank()) badMetadata("applyTo string array cannot have blank entries.")
                subnode.asText()
            }.distinct()

            tree.fieldNames().forEach { nodeName ->
                val node = tree[nodeName]!!
                val metadataField: MetadataField? = MetadataField.GLOBAL_FIELDS[nodeName] ?: MetadataField.GAME_FIELDS[data.game]?.get(nodeName)
                if (metadataField == null) {
                    Toolboks.LOGGER.warn("Unrecognized metadata field name \"$nodeName\" in $dataName")
                }
                // TODO do something with the node
            }
        }

    }
}

enum class IDType {
    GAME, DATAMODEL
}

data class MetadataField(val jsonField: String, val name: String, val idTypes: EnumSet<IDType>) {
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

    private fun toPair(): Pair<String, MetadataField> = name to this
}
