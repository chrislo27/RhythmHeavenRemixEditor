package io.github.chrislo27.rhre3.theme

import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks


object LoadedThemes {

    var index: Int = 0
        set(value) {
            field = value.coerceIn(0, (themes.size - 1).coerceAtLeast(0))
        }
    val currentTheme: Theme
        get() = if (themes.isEmpty()) error("Uninitialized themes - reloadThemes must be called") else themes[index]

    var themes: List<Theme> = listOf()
        private set
    val THEMES_FOLDER: FileHandle by lazy { RHRE3.RHRE3_FOLDER.child("themes/") }

    fun scroll(dir: Int) {
        index += dir
        if (index >= themes.size)
            index = 0
        else if (index < 0)
            index = themes.size - 1
    }

    @Synchronized
    fun persistIndex(preferences: Preferences) {
        preferences.putInteger(PreferenceKeys.THEME_INDEX, index).flush()
    }

    @Synchronized
    fun reloadThemes(preferences: Preferences, fromPrefs: Boolean) {
        themes.filter { it !in Themes.defaultThemes }.forEach(Theme::dispose)

        val themes = Themes.defaultThemes.toMutableList()

        val folder = THEMES_FOLDER
        folder.mkdirs()
        val files = folder.list(".json").filter { it.nameWithoutExtension() != "example" }
        Toolboks.LOGGER.info("Found ${files.size} json palette files")
        files.forEachIndexed { index, it ->
            Toolboks.LOGGER.info("Attempting to parse theme ${it.name()}")
            try {
                val themeObj: Theme = JsonHandler.fromJson(it.readString("UTF-8"))
                if (!themeObj.nameIsLocalization && themeObj.name == Theme.DEFAULT_NAME) {
                    themeObj.name = "Custom Theme ${index + 1}"
                }
                val uninitialized = themeObj.getUninitialized()
                if (uninitialized.isEmpty()) {
                    themes += themeObj
                    Toolboks.LOGGER.info("Loaded theme ${it.name()} successfully")
                } else {
                    Toolboks.LOGGER.info(
                            "Couldn't load theme ${it.name()}, missing fields (see examples for details): ${uninitialized.map { it.name }}")
                }
            } catch (e: Exception) {
                Toolboks.LOGGER.error("Failed to parse theme ${it.name()}, skipping")
                e.printStackTrace()
            }
        }
        
        this.themes = themes

        val exampleFolder = folder.child("example/")
        exampleFolder.mkdirs()
        Themes.defaultThemes.forEachIndexed { index, it ->
            val name = it.name
            it.name = "(Example) ${it.name}"
            exampleFolder.child("example_${index + 1}.json").writeString(JsonHandler.toJson(it), false, "UTF-8")
            it.name = name
        }

        index = if (!fromPrefs) 0 else preferences.getInteger(PreferenceKeys.THEME_INDEX, 0)

        if (index !in 0 until themes.size) {
            index = 0
        }
    }

}