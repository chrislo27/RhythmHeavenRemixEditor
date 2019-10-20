package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.patternstorage.FileStoredPattern
import io.github.chrislo27.rhre3.patternstorage.PatternStorage
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel

class PatternDeleteScreen(main: RHRE3Application, val editor: Editor, val pattern: FileStoredPattern, val lastScreen: Screen?)
    : ToolboksScreen<RHRE3Application, PatternDeleteScreen>(main) {

    override val stage: GenericStage<PatternDeleteScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val button: Button<PatternDeleteScreen>

    init {
        stage.titleLabel.text = "screen.patternStore.delete.title"
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_pattern_delete"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = lastScreen ?: ScreenRegistry["editor"]
        }

        val palette = main.uiPalette

        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = true
            this.text = "screen.patternStore.delete.confirmation"
            this.textWrapping = true
            this.location.set(screenY = 0.5f, screenHeight = 0.25f)
        }
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.textColor = Color.LIGHT_GRAY
            this.text = pattern.name
            this.textWrapping = false
            this.location.set(screenY = 0.25f, screenHeight = 0.25f)
        }

        button = object : Button<PatternDeleteScreen>(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                                                                   clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                PatternStorage.deletePattern(pattern).persist()
                editor.stage.updateSelected()
                main.screen = ScreenRegistry["editor"]
            }
        }.apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "screen.patternStore.delete.button"
            })
        }

        stage.bottomStage.elements += button
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
