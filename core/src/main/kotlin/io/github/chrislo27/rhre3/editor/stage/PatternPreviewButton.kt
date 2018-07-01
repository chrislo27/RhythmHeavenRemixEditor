package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.entity.model.ILoadsSounds
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.PlayState.PAUSED
import io.github.chrislo27.rhre3.track.PlayState.PLAYING
import io.github.chrislo27.rhre3.track.PlayState.STOPPED
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.rhre3.util.Swing
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.ui.*


class PatternPreviewButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    companion object {
        private val TEXTS: List<String> = listOf(Editor.VOLUME_CHAR, "■", "♬")

        private val ownEditor: Editor = Editor(RHRE3Application.instance, RHRE3Application.instance.defaultCamera)
        private val ownRemix: Remix
            get() = ownEditor.remix

        init {
            ownEditor.remix = ownEditor.createRemix(false)
            ownRemix.tempos.add(TempoChange(ownRemix.tempos, 0f, ownRemix.tempos.defaultTempo, Swing.STRAIGHT))
        }
    }

    val label = TextLabel(palette, this, this.stage).apply {
        this.isLocalizationKey = false
        this.textAlign = Align.center
        this.textWrapping = false
        this.text = TEXTS[0]
    }
    private val main: RHRE3Application
        get() = editor.main

    private var datamodel: Datamodel? = null
    private var playState: PlayState = STOPPED

    override fun getHoverText(): String {
        return Localization[if (playState == STOPPED) "editor.previewPattern" else "editor.previewPattern.stop"]
    }

    init {
        addLabel(label)
        background = false
    }

    fun update(currentDatamodel: Datamodel?) {
        datamodel = currentDatamodel
        visible = visible && currentDatamodel != null
        stop()
    }

    fun stop() {
        if (ownRemix.playState != STOPPED)
            ownRemix.playState = STOPPED
        playState = STOPPED
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        if (playState == STOPPED) {
            val datamodel = datamodel
            if (datamodel != null) {
                ownRemix.entities.clear()
                val entity = datamodel.createEntity(ownRemix, null)
                if (entity is ILoadsSounds) {
                    entity.loadSounds()
                }

                entity.updateBounds {
                    entity.bounds.x = 0f
                }

                ownRemix.entities += entity

                ownRemix.tempos.map.values.toList().forEach { ownRemix.tempos.remove(it) }
                fun ModelEntity<*>.checkSelfAndChildrenForBaseBpm(): Float {
                    return this.datamodel.possibleBaseBpm?.start ?: 0f
                }

                val baseBpm: Float = entity.checkSelfAndChildrenForBaseBpm()
                val targetTempo = if (baseBpm <= 0f) 120f else baseBpm
                ownRemix.tempos.add(TempoChange(ownRemix.tempos, 0f, targetTempo, Swing.STRAIGHT))

                ownRemix.recomputeCachedData()

                ownRemix.playbackStart = 0f
                ownRemix.playState = STOPPED
                ownRemix.playState = PLAYING
                playState = PAUSED // Used as buffer
            }
        } else {
            ownRemix.playState = STOPPED
            playState = STOPPED
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        val filter = editor.pickerSelection.filter
        if ((filter.areDatamodelsEmpty && datamodel != null) || (!filter.areDatamodelsEmpty && filter.currentDatamodel != datamodel)) {
            update(if (filter.areDatamodelsEmpty) null else filter.currentDatamodel)
        }

        if (ownRemix.playState == PLAYING && playState == PLAYING && ownRemix.beat > ownRemix.lastPoint) {
            playState = STOPPED
        }

        label.textColor = if (editor.currentTool == Tool.SELECTION) Editor.SELECTED_TINT else null
        label.text = TEXTS[when (playState) {
            STOPPED -> 0
            PAUSED -> 1
            PLAYING -> 1
        }]

        super.render(screen, batch, shapeRenderer)

        if (ownRemix.playState == PLAYING && playState != PAUSED) {
            ownRemix.timeUpdate(Gdx.graphics.deltaTime)
        }
        // Used as one frame buffer
        if (playState == PAUSED) {
            playState = PLAYING
        }
    }
}
