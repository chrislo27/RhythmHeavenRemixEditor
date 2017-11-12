package io.github.chrislo27.rhre3.goat

import com.badlogic.gdx.Preferences


class GoatData(var level: Int, var food: Int, var name: String,
               var background: GoatBackground, var hat: GoatHat, var face: GoatFace) {

    companion object {
        fun createFromPreferences(preferences: Preferences): GoatData {
            return GoatData().apply {
                loadFromPreferences(preferences)
            }
        }
    }

    constructor() : this(1, 0, "Goat", GoatBackground.DEFAULT, GoatHat.NONE, GoatFace.DEFAULT)

    val foodForLevel: Int
        get() = level + 1

    fun saveToPreferences(preferences: Preferences) {
        preferences.apply {
            putInteger("level", level)
            putInteger("food", food)
            putString("name", name)
            putString("background", background.name)
            putString("hat", hat.name)
            putString("face", face.name)
        }
        preferences.flush()
    }

    fun loadFromPreferences(preferences: Preferences) {
        preferences.apply {
            level = getInteger("level", 1)
            food = getInteger("food", 0)
            name = getString("name", "Goat")
            background = GoatBackground.VALUES.firstOrNull {
                it.name == getString("background")
            } ?: GoatBackground.DEFAULT
            hat = GoatHat.VALUES.firstOrNull { it.name == getString("hat") } ?: GoatHat.NONE
            face = GoatFace.VALUES.firstOrNull { it.name == getString("face") } ?: GoatFace.DEFAULT
        }
    }

}
