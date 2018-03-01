package io.github.chrislo27.rhre3.editor.action

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.IVolumetric
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.track.Remix


class EntityRevolumeAction(val editor: Editor, val entity: Entity, val newVolume: Int,
                           val oldVolume: Int = (entity as? IVolumetric)?.volumePercent ?: IVolumetric.DEFAULT_VOLUME)
    : ReversibleAction<Remix> {

    override fun redo(context: Remix) {
        (entity as? IVolumetric)?.volumePercent = newVolume
    }

    override fun undo(context: Remix) {
        (entity as? IVolumetric)?.volumePercent = oldVolume
    }
}