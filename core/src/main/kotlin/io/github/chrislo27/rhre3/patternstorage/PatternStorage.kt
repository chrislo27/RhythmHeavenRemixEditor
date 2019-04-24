package io.github.chrislo27.rhre3.patternstorage

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import java.util.*


object PatternStorage {

    val MAX_PATTERN_NAME_SIZE: Int = 32

    private val FOLDER: FileHandle by lazy {
        RHRE3.RHRE3_FOLDER.child("storedPatterns/").apply {
            mkdirs()
        }
    }

    val patterns: Map<UUID, FileStoredPattern> = linkedMapOf()

    fun load() {
        patterns as MutableMap
        patterns.clear()

        val files = FOLDER.list(".json")
        files.forEach { fh ->
            try {
                val json = fh.readString("UTF-8")
                val p = JsonHandler.fromJson<FileStoredPattern>(json)
                p.filename = fh.name()
                patterns[p.uuid] = p
            } catch (e: Exception) {
                Toolboks.LOGGER.warn("Failed to load stored pattern ${fh.name()}")
                e.printStackTrace()
            }
        }

        sort()
    }

    fun persist(deleteAllOthers: Boolean = false) {
        val values = patterns.values.toList()

        values.mapNotNull { pattern ->
            val fh = FOLDER.child(pattern.filename ?: ("${pattern.uuid}.json"))

            try {
                fh.writeString(JsonHandler.toJson(pattern), false, "UTF-8")
                pattern.filename = fh.name()

                pattern
            } catch (e: Exception) {
                Toolboks.LOGGER.warn("Failed to save stored pattern ${pattern.name} (${pattern.uuid})")
                e.printStackTrace()
                null
            }
        }.fold(JsonHandler.OBJECT_MAPPER.createArrayNode()) { arrayNode, pattern ->
            arrayNode.addObject().apply {
                this.put("file", pattern.filename)
                this.put("name", pattern.name)
                this.put("uuid", pattern.uuid.toString())
            }
            arrayNode
        }.also { arrayNode ->
            FOLDER.child("list/").apply { mkdirs() }.child("list.json").writeString(JsonHandler.toJson(arrayNode), false, "UTF-8")
        }
        val filenames = values.mapNotNull(FileStoredPattern::filename)
        FOLDER.list().filter { it.name() !in filenames }.forEach { it.delete() }
    }

    fun addPattern(pattern: FileStoredPattern): PatternStorage {
        patterns as MutableMap
        patterns[pattern.uuid] = pattern
        sort()
        return this
    }

    fun deletePattern(pattern: FileStoredPattern): PatternStorage {
        patterns as MutableMap
        patterns.remove(pattern.uuid, pattern)
        sort()
        return this
    }

    private fun sort() {
        val values = patterns.values.toList().sortedBy { it.name.toLowerCase(Locale.ROOT) }

        patterns as MutableMap
        patterns.clear()
        values.forEach { patterns[it.uuid] = it }
    }

}