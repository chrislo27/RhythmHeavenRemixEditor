package io.github.chrislo27.rhre3.screen.info

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.credits.CreditsGame
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.extras.RhythmGameScreen
import io.github.chrislo27.rhre3.extras.TestAffineScreen
import io.github.chrislo27.rhre3.extras.UpbeatGame
import io.github.chrislo27.rhre3.util.FadeIn
import io.github.chrislo27.rhre3.util.FadeOut
import io.github.chrislo27.rhre3.util.WipeFrom
import io.github.chrislo27.rhre3.util.WipeTo
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown


class ExtrasStage(parent: UIElement<InfoScreen>?, camera: OrthographicCamera, val infoScreen: InfoScreen)
    : Stage<InfoScreen>(parent, camera) {

    private val main: RHRE3Application get() = infoScreen.main
    private val preferences: Preferences get() = main.preferences
    private val editor: Editor get() = infoScreen.editor

    private val upbeatHardButton: Button<InfoScreen>
    
    init {
        val palette = infoScreen.stage.palette
        val padding = 0.025f
        val buttonWidth = 0.45f
        val buttonHeight = 0.1f
        val squareWidth = 38f / 456f
        val fontScale = 0.75f

        this.elements += Button(palette, this, this).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.center
                this.text = "Bouncy Road Mania"
                this.location.set(screenX = squareWidth * 1.25f, screenWidth = 1f - squareWidth * 2.5f)
            })
            addLabel(ImageLabel(palette, this, this.stage).apply {
                this.location.set(screenX = 0f, screenWidth = squareWidth, pixelX = 1f, pixelWidth = -2f, pixelY = 1f, pixelHeight = -2f)
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_bouncy_road_mania"))
            })
            this.location.set(screenX = padding,
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI("""https://github.com/chrislo27/BouncyRoadMania""")
            }
        }
//        this.elements += Button(palette, this, this).apply {
//            addLabel(TextLabel(palette, this, this.stage).apply {
//                this.fontScaleMultiplier = fontScale
//                this.isLocalizationKey = false
//                this.textWrapping = false
//                this.textAlign = Align.center
//                this.text = "Lockstep Maker"
//            })
//            this.location.set(screenX = padding,
//                              screenY = padding * 6 + buttonHeight * 5,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//        }
//        this.elements += Button(palette, this, this).apply {
//            addLabel(TextLabel(palette, this, this.stage).apply {
//                this.fontScaleMultiplier = fontScale
//                this.isLocalizationKey = false
//                this.textWrapping = false
//                this.textAlign = Align.center
//                this.text = "Goat Feeding"
//            })
//            this.location.set(screenX = padding,
//                              screenY = padding * 5 + buttonHeight * 4,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//        }
//        this.elements += Button(palette, this, this).apply {
//            addLabel(TextLabel(palette, this, this.stage).apply {
//                this.fontScaleMultiplier = fontScale
//                this.isLocalizationKey = false
//                this.textWrapping = false
//                this.textAlign = Align.center
//                this.text = "Mechanical Horse"
//            })
//            this.location.set(screenX = padding,
//                              screenY = padding * 4 + buttonHeight * 3,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//        }

        this.elements += Button(palette, this, this).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.center
                this.text = "extras.upbeat"
            })
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 7 + buttonHeight * 6,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
            this.leftClickAction = { _, _ ->
                val game = UpbeatGame(main, false)
                main.screen = TransitionScreen(main, infoScreen, RhythmGameScreen(main, game), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                AssetRegistry.get<Sound>("sfx_enter_game").play()
            }
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "extras.upbeat.tooltip"
        }
        upbeatHardButton = Button(palette, this, this).apply {
            addLabel(TextLabel(palette, this, this.stage).apply {
                this.fontScaleMultiplier = fontScale
                this.isLocalizationKey = true
                this.textWrapping = false
                this.textAlign = Align.center
                this.text = "extras.upbeat.hardMode"
            })
            this.location.set(screenX = 1f - (padding + buttonWidth),
                              screenY = padding * 6 + buttonHeight * 5,
                              screenWidth = buttonWidth,
                              screenHeight = buttonHeight)
            this.leftClickAction = { _, _ ->
                val game = UpbeatGame(main, true)
                main.screen = TransitionScreen(main, infoScreen, RhythmGameScreen(main, game), WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                AssetRegistry.get<Sound>("sfx_enter_game").play()
            }
            this.enabled = false
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "extras.upbeat.hardMode.tooltip.locked"
        }
        this.elements += upbeatHardButton
//        this.elements += Button(palette, this, this).apply {
//            addLabel(TextLabel(palette, this, this.stage).apply {
//                this.fontScaleMultiplier = fontScale
//                this.isLocalizationKey = false
//                this.textWrapping = false
//                this.textAlign = Align.center
//                this.text = "Notes Shop"
//            })
//            this.location.set(screenX = 1f - (padding + buttonWidth),
//                              screenY = padding * 5 + buttonHeight * 4,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//            this.leftClickAction = { _, _ ->
////                main.screen = TestAffineScreen(main)
//            }
//        }
//        this.elements += Button(palette, this, this).apply {
//            addLabel(TextLabel(palette, this, this.stage).apply {
//                this.fontScaleMultiplier = fontScale
//                this.isLocalizationKey = false
//                this.textWrapping = false
//                this.textAlign = Align.center
//                this.text = "Challenge Train"
//            })
//            this.location.set(screenX = 1f - (padding + buttonWidth),
//                              screenY = padding * 4 + buttonHeight * 3,
//                              screenWidth = buttonWidth,
//                              screenHeight = buttonHeight)
//        }
    }
    
    fun show() {
        if (preferences.getInteger(PreferenceKeys.EXTRAS_UPBEAT_HIGH_SCORE, 0) >= 288) {
            upbeatHardButton.run {
                this.enabled = true
                this.tooltipText = "extras.upbeat.hardMode.tooltip.unlocked"
            }
        }
    }
}