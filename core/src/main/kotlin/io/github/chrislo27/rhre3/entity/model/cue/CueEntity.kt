package io.github.chrislo27.rhre3.entity.model.cue

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.*
import io.github.chrislo27.rhre3.entity.model.special.PitchBenderEntity
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.sfxdb.BaseBpmRules
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.*
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.util.gdxutils.maxX
import net.beadsproject.beads.ugens.SamplePlayer
import kotlin.math.min


class CueEntity(remix: Remix, datamodel: Cue)
    : ModelEntity<Cue>(remix, datamodel), IRepitchable, IStretchable, ISoundDependent, IVolumetric {
    
    companion object {
        val FILLBOTS_ID_REGEX: Regex = "fillbots(.+)?/water".toRegex()
        
        /**
         * https://www.desmos.com/calculator/kqyb422xh7
         */
        fun getFillbotsPitch(beat: Float, duration: Float): Float {
            // small:  1.0f - 1.6f
            // medium: 0.6f - 1.2f
            // big:    0.5f - 1.1f
            // f(x, z) = (0.6/z)x + (z <= 3 ? -0.2z + 1.2 : -0.025z + 0.675)
            
            return (0.6f / duration) * beat +
                    if (duration <= 3)
                        (-0.2f * duration + 1.2f)
                    else
                        (-0.025f * duration + 0.675f)
        }
    }
    
    private inner class PitchBendingSemitone(var toSemitone: Int = 0, var secondsTriggeredAt: Float = 0f) {
        val progressTime: Float = 0.05f
        
        var fromSemitone: Float = 0f
        val progress: Float
            get() = MathUtils.lerp(fromSemitone, toSemitone.toFloat(), ((remix.seconds - secondsTriggeredAt) / progressTime).coerceIn(0f, 1f))
        
        fun update(to: Int) {
            fromSemitone = progress
            toSemitone = to
            secondsTriggeredAt = remix.seconds
        }
    }
    
    private val cue: Cue = datamodel
    
    // Pitch and stretchability
    override var semitone: Int = 0
    override val canBeRepitched: Boolean = datamodel.repitchable
    override val isStretchable: Boolean = datamodel.stretchable
    
    // Volume
    override var volumePercent: Int = IVolumetric.DEFAULT_VOLUME
        set(value) {
            field = value.coerceIn(volumeRange)
        }
    override var volumeCoefficient: Float = 1f
    override val isMuted: Boolean
        get() = IVolumetric.isRemixMutedExternally(remix)
    override val isVolumetric: Boolean = true
    
    // Metadata
    var stopAtEnd: Boolean = false
    private var instrumentByte: Byte = 0
    var instrument: Int
        get() {
            return instrumentByte.toInt() and 0xFF
        }
        set(value) {
            instrumentByte = value.toByte()
        }
    val isFillbotsFill: Boolean = cue.id.matches(FILLBOTS_ID_REGEX)
    val isSkillStar: Boolean = cue.id == SFXDatabase.SKILL_STAR_ID
    
    // Tracking sound IDs
    private var soundId: Long = -1L
    private var introSoundId: Long = -1L
    private var endingSoundId: Long = -1L
    
    // Audio derivatives
    private val rulesAllowBaseBpm: Boolean
        get() = cue.usesBaseBpm && (cue.baseBpmRules == BaseBpmRules.ALWAYS || (cue.baseBpmRules == (if (remix.timeStretchingAllowed) BaseBpmRules.ONLY_TIME_STRETCH else BaseBpmRules.NO_TIME_STRETCH)))
    val usesAudioDerivatives: Boolean
        get() = remix.timeStretchingAllowed && rulesAllowBaseBpm && cue.useTimeStretching
    
    private var mainCueLoaded: Boolean = false
    private var _mainCuePtr: AudioPointer? = null
        get() {
            // Compute current derivative
            val currentDeriv = if (usesAudioDerivatives && !remix.suppressDerivativeAudioLoading) createDerivative() else Derivative.NO_CHANGES
            
            val currentValue = field
            if (currentValue == null) {
                // Create it
                val loaded = SoundCache.load(SampleID(cue.soundFile, currentDeriv))
                field = loaded
                _beadsSound = BeadsSound(loaded.audio)
                mainCueLoaded = true
            } else {
                // Check that the derivative hasn't changed; if so then unload the old sample and load a new one in
                if (currentValue.sampleID.derivative != currentDeriv) {
                    val oldSampleID = currentValue.sampleID
                    
                    val loaded = SoundCache.load(SampleID(cue.soundFile, currentDeriv))
                    field = loaded
                    _beadsSound = BeadsSound(loaded.audio)
                    mainCueLoaded = true
                    
                    // Unload the old sample ID last because its parent is used to create the new deriv
                    SoundCache.unload(oldSampleID)
                }
            }
            return field
        }
    private val mainCuePtr: AudioPointer
        get() = _mainCuePtr!!
    private var _beadsSound: BeadsSound? = null
    private val beadsSound: BeadsSound
        get() {
            if (_beadsSound == null) {
                mainCuePtr
            }
            return _beadsSound!!
        }
    private var introCueLoaded: Boolean = false
    private var introCueBeadsSound: BeadsSound? = null
        get() {
            val introSoundCue = cue.introSoundCue
            return if (introSoundCue != null) {
                if (field == null && !introCueLoaded) {
                    field = BeadsSound(SoundCache.load(SampleID(introSoundCue.soundFile, Derivative.NO_CHANGES)).audio)
                    introCueLoaded = true
                }
                field!!
            } else null
        }
    private var endingCueLoaded: Boolean = false
    private var endingCueBeadsSound: BeadsSound? = null
        get() {
            val endingSoundCue = cue.endingSoundCue
            return if (endingSoundCue != null) {
                if (field == null && !endingCueLoaded) {
                    field = BeadsSound(SoundCache.load(SampleID(endingSoundCue.soundFile, Derivative.NO_CHANGES)).audio)
                    endingCueLoaded = true
                }
                field!!
            } else null
        }
    
    private val currentPitchBendingSemitone = PitchBendingSemitone()
    
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
    
    override fun getTextForSemitone(semitone: Int): String {
        if (cue.writtenPitch != 0) {
            return "[${Semitones.getSemitoneName(semitone + cue.writtenPitch)}]"
        }
        return Semitones.getSemitoneName(semitone)
    }
    
    /**
     * Returns the base BPM adjusted pitch
     */
    private fun Cue.getBaseBpmRate(atBeat: Float): Float {
        return getAdjustedRateForBaseBpm(remix.tempos.tempoAt(atBeat))
    }
    
    /**
     * Returns the remix speed multiplier if affected by that, otherwise 1.0
     */
    private fun getPitchMultiplierFromRemixSpeed(): Float {
        return if (this.canBeRepitched || this.semitone != 0 || cue.usesBaseBpm) remix.speedMultiplier else 1f
    }
    
    private fun createDerivative(): Derivative {
        return Derivative((cue.getBaseBpmRate(this.bounds.x) - 1f) * 100, semitone.toFloat(), 0f)
    }
    
    fun play(position: Float = 0f, introSoundPos: Float = 0f) {
        // Combination of the semitone pitch + the remix speed multiplier
        val pitch = getSemitonePitch() * getPitchMultiplierFromRemixSpeed()
        val rate = if (!rulesAllowBaseBpm) {
            1f
        } else cue.getBaseBpmRate(remix.beat)
        val apparentRate = (pitch * rate)
        val loopParams = if (cue.loops) LoopParams(SamplePlayer.LoopType.LOOP_FORWARDS, cue.loopStart.toDouble(), cue.loopEnd.toDouble()) else LoopParams.NO_LOOP_FORWARDS
        soundId = beadsSound.playWithLoop(pitch = pitch, rate = rate, volume = volume,
                                          position = (position.toDouble()) * apparentRate,
                                          loopParams = loopParams)
        
        introCueBeadsSound?.play(loop = false, pitch = pitch,
                                 rate = cue.introSoundCue!!.getBaseBpmRate(remix.beat), volume = volume,
                                 position = (introSoundPos.toDouble()) * apparentRate)
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
        if (cue.pitchBending)
            currentPitchBendingSemitone.update(0)
        play(startPos, introPos)
        endingSoundId = -1L
    }
    
    override fun whilePlaying() {
        if (soundId != -1L) {
            // Adjust its rate for base BPM, Fillbots, or pitch bending
            when {
                cue.usesBaseBpm -> {
                    beadsSound.setRate(soundId, if (usesAudioDerivatives) {
                        if (cue.baseBpmRules == BaseBpmRules.NO_TIME_STRETCH) 1f
                        else (remix.tempos.tempoAt(remix.beat) / remix.tempos.tempoAt(this.bounds.x))
                    } else (if (rulesAllowBaseBpm) cue.getBaseBpmRate(remix.beat) else 1f))
                }
                isFillbotsFill -> {
                    val sound = beadsSound
                    val pitch = getFillbotsPitch(remix.beat - bounds.x, bounds.width)
                    
                    sound.setPitch(soundId, pitch)
                }
                cue.pitchBending -> {
                    val firstPitchBender: PitchBenderEntity? = remix.entities.find { it is PitchBenderEntity && remix.beat in it.bounds.x..it.bounds.maxX } as PitchBenderEntity?
                    val sound = beadsSound
                    val targetPitchBendTone = firstPitchBender?.semitone ?: 0
                    if (targetPitchBendTone != currentPitchBendingSemitone.toSemitone) {
                        currentPitchBendingSemitone.update(targetPitchBendTone)
                    }
                    val currentPitchBendTone = currentPitchBendingSemitone.progress
                    val pitch = (if (currentPitchBendTone == 0f)
                        getSemitonePitch()
                    else Semitones.getALPitchF(semitone + currentPitchBendTone)) * getPitchMultiplierFromRemixSpeed()
                    sound.setPitch(soundId, pitch)
                    val introSoundCue = cue.introSoundCue
                    if (introSoundCue != null && introSoundId != -1L) {
                        introCueBeadsSound?.setPitch(introSoundId, pitch)
                    }
                }
            }
        }
        // Play its ending sound once the earliness elapses
        val endingSoundCue = cue.endingSoundCue
        if (endingSoundCue != null && endingSoundId == -1L) {
            val endingCueBeadsSound = endingCueBeadsSound
            if (endingCueBeadsSound != null && remix.seconds >= remix.tempos.beatsToSeconds(bounds.maxX) - endingSoundCue.earliness) {
                endingSoundId = endingCueBeadsSound.play(loop = false, volume = volume,
                                                         rate = if (rulesAllowBaseBpm) endingSoundCue.getBaseBpmRate(remix.beat) else 1f,
                                                         pitch = (if (!cue.pitchBending)
                                                             getSemitonePitch()
                                                         else Semitones.getALPitchF(semitone + currentPitchBendingSemitone.progress)) * getPitchMultiplierFromRemixSpeed(), position = 0.0).coerceAtLeast(0L)
            }
        }
    }
    
    override fun onEnd() {
        // If the sound needs to be stopped, stop it (and its intro sound if any)
        if (cue.loops || rulesAllowBaseBpm || isFillbotsFill || stopAtEnd) {
            beadsSound.stop(soundId)
            if (introSoundId != -1L) {
                introCueBeadsSound?.stop(introSoundId)
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
    
    override fun preloadSounds() {
        mainCuePtr
        if (cue.introSoundCue != null) {
            introCueBeadsSound
        }
        if (cue.endingSoundCue != null) {
            endingCueBeadsSound
        }
    }
    
    override fun unloadSounds() {
        if (mainCueLoaded) {
            SoundCache.unload(mainCuePtr.sampleID)
            mainCueLoaded = false
            _mainCuePtr = null
            _beadsSound = null
        }
        
        val introSoundCue = cue.introSoundCue
        if (introSoundCue != null && introCueLoaded) {
            SoundCache.unload(SampleID(introSoundCue.soundFile, Derivative.NO_CHANGES))
            introCueBeadsSound = null
        }
        val endingSoundCue = cue.endingSoundCue
        if (endingSoundCue != null && endingCueLoaded) {
            SoundCache.unload(SampleID(endingSoundCue.soundFile, Derivative.NO_CHANGES))
            endingCueBeadsSound = null
        }
    }
}