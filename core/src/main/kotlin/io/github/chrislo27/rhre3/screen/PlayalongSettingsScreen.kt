package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper


class PlayalongSettingsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PlayalongSettingsScreen>(main) {

    companion object {
        private val FILLED_A_BUTTON = "\uE0E0"
        private val BORDERED_A_BUTTON = "\uE0A0"
    }

    override val stage: GenericStage<PlayalongSettingsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val label: TextLabel<PlayalongSettingsScreen>

    init {
        val palette = main.uiPalette
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }
        stage.titleLabel.text = "screen.playalongSettings.title"
        stage.titleIcon.visible = false
        label = TextLabel(palette.copy(ftfont = main.fonts[main.defaultFontLargeKey]), stage, stage).apply {
            this.alignment = Align.topLeft
            this.location.set(this@PlayalongSettingsScreen.stage.titleIcon.location)
            this.text = FILLED_A_BUTTON
            this.isLocalizationKey = false
            this.textWrapping = false
        }
        stage.elements += label
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        val interval = TempoUtils.beatsToSeconds(2f, 125f)
        val pressDuration = 0.15f
        if (MathHelper.getSawtoothWave(interval) < pressDuration / interval) {
            label.text = BORDERED_A_BUTTON
            label.fontScaleMultiplier = 0.7f
        } else {
            label.text = FILLED_A_BUTTON
            label.fontScaleMultiplier = 0.8f
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}