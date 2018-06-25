package io.github.chrislo27.rhre3.patternstorage

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.util.JsonHandler
import java.util.*


object PatternStorage {

    val MAX_PATTERN_NAME_SIZE: Int = 32

    private lateinit var preferences: Preferences

    val patterns: Map<UUID, StoredPattern> = mutableMapOf()

    fun load(prefs: Preferences) {
        patterns as MutableMap
        preferences = prefs
        preferences.get().filter { it.key.startsWith("pattern_") }
                .forEach { key, value ->
                    try {
                        // Check UUID is valid
                        UUID.fromString(key.substringAfter("pattern_"))

                        val pattern = JsonHandler.fromJson<StoredPattern>(value as String)
                        patterns[pattern.uuid] = pattern
                    } catch (e: IllegalArgumentException) {
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
    }

    fun persist() {
        if (this::preferences.isInitialized) {
            preferences.clear()

            patterns.values.forEach {
                try {
                    preferences.putString("pattern_" + it.uuid.toString(), JsonHandler.toJson(it))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            preferences.flush()
        }
    }

    fun addPattern(pattern: StoredPattern): PatternStorage {
        patterns as MutableMap
        patterns[pattern.uuid] = pattern
        return this
    }

}