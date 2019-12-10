package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.RemixRecovery
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextLabel


class CloseWarningScreen(main: RHRE3Application, val lastScreen: Screen?) : ToolboksScreen<RHRE3Application, CloseWarningScreen>(main) {

    override val stage: GenericStage<CloseWarningScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    init {
        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_warn"))
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }
        stage.titleLabel.apply {
            this.isLocalizationKey = true
            this.text = "screen.closeWarning.title"
        }
        stage.onBackButtonClick = {
            main.screen = lastScreen
        }
        stage.backButton.apply {
            this.enabled = true
            this.visible = true
        }

        val palette = main.uiPalette

        stage.centreStage.elements += FalseCheckbox(palette, stage.centreStage, stage.centreStage).apply {
            this.checked = !main.preferences.getBoolean(PreferenceKeys.SETTINGS_CLOSE_WARNING, true)

            this.textLabel.apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.left
                this.text = "screen.closeWarning.dontShowAgain"
            }

            this.checkedStateChanged = {
                main.preferences.putBoolean(PreferenceKeys.SETTINGS_CLOSE_WARNING, !it).flush()
            }

            this.location.set(screenX = 0.2f, screenY = 0.1f, screenWidth = 0.6f, screenHeight = 0.15f)
        }
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = true
            this.textWrapping = false
            this.text = "screen.closeWarning.settingsHint"
            this.textColor = Color.LIGHT_GRAY

            this.location.set(screenX = 0f, screenY = 0f, screenWidth = 1f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = true
            this.textWrapping = true
            this.text = "screen.closeWarning.areYouSure"

            this.location.set(screenX = 0.1f, screenY = 0.25f, screenWidth = 0.8f, screenHeight = 0.75f)
        }
        stage.bottomStage.elements += Button(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                                                          clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), stage.bottomStage, stage.bottomStage).apply {
            val backBtnLoc = this@CloseWarningScreen.stage.backButton.location
            this.location.set(0.5f - backBtnLoc.screenWidth / 2, backBtnLoc.screenY, backBtnLoc.screenWidth, backBtnLoc.screenHeight)
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_x"))
            })
            this.leftClickAction = { _, _ ->
                RemixRecovery.removeSelfFromShutdownHooks()
                Gdx.app.exit()
            }
        }

        stage.updatePositions()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun getDebugString(): String? {
        return "lastScreen: ${lastScreen?.javaClass?.canonicalName}"
    }
}
