package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import kotlin.math.roundToInt
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

    override var visible: Boolean by Delegates.observable(super.visible) { _, _, new -> if (new) onShow() else onHide() }

    private var skillStarZoom: Float = 1f

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

        perfectLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = true
            this.text = "playalong.goForPerfect"
            this.textWrapping = false
            this.textAlign = Align.left
            this.location.set(screenX = paddingX, screenY = 1f - 0.2f - paddingY, screenWidth = 0.25f, screenHeight = 0.2f)
        }
        lowerStage.elements += perfectLabel

        scoreLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textAlign = Align.center
            this.location.set(screenX = 0.5f - 0.125f / 2, screenY = 1f - 0.2f - paddingY, screenWidth = 0.125f, screenHeight = 0.2f)
        }
        lowerStage.elements += scoreLabel
        skillStarLabel = TextLabel(palette.copy(ftfont = main.fonts[main.defaultBorderedFontLargeKey]), lowerStage, lowerStage).apply {
            this.isLocalizationKey = false
            this.text = "★"
            this.textWrapping = true
            this.textAlign = Align.center
            this.location.set(screenY = 1f - 0.2f - paddingY, screenWidth = 0.025f, screenHeight = 0.2f)
            this.location.set(screenX = scoreLabel.location.screenX - this.location.screenWidth)
        }
        lowerStage.elements += skillStarLabel

//        this.elements += FlickingStage(this, this).apply {
//            this.colour.set(Color.valueOf("00BC67"))
//            this.location.set(screenX = 0.6f)
//            this.location.set(location.screenX, 0f, 1f - location.screenX, 1f)
//            this.visible = false
//        }
        updateScoreLabel()
    }

    fun updateScoreLabel() {
        val score = playalong.score.roundToInt()
        scoreLabel.text = "[#${when (score) {
            in 0 until 60 -> TRY_AGAIN_COLOUR
            in 60 until 80 -> OK_COLOUR
            else -> SUPERB_COLOUR
        }}]$score[]"
        skillStarLabel.textColor = when {
            playalong.skillStarEntity == null -> Colors.get("X")
            !playalong.gotSkillStar -> Color.GRAY
            else -> Color.YELLOW
        }
    }

    fun onSkillStarGet() {
        skillStarZoom = 1f
    }

    fun onPerfectFail() {

    }

    fun onShow() {
        remix.recomputeCachedData()
        val noPlayalong = playalong.inputActions.isEmpty()
        noEntitiesLabel.visible = noPlayalong
        lowerStage.visible = !noPlayalong
        updateScoreLabel()
    }

    fun onHide() {
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)
        if (skillStarZoom > 0f) {
            skillStarZoom -= Gdx.graphics.deltaTime / 0.5f
        }
        if (skillStarZoom < 0f) skillStarZoom = 0f
        skillStarLabel.fontScaleMultiplier = Interpolation.pow4In.apply(4f, 1f, 1f - skillStarZoom) / 4f // / 4 b/c of big font

        val perfectLabelFlash = MathHelper.getSawtoothWave(1.35f)
        perfectLabel.visible = remix.playState != PlayState.PLAYING || perfectLabelFlash > 0.35f
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