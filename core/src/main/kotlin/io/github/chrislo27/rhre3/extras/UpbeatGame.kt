package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.rhre3.util.*
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.transition.TransitionScreen
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import org.lwjgl.openal.AL10
import rhmodding.bread.model.brcad.Animation
import rhmodding.bread.model.brcad.BRCAD
import java.nio.ByteBuffer
import kotlin.math.absoluteValue
import kotlin.math.cos


class UpbeatGame(main: RHRE3Application, val hardMode: Boolean) : RhythmGame(main) {

    companion object {
        val MAX_OFFSET_SEC: Float = 5f / 60
    }

    private val sheet: Texture = Texture("extras/upbeat/upbeat_spritesheet.png").apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val brcad: BRCAD = BRCAD.read(ByteBuffer.wrap(Gdx.files.internal("extras/upbeat/upbeat.bin").readBytes()))

    private val bgRegion: TextureRegion = TextureRegion(sheet, 994, 2, 28, 28)
    private val needleRegion: TextureRegion = TextureRegion(sheet, 8, 456, 400, 48)
    private val needlePivot: Vector2 = Vector2(232f, 24f)
    private val shadowAni: Animation = brcad.animations[1] // Stand (not used), up R, up L, falldown
    private val stepUpLAni: Animation = brcad.animations[2]
    private val stepUpRAni: Animation = brcad.animations[3]
    private val stepAniFrameCount = stepUpLAni.steps.sumBy { it.delay.toInt() }
    private val hitLfromLAni: Animation = brcad.animations[4]
    private val hitLfromRAni: Animation = brcad.animations[5]
    private val hitRfromRAni: Animation = brcad.animations[6]
    private val hitRfromLAni: Animation = brcad.animations[7]
    private val gutsLfromLAni: Animation = brcad.animations[8]
    private val gutsLfromRAni: Animation = brcad.animations[9]
    private val gutsRfromRAni: Animation = brcad.animations[10]
    private val gutsRfromLAni: Animation = brcad.animations[11]
    private val downLfromLAni: Animation = brcad.animations[12]
    private val downLfromRAni: Animation = brcad.animations[13]
    private val downRfromRAni: Animation = brcad.animations[14]
    private val downRfromLAni: Animation = brcad.animations[15]
    private val lampAni: List<Animation> = brcad.animations.subList(17, 22) // sizes [0, 4]
    private val lampAniFrameCount: List<Int> = lampAni.map { it.steps.sumBy { it.delay.toInt() } }
    private val secretCodeAni: Animation = brcad.animations[16]
    private val sfxBip: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/bip.ogg"))
    private val sfxEndDing: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/ding.ogg"))
    private val sfxMetronomeL: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/metronomeL.ogg"))
    private val sfxMetronomeR: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/metronomeR.ogg"))
    private val sfxStep: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/step.ogg"))
    private val sfxFailVoice: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/fail_voice.ogg"))
    private val sfxFailBoink: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/fail_boink.ogg"))
    private val sfxCowbell: Sound = AssetRegistry["sfx_cowbell"]
    private val music: List<Music> = listOf(Gdx.audio.newMusic(Gdx.files.internal("extras/upbeat/music0.ogg")))
    private val musicFail: Music = Gdx.audio.newMusic(Gdx.files.internal("extras/fail_music_nohi.ogg"))
    private val musicFailHiScore: Music = Gdx.audio.newMusic(Gdx.files.internal("extras/fail_music_hi.ogg"))
    private val segmentTempos: List<Float> = listOf(75f, 82f, 90f, 100f, 108f, 115f, 125f, 140f, 150f)
    private val showSecretCodeFreq = 5 // On segment index 4, 9, etc

    private val uiCamera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    private val tmpUiMatrix: Matrix4 = Matrix4()
    private var manUpRight: Boolean = true
    private var stepFrame = 999f
    private var lampFlashFrame = 999f
    private var hitDownFrame = 0f
    private var hitAniFrameCount: Int = 1
    private var hitAnimation: Animation? = null
        set(value) {
            field = value
            hitAniFrameCount = value?.steps?.sumBy { it.delay.toInt() } ?: 1
        }
    private var lampIndex = 0
    private var codeIndex = -1
    private var needleMovement: Float = -1f // 0f = centre, -1f = right, 1f = left
    private val defaultNeedleAngle = 60f
    private var needleMaxAngle: Float = defaultNeedleAngle
    private var needleGoingRight = false
    private var currentMusic: Music? = null
    private var failed = false
    private var showFailScreen = false
    private var score = 0
    private val highScore = main.preferences.getInteger(if (hardMode) PreferenceKeys.EXTRAS_UPBEAT_HARD_HIGH_SCORE else PreferenceKeys.EXTRAS_UPBEAT_HIGH_SCORE, 0).coerceAtLeast(0)
    private var segmentsCompleted = 0
    private val setsCompleted: Int get() = segmentsCompleted / segmentTempos.size
    private var lastDistraction: Distractions = Distractions.NONE
    private var darknessAlpha = 0f
    private var fadeNeedle = false

    enum class Distractions {
        NONE, NEEDLE_FADE, DARKNESS, NARROW_ANGLE, WIDE_ANGLE;

        companion object {
            val REAL_LIST = values().toList() - NONE
        }
    }

    init {
        seconds = -1f
        events += GenerateSegmentEvent(0f, 0)
    }

    override fun _render(main: RHRE3Application, batch: SpriteBatch) {
        batch.packedColor = Color.WHITE_FLOAT_BITS
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        batch.draw(bgRegion, camera.position.x - width / 2f * camera.zoom, camera.position.y - height / 2f * camera.zoom, width * camera.zoom, height * camera.zoom)

        val manY = 0.53f * height
        shadowAni.steps[if (failed) 3 else if (manUpRight) 1 else 2].render(batch, sheet, brcad.sprites, width * 0.5f, manY)

        val needleScale = 1.25f
        if (fadeNeedle) {
            batch.setColor(1f, 1f, 1f, ((needleMovement.absoluteValue - 0.5f) * 2f).coerceIn(0f, 1f))
        }
        batch.draw(needleRegion, width / 2f - needlePivot.x, height * 0.235f, needlePivot.x, needlePivot.y, needleRegion.regionWidth.toFloat(), needleRegion.regionHeight.toFloat(), needleScale, needleScale, needleMovement * needleMaxAngle - 90f)
        batch.setColor(1f, 1f, 1f, 1f)

        val hitAni = hitAnimation
        if (hitAni != null) {
            val currentHit = hitAni.getCurrentStep(hitDownFrame.toInt())!!
            currentHit.render(batch, sheet, brcad.sprites, width * 0.5f, manY)

            batch.setColor(0f, 0f, 0f, darknessAlpha)
            batch.fillRect(camera.position.x - width / 2f * camera.zoom, camera.position.y - height / 2f * camera.zoom, width * camera.zoom, height * camera.zoom)
            batch.setColor(1f, 1f, 1f, 1f)
        } else {
            val stepAni = if (manUpRight) stepUpRAni else stepUpLAni
            val currentUpStep = stepAni.getCurrentStep(stepFrame.toInt())!!
            currentUpStep.render(batch, sheet, brcad.sprites, width * 0.5f, manY)

            batch.setColor(0f, 0f, 0f, darknessAlpha)
            batch.fillRect(camera.position.x - width / 2f * camera.zoom, camera.position.y - height / 2f * camera.zoom, width * camera.zoom, height * camera.zoom)
            batch.setColor(1f, 1f, 1f, 1f)

            val lampPartPoint = brcad.sprites[currentUpStep.spriteIndex.toInt()].parts[3]
            lampAni[lampIndex].getCurrentStep(lampFlashFrame.toInt())!!.render(batch, sheet, brcad.sprites,
                                                                               width * 0.5f + (lampPartPoint.posX - 512f + lampPartPoint.regionW.toInt() / 2),
                                                                               manY - lampPartPoint.posY - lampPartPoint.regionH.toInt() / 2f + 512f)
            if (codeIndex >= 0) {
                secretCodeAni.steps[codeIndex].render(batch, sheet, brcad.sprites,
                                                      width * 0.5f + (lampPartPoint.posX - 512f + lampPartPoint.regionW.toInt() / 2),
                                                      manY - lampPartPoint.posY - lampPartPoint.regionH.toInt() / 2f + 512f)
            }
        }

        uiCamera.update()
        tmpUiMatrix.set(batch.projectionMatrix)
        batch.projectionMatrix = uiCamera.combined

        val uiWidth = uiCamera.viewportWidth
        val uiHeight = uiCamera.viewportHeight

        if (showFailScreen) {
            batch.setColor(0f, 0f, 0f, 0.5f)
            batch.fillRect(0f, 0f, uiCamera.viewportWidth, uiCamera.viewportHeight)
            batch.setColor(1f, 1f, 1f, 1f)
            val font = main.defaultBorderedFontLarge
            font.setColor(1f, 1f, 1f, 1f)
            font.scaleFont(uiCamera)
            font.drawCompressed(batch, Localization["extras.playing.gameOver"], 0f, uiCamera.viewportHeight * 0.65f, uiCamera.viewportWidth, Align.center)
            font.scaleMul(0.4f)
            font.drawCompressed(batch, "[ESC]", 0f, uiCamera.viewportHeight * 0.25f, uiCamera.viewportWidth, Align.center)
            font.scaleMul(1 / 0.4f)
            if (score > highScore) {
                font.scaleMul(0.5f)
                font.color = Colors.get("RAINBOW")
                font.drawCompressed(batch, Localization["extras.playing.newHighScore"], 0f, uiCamera.viewportHeight * 0.475f, uiCamera.viewportWidth, Align.center)
                font.setColor(1f, 1f, 1f, 1f)
                font.scaleMul(1 / 0.5f)
            }
            font.unscaleFont()
            font.setColor(1f, 1f, 1f, 1f)
        }

        var font = main.defaultBorderedFontLarge
        font.setColor(1f, 1f, 1f, 1f)
        font.scaleFont(uiCamera)
        font.scaleMul(0.75f)
        font.draw(batch, "$score", uiWidth * 0.5f, uiHeight * 0.9f, 0f, Align.center, false)
        val line = font.capHeight
        font.unscaleFont()
        font = main.defaultBorderedFont
        if (hardMode) {
            font.setColor(1f, 0f, 0f, 1f)
        } else {
            font.setColor(1f, 69f / 255f, 13f / 255f, 1f)
        }
        font.scaleFont(uiCamera)
        font.draw(batch, Localization["extras.playing.highScore", "${highScore.coerceAtLeast(score)}"], uiWidth * 0.5f, uiHeight * 0.9f + line, 0f, Align.center, false)
        font.unscaleFont()
        font.setColor(1f, 1f, 1f, 1f)

        batch.projectionMatrix = tmpUiMatrix
    }

    override fun update(delta: Float) {
        super.update(delta)

        stepFrame += Gdx.graphics.deltaTime * 60f
        if (stepFrame > stepAniFrameCount - 1) stepFrame = stepAniFrameCount - 1f
        lampFlashFrame += Gdx.graphics.deltaTime * 60f
        if (lampFlashFrame > lampAniFrameCount[0] - 1) lampFlashFrame = lampAniFrameCount[0] - 1f // Use first lamp animation as reference (0 intentional)
        hitAnimation?.let {
            hitDownFrame += Gdx.graphics.deltaTime * 60f
            val maxDelay = hitAniFrameCount
            if (hitDownFrame > maxDelay - 1) hitDownFrame = maxDelay - 1f
        }

        if (!failed) {
            val nextInputs: List<InputEvent> = events.toList().filterIsInstance<InputEvent>().sortedBy { it.beat }
            nextInputs.forEach { i ->
                if (seconds > tempos.beatsToSeconds(i.beat) + MAX_OFFSET_SEC && !i.past) {
                    i.past = true
                    // Check if needle hits leg
                    if (needleGoingRight == i.newLegUpRight) {
                        if (i.triggered != 1) {
                            fail(!manUpRight, needleGoingRight, i.beat)
                        } else {
                            score++
                        }
                    }
                }
            }
        }
    }

    override fun onPauseTriggered(): Boolean {
        if (showFailScreen) {
            main.screen = TransitionScreen(main, main.screen, ScreenRegistry["info"], WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
            return true
        }
        return false
    }

    fun fail(hitRightLeg: Boolean, needleSweepingRight: Boolean, inputBeat: Float) {
        if (failed) return
        failed = true
        sfxFailBoink.play()
        sfxFailVoice.play()
        currentMusic?.stop()
        currentMusic = null
        events.clear()
        lampIndex = 0
        codeIndex = -1
        darknessAlpha = 0f
        camera.zoom = 1f
        events += MetronomeEvent(inputBeat - 0.5f, !needleGoingRight)
        events += RGSimpleEvent(this, tempos.secondsToBeats(tempos.beatsToSeconds(inputBeat + 0.5f) + 0.75f)) {
            playState = PlayState.PAUSED
            // Set guts or down animation
            hitDownFrame = 0f
            val guts = score > highScore // Guts is used when beating the high score
            hitAnimation = if (guts) {
                if (hitRightLeg) {
                    if (needleSweepingRight) gutsRfromLAni else gutsRfromRAni
                } else {
                    if (needleSweepingRight) gutsLfromLAni else gutsLfromRAni
                }
            } else {
                if (hitRightLeg) {
                    if (needleSweepingRight) downRfromLAni else downRfromRAni
                } else {
                    if (needleSweepingRight) downLfromLAni else downLfromRAni
                }
            }
            showFailScreen = true
            (if (score > highScore) musicFailHiScore else musicFail).play()
            if (score > highScore || highScore <= 0) {
                main.preferences.putInteger(if (hardMode) PreferenceKeys.EXTRAS_UPBEAT_HARD_HIGH_SCORE else PreferenceKeys.EXTRAS_UPBEAT_HIGH_SCORE, score).flush()
            }
        }
        // Set hit animation
        hitDownFrame = 0f
        hitAnimation = if (hitRightLeg) {
            if (needleSweepingRight) hitRfromLAni else hitRfromRAni
        } else {
            if (needleSweepingRight) hitLfromLAni else hitLfromRAni
        }
    }

    override fun onInput(button: InputButtons, release: Boolean): Boolean {
        if (super.onInput(button, release)) return true
        when (button) {
            InputButtons.A -> {
                if (!release) {
                    if (!failed) {
                        manUpRight = !manUpRight
                        stepFrame = 0f
                        sfxStep.play()
                        val nextInputs: List<InputEvent> = events.toList().filterIsInstance<InputEvent>().sortedBy { it.beat }
                        nextInputs.firstOrNull {
                            val s = tempos.beatsToSeconds(it.beat)
                            if (!it.past && seconds in (s - MAX_OFFSET_SEC)..(s + MAX_OFFSET_SEC)) {
                                it.triggered++
                                true
                            } else false
                        }
                    }
                    return true
                }
            }
            else -> {
            }
        }
        return false
    }

    override fun onPlayStateChange(old: PlayState, current: PlayState) {
        when (current) {
            PlayState.PLAYING -> currentMusic?.play()
            PlayState.STOPPED -> currentMusic?.stop()
            PlayState.PAUSED -> currentMusic?.pause()
        }
    }

    override fun dispose() {
        super.dispose()
        sheet.dispose()
        sfxMetronomeL.dispose()
        sfxMetronomeR.dispose()
        sfxBip.dispose()
        sfxEndDing.dispose()
        sfxStep.dispose()
        sfxFailVoice.dispose()
        sfxFailBoink.dispose()
        music.forEach { it.dispose() }
        musicFail.dispose()
        musicFailHiScore.dispose()
        val timesPlayedKey = if (hardMode) PreferenceKeys.EXTRAS_UPBEAT_HARD_TIMES_PLAYED else PreferenceKeys.EXTRAS_UPBEAT_TIMES_PLAYED
        main.preferences.putInteger(timesPlayedKey, main.preferences.getInteger(timesPlayedKey, 0) + 1).flush()
    }

    override fun getDebugString(): String {
        return super.getDebugString() + "score: $score\nhighscore: $highScore\nsegmentIndex: $segmentsCompleted\nmaxAngle: $needleMaxAngle deg\ndistraction: ${lastDistraction}"
    }

    inner class BipEvent(beat: Float) : RGEvent(this, beat) {
        override fun onStart() {
            sfxBip.play()
            lampFlashFrame = 0f
        }
    }

    inner class CowbellEvent(beat: Float) : RGEvent(this, beat) {
        override fun onStart() {
            sfxCowbell.play()
        }
    }

    inner class MetronomeEvent(beat: Float, val rightToLeft: Boolean) : RGEvent(this, beat) {
        init {
            length = 1f
        }

        private fun updateNeedle() {
            needleMovement = (if (rightToLeft) -1 else 1) * cos(Math.PI * ((this@UpbeatGame.beat - this.beat) / (length))).toFloat()
        }

        override fun onStart() {
            (if (rightToLeft) sfxMetronomeR else sfxMetronomeL).play()
            needleGoingRight = !rightToLeft
            updateNeedle()
        }

        override fun whilePlaying() {
            updateNeedle()
        }

        override fun onEnd() {
            updateNeedle()
        }
    }

    inner class TextBoxEvent(beat: Float, length: Float, val textBox: TextBox) : RGEvent(this, beat) {
        init {
            this.length = length
        }

        override fun onStart() {
            currentTextBox = textBox
        }

        override fun onEnd() {
            if (currentTextBox === this.textBox) currentTextBox = null
        }
    }

    inner class InputEvent(beat: Float, val newLegUpRight: Boolean) : RGEvent(this, beat) {
        var triggered = 0
        var past = false
    }

    inner class EndSegmentEvent(beat: Float) : RGEvent(this, beat) {
        init {
            length = 1f
        }

        override fun onStart() {
            sfxEndDing.play()
        }

        override fun onEnd() {
            events.clear()
            currentMusic?.stop()
            currentMusic = null
            seconds = 0f
            segmentsCompleted++
            fadeNeedle = false
            darknessAlpha = 0f
            events += GenerateSegmentEvent(0f, segmentsCompleted)
        }
    }

    inner class GenerateSegmentEvent(beat: Float, val segmentIndex: Int) : RGEvent(this, beat) {
        override fun onStart() {
            val showCode = (segmentIndex + 1) % showSecretCodeFreq == 0 && segmentIndex > 0
            val tempo = if (hardMode) {
                if (setsCompleted >= 1) (if (MathUtils.randomBoolean(0.333f)) {
                    (MathUtils.random(35, 55).toFloat())
                } else {
                    (MathUtils.random(160, 200).toFloat())
                }) else {
                    (segmentTempos[segmentIndex % segmentTempos.size] * 1.25f)
                }
            } else {
                if (segmentIndex < segmentTempos.size) {
                    segmentTempos[segmentIndex]
                } else listOf(160f, 170f, 180f)[(segmentIndex - segmentTempos.size) % 3]
            }
            tempos.clear()
            tempos.add(TempoChange(tempos, 0f, tempo, Swing.STRAIGHT, 0f))
            (0..7).forEach { b ->
                events += BipEvent(b + 0.5f)
            }
            events += CowbellEvent(0f)
            events += CowbellEvent(2f)
            events += CowbellEvent(4f)
            events += CowbellEvent(5f)
            events += CowbellEvent(6f)
            events += CowbellEvent(7f)
            events += RGSimpleEvent(game, 8f - 0.25f /* pickup measure */) {
                val music = music[0]
                currentMusic = music
                music.play()
                (music as? OpenALMusic)?.let {
                    val sourceIDField = OpenALMusic::class.java.getDeclaredField("sourceID")
                    sourceIDField.isAccessible = true
                    AL10.alSourcef(sourceIDField.getInt(it), AL10.AL_PITCH, tempo / 125f)
                }
            }
            (8 until 40).forEach { b ->
                events += MetronomeEvent(b.toFloat(), b % 2 == 0)
                events += BipEvent(b + 0.5f)
                events += InputEvent(b + 0.5f, b % 2 != 0)
            }
            events += EndSegmentEvent(40f)
            fun createTextBox(text: String): TextBox {
                return TextBox(text, false, offsetY = -400f, offsetW = -256f, offsetX = 128f, offsetH = 64f)
            }
            if (!hardMode) {
                if (segmentIndex in 1..26) {
                    events += TextBoxEvent(0f, 6f, createTextBox(Localization["extras.upbeat.praise${segmentIndex}"]))
                }
            } else {
                if (segmentIndex in 0..6) {
                    events += TextBoxEvent(0f, 6f, createTextBox(Localization["extras.upbeat.praiseHardMode${segmentIndex}"]))
                }
            }
            if (showCode && !hardMode) {
                events += RGSimpleEvent(this@UpbeatGame, 9.5f) {
                    lampIndex = 1
                }
                events += RGSimpleEvent(this@UpbeatGame, 10.5f) {
                    lampIndex = 2
                }
                events += RGSimpleEvent(this@UpbeatGame, 11.5f) {
                    lampIndex = 3
                }
                events += RGSimpleEvent(this@UpbeatGame, 12.5f) {
                    lampIndex = 4
                    codeIndex = 4
                }
                events += RGSimpleEvent(this@UpbeatGame, 13.5f) {
                    codeIndex = 5
                }
                events += RGSimpleEvent(this@UpbeatGame, 14.5f) {
                    codeIndex = 6
                }
                events += RGSimpleEvent(this@UpbeatGame, 15.5f) {
                    codeIndex = 7
                }
                events += RGSimpleEvent(this@UpbeatGame, 16.5f) {
                    codeIndex = 8
                }
                events += RGSimpleEvent(this@UpbeatGame, 17.5f) {
                    lampIndex = 0
                    codeIndex = -1
                }
            }
            if (hardMode) { // Distraction generator // setsCompleted >= 1
                val chosenDistraction = (Distractions.REAL_LIST - lastDistraction).takeUnless { it.isEmpty() }?.random() ?: Distractions.REAL_LIST.random()
                when (chosenDistraction) {
                    Distractions.NONE -> {
                    }
                    Distractions.NEEDLE_FADE -> {
                        fadeNeedle = true
                    }
                    Distractions.DARKNESS -> {
                        (0 until 4).forEach { i ->
                            events += RGSimpleEvent(this@UpbeatGame, 4.5f + i) {
                                darknessAlpha = (i + 1) * 0.25f
                            }
                            events += RGSimpleEvent(this@UpbeatGame, 38.5f - i) {
                                darknessAlpha = i * 0.25f
                            }
                        }
                    }
                    Distractions.NARROW_ANGLE -> {
                        (0 until 4).forEach { i ->
                            events += RGSimpleEvent(this@UpbeatGame, 4f + i) {
                                needleMaxAngle = MathUtils.lerp(defaultNeedleAngle, 15f, (i + 1) * 0.25f)
                            }
                        }
                        events += RGSimpleEvent(this@UpbeatGame, 39.5f) {
                            needleMaxAngle = defaultNeedleAngle
                        }
                    }
                    Distractions.WIDE_ANGLE -> {
                        (0 until 4).forEach { i ->
                            events += RGSimpleEvent(this@UpbeatGame, 4f + i) {
                                needleMaxAngle = MathUtils.lerp(defaultNeedleAngle, 115f, (i + 1) * 0.25f)
                            }
                        }
                        events += RGSimpleEvent(this@UpbeatGame, 39.5f) {
                            needleMaxAngle = defaultNeedleAngle
                        }
                    }
                }
                lastDistraction = chosenDistraction
            }
            events.sortBy { it.beat }
        }
    }
}