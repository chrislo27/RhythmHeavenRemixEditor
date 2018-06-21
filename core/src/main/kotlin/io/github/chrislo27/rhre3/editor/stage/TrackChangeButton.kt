package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.action.EntitySelectionAction
import io.github.chrislo27.rhre3.editor.action.TrackResizeAction
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette
import kotlin.math.roundToInt


class TrackChangeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val remix: Remix
        get() = editor.remix

    override fun getHoverText(): String {
        // TODO cache?
        return Localization["editor.trackChange"] + "\n" +
                Localization[if (remix.canIncreaseTrackCount()) "editor.trackChange.increase" else "editor.trackChange.max"] + "\n" +
                Localization[if (!remix.canDecreaseTrackCount()) "editor.trackChange.min" else if (!remix.wouldEntitiesFitNewTrackCount(remix.trackCount - 1)) "editor.trackChange.impedance" else "editor.trackChange.decrease"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (remix.canIncreaseTrackCount()) {
            remix.mutate(TrackResizeAction(editor, remix.trackCount, remix.trackCount + 1))
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        if (remix.canDecreaseTrackCount()) {
            if (!remix.wouldEntitiesFitNewTrackCount(remix.trackCount - 1)) {
                // Jump to first blocking entity
                val entities = remix.entities.filterNot { it is EndEntity }
                        .filter { (it.bounds.y + it.bounds.height).roundToInt() >= remix.trackCount }.takeUnless(List<Entity>::isEmpty) ?: return

                if (!(editor.selection.containsAll(entities) && editor.selection.size == entities.size)) {
                    remix.mutate(EntitySelectionAction(editor, editor.selection.toList(), entities))
                }
                editor.camera.position.x = entities.first().bounds.x
            } else {
                remix.mutate(TrackResizeAction(editor, remix.trackCount, remix.trackCount - 1))
            }
        }
    }
}