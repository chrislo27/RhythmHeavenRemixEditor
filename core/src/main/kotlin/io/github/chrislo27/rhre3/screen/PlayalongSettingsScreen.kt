package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.playalong.PlayalongChars
import io.github.chrislo27.rhre3.playalong.PlayalongControls
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.TempoUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.util.MathHelper
import kotlin.math.sign


class PlayalongSettingsScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, PlayalongSettingsScreen>(main) {

    companion object {
        private val FILLED_A_BUTTON = PlayalongChars.FILLED_A
        private val BORDERED_A_BUTTON = PlayalongChars.BORDERED_A
    }

    override val stage: GenericStage<PlayalongSettingsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val titleLabelIcon: TextLabel<PlayalongSettingsScreen>

    private val controlsLabel: TextLabel<PlayalongSettingsScreen>

    init {
        val palette = main.uiPalette
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }
        stage.titleLabel.text = "screen.playalongSettings.title"
        stage.titleIcon.visible = false
        titleLabelIcon = TextLabel(palette.copy(ftfont = main.fonts[main.defaultFontLargeKey]), stage, stage).apply {
            this.alignment = Align.topLeft
            this.location.set(this@PlayalongSettingsScreen.stage.titleIcon.location)
            this.text = FILLED_A_BUTTON
            this.isLocalizationKey = false
            this.textWrapping = false
        }
        stage.elements += titleLabelIcon

        controlsLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.textWrapping = false
            this.isLocalizationKey = false
            this.location.set(screenHeight = 0.1f)
        }
        stage.centreStage.elements += controlsLabel

        val currentControls = main.playalongControls.copy()
        val isCustom = currentControls !in PlayalongControls.standardControls.values
        val controlsList = (if (isCustom) listOf(PlayalongControls.strCustom to currentControls) else listOf()) + PlayalongControls.standardControls.entries.map { it.toPair() }
        stage.bottomStage.elements += object : Button<PlayalongSettingsScreen>(palette, stage.bottomStage, stage.bottomStage) {

            var index: Int = controlsList.indexOfFirst { currentControls == it.second }.coerceAtLeast(0)

            fun cycle(dir: Int) {
                var newIndex = index + dir.sign
                if (newIndex < 0) {
                    newIndex = controlsList.size - 1
                } else if (newIndex >= controlsList.size) {
                    newIndex = 0
                }
                index = newIndex
                main.playalongControls = controlsList[index].second
                updateLabel()
            }

            fun updateLabel() {
                val label = labels.first() as TextLabel
                label.text = Localization["screen.playalongSettings.controls", controlsList[index].first]
                controlsLabel.text = controlsList[index].second.toInputString()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }

            init {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                    this.textWrapping = false
                })
                updateLabel()
            }
        }.apply {
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }

        val interval = TempoUtils.beatsToSeconds(2f, 125f)
        val pressDuration = 0.15f
        if (MathHelper.getSawtoothWave(interval) < pressDuration / interval) {
            titleLabelIcon.text = BORDERED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.7f
        } else {
            titleLabelIcon.text = FILLED_A_BUTTON
            titleLabelIcon.fontScaleMultiplier = 0.8f
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}