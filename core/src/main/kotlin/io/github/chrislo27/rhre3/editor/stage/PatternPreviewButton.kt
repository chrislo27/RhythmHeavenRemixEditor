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
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.PlayState.PAUSED
import io.github.chrislo27.rhre3.track.PlayState.PLAYING
import io.github.chrislo27.rhre3.track.PlayState.STOPPED
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.toolboks.ui.*


class PatternPreviewButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                           stage: Stage<EditorScreen>) : Button<EditorScreen>(palette, parent, stage) {

    companion object {
        private val TEXTS: List<String> = listOf(Editor.VOLUME_CHAR, "■", "♬")

        private val ownEditor: Editor = Editor(RHRE3Application.instance, RHRE3Application.instance.defaultCamera)
        private val ownRemix: Remix
            get() = ownEditor.remix

        init {
            ownEditor.remix = ownEditor.createRemix(false)
            ownRemix.tempos.add(TempoChange(ownRemix.tempos, 0f, 0f, ownRemix.tempos.defaultTempo))
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

    init {
        addLabel(label)
        background = false
    }

    fun update(currentDatamodel: Datamodel?) {
        datamodel = currentDatamodel
        visible = currentDatamodel != null
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
                fun ModelEntity<*>.checkSelfAndChildrenForBaseBpm(): Float? {
                    if (this.datamodel is Cue && this.datamodel.usesBaseBpm) {
                        return this.datamodel.baseBpm
                    }
                    val children: Float? = if (this is MultipartEntity<*> && this.getInternalEntities().isNotEmpty()) {
                        this.getInternalEntities()
                                .filterIsInstance<ModelEntity<*>>()
                                .mapNotNull(ModelEntity<*>::checkSelfAndChildrenForBaseBpm)
                                .firstOrNull()
                    } else null
                    return children
                }
                val baseBpm: Float? = entity.checkSelfAndChildrenForBaseBpm()
                val targetTempo = baseBpm ?: 120f
                ownRemix.tempos.add(TempoChange(ownRemix.tempos, 0f, 0f, targetTempo))

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
