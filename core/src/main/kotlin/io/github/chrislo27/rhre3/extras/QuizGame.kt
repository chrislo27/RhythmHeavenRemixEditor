package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils
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
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.getTextHeight
import io.github.chrislo27.toolboks.util.gdxutils.getTextWidth
import io.github.chrislo27.toolboks.util.gdxutils.scaleMul
import rhmodding.bread.model.bccad.Animation
import rhmodding.bread.model.bccad.BCCAD
import java.nio.ByteBuffer


class QuizGame(main: RHRE3Application) : RhythmGame(main) {

    private val bgSheet: Texture = Texture("extras/quiz/quiz_bg.png").apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val partsSheet: Texture = Texture("extras/quiz/quiz_parts.png").apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val bgBccad: BCCAD = BCCAD.read(ByteBuffer.wrap(Gdx.files.internal("extras/quiz/quiz_bg.bin").readBytes()))
    private val partsBccad: BCCAD = BCCAD.read(ByteBuffer.wrap(Gdx.files.internal("extras/quiz/quiz_parts.bin").readBytes()))

    private val bgAni: Animation = bgBccad.animations[0]
    private val panelLAni: Animation = bgBccad.animations[1]
    private val panelRAni: Animation = bgBccad.animations[2]
    private val signAni: Animation = bgBccad.animations[3]
    private val signBrokenAni: Animation = bgBccad.animations[4]
    private val spotlightAni: Animation = bgBccad.animations[5]
    private val balloonAni: Animation = bgBccad.animations[6]
    private val playerHandRAni: Animation = partsBccad.animations[0]
    private val playerHandRFrontAni: Animation = partsBccad.animations[1]
    private val playerHandLAni: Animation = partsBccad.animations[2]
    private val playerReadyRAni: Animation = partsBccad.animations[3]
    private val playerReadyRFrontAni: Animation = partsBccad.animations[4]
    private val playerReadyLAni: Animation = partsBccad.animations[5]
    private val hostHandLAni: Animation = partsBccad.animations[6]
    private val hostHandLFrontAni: Animation = partsBccad.animations[7]
    private val hostHandRAni: Animation = partsBccad.animations[8]
    private val hostReadyLAni: Animation = partsBccad.animations[9]
    private val hostReadyLFrontAni: Animation = partsBccad.animations[10]
    private val hostReadyRAni: Animation = partsBccad.animations[11]
    private val playerKeyAni: Animation = partsBccad.animations[34]
    private val playerButtonAni: Animation = partsBccad.animations[35]
    private val hostKeyAni: Animation = partsBccad.animations[36]
    private val hostButtonAni: Animation = partsBccad.animations[37]
    private val playerFaceNeutralAni: Animation = partsBccad.animations[12]
    private val playerFaceGleeAni: Animation = partsBccad.animations[13]
    private val playerFaceSadAni: Animation = partsBccad.animations[14]
    private val playerFaceEvo1Ani: Animation = partsBccad.animations[15]
    private val playerFaceEvo2Ani: Animation = partsBccad.animations[16]
    private val playerFaceEvo3Ani: Animation = partsBccad.animations[17]
    private val hostFaceNeutralAni: Animation = partsBccad.animations[18]
    private val hostFaceGleeAni: Animation = partsBccad.animations[19]
    private val hostFaceSadAni: Animation = partsBccad.animations[20]
    private val hostFaceEvo1Ani: Animation = partsBccad.animations[21]
    private val hostFaceEvo2Ani: Animation = partsBccad.animations[22]
    private val hostFaceEvo3Ani: Animation = partsBccad.animations[23]
    private val hostFaceEvo4Ani: Animation = partsBccad.animations[24]
    private val playerNumberAni: Animation = partsBccad.animations[25]
    private val hostNumberAni: Animation = partsBccad.animations[26]
    private val timerBaseAni: Animation = partsBccad.animations[38]
    private val timerHandRegion: TextureRegion = TextureRegion(partsSheet, 351, 646, 10, 41)
    private val hitLFrames = playerHandLAni.steps.sumBy { it.delay.toInt() }
    private val hitRFrames = playerHandRAni.steps.sumBy { it.delay.toInt() }
    private val readyFrames = playerReadyLAni.steps.sumBy { it.delay.toInt() }
    private val buttonFrames = playerKeyAni.steps.sumBy { it.delay.toInt() }

    private val sfxReveal: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/answer_reveal.ogg"))
    private val sfxCorrect: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/correct.ogg"))
    private val sfxIncorrect: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/incorrect.ogg"))
    private val sfxTimerStart: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/timer_start.ogg"))
    private val sfxTimerStop: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/timer_stop.ogg"))
    private val sfxTripleBell: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/triple_bell.ogg"))
    private val sfxPlayerA: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/contestantA.ogg"))
    private val sfxPlayerD: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/contestantDPad.ogg"))
    private val sfxHostA: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/hostA.ogg"))
    private val sfxHostD: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/quiz/hostDPad.ogg"))

    private val screenColorShader: ShaderProgram = ScreenColorShader.createShader()

    private var globalFrame: Float = 0f
    private val hostState: PersonState = PersonState(false)
    private val playerState: PersonState = PersonState(true)
    private var hostText: String? = null
        set(value) {
            field = value
            if (value != null) playState = PlayState.PAUSED
        }
    private var showTimer = false
    private var timerProgress: Float = 0f
    private var allowPlayerInput = false
    private var showSpotlight = false
    private var playerCounter = 0
    private var hostCounter = -1

    private val patternsType: Int = MathUtils.random(0, 0)
    private var segmentsCompleted = 0
    private var score = 0
    private val highScore = main.preferences.getInteger(PreferenceKeys.EXTRAS_QUIZ_HIGH_SCORE, 0).coerceAtLeast(0)
    private var exploded = false

    init {
        camera.setToOrtho(false, 240 / 9f * 16f, 240f)
        camera.zoom = 1f
        camera.update()

        hostState.lockToReady()
        playerState.lockToReady()

        tempos.add(TempoChange(tempos, 0f, 60f, Swing.STRAIGHT, 0f))
        events += TextEvent(0f, Localization["extras.quiz.dialogue.intro0"])
        events += TextEvent(0.25f, Localization["extras.quiz.dialogue.intro1"])
        events += TextEvent(0.5f, Localization["extras.quiz.dialogue.intro2"])
        events += RGSimpleEvent(this, 0.75f) {
            tempos.clear()
            tempos.add(TempoChange(tempos, 0f, 120f, Swing.STRAIGHT, 0f))
            events.clear()
            events += GenerateSegmentEvent(0f, segmentsCompleted)
        }
    }

    override fun _render(main: RHRE3Application, batch: SpriteBatch) {
        val width = camera.viewportWidth
        val height = camera.viewportHeight

        val centreX = width / 2f
        val centreY = height / 2f

        // Render stage
        bgAni.steps.forEach { step ->
            step.render(batch, bgSheet, bgBccad.sprites, centreX, centreY)
        }

        val panelLOffsetX = -169f + 64f
        val panelLOffsetY = 36f - 104f
        val panelROffsetX = 42f + 64f
        val panelROffsetY = 36f - 104f + 2f
        val playerROffsetX = -12f
        val playerROffsetY = 48f
        val playerLOffsetX = 18f
        val playerLOffsetY = 42f
        val playerFaceOffX = 8f
        val playerFaceOffY = 70f
        val playerNumOffX = -8f
        val playerNumOffY = -9f
        val hostLOffsetX = 20f
        val hostLOffsetY = 48f
        val hostROffsetX = -18f
        val hostROffsetY = 42f
        val hostFaceOffX = -16f
        val hostFaceOffY = 70f
        val hostNumOffX = 8f
        val hostNumOffY = -8f

        // Render bodies
        // {Player|Master}{Hand|Ready}{R|L} animations have the body
        (if (playerState.useReady) playerReadyRAni else playerHandRAni).render(batch, partsSheet, partsBccad.sprites, playerState.bodyFrame, centreX + panelROffsetX + playerROffsetX, centreY + panelROffsetY + playerROffsetY)
        (if (hostState.useReady) hostReadyLAni else hostHandLAni).render(batch, partsSheet, partsBccad.sprites, hostState.bodyFrame, centreX + panelLOffsetX + hostLOffsetX, centreY + panelLOffsetY + hostLOffsetY)

        // Render panels
        panelLAni.render(batch, bgSheet, bgBccad.sprites, 0, centreX, centreY)
        panelRAni.render(batch, bgSheet, bgBccad.sprites, 0, centreX, centreY)
        // Render numbers
        run {
            val digitTen = if (hostCounter < 0) 10 else (hostCounter / 10).coerceIn(0, 9)
            val digitOne = if (hostCounter < 0) 10 else (hostCounter.coerceIn(0, 99) % 10)
            hostNumberAni.steps[digitTen].render(batch, partsSheet, partsBccad.sprites, centreX + panelLOffsetX + hostNumOffX, centreY + panelLOffsetY + hostNumOffY)
            hostNumberAni.steps[digitOne].render(batch, partsSheet, partsBccad.sprites, centreX + panelLOffsetX + hostNumOffX + 4f * 6, centreY + panelLOffsetY + hostNumOffY + 4f)
        }
        run {
            val digitTen = if (playerCounter < 0) 10 else (playerCounter / 10).coerceIn(0, 9)
            val digitOne = if (playerCounter < 0) 10 else (playerCounter.coerceIn(0, 99) % 10)
            playerNumberAni.steps[digitTen].render(batch, partsSheet, partsBccad.sprites, centreX + panelROffsetX + playerNumOffX - 4f * 6, centreY + panelROffsetY + playerNumOffY + 4f)
            playerNumberAni.steps[digitOne].render(batch, partsSheet, partsBccad.sprites, centreX + panelROffsetX + playerNumOffX, centreY + panelROffsetY + playerNumOffY)
        }

        // Render buttons on panels
        hostButtonAni.render(batch, partsSheet, partsBccad.sprites, hostState.dpadFrame, centreX + panelLOffsetX, centreY + panelLOffsetY + 3f)
        hostKeyAni.render(batch, partsSheet, partsBccad.sprites, hostState.aBtnFrame, centreX + panelLOffsetX, centreY + panelLOffsetY)
        playerButtonAni.render(batch, partsSheet, partsBccad.sprites, playerState.dpadFrame, centreX + panelROffsetX, centreY + panelROffsetY)
        playerKeyAni.render(batch, partsSheet, partsBccad.sprites, playerState.aBtnFrame, centreX + panelROffsetX, centreY + panelROffsetY)

        // Render arms, faces, hands
        playerState.currentFace.render(batch, partsSheet, partsBccad.sprites, playerState.faceFrame, centreX + panelROffsetX + playerFaceOffX, centreY + panelROffsetY + playerFaceOffY)
        (if (playerState.useReady) playerReadyLAni else playerHandLAni).render(batch, partsSheet, partsBccad.sprites, playerState.bodyInvFrame, centreX + panelROffsetX + playerLOffsetX, centreY + panelROffsetY + playerLOffsetY)
        (if (playerState.useReady) playerReadyRFrontAni else playerHandRFrontAni).render(batch, partsSheet, partsBccad.sprites, playerState.bodyFrame, centreX + panelROffsetX + playerROffsetX, centreY + panelROffsetY + playerROffsetY)

        hostState.currentFace.render(batch, partsSheet, partsBccad.sprites, hostState.faceFrame, centreX + panelLOffsetX + hostFaceOffX, centreY + panelLOffsetY + hostFaceOffY)
        (if (hostState.useReady) hostReadyRAni else hostHandRAni).render(batch, partsSheet, partsBccad.sprites, hostState.bodyInvFrame, centreX + panelLOffsetX + hostROffsetX, centreY + panelLOffsetY + hostROffsetY)
        (if (hostState.useReady) hostReadyLFrontAni else hostHandLFrontAni).render(batch, partsSheet, partsBccad.sprites, hostState.bodyFrame, centreX + panelLOffsetX + hostLOffsetX, centreY + panelLOffsetY + hostLOffsetY)

        // Render timer
        if (showTimer) {
            timerBaseAni.render(batch, partsSheet, partsBccad.sprites, 0, centreX, centreY)
            val w = timerHandRegion.regionWidth
            val h = timerHandRegion.regionHeight
            batch.draw(timerHandRegion, centreX - w / 2f, centreY - h / 2f, w / 2f, h / 2f, w * 1f, h * 1f, 1f, 1f, -360f * timerProgress)
        }

        // Render sign (needs shader)
        batch.shader = screenColorShader
        signAni.renderWithShader(batch, screenColorShader, bgSheet, bgBccad.sprites, globalFrame.toInt(), centreX, centreY)
        batch.shader = null

        // Score
        run {
            val font = main.defaultBorderedFontLarge
            font.scaleFont(camera)
            font.scaleMul(0.4f)
            font.setColor(1f, 1f, 1f, 1f)
            font.drawCompressed(batch, Localization["extras.playing.highScore", "${highScore.coerceAtLeast(score)}"], width * 0.025f, height * 0.95f, width * 0.35f, Align.left)
            font.drawCompressed(batch, Localization["extras.playing.score", "$score"], width * 0.625f, height * 0.95f, width * 0.35f, Align.right)
            font.unscaleFont()
        }

        if (showSpotlight) {
            spotlightAni.render(batch, bgSheet, bgBccad.sprites, 0, centreX, centreY)
        }

        val hostText = this.hostText
        if (hostText != null) {
            balloonAni.render(batch, bgSheet, bgBccad.sprites, 0, centreX, centreY)
            val font = main.defaultFontMedium
            font.scaleFont(camera)
            font.scaleMul(0.75f)
            font.setColor(0f, 0f, 0f, 1f)
            val textBoxWidth = width * 0.35f
            val textWidth = font.getTextWidth(hostText, textBoxWidth, false)
            val textHeight = font.getTextHeight(hostText)
            font.drawCompressed(batch, hostText, (centreX - textWidth / 2f).coerceAtLeast(centreX - textBoxWidth * 0.5f), height * 0.57f + (height * 0.32f / 2f) + textHeight / 2, textBoxWidth, Align.left)
            val bordered = MathHelper.getSawtoothWave(1.25f) >= 0.25f && InputButtons.A !in pressedInputs
            font.draw(batch, if (bordered) "\uE0A0" else "\uE0E0", centreX, height * 0.545f, 0f, Align.center, false)
            font.unscaleFont()
            font.setColor(1f, 1f, 1f, 1f)
        }
    }

    override fun update(delta: Float) {
        super.update(delta)
        globalFrame += Gdx.graphics.deltaTime * 60f
        playerState.update(delta)
        hostState.update(delta)
    }
    
    private fun checkCounterExceeded() {
        if (playerCounter >= 100 && !exploded) {
            exploded = true
            main.screen = FakeCrashScreen(main, IllegalStateException("Quiz Show cannot handle this many inputs. Nice try."), main.screen)
        }
    }

    override fun onInput(button: InputButtons, release: Boolean): Boolean = super.onInput(button, release) || run {
        val hostText = this.hostText
        if (hostText != null) {
            if (button == InputButtons.A) {
                if (!release) {
                    AssetRegistry.get<Sound>("sfx_text_advance_1").play()
                } else {
                    AssetRegistry.get<Sound>("sfx_text_advance_2").play()
                    this.hostText = null
                    playState = PlayState.PLAYING
                }
                true
            } else false
        } else if (!release && allowPlayerInput) {
            if (button == InputButtons.A) {
                playerState.hitLeft()
                playerCounter++
                checkCounterExceeded()
                sfxPlayerA.play()
                true
            } else if (button == InputButtons.LEFT || button == InputButtons.RIGHT || button == InputButtons.UP || button == InputButtons.DOWN) {
                playerState.hitRight()
                playerCounter++
                checkCounterExceeded()
                sfxPlayerD.play()
                true
            } else false
        } else false
    }

    override fun dispose() {
        super.dispose()
        screenColorShader.dispose()
        bgSheet.dispose()
        partsSheet.dispose()
        sfxReveal.dispose()
        sfxCorrect.dispose()
        sfxIncorrect.dispose()
        sfxTimerStart.dispose()
        sfxTimerStop.dispose()
        sfxTripleBell.dispose()
        sfxPlayerA.dispose()
        sfxPlayerD.dispose()
        sfxHostA.dispose()
        sfxHostD.dispose()
        val timesPlayedKey = PreferenceKeys.EXTRAS_QUIZ_TIMES_PLAYED
        main.preferences.putInteger(timesPlayedKey, main.preferences.getInteger(timesPlayedKey, 0) + 1).flush()
        if (!exploded && score > highScore) {
            main.preferences.putInteger(PreferenceKeys.EXTRAS_QUIZ_HIGH_SCORE, score).flush()
        }
    }

    /**
     * @return A triple of events list, pattern duration, and tempo in that order
     */
    fun generateInputs(segmentIndex: Int): Triple<List<RGEvent>, Float, Float> {
        val events = mutableListOf<RGEvent>()
        val duration: Float
        val tempo: Float
        fun inp(beat: Float, a: Boolean) = events.add(HostButtonEvent(beat, a))
        when (val patternType = patternsType.coerceIn(0, 0)) {
            0 -> {
                duration = 6f
                tempo = 100f + 5 * segmentIndex
                when (if (segmentIndex in 0..11) segmentIndex else MathUtils.random(0, 11)) {
                    0 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(2f, true)
                    }
                    1 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.5f, false)
                        inp(2f, true)
                    }
                    2 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.50f, true)
                        inp(1.75f, false)
                        inp(2f, false)
                    }
                    3 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.50f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                        inp(3f, false)
                    }
                    4 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.50f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.50f, true)
                        inp(2.75f, false)
                        inp(3f, true)
                    }
                    5 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.5f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                    }
                    6 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.5f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                        inp(2.75f, false)
                    }
                    7 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.5f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                    }
                    8 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.5f, true)
                        inp(2f, true)
                        inp(2.25f, false)
                        inp(2.5f, true)
                        inp(2.75f, false)
                    }
                    9 -> {
                        inp(0f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.5f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                        inp(2.75f, false)
                    }
                    10 -> {
                        inp(0f, true)
                        inp(0.5f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.5f, true)
                        inp(2f, true)
                        inp(2.5f, false)
                    }
                    11 -> {
                        inp(0f, true)
                        inp(0.5f, true)
                        inp(1f, true)
                        inp(1.25f, false)
                        inp(1.5f, true)
                        inp(1.75f, false)
                        inp(2f, true)
                        inp(2.5f, true)
                        inp(2.75f, false)
                    }
                }
            }
            else -> error("Pattern type $patternType not supported")
        }
        return Triple(events, duration, tempo)
    }

    override fun getDebugString(): String {
        return super.getDebugString() + "score: $score\nhighscore: $highScore\nsegmentIndex: $segmentsCompleted"
    }

    inner class HostButtonEvent(beat: Float, val aButton: Boolean) : RGEvent(this, beat) {
        override fun onStart() {
            if (!aButton) {
                hostState.hitRight()
                sfxHostA.play()
            } else {
                hostState.hitLeft()
                sfxHostD.play()
            }
        }
    }

    inner class TextEvent(beat: Float, val text: String?) : RGEvent(this, beat) {
        override fun onStart() {
            hostText = text
        }
    }

    inner class TimerEvent(beat: Float, duration: Float) : RGEvent(this, beat) {
        init {
            this.length = duration
        }

        override fun onStart() {
            timerProgress = 0f
            sfxTimerStart.play()
        }

        override fun whilePlaying() {
            timerProgress = ((game.beat - this.beat) / length).coerceIn(0f, 1f)
        }

        override fun onEnd() {
            timerProgress = 0f
            sfxTimerStop.play()
        }
    }

    inner class GenerateSegmentEvent(beat: Float, val segmentIndex: Int) : RGEvent(this, beat) {
        override fun onStart() {
            tempos.clear()
            tempos.add(TempoChange(tempos, 0f, 60f / 1.5f, Swing.STRAIGHT, 0f))
            events += RGSimpleEvent(this@QuizGame, 0f) {
                hostState.unlockReady()
                hostState.setFace(if (segmentsCompleted >= 20) hostFaceEvo4Ani else if (segmentsCompleted >= 15) hostFaceEvo3Ani else if (segmentIndex >= 10) hostFaceEvo2Ani else if (segmentsCompleted >= 5) hostFaceEvo1Ani else hostFaceNeutralAni, true)
                hostState.faceFrame = hostState.faceFrameLimit - 1
            }
            // Generate inputs
            val (inputs: List<RGEvent>, patternLength: Float, tempo: Float) = generateInputs(segmentIndex)
            tempos.add(TempoChange(tempos, 1f, tempo, Swing.STRAIGHT, 0f))
            inputs.onEach { it.beat += 1f }
            events.addAll(inputs)
            val numHostInputs = inputs.size
            tempos.add(TempoChange(tempos, 1f + patternLength, 60f / 0.5f, Swing.STRAIGHT, 0f))
            events += RGSimpleEvent(this@QuizGame, 2f + patternLength) {
                hostState.lockToReady()
                hostState.lockFaceFrame = false
            }
            events += RGSimpleEvent(this@QuizGame, 3f + patternLength) {
                showTimer = true
                timerProgress = 0f
                playerState.unlockReady()
                playerState.setFace(if (segmentsCompleted >= 20) playerFaceEvo3Ani else if (segmentsCompleted >= 15) playerFaceEvo2Ani else if (segmentsCompleted >= 10) playerFaceEvo1Ani else playerFaceNeutralAni, true)
                playerState.faceFrame = playerState.faceFrameLimit - 1
            }
            tempos.add(TempoChange(tempos, 4f + patternLength, tempo, Swing.STRAIGHT, 0f))
            events += RGSimpleEvent(this@QuizGame, 4f + patternLength) {
                allowPlayerInput = true
            }
            events += TimerEvent(4f + patternLength, patternLength)
            tempos.add(TempoChange(tempos, 4f + patternLength * 2, 60f, Swing.STRAIGHT, 0f))
            events += RGSimpleEvent(this@QuizGame, 4.25f + patternLength * 2) {
                playerState.lockToReady()
                playerState.lockFaceFrame = false
                allowPlayerInput = false
                showTimer = false
                sfxTripleBell.play()
            }
            events += TextEvent(4.5f + patternLength * 2, Localization["extras.quiz.dialogue.time0"])
            events += TextEvent(4.75f + patternLength * 2, Localization["extras.quiz.dialogue.time1"])
            events += RGSimpleEvent(this@QuizGame, 5.5f + patternLength * 2) {
                showSpotlight = true
            }
            events += RGSimpleEvent(this@QuizGame, 6.5f + patternLength * 2) {
                hostCounter = numHostInputs
                sfxReveal.play()
            }
            events += RGSimpleEvent(this@QuizGame, 7.5f + patternLength * 2) {
                showSpotlight = false
                // Determine correctness here
                val correct = playerCounter == hostCounter
                if (correct) {
                    score++
                    sfxCorrect.play()
                    hostText = Localization["extras.quiz.dialogue.correct0"]
                    playerState.setFace(playerFaceGleeAni, false)
                    hostState.setFace(hostFaceGleeAni, false)
                    events += TextEvent(7.75f + patternLength * 2, Localization["extras.quiz.dialogue.correct1"])
                    events += RGSimpleEvent(this@QuizGame, 7.75f + patternLength * 2) {
                        playerState.setFace(playerFaceNeutralAni, true)
                        hostState.setFace(hostFaceNeutralAni, true)
                    }
                    events += EndSegmentEvent(8f + patternLength * 2)
                } else {
                    sfxIncorrect.play()
                    hostText = Localization["extras.quiz.dialogue.incorrect0"]
                    playerState.setFace(playerFaceSadAni, false)
                    hostState.setFace(hostFaceSadAni, false)
                    events += TextEvent(7.75f + patternLength * 2, Localization["extras.quiz.dialogue.incorrect1"])
                    events += RGSimpleEvent(this@QuizGame, 7.75f + patternLength * 2) {
                        hostState.setFace(hostFaceNeutralAni, true)
                    }
                    events += RGSimpleEvent(this@QuizGame, 8.5f + patternLength * 2) {
                        playState = PlayState.PAUSED
                        main.screen = TransitionScreen(main, main.screen, ScreenRegistry["info"], WipeTo(Color.BLACK, 0.35f), WipeFrom(Color.BLACK, 0.35f))
                    }
                }
            }
        }
    }

    inner class EndSegmentEvent(beat: Float) : RGEvent(this, beat) {
        override fun onStart() {
            events.clear()
            seconds = 0f
            segmentsCompleted++
            hostCounter = -1
            playerCounter = 0
            events += GenerateSegmentEvent(0f, segmentsCompleted)
        }
    }

    inner class PersonState(val player: Boolean) {

        private var frameCounter = 0f

        var stayReady: Boolean = true
        var useReady: Boolean = true
        var faceFrame = 0
        var currentFace: Animation = if (player) playerFaceNeutralAni else hostFaceNeutralAni
            private set
        var lockFaceFrame = false
        var faceFrameLimit = 0
            private set
        private var readyFrame: Int = 0
        private var leftHitFrame: Int = 0
        private var rightHitFrame: Int = 0
        private var padButtonFrame: Int = 0
        private var aButtonFrame: Int = 0

        val bodyFrame: Int
            get() = if (stayReady) 0 else if (useReady) readyFrame else (if (player) rightHitFrame else leftHitFrame)
        val bodyInvFrame: Int
            get() = if (stayReady) 0 else if (useReady) readyFrame else (if (player) leftHitFrame else rightHitFrame)
        val dpadFrame: Int
            get() = if (player) (buttonFrames - padButtonFrame - 1) else padButtonFrame
        val aBtnFrame: Int
            get() = if (player) (buttonFrames - aButtonFrame - 1) else aButtonFrame

        fun update(delta: Float) {
            frameCounter += delta * 60f
            if (frameCounter >= 1f) {
                val frames: Int = frameCounter.toInt()
                frameCounter -= frames

                readyFrame += frames
                if (readyFrame >= readyFrames) readyFrame = readyFrames - 1
                leftHitFrame += frames
                if (leftHitFrame >= hitLFrames) leftHitFrame = hitLFrames - 1
                rightHitFrame += frames
                if (rightHitFrame >= hitRFrames) rightHitFrame = hitRFrames - 1
                padButtonFrame -= frames
                if (padButtonFrame < 0) padButtonFrame = 0
                aButtonFrame -= frames
                if (aButtonFrame < 0) aButtonFrame = 0
                if (!lockFaceFrame) {
                    faceFrame += frames
                }
                if (faceFrame >= faceFrameLimit) faceFrame = faceFrameLimit - 1
            }
        }

        fun setFace(animation: Animation, lock: Boolean) {
            currentFace = animation
            faceFrame = 0
            faceFrameLimit = animation.steps.sumBy { it.delay.toInt() }
            lockFaceFrame = lock
        }

        fun hitLeft() {
            stayReady = false
            useReady = false
            leftHitFrame = 0
            padButtonFrame = buttonFrames
            lockFaceFrame = false
            faceFrame = 0
        }

        fun hitRight() {
            stayReady = false
            useReady = false
            rightHitFrame = 0
            aButtonFrame = buttonFrames
            lockFaceFrame = false
            faceFrame = 0
        }

        fun lockToReady() {
            stayReady = true
            useReady = true
            readyFrame = 0
        }

        fun unlockReady() {
            stayReady = false
            useReady = true
            readyFrame = 40
        }
    }

}