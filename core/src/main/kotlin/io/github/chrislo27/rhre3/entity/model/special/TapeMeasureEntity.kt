package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.TapeMeasure
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.modding.ModdingUtils


class TapeMeasureEntity(remix: Remix, datamodel: TapeMeasure)
    : ModelEntity<TapeMeasure>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = true
    override val renderText: String
        get() = "${Editor.THREE_DECIMAL_PLACE_FORMATTER.format(bounds.width)} ♩${if (ModdingUtils.moddingToolsEnabled) "\n" + ModdingUtils.currentGame.beatsToTickflowString(bounds.width) else ""}"

    init {
        bounds.width = 1f
        bounds.height = 1f
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.keepTheBeat
    }

    override fun copy(remix: Remix): TapeMeasureEntity {
        return TapeMeasureEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@TapeMeasureEntity.bounds)
            }
        }
    }

}
