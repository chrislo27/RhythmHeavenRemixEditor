package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.ShakeScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.Semitones


class ShakeEntity(remix: Remix, datamodel: ShakeScreen)
    : ModelEntity<ShakeScreen>(remix, datamodel), IStretchable, IRepitchable {

    companion object {

        fun getShakeIntensity(semitone: Int): Float {
            return Semitones.getALPitch(semitone)
        }

    }

    override val isStretchable: Boolean = true
    override var semitone: Int = 0
    override val canBeRepitched: Boolean = true
    override val showPitchOnTooltip: Boolean = false

    init {
        bounds.width = 1f
        bounds.height = 1f
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.special
    }

    override fun getTextForSemitone(semitone: Int): String {
        return "${Math.round(getShakeIntensity(semitone) * 100)}%"
    }

    override fun onStart() {
        if (this !in remix.currentShakeEntities) {
            remix.currentShakeEntities += this
        }
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
        remix.currentShakeEntities -= this
    }

    override fun copy(remix: Remix): ShakeEntity {
        return ShakeEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@ShakeEntity.bounds)
            }
            it.semitone = this@ShakeEntity.semitone
        }
    }

}