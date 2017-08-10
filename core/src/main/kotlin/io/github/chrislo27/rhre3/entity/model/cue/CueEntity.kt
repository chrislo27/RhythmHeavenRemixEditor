package io.github.chrislo27.rhre3.entity.model.cue

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.track.Remix


class CueEntity(remix: Remix, datamodel: Cue) : ModelEntity<Cue>(remix, datamodel), IRepitchable {

    companion object {
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
            in any duration (0.6 / duration) and starts at a different place based on a possiblity of
            two linear equations.
             */
            // f(x, z) = (0.6/z)x + (z <= 3 ? -0.2z + 1.2 : -0.025z + 0.675)

            return (0.6f / beat) +
                    if (duration <= 3)
                        (-0.2f * duration + 1.2f)
                    else
                        (-0.025f * duration + 0.675f)
        }
    }

    private val cue: Cue = datamodel
    private val isFillbotsFill: Boolean by lazy {
        cue.id == "fillbots/water" || cue.id == "fillbotsMegamix/water"
    }

    override var semitone: Int = 0
    override val canBeRepitched: Boolean = datamodel.repitchable

    private var soundId: Long = -1L
    private var introSoundId: Long = -1L
    private var endingSoundId: Long = -1L

    init {
        this.bounds.width = cue.duration
        this.bounds.height = 1f
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.cue
    }

    override fun onStart() {
        soundId = cue.sound.sound.play(1f, cue.getPitchForBaseBpm(remix.tempos.tempoAt(remix.beat)), 0f)
        cue.sound.sound.setLooping(soundId, cue.loops)

        introSoundId = cue.introSoundCue?.sound?.sound?.play(1f,
                                                             cue.introSoundCue!!.getPitchForBaseBpm(
                                                                     remix.tempos.tempoAt(remix.beat)),
                                                             0f) ?: -1L
        if (introSoundId != -1L) {
            cue.introSoundCue?.sound?.sound?.setLooping(introSoundId, false)
        }
    }

    override fun whilePlaying() {
        if (soundId != -1L && cue.usesBaseBpm) {
            when {
                cue.usesBaseBpm ->
                    cue.sound.sound.setPitch(soundId, cue.getPitchForBaseBpm(remix.tempos.tempoAt(remix.beat)))
                isFillbotsFill -> {
                    val sound = cue.sound.sound

                    sound.setPitch(soundId, getFillbotsPitch(remix.beat - bounds.x, bounds.width))
                }
            }
        }
    }

    override fun onEnd() {
        if (cue.loops) {
            cue.sound.sound.stop(soundId)
        }

        endingSoundId =
                cue.endingSoundCue?.sound?.sound?.
                        play(1f,
                             cue.endingSoundCue!!.getPitchForBaseBpm(remix.tempos.tempoAt(remix.beat)),
                             0f) ?: -1L
        if (endingSoundId != -1L) {
            cue.endingSoundCue?.sound?.sound?.setLooping(endingSoundId, false)
        }
    }

}