package io.github.chrislo27.rhre3.patternstorage

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.util.JsonHandler
import java.util.*


object PatternStorage {

    val MAX_PATTERN_NAME_SIZE: Int = 32

    private lateinit var preferences: Preferences

    val patterns: Map<UUID, StoredPattern> = linkedMapOf()

    fun load(prefs: Preferences) {
        patterns as MutableMap
        preferences = prefs
        preferences.getString("patterns", null)?.let { json ->
            try {
                val array = JsonHandler.fromJson<Array<StoredPattern>>(json)
                array.forEach { patterns[it.uuid] = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resort()
    }

    fun persist() {
        if (this::preferences.isInitialized) {
            val array = JsonHandler.OBJECT_MAPPER.createArrayNode()

            patterns.values.forEach {
                try {
                    array.addPOJO(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            preferences.putString("patterns", JsonHandler.toJson(array))

            preferences.flush()
        }
    }

    fun addPattern(pattern: StoredPattern): PatternStorage {
        patterns as MutableMap
        patterns[pattern.uuid] = pattern
        resort()
        return this
    }

    fun deletePattern(pattern: StoredPattern): PatternStorage {
        patterns as MutableMap
        patterns.remove(pattern.uuid, pattern)
        resort()
        return this
    }

    private fun resort() {
        val values = patterns.values.toList().sortedBy { it.name.toLowerCase(Locale.ROOT) }

        patterns as MutableMap
        patterns.clear()
        values.forEach { patterns[it.uuid] = it }
    }

}