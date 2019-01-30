package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.rhre3.editor.CameraPan
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.action.EntitySelectionAction
import io.github.chrislo27.rhre3.editor.action.TrackResizeAction
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import kotlin.math.roundToInt


class TrackChangeButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    private val remix: Remix
        get() = editor.remix

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_track_change_button"))
        })
    }

    override fun getHoverText(): String {
        return Localization["editor.trackChange"] + " [LIGHT_GRAY](${Editor.MIN_TRACK_COUNT}≦[]${remix.trackCount}[LIGHT_GRAY]≦${Editor.MAX_TRACK_COUNT})[]\n" +
                Localization[if (remix.canIncreaseTrackCount()) "editor.trackChange.increase" else "editor.trackChange.max"] + "\n" +
                Localization[if (!remix.canDecreaseTrackCount()) "editor.trackChange.min" else if (remix.entitiesTouchTrackTop) "editor.trackChange.impedance" else "editor.trackChange.decrease"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (remix.playState == PlayState.STOPPED && remix.canIncreaseTrackCount()) {
            remix.mutate(TrackResizeAction(editor, remix.trackCount, remix.trackCount + 1))
            remix.recomputeCachedData()
        }
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        if (remix.playState == PlayState.STOPPED && remix.canDecreaseTrackCount()) {
            if (!remix.wouldEntitiesFitNewTrackCount(remix.trackCount - 1)) {
                // Jump to first blocking entity
                val entities = remix.entities.filterNot { it is EndEntity }
                        .filter { (it.bounds.y + it.bounds.height).roundToInt() >= remix.trackCount }.takeUnless(List<Entity>::isEmpty) ?: return

                if (!(editor.selection.containsAll(entities) && editor.selection.size == entities.size)) {
                    remix.mutate(EntitySelectionAction(editor, editor.selection.toList(), entities))
                }
                editor.cameraPan = CameraPan(editor.camera.position.x, entities.first().bounds.x, 0.5f, Interpolation.exp10Out)
            } else {
                remix.mutate(TrackResizeAction(editor, remix.trackCount, remix.trackCount - 1))
                remix.recomputeCachedData()
            }
        }
    }
}