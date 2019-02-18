package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.playalong.InputAction
import io.github.chrislo27.rhre3.playalong.InputResult
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.properties.Delegates


class PlayalongStage(val editor: Editor,
                     val palette: UIPalette, parent: UIElement<EditorScreen>?, camera: OrthographicCamera)
    : Stage<EditorScreen>(parent, camera) {

    companion object {
        val TRY_AGAIN_COLOUR = "01BDFD"
        val OK_COLOUR = "00CD00"
        val SUPERB_COLOUR = "FD0304"
    }

    private val remix: Remix get() = editor.remix
    private val playalong: Playalong get() = remix.playalong
    private val main: RHRE3Application get() = editor.main

    val lowerStage: Stage<EditorScreen>
    val noEntitiesLabel: TextLabel<EditorScreen>
    val perfectLabel: TextLabel<EditorScreen>
    val scoreLabel: TextLabel<EditorScreen>
    val skillStarLabel: TextLabel<EditorScreen>
    val acesLabel: TextLabel<EditorScreen>
    val perfectIcon: ImageLabel<EditorScreen>
    val perfectHitIcon: ImageLabel<EditorScreen>
    val flickingStage: FlickingStage<EditorScreen>
    val timingDisplayStage: TimingDisplayStage

    override var visible: Boolean by Delegates.observable(super.visible) { _, _, new -> if (new) onShow() else onHide() }

    private val perfectTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect"))
    private val perfectHitTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect_hit"))
    private val perfectFailTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect_failed"))
    private var skillStarZoom: Float = 1f
    private var perfectAnimation: Float = 0f

    init {
        val palette = main.uiPalette
        lowerStage = Stage(this, this.camera).apply {
            this.location.set(screenHeight = 0.25f)
            this.location.set(0f, 0f, 1f)
        }
        this.elements += ColourPane(this, this).apply {
            this.colour.set(0f, 0f, 0f, 0.65f)
            this.location.set(lowerStage.location)
        }
        flickingStage = FlickingStage(this, this).apply {
            this.colour.set(Color.valueOf("00BC67"))
            this.location.set(screenX = 0.65f)
            this.location.set(location.screenX, 0f, 1f - location.screenX, 1f)
            this.visible = false

            this.onTapDown = this@PlayalongStage::onTapDown
            this.onTapRelease = this@PlayalongStage::onTapRelease
            this.onFlick = this@PlayalongStage::onFlick
            this.onSlide = this@PlayalongStage::onTapSlide
        }
        this.elements += flickingStage
        this.elements += lowerStage

        noEntitiesLabel = object : TextLabel<EditorScreen>(palette, this, this) {
            override fun getRealText(): String {
                return if (!isLocalizationKey) super.getRealText() else Localization[text, "[#DDDDDD]${Localization[Series.OTHER.localization]} ➡ ${GameRegistry.data.playalongGame.group} ➡ ${GameRegistry.data.playalongGame.name}[]"]
            }
        }.apply {
            this.isLocalizationKey = true
            this.text = "playalong.noCues"
            this.location.set(lowerStage.location)
        }
        this.elements += noEntitiesLabel

        val paddingX = 0.0125f
        val paddingY = 0.05f

        perfectIcon = ImageLabel(palette, lowerStage, lowerStage).apply {
            this.image = perfectTexReg
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenX = paddingX, screenY = 1f - 0.2f - paddingY, screenWidth = 0.035f, screenHeight = 0.2f)
        }
        lowerStage.elements += perfectIcon
        perfectHitIcon = ImageLabel(palette, lowerStage, lowerStage).apply {
            this.image = perfectHitTexReg
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(perfectIcon.location)
            this.tint.set(1f, 1f, 1f, 0f)
        }
        lowerStage.elements += perfectHitIcon
        perfectLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = true
            this.text = "playalong.goForPerfect"
            this.textWrapping = false
            this.textColor = Color(1f, 1f, 1f, 1f)
            this.textAlign = Align.left
            this.location.set(screenX = perfectIcon.location.screenX + perfectHitIcon.location.screenWidth,
                              screenY = 1f - 0.2f - paddingY, screenWidth = 0.25f, screenHeight = 0.2f)
        }
        lowerStage.elements += perfectLabel

        scoreLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontLargeKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textAlign = Align.center
            this.fontScaleMultiplier = 0.4f
            this.location.set(screenX = 0.5f - 0.125f / 2, screenY = 1f - 0.25f - paddingY, screenWidth = 0.125f, screenHeight = 0.25f)
        }
        lowerStage.elements += scoreLabel
        skillStarLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontLargeKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = false
            this.text = "★"
            this.textWrapping = true
            this.textAlign = Align.center
            this.location.set(screenY = 1f - 0.25f - paddingY, screenWidth = 0.035f, screenHeight = 0.25f)
            this.location.set(screenX = scoreLabel.location.screenX - this.location.screenWidth)
        }
        lowerStage.elements += skillStarLabel
        acesLabel = object : TextLabel<EditorScreen>(palette, lowerStage, lowerStage) {
            override fun getRealText(): String {
                return Localization[text, playalong.aces]
            }
        }.apply {
            this.isLocalizationKey = true
            this.text = "playalong.numAces"
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.center
            this.location.set(screenY = skillStarLabel.location.screenY - paddingX - 0.2f, screenWidth = 0.125f, screenHeight = 0.2f)
            this.location.set(screenX = 0.5f - this.location.screenWidth / 2)
        }
        lowerStage.elements += acesLabel

        lowerStage.elements += object : Button<EditorScreen>(palette, lowerStage, lowerStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                setPerfectVisibility(!perfectIcon.visible)
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = perfectTexReg
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            })
            this.location.set(screenX = paddingX, screenY = paddingY, screenWidth = 0.035f, screenHeight = 0.25f)
        }

        timingDisplayStage = TimingDisplayStage(this, lowerStage, lowerStage.camera).apply {
            this.location.set(screenWidth = 0.5f, screenY = paddingY)
            this.location.set(screenX = 0.5f - this.location.screenWidth / 2, screenHeight = acesLabel.location.screenY - this.location.screenY - paddingY)
        }
        lowerStage.elements += timingDisplayStage

        updateScoreLabel()
        setPerfectVisibility(false)
    }

    fun setPerfectVisibility(visible: Boolean) {
        perfectIcon.visible = visible
        perfectHitIcon.visible = visible
        perfectLabel.visible = visible
    }

    fun updateScoreLabel() {
        val score = playalong.score.roundToInt()
        scoreLabel.text = "[#${when (score) {
            in 0 until 60 -> TRY_AGAIN_COLOUR
            in 60 until 80 -> OK_COLOUR
            else -> SUPERB_COLOUR
        }}]$score[]"
        skillStarLabel.textColor = when {
            playalong.skillStarInput == null -> Colors.get("X")
            !playalong.gotSkillStar -> Color.GRAY
            else -> Color.YELLOW
        }
    }

    fun onInput(inputAction: InputAction, inputResult: InputResult, start: Boolean) {
        timingDisplayStage.flash(inputResult)
    }

    fun onSkillStarGet() {
        skillStarZoom = 1f
    }

    private fun skillStarPulse() {
        skillStarZoom = -0.35f
    }

    fun onPerfectFail() {
        perfectAnimation = 1f
        perfectIcon.image = perfectFailTexReg
    }

    fun onPerfectHit() {
        perfectAnimation = 1f
        perfectIcon.image = perfectTexReg
    }

    fun reset() {
        remix.recomputeCachedData()
        val noPlayalong = playalong.inputActions.isEmpty()
        noEntitiesLabel.visible = noPlayalong
        lowerStage.visible = !noPlayalong
        updateScoreLabel()
        perfectIcon.image = perfectTexReg
        perfectAnimation = 0f
        flickingStage.visible = playalong.needsTouchScreen
    }

    fun onTapDown(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE, PlayalongInput.TOUCH_QUICK_TAP), 0)
    }

    fun onTapRelease(tapPoint: FlickingStage.TapPoint, short: Boolean) {
        playalong.handleInput(false, if (short) setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE, PlayalongInput.TOUCH_QUICK_TAP) else setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE), 0)
    }

    fun onFlick(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_FLICK), 0)
    }

    fun onTapSlide(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_SLIDE), 0)
    }

    fun onShow() {
        reset()
    }

    fun onHide() {
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        // Skill Star pulses 3 beats before hitting it
        val skillStarEntity = playalong.skillStarEntity
        if (skillStarZoom == 0f && skillStarEntity != null && playalong.skillStarInput != null && remix.playState == PlayState.PLAYING) {
            val threshold = 0.1f
            for (i in 1 until 4) {
                val beatPoint = remix.tempos.beatsToSeconds(skillStarEntity.bounds.x - i)
                if (remix.seconds in beatPoint..beatPoint + threshold) {
                    skillStarPulse()
                    break
                }
            }
        }
        if (skillStarZoom.absoluteValue > 0f) {
            val sign = skillStarZoom.sign
            skillStarZoom -= Gdx.graphics.deltaTime / 0.5f * sign
            if (skillStarZoom * sign < 0f)
                skillStarZoom = 0f
        }
        skillStarLabel.fontScaleMultiplier = (if (skillStarZoom > 0f) Interpolation.pow4In else Interpolation.linear).apply(4f, 1f, 1f - skillStarZoom.absoluteValue) / 4f // / 4 b/c of big font

        val perfectLabelFlash = MathHelper.getSawtoothWave(1.35f)
        perfectLabel.textColor?.a = if (remix.playState != PlayState.PLAYING || perfectLabelFlash > 0.35f) 1f else 0f

        perfectHitIcon.tint.a = if (playalong.perfectSoFar) perfectAnimation else 0f
        if (playalong.perfectSoFar && perfectIcon.image != perfectTexReg) {
            perfectIcon.image = perfectTexReg
        }
        if (!playalong.perfectSoFar) {
            if (perfectAnimation <= 0f) {
                if (perfectIcon.location.pixelX != 0f || perfectIcon.location.pixelY != 0f) {
                    perfectIcon.location.set(pixelX = 0f, pixelY = 0f)
                    perfectIcon.stage.updatePositions()
                }
            } else {
                // Shake icon
                val maxShake = 3
                perfectIcon.location.set(pixelX = 1f * MathUtils.randomSign() * MathUtils.random(0, maxShake), pixelY = 1f * MathUtils.randomSign() * MathUtils.random(0, maxShake))
                perfectIcon.stage.updatePositions()
            }
        }

        if (perfectAnimation > 0f) {
            perfectAnimation -= Gdx.graphics.deltaTime / (if (playalong.perfectSoFar) 0.125f else 0.5f)
        }
        if (perfectAnimation < 0f) perfectAnimation = 0f
    }

    override fun keyUp(keycode: Int): Boolean {
        val ret = super.keyUp(keycode)
        if (ret) return ret
        return playalong.onKeyUp(keycode)
    }

    override fun keyDown(keycode: Int): Boolean {
        val ret = super.keyDown(keycode)
        if (ret) return ret
        return playalong.onKeyDown(keycode)
    }
}