package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.FalseCheckbox
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Stage


class InfoScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, InfoScreen>(main) {

    override val stage: Stage<InfoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val preferences: Preferences
        get() = main.preferences

    init {
        stage as GenericStage<InfoScreen>
        val palette = stage.palette

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_info"))
        }
        stage.titleLabel.apply {
            this.text = "editor.info"
        }
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("editor")
        }

        stage.centreStage.also { centre ->
            val padding = 0.05f
            val buttonWidth = 0.4f
            val buttonHeight = 0.15f
            val fontScale = 0.8f

            // Disable minimap
            centre.elements += object : FalseCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_MINIMAP, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_MINIMAP, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.disableMinimap"
                }

                this.location.set(screenX = padding,
                                  screenY = padding,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }

            // Chase camera
            centre.elements += object : TrueCheckbox<InfoScreen>(palette, centre, centre) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    preferences.putBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, checked).flush()
                }
            }.apply {
                this.checked = preferences.getBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, false)

                this.textLabel.apply {
                    this.fontScaleMultiplier = fontScale
                    this.isLocalizationKey = true
                    this.text = "screen.info.chaseCamera"
                }

                this.location.set(screenX = padding,
                                  screenY = padding * 2 + buttonHeight,
                                  screenWidth = buttonWidth,
                                  screenHeight = buttonHeight)
            }
        }

        stage.updatePositions()
    }

    override fun renderUpdate() {
        super.renderUpdate()
        stage as GenericStage
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}