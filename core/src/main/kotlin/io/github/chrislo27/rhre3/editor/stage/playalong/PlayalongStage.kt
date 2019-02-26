package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.playalong.*
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.PlayState.PAUSED
import io.github.chrislo27.rhre3.track.PlayState.PLAYING
import io.github.chrislo27.rhre3.track.PlayState.STOPPED
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
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

        val TEMPO_DOWN_COLOUR: Color = Color.valueOf("3199E2")
        val TEMPO_UP_COLOUR: Color = Color.valueOf("E03131")
        val HEART_COLOUR: Color = Color.valueOf("FF7D7D")
        val TEMPO_UP_SYMBOL = "▲"
        val TEMPO_DOWN_SYMBOL = "▼"
        val HEART_SYMBOL = "♥"

        val TEMPO_UP_RANGE = 5..300
        val TEMPO_CHANGE_INCREMENT = 5
    }

    data class Hearts(val num: Int, val total: Int) {
        companion object {
            val EMPTY = Hearts(0, 0)
        }

        constructor(total: Int) : this(total, total)
    }

    private val remix: Remix get() = editor.remix
    private val playalong: Playalong get() = remix.playalong
    private val main: RHRE3Application get() = editor.main
    private val preferences: Preferences get() = main.preferences

    val lowerStage: Stage<EditorScreen>
    val noEntitiesLabel: TextLabel<EditorScreen>
    val perfectLabel: TextLabel<EditorScreen>
    val monsterGoalLabel: TextLabel<EditorScreen>
    val scoreLabel: TextLabel<EditorScreen>
    val skillStarLabel: TextLabel<EditorScreen>
    val acesLabel: TextLabel<EditorScreen>
    val perfectIcon: ImageLabel<EditorScreen>
    val perfectHitIcon: ImageLabel<EditorScreen>
    val monsterGoalIcon: ImageLabel<EditorScreen>
    val flickingStage: FlickingStage<EditorScreen>
    val timingDisplayStage: TimingDisplayStage
    val tempoUpButton: Button<EditorScreen>
    val tempoDownButton: Button<EditorScreen>
    val tempoLabel: TextLabel<EditorScreen>
    val monsterMawButton: Button<EditorScreen>
    val heartsButton: Button<EditorScreen>

    override var visible: Boolean by Delegates.observable(super.visible) { _, _, new -> if (new) onShow() else onHide() }

    private val perfectTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect"))
    private val perfectHitTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect_hit"))
    private val perfectFailTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_perfect_failed"))
    private val heartTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_heart"), 0, 0, 64, 64)
    private val heartBrokenTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_heart"), 0, 0, 64, 64)
    private val monsterIconTexReg: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_monster_icon"))

    private var skillStarZoom: Float = 1f
    private var perfectAnimation: Float = 0f
    private var lastMonsterAceSfx: Float = -1f

    var hearts: Hearts = Hearts.EMPTY
    var heartsCooldown: Float = 0f
    var heartsInvuln: Float = 0f

    var tempoChange: Int = 100
        private set(value) {
            field = value
            tempoLabel.text = "[#${if (value == 100) "FFFFFF" else if (value > 100) TEMPO_UP_COLOUR.toString() else TEMPO_DOWN_COLOUR.toString()}]Tempo ${if (value >= 100) "Up" else "Down"}![]\n$value%"
            tempoUpButton.enabled = value < TEMPO_UP_RANGE.last
            tempoDownButton.enabled = value > TEMPO_UP_RANGE.first
        }
    var monsterGoal: Float = 0f
        set(value) {
            field = value
            playalong.monsterGoal = field
        }
    var monsterGoalPreset: MonsterPresets? = null
        set(value) {
            field = value
            monsterGoal = value?.speed ?: 0f
        }

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

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                editor.stage.playalongToggleButton.onLeftClick(0f, 0f)
                val game = GameRegistry.data.playalongGame
                editor.stage.selectInPicker(game, null)
            }

            override fun canBeClickedOn(): Boolean = true
        }.apply {
            this.isLocalizationKey = true
            this.text = "playalong.noCues"
            this.location.set(lowerStage.location)
        }
        this.elements += noEntitiesLabel

        val paddingX = 0.0125f
        val paddingY = 0.05f

        val buttonWidth = 0.035f
        val buttonHeight = 0.2f
        perfectIcon = ImageLabel(palette, lowerStage, lowerStage).apply {
            this.image = perfectTexReg
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenX = paddingX, screenY = 1f - buttonHeight - paddingY, screenWidth = buttonWidth, screenHeight = buttonHeight)
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
            this.location.set(screenX = perfectIcon.location.screenX + perfectIcon.location.screenWidth,
                              screenY = 1f - buttonHeight - paddingY, screenWidth = 0.3f, screenHeight = buttonHeight)
        }
        lowerStage.elements += perfectLabel
        monsterGoalIcon = ImageLabel(palette, lowerStage, lowerStage).apply {
            this.image = monsterIconTexReg
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenX = paddingX, screenY = 1f - buttonHeight * 2 - paddingY * 2, screenWidth = buttonWidth, screenHeight = buttonHeight)
        }
        lowerStage.elements += monsterGoalIcon
        monsterGoalLabel = object : TextLabel<EditorScreen>(palette.copy(ftfont = main.fonts[main.defaultBorderedFontKey]), lowerStage, lowerStage) {
            override fun getRealText(): String {
                val monsterGoalArg = monsterGoalPreset?.localizationKey?.let { Localization[it] } ?: Editor.THREE_DECIMAL_PLACES_FORMATTER.format(monsterGoal)
                return Localization["playalong.monsterGoal", monsterGoalArg]
            }
        }.apply {
            this.isLocalizationKey = false
            this.text = ""
            this.textWrapping = false
            this.textColor = Color(1f, 1f, 1f, 1f)
            this.textAlign = Align.left
            this.location.set(screenX = monsterGoalIcon.location.screenX + monsterGoalIcon.location.screenWidth,
                              screenY = 1f - buttonHeight * 2 - paddingY * 2, screenWidth = 0.3f, screenHeight = buttonHeight)
        }
        lowerStage.elements += monsterGoalLabel

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
            this.location.set(screenY = 1f - 0.25f - paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
            this.location.set(screenX = scoreLabel.location.screenX - this.location.screenWidth)
        }
        lowerStage.elements += skillStarLabel
        acesLabel = object : TextLabel<EditorScreen>(palette, lowerStage, lowerStage) {
            override fun getRealText(): String {
                return Localization[text, playalong.aces, playalong.numResultsExpected]
            }
        }.apply {
            this.isLocalizationKey = true
            this.text = "playalong.numAces"
            this.textWrapping = false
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.center
            this.location.set(screenY = skillStarLabel.location.screenY - paddingX - buttonHeight, screenWidth = 0.275f, screenHeight = buttonHeight)
            this.location.set(screenX = 0.5f - this.location.screenWidth / 2)
        }
        lowerStage.elements += acesLabel

        lowerStage.elements += object : Button<EditorScreen>(palette, lowerStage, lowerStage), EditorStage.HasHoverText {
            override fun getHoverText(): String = Localization["playalong.goForPerfect.tooltip"]

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                setPerfectVisibility(!perfectIcon.visible)
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = perfectTexReg
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            })
            this.location.set(screenX = paddingX, screenY = paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
        }
        monsterMawButton = object : Button<EditorScreen>(palette, lowerStage, lowerStage), EditorStage.HasHoverText {
            override fun getHoverText(): String = if (!enabled) "" else Localization["playalong.monsterGoal.tooltip"]
            fun cycle(dir: Int) {
                val monsterPreset = monsterGoalPreset
                val values = MonsterPresets.VALUES
                val currentIndex = if (monsterPreset == null) -1 else values.indexOf(monsterPreset)
                var newIndex = currentIndex + dir.sign
                if (newIndex < -1)
                    newIndex = values.size - 1
                else if (newIndex >= values.size)
                    newIndex = -1
                monsterGoalPreset = values.getOrNull(newIndex)
                updateLabels()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = monsterIconTexReg
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            })
            this.location.set(screenX = paddingX + (paddingX / 2f) + buttonWidth, screenY = paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
        }
        lowerStage.elements += monsterMawButton
        tempoDownButton = object : Button<EditorScreen>(palette, lowerStage, lowerStage), EditorStage.HasHoverText {
            override fun getHoverText(): String = if (!enabled) "" else Localization["playalong.tempoDown.tooltip", TEMPO_CHANGE_INCREMENT.absoluteValue, TEMPO_CHANGE_INCREMENT.absoluteValue * 2]
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                tempoChange -= TEMPO_CHANGE_INCREMENT.absoluteValue * (if (Gdx.input.isControlDown() || Gdx.input.isShiftDown()) 2 else 1)
                if (tempoChange < TEMPO_UP_RANGE.first) {
                    tempoChange = TEMPO_UP_RANGE.first
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = TEMPO_DOWN_SYMBOL
                this.textColor = TEMPO_DOWN_COLOUR
                this.textWrapping = false
            })
            this.location.set(screenX = paddingX + (paddingX / 2f) * 2 + buttonWidth * 2, screenY = paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
        }
        lowerStage.elements += tempoDownButton
        tempoLabel = object : TextLabel<EditorScreen>(palette, lowerStage, lowerStage) {
            override fun getRealText(): String {
                val value = tempoChange
                return "[#${if (value == 100) "FFFFFF" else if (value > 100) TEMPO_UP_COLOUR.toString() else TEMPO_DOWN_COLOUR.toString()}]" + Localization[if (value >= 100) "playalong.tempoUp" else "playalong.tempoDown"] + "[]\n$value%"
            }
        }.apply {
            this.isLocalizationKey = true
            this.text = ""
            this.fontScaleMultiplier = 0.5f
            this.background = true
            this.textWrapping = false
            this.location.set(screenX = paddingX + (paddingX / 2f) * 2 + buttonWidth * 3, screenY = paddingY, screenWidth = buttonWidth * 2, screenHeight = 0.25f)
        }
        lowerStage.elements += tempoLabel
        tempoUpButton = object : Button<EditorScreen>(palette, lowerStage, lowerStage), EditorStage.HasHoverText {
            override fun getHoverText(): String = if (!enabled) "" else Localization["playalong.tempoUp.tooltip", TEMPO_CHANGE_INCREMENT.absoluteValue, TEMPO_CHANGE_INCREMENT.absoluteValue * 2]
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                tempoChange += TEMPO_CHANGE_INCREMENT.absoluteValue * (if (Gdx.input.isControlDown() || Gdx.input.isShiftDown()) 2 else 1)
                if (tempoChange > TEMPO_UP_RANGE.last) {
                    tempoChange = TEMPO_UP_RANGE.last
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = false
                this.text = TEMPO_UP_SYMBOL
                this.textColor = TEMPO_UP_COLOUR
                this.textWrapping = false
            })
            this.location.set(screenX = paddingX + (paddingX / 2f) * 2 + buttonWidth * 5, screenY = paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
        }
        lowerStage.elements += tempoUpButton
        heartsButton = object : Button<EditorScreen>(palette, lowerStage, lowerStage), EditorStage.HasHoverText {
            override fun getHoverText(): String = if (!enabled) "" else Localization["playalong.lifeGoal.tooltip"]

            val heartsList = listOf(Hearts.EMPTY, Hearts(3), Hearts(2), Hearts(1))
            fun cycle(dir: Int) {
                val currentIndex = heartsList.indexOfFirst { it.total == hearts.total }.coerceAtLeast(0)
                var newIndex = currentIndex + dir.sign
                if (newIndex < 0)
                    newIndex = heartsList.size - 1
                else if (newIndex >= heartsList.size)
                    newIndex = 0
                hearts = heartsList[newIndex].copy()
                updateLabels()
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                cycle(1)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                cycle(-1)
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = heartTexReg
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            })
            this.location.set(screenX = paddingX + (paddingX / 2f) * 3 + buttonWidth * 6, screenY = paddingY, screenWidth = buttonWidth, screenHeight = 0.25f)
        }
        lowerStage.elements += heartsButton

        timingDisplayStage = TimingDisplayStage(this, lowerStage, lowerStage.camera).apply {
            this.location.set(screenWidth = 0.4f, screenY = paddingY)
            this.location.set(screenX = 0.5f - this.location.screenWidth / 2, screenHeight = acesLabel.location.screenY - this.location.screenY - paddingY)
        }
        lowerStage.elements += timingDisplayStage

        updateLabels()
        setPerfectVisibility(false)
        tempoChange = 100 // Force text label update
    }

    fun playStateListener(old: PlayState, new: PlayState) {
        if (!this.visible) return
        when (new) {
            STOPPED -> {
                remix.speedMultiplier = 1f
                disableButtonsWhilePlaying(false)
                if (hearts != Hearts.EMPTY) {
                    hearts = Hearts(hearts.total)
                }
            }
            PAUSED -> {
                disableButtonsWhilePlaying(true)
            }
            PLAYING -> {
                setRemixSpeed()
                heartsInvuln = 0f
                disableButtonsWhilePlaying(true)
                lastMonsterAceSfx = remix.tempos.beatsToSeconds(remix.playbackStart) - 5f
            }
        }
    }

    private fun disableButtonsWhilePlaying(playing: Boolean) {
        tempoUpButton.enabled = !playing
        tempoDownButton.enabled = !playing
        heartsButton.enabled = !playing
        monsterMawButton.enabled = !playing
        if (!playing) {
            tempoChange = tempoChange // Updates the enabled state for the tempo buttons
        }
    }

    fun setPerfectVisibility(visible: Boolean) {
        perfectIcon.visible = visible
        perfectHitIcon.visible = visible
        perfectLabel.visible = visible
    }

    fun updateLabels() {
        if (hearts == Hearts.EMPTY) {
            val score = playalong.score.roundToInt()
            scoreLabel.text = "[#${when (score) {
                in 0 until 60 -> TRY_AGAIN_COLOUR
                in 60 until 80 -> OK_COLOUR
                else -> SUPERB_COLOUR
            }}]$score[]"
        } else {
            scoreLabel.text = ""
            for (h in 1..hearts.total) {
                scoreLabel.text += "[${if (h <= hearts.num) "#$HEART_COLOUR" else "DARK_GRAY"}]$HEART_SYMBOL[]"
            }
        }
        skillStarLabel.textColor = when {
            playalong.skillStarInput == null -> Colors.get("X")
            !playalong.gotSkillStar -> Color.GRAY
            else -> Color.YELLOW
        }
        monsterGoalIcon.visible = monsterGoal > 0f
        monsterGoalLabel.visible = monsterGoal > 0f
    }

    fun onInput(inputAction: InputAction, inputResult: InputResult, start: Boolean) {
        timingDisplayStage.flash(inputResult)
        if (inputResult.timing == InputTiming.MISS && hearts.total > 0 && heartsInvuln <= 0f && hearts.num > 0) {
            val oldHeartsCount = hearts.num
            hearts = hearts.copy(num = hearts.num - 1)
            heartsCooldown = 1f
            heartsInvuln = 1.75f
            updateLabels()
            if (hearts.num == 0 && oldHeartsCount > 0) {
                remix.playState = PAUSED
            }
            if (preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, true) && remix.playbackStart < playalong.inputActions.firstOrNull()?.beat ?: Float.NEGATIVE_INFINITY) {
                AssetRegistry.get<Sound>("playalong_sfx_perfect_fail").play()
            }
        }

        if (inputResult.timing == InputTiming.ACE && playalong.isMonsterGoalActive && this.visible) {
            if (preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_ACE, true)) {
                val sound = AssetRegistry.get<Sound>("playalong_sfx_monster_ace")
                val volume = MathUtils.lerp(0.5f, 1f, ((remix.seconds - lastMonsterAceSfx) / remix.speedMultiplier / 0.85f).coerceIn(0f, 1f))
                sound.play(volume)
                lastMonsterAceSfx = remix.seconds
            }
        }
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
        val first = playalong.inputActions.firstOrNull()
        if (first != null && remix.playbackStart < first.beat && perfectIcon.visible && this.visible) {
            if (preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_PERFECT_FAIL, true) && hearts.total == 0) {
                AssetRegistry.get<Sound>("playalong_sfx_perfect_fail").play()
            }
        }
    }

    fun onPerfectHit() {
        perfectAnimation = 1f
        perfectIcon.image = perfectTexReg
    }

    fun onMonsterGoalFail() {
        remix.playState = PlayState.PAUSED
        if (preferences.getBoolean(PreferenceKeys.PLAYALONG_SFX_MONSTER_FAIL, true) && this.visible) {
            AssetRegistry.get<Sound>("playalong_sfx_monster_fail").play()
        }
    }

    fun reset() {
        remix.recomputeCachedData()
        val noPlayalong = playalong.inputActions.isEmpty()
        noEntitiesLabel.visible = noPlayalong
        lowerStage.visible = !noPlayalong
        updateLabels()
        perfectIcon.image = perfectTexReg
        perfectAnimation = 0f
        flickingStage.visible = playalong.needsTouchScreen
    }

    fun onTapDown(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE, PlayalongInput.TOUCH_QUICK_TAP), 0, tapPoint.isMouse)
    }

    fun onTapRelease(tapPoint: FlickingStage.TapPoint, short: Boolean) {
        playalong.handleInput(false, if (short) setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE, PlayalongInput.TOUCH_QUICK_TAP) else setOf(PlayalongInput.TOUCH_TAP, PlayalongInput.TOUCH_RELEASE), 0, tapPoint.isMouse)
    }

    fun onFlick(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_FLICK), 0, tapPoint.isMouse)
    }

    fun onTapSlide(tapPoint: FlickingStage.TapPoint) {
        playalong.handleInput(true, setOf(PlayalongInput.TOUCH_SLIDE), 0, tapPoint.isMouse)
    }

    private fun setRemixSpeed() {
        if (playalong.inputActions.isNotEmpty()) {
            remix.speedMultiplier = tempoChange / 100f
        }
    }

    fun onShow() {
        if (remix.playState == STOPPED) {
            reset()
        }
        setRemixSpeed()
        disableButtonsWhilePlaying(remix.playState != STOPPED)
    }

    fun onHide() {
        remix.speedMultiplier = 1f
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        if (playalong.monsterGoal != this.monsterGoal) {
            playalong.monsterGoal = this.monsterGoal
        }

        // Skill Star pulses 3 beats before hitting it
        val skillStarEntity = playalong.skillStarEntity
        if (skillStarZoom == 0f && skillStarEntity != null && playalong.skillStarInput != null && remix.playState == PLAYING) {
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
        scoreLabel.fontScaleMultiplier = (if (hearts.total > 0 && heartsCooldown > 0) Interpolation.pow4In.apply(1.5f, 1f, (1f - heartsCooldown)) else 1f) * 0.4f

        val perfectLabelFlash = MathHelper.getSawtoothWave(1.35f)
        perfectLabel.textColor?.a = if (remix.playState != PLAYING || perfectLabelFlash > 0.35f) 1f else 0f

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

        if (heartsCooldown > 0)
            heartsCooldown -= Gdx.graphics.deltaTime / 0.5f
        if (heartsCooldown < 0)
            heartsCooldown = 0f

        if (remix.playState == PLAYING) {
            if (heartsInvuln > 0)
                heartsInvuln -= Gdx.graphics.deltaTime
            if (heartsInvuln < 0)
                heartsInvuln = 0f
        }
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