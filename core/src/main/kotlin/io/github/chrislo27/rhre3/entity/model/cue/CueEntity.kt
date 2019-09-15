package io.github.chrislo27.rhre3.entity.model.cue

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.*
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.LoopParams
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import net.beadsproject.beads.ugens.SamplePlayer
import kotlin.math.min


class CueEntity(remix: Remix, datamodel: Cue)
    : ModelEntity<Cue>(remix, datamodel), IRepitchable, IStretchable, ILoadsSounds, IVolumetric {

    companion object {
        val FILLBOTS_ID_REGEX: Regex = "fillbots(.+)?/water".toRegex()

        /**
         * https://www.desmos.com/calculator/kqyb422xh7
         */
        fun getFillbotsPitch(beat: Float, duration: Float): Float {
            // small:  1.0f - 1.6f
            // medium: 0.6f - 1.2f
            // big:    0.5f - 1.1f

            /*
            The function below was computed by graphing the three cases (1, 3, 7 beats duration)
            and the pitch as y. By plotting three points of which were the b value of each linear equation,
            one could compute two linear equations - this is what the if statement represents.
            Therefore, this equation is made up simply that the pitch increases by 0.6 units
            in any duration (0.6 / duration) and starts at a different place based on a possibility of
            two linear equations.
             */
            // f(x, z) = (0.6/z)x + (z <= 3 ? -0.2z + 1.2 : -0.025z + 0.675)

            return (0.6f / duration) * beat +
                    if (duration <= 3)
                        (-0.2f * duration + 1.2f)
                    else
                        (-0.025f * duration + 0.675f)
        }
    }

    private val cue: Cue = datamodel
    val isFillbotsFill: Boolean = cue.id.matches(FILLBOTS_ID_REGEX)
    val isSkillStar: Boolean = cue.id == SFXDatabase.SKILL_STAR_ID

    override var semitone: Int = 0
    override val canBeRepitched: Boolean = datamodel.repitchable
    override val isStretchable: Boolean = datamodel.stretchable
    var stopAtEnd: Boolean = false
    private var instrumentByte: Byte = 0
    var instrument: Int
        get() {
            return instrumentByte.toInt() and 0xFF
        }
        set(value) {
            instrumentByte = value.toByte()
        }

    private var soundId: Long = -1L
    private var introSoundId: Long = -1L
    private var endingSoundId: Long = -1L

    init {
        this.bounds.width = cue.duration
        this.bounds.height = 1f
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.cue
    }

    private fun getSemitonePitch(): Float {
        return Semitones.getALPitch(semitone)
    }

    private fun Cue.getBaseBpmRate(): Float {
        return getPitchForBaseBpm(remix.tempos.tempoAt(remix.beat), bounds.width) * (if (cue.baseBpm <= 0f) 1f else remix.speedMultiplier)
    }

    private fun getPitchMultiplierFromRemixSpeed(): Float {
        return if (this.canBeRepitched || this.semitone != 0) remix.speedMultiplier else 1f
    }

    override var volumePercent: Int = IVolumetric.DEFAULT_VOLUME
        set(value) {
            field = value.coerceIn(volumeRange)
        }
    override var volumeCoefficient: Float = 1f
    override val isMuted: Boolean
        get() = IVolumetric.isRemixMutedExternally(remix)
    override val isVolumetric: Boolean = true

    fun play(position: Float = 0f, introSoundPos: Float = 0f) {
        val pitch = getSemitonePitch() * getPitchMultiplierFromRemixSpeed()
        val rate = cue.getBaseBpmRate()
        val apparentRate = (pitch * rate)
        val loopParams = if (cue.loops) LoopParams(SamplePlayer.LoopType.LOOP_FORWARDS, cue.loopStart.toDouble(), cue.loopEnd.toDouble()) else LoopParams.NO_LOOP_FORWARDS
        soundId = cue.sound.audio.beadsSound.playWithLoop(pitch = pitch, rate = rate, volume = volume,
                                                          position = (position.toDouble()) * apparentRate,
                                                          loopParams = loopParams)

        val introSoundCue = cue.introSoundCue
        if (introSoundCue != null) {
            introSoundId = introSoundCue.sound.audio.beadsSound.play(loop = false, pitch = pitch,
                                                               rate = introSoundCue.getBaseBpmRate(), volume = volume,
                                                               position = (introSoundPos.toDouble()) * apparentRate)
        }
    }

    override fun getLowerUpdateableBound(): Float {
        return min(bounds.x, remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds(bounds.x) - min(cue.earliness, cue.introSoundCue?.earliness ?: Float.POSITIVE_INFINITY)))
    }

    override fun onStart() {
        if (isSkillStar && remix.doUpdatePlayalong && remix.main.screen is EditorScreen) {
            return // Do not play if in playalong mode
        }
        val startPos = if (remix.playbackStart > remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds(bounds.x) - cue.earliness)) {
            remix.seconds - remix.tempos.beatsToSeconds(this.bounds.x) + cue.earliness
        } else 0f
        val introSoundCue = cue.introSoundCue
        val introPos = if (introSoundCue != null && remix.playbackStart > remix.tempos.secondsToBeats(remix.tempos.beatsToSeconds(bounds.x) - introSoundCue.earliness)) {
            remix.seconds - remix.tempos.beatsToSeconds(this.bounds.x) + introSoundCue.earliness
        } else 0f
        play(startPos, introPos)
        endingSoundId = -1L
    }

    override fun whilePlaying() {
        if (soundId != -1L) {
            when {
                cue.usesBaseBpm -> {
                    cue.sound.audio.beadsSound.setRate(soundId, cue.getBaseBpmRate())
                }
                isFillbotsFill -> {
                    val sound = cue.sound.audio.beadsSound
                    val pitch = getFillbotsPitch(remix.beat - bounds.x, bounds.width)

                    sound.setPitch(soundId, pitch)
                }
            }
        }
        val endingSoundCue = cue.endingSoundCue
        if (endingSoundCue != null && endingSoundId == -1L) {
            if (remix.seconds >= remix.tempos.beatsToSeconds(bounds.maxX) - endingSoundCue.earliness) {
                endingSoundId = endingSoundCue.sound.audio.beadsSound.play(loop = false, volume = volume,
                                                                     rate = endingSoundCue.getBaseBpmRate(),
                                                                     pitch = getSemitonePitch(), position = 0.0).coerceAtLeast(0L)
            }
        }
    }

    override fun onEnd() {
        if (cue.loops || cue.usesBaseBpm || isFillbotsFill || stopAtEnd) {
            cue.sound.audio.beadsSound.stop(soundId)
            if (introSoundId != -1L) {
                cue.introSoundCue?.sound?.audio?.beadsSound?.stop(introSoundId)
            }
        }
    }

    override fun copy(remix: Remix): CueEntity {
        return CueEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this.bounds)
            }
            it.semitone = this.semitone
            it.stopAtEnd = this.stopAtEnd
            it.volumePercent = this.volumePercent
        }
    }

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)
        if (stopAtEnd) {
            objectNode.put("stopAtEnd", stopAtEnd)
        }
        if (instrument != 0) {
            objectNode.put("instrument", instrument)
        }
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        stopAtEnd = objectNode["stopAtEnd"]?.asBoolean(false) ?: false
        instrument = objectNode["instrument"]?.asInt(0) ?: 0
    }

    override fun loadSounds() {
        datamodel.loadSounds()
    }

    override fun unloadSounds() {
        datamodel.unloadSounds()
    }
}