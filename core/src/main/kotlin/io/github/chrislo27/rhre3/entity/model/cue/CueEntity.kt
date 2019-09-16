package io.github.chrislo27.rhre3.entity.model.cue

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.*
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.BeadsSound
import io.github.chrislo27.rhre3.soundsystem.Derivative
import io.github.chrislo27.rhre3.soundsystem.LoopParams
import io.github.chrislo27.rhre3.soundsystem.SoundStretch
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

    /**
     * Simply returns the current adjusted pitch, not accounting for [Cue.useTimeStretching]
     */
    private fun Cue.getBaseBpmRate(atBeat: Float): Float {
        return getAdjustedRateForBaseBpm(remix.tempos.tempoAt(atBeat))
    }

    private fun getPitchMultiplierFromRemixSpeed(): Float {
        return if (this.canBeRepitched || this.semitone != 0 || cue.usesBaseBpm) remix.speedMultiplier else 1f
    }

    override var volumePercent: Int = IVolumetric.DEFAULT_VOLUME
        set(value) {
            field = value.coerceIn(volumeRange)
        }
    override var volumeCoefficient: Float = 1f
    override val isMuted: Boolean
        get() = IVolumetric.isRemixMutedExternally(remix)
    override val isVolumetric: Boolean = true

    private val usesAudioDerivatives: Boolean
        get() = SoundStretch.isSupported && cue.usesBaseBpm && cue.useTimeStretching && !remix.main.disableTimeStretching

    private var lastCachedDerivative: Derivative? = null
    private val beadsSound: BeadsSound
        get() = if (usesAudioDerivatives && !cue.baseBpmOnlyWhenNotTimeStretching) {
            val deriv = createDerivative()
            lastCachedDerivative = deriv
            cue.sound.derivativeOf(deriv, quick = !remix.main.useHighQualityTimeStretching && !remix.isExporting).beadsSound
        } else {
            cue.sound.audio.beadsSound
        }

    private fun createDerivative(): Derivative {
        return Derivative((cue.getBaseBpmRate(this.bounds.x) - 1f) * 100, semitone.toFloat(), 0f)
    }

    fun play(position: Float = 0f, introSoundPos: Float = 0f) {
        // Combination of the semitone pitch + the remix speed multiplier
        val pitch = getSemitonePitch() * getPitchMultiplierFromRemixSpeed()
        val rate = if (usesAudioDerivatives) 1f else cue.getBaseBpmRate(remix.beat)
        val apparentRate = (pitch * rate)
        val loopParams = if (cue.loops) LoopParams(SamplePlayer.LoopType.LOOP_FORWARDS, cue.loopStart.toDouble(), cue.loopEnd.toDouble()) else LoopParams.NO_LOOP_FORWARDS
        soundId = beadsSound.playWithLoop(pitch = pitch, rate = rate, volume = volume,
                                          position = (position.toDouble()) * apparentRate,
                                          loopParams = loopParams)

        val introSoundCue = cue.introSoundCue
        if (introSoundCue != null) {
            introSoundId = introSoundCue.sound.audio.beadsSound.play(loop = false, pitch = pitch,
                                                                     rate = introSoundCue.getBaseBpmRate(remix.beat), volume = volume,
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
                    beadsSound.setRate(soundId, if (usesAudioDerivatives) {
                        if (cue.baseBpmOnlyWhenNotTimeStretching) 1f
                        else (remix.tempos.tempoAt(remix.beat) / remix.tempos.tempoAt(this.bounds.x))
                    } else cue.getBaseBpmRate(remix.beat))
                }
                isFillbotsFill -> {
                    val sound = beadsSound
                    val pitch = getFillbotsPitch(remix.beat - bounds.x, bounds.width)

                    sound.setPitch(soundId, pitch)
                }
            }
        }
        val endingSoundCue = cue.endingSoundCue
        if (endingSoundCue != null && endingSoundId == -1L) {
            if (remix.seconds >= remix.tempos.beatsToSeconds(bounds.maxX) - endingSoundCue.earliness) {
                endingSoundId = endingSoundCue.sound.audio.beadsSound.play(loop = false, volume = volume,
                                                                           rate = endingSoundCue.getBaseBpmRate(remix.beat),
                                                                           pitch = getSemitonePitch(), position = 0.0).coerceAtLeast(0L)
            }
        }
    }

    override fun onEnd() {
        if (cue.loops || (cue.usesBaseBpm && (usesAudioDerivatives && !cue.baseBpmOnlyWhenNotTimeStretching)) || isFillbotsFill || stopAtEnd) {
            beadsSound.stop(soundId)
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
        val lastCached = lastCachedDerivative
        if (usesAudioDerivatives) {
            val currentDeriv = createDerivative()
            if (lastCached != currentDeriv) {
                if (lastCached != null) {
                    cue.sound.unloadDerivative(lastCached)
                }
                beadsSound
            }
        } else {
            if (lastCached != null) {
                cue.sound.unloadDerivative(lastCached)
            }
        }
    }

    override fun unloadSounds() {
        val lastCached = lastCachedDerivative
        if (lastCached != null) {
            cue.sound.unloadDerivative(lastCached)
        }
        datamodel.unloadSounds()
    }
}