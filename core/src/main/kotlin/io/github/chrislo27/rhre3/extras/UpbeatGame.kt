package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl.audio.OpenALMusic
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.rhre3.util.Swing
import org.lwjgl.openal.AL10
import rhmodding.bread.model.brcad.Animation
import rhmodding.bread.model.brcad.BRCAD
import java.nio.ByteBuffer
import kotlin.math.cos


class UpbeatGame : RhythmGame() {

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
    private val sfxFailBoink: Sound = Gdx.audio.newSound(Gdx.files.internal("extras/upbeat/fail_boink.ogg"))
    private val music0: Music = Gdx.audio.newMusic(Gdx.files.internal("extras/upbeat/music0.ogg"))

    private var manUpRight: Boolean = true
    private var stepFrame = 999f
    private var lampFlashFrame = 999f
    private var needleMovement: Float = -1f // 0f = centre, -1f = centre, 1f = left
    private val needleMaxAngle: Float = 60f
    
    init {
        // DEBUG
        tempos.add(TempoChange(tempos, 0f, 80f, Swing.STRAIGHT, 0f))
        events += BipEvent(0 + 0.5f)
        events += BipEvent(1 + 0.5f)
        events += BipEvent(2 + 0.5f)
        events += BipEvent(3 + 0.5f)
        events += RGSimpleEvent(this, 4f - 0.25f /* pickup measure */) {
            val music = music0
            music.play()
            (music as? OpenALMusic)?.let {
                val sourceIDField = OpenALMusic::class.java.getDeclaredField("sourceID")
                sourceIDField.isAccessible = true
                AL10.alSourcef(sourceIDField.getInt(it), AL10.AL_PITCH, 80f / 125f)
            }
        }
        (4 until 36).forEach { b ->
            events += MetronomeEvent(b.toFloat(), b % 2 == 0)
            events += BipEvent(b + 0.5f)
        }
        events += EndSegmentEvent(36f)
    }

    override fun _render(main: RHRE3Application, batch: SpriteBatch) {
        batch.packedColor = Color.WHITE_FLOAT_BITS
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        batch.draw(bgRegion, 0f, 0f, width, height)

        val manY = 0.53f * height
        shadowAni.steps[if (manUpRight) 1 else 2].render(batch, sheet, brcad.sprites, width * 0.5f - 512f, manY - 512f)

        val needleScale = 1.25f
        batch.draw(needleRegion, width / 2f - needlePivot.x, height * 0.235f, needlePivot.x, needlePivot.y, needleRegion.regionWidth.toFloat(), needleRegion.regionHeight.toFloat(), needleScale, needleScale, needleMovement * needleMaxAngle - 90f)

        val stepAni = if (manUpRight) stepUpRAni else stepUpLAni
        val currentUpStep = stepAni.getCurrentStep(stepFrame.toInt())!!
        currentUpStep.render(batch, sheet, brcad.sprites, width * 0.5f - 512f, manY - 512f)
        val lampPartPoint = brcad.sprites[currentUpStep.spriteIndex.toInt()].parts[3]
        lampAni[0].getCurrentStep(lampFlashFrame.toInt())!!.render(batch, sheet, brcad.sprites,
                                                                   width * 0.5f - 512f + (lampPartPoint.posX - 512f + lampPartPoint.regionW.toInt() / 2),
                                                                   manY - lampPartPoint.posY - lampPartPoint.regionH.toInt() / 2f)
    }

    override fun update(delta: Float) {
        super.update(delta)
        
        stepFrame += Gdx.graphics.deltaTime * 60f
        if (stepFrame > stepAniFrameCount - 1) stepFrame = stepAniFrameCount - 1f
        lampFlashFrame += Gdx.graphics.deltaTime * 60f
        if (lampFlashFrame > lampAniFrameCount[0] - 1) lampFlashFrame = lampAniFrameCount[0] - 1f
        
//        // Expire old events
//        events.removeIf { it.playbackCompletion == PlaybackCompletion.FINISHED }
    }

    override fun onInput(button: InputButtons, release: Boolean): Boolean {
        if (super.onInput(button, release)) return true
        when (button) {
            InputButtons.A -> {
                if (!release) {
                    manUpRight = !manUpRight
                    stepFrame = 0f
                    sfxStep.play()
                    return true
                }
            }
            else -> {
            }
        }
        return false
    }

    override fun dispose() {
        super.dispose()
        sheet.dispose()
        sfxMetronomeL.dispose()
        sfxMetronomeR.dispose()
        sfxBip.dispose()
        sfxEndDing.dispose()
        sfxStep.dispose()
        music0.dispose()
    }
    
    inner class BipEvent(beat: Float) : RGEvent(this, beat) {
        override fun onStart() {
            sfxBip.play()
            lampFlashFrame = 0f
        }
    }
    
    inner class MetronomeEvent(beat: Float, val right: Boolean) : RGEvent(this, beat) {
        init {
            length = 1f
        }

        private fun updateNeedle() {
            needleMovement = (if (right) -1 else 1) * cos(Math.PI * ((this@UpbeatGame.beat - this.beat) / (length))).toFloat()
        }
        
        override fun onStart() {
            (if (right) sfxMetronomeR else sfxMetronomeL).play()
            updateNeedle()
        }

        override fun whilePlaying() {
            updateNeedle()
        }
        override fun onEnd() {
            updateNeedle()
        }
    }
    
    inner class EndSegmentEvent(beat: Float) : RGEvent(this, beat) {
        init {
            length = 1.5f
        }
        
        override fun onStart() {
            sfxEndDing.play()
        }

        override fun onEnd() {
            events.clear()
        }
    }
}