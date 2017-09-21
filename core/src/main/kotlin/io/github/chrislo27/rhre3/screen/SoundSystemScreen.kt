package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel


class SoundSystemScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, SoundSystemScreen>(main) {

    override val stage: Stage<SoundSystemScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    init {
        stage as GenericStage
        val palette = stage.palette

        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_update"))
        }
        stage.titleLabel.text = "screen.soundSystem.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        stage.centreStage.elements += object : TextLabel<SoundSystemScreen>(palette, stage.centreStage,
                                                                            stage.centreStage) {

            override fun frameUpdate(screen: SoundSystemScreen) {
                super.frameUpdate(screen)
                this.visible = main.preferences.getString(PreferenceKeys.SETTINGS_SOUND_SYSTEM,
                                                          SoundSystem.allSystems.first().id) != SoundSystem.system.id
            }
        }.apply {
            this.location.set(screenX = 0f, screenY = 0f, screenWidth = 1f, screenHeight = 0.125f)
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "screen.soundSystem.restart"
            this.textWrapping = false
        }
        val descLabel = object : TextLabel<SoundSystemScreen>(palette, stage.centreStage,
                                                              stage.centreStage) {
            override fun render(screen: SoundSystemScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
            }
        }.apply {
            this.location.set(screenX = 0f, screenY = 0.125f, screenWidth = 1f, screenHeight = 0.875f)
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "screen.soundSystem.${main.preferences.getString(PreferenceKeys.SETTINGS_SOUND_SYSTEM,
                                                                         SoundSystem.allSystems.first().id)}.desc"
            this.textWrapping = true
        }
        stage.centreStage.elements += descLabel

        stage.bottomStage.elements += object : Button<SoundSystemScreen>(palette, stage.bottomStage,
                                                                         stage.bottomStage) {
            private val textLabel: TextLabel<SoundSystemScreen>
                get() = labels.first() as TextLabel

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                var index = SoundSystem.allSystems.indexOfFirst {
                    main.preferences.getString(PreferenceKeys.SETTINGS_SOUND_SYSTEM,
                                               SoundSystem.allSystems.first().id) == it.id
                } + 1
                if (index >= SoundSystem.allSystems.size) {
                    index = 0
                }

                main.preferences.putString(PreferenceKeys.SETTINGS_SOUND_SYSTEM,
                                           SoundSystem.allSystems[index].id).flush()
                textLabel.text = "screen.soundSystem.${SoundSystem.allSystems[index].id}.name"
                descLabel.text = "screen.soundSystem.${SoundSystem.allSystems[index].id}.desc"
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.soundSystem.${SoundSystem.system.id}.name"
            })

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        stage.updatePositions()
    }

    override fun show() {
        super.show()
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }
}