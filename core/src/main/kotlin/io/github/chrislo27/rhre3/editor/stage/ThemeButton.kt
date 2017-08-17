package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.theme.ExampleTheme
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


class ThemeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private var index: Int = 0

    private lateinit var themes: List<Theme>

    override fun getHoverText(): String {
        return Localization["editor.palette", themes[index].getRealName()]
    }

    private fun reloadPalettes(fromPrefs: Boolean) {
        index = if (!fromPrefs) 0 else editor.main.preferences.getInteger(PreferenceKeys.THEME_INDEX, 0)
        themes = mutableListOf(*Theme.Themes.defaultThemes.toTypedArray())
        themes as MutableList

        val folder = Gdx.files.local("themes/")
        folder.mkdirs()
        val files = folder.list(".json").filter { it.nameWithoutExtension() != "example" }
        Toolboks.LOGGER.info("Found ${files.size} json palette files, attempting to read")
        files.forEachIndexed { index, it ->
            Toolboks.LOGGER.info("Attempting to parse ${it.name()}")
            try {
                val themeObj: Theme = JsonHandler.fromJson(it.readString("UTF-8"))
                if (!themeObj.nameIsLocalization && themeObj.name == Theme.DEFAULT_NAME) {
                    themeObj.name = "Custom Palette ${index + 1}"
                }
                themes += themeObj
                Toolboks.LOGGER.info("Loaded ${it.name()} successfully")
            } catch (e: Exception) {
                Toolboks.LOGGER.info("Failed to parse ${it.name()}, skipping")
                e.printStackTrace()
            }
        }

        folder.child("example.json").writeString(JsonHandler.toJson(ExampleTheme), false, "UTF-8")

        editor.theme = themes[index]
    }

    private fun scrollThemes() {
        index++
        if (index >= themes.size)
            index = 0
    }

    private fun persist() {
        editor.main.preferences.putInteger(PreferenceKeys.THEME_INDEX, index).flush()
    }

    init {
        reloadPalettes(true)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)

        scrollThemes()
        editor.theme = themes[index]
        persist()
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        reloadPalettes(false)
        persist()
    }
}
