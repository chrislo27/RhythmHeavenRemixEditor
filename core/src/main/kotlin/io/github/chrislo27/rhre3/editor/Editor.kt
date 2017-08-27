package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.action.*
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.areAnyResponseCopyable
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.entity.model.MultipartEntity
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.entity.model.special.EndEntity
import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.oopsies.GroupedAction
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import io.github.chrislo27.rhre3.screen.InfoScreen
import io.github.chrislo27.rhre3.tempo.TempoChange
import io.github.chrislo27.rhre3.theme.DarkTheme
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.track.music.MusicVolumeChange
import io.github.chrislo27.rhre3.track.timesignature.TimeSignature
import io.github.chrislo27.rhre3.tracker.Tracker
import io.github.chrislo27.rhre3.tracker.TrackerExistenceAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch


class Editor(val main: RHRE3Application, stageCamera: OrthographicCamera)
    : Disposable, InputProcessor {

    companion object {
        const val ENTITY_HEIGHT: Float = 48f
        const val ENTITY_WIDTH: Float = ENTITY_HEIGHT * 4

        const val ICON_SIZE: Float = 32f
        const val ICON_PADDING: Float = 6f
        const val ICON_COUNT_X: Int = 13
        const val ICON_COUNT_Y: Int = 4

        const val TRACK_COUNT: Int = 5
        const val TRACK_LINE: Float = 2f
        const val PATTERN_COUNT: Int = 5

        const val MESSAGE_BAR_HEIGHT: Int = 14
        const val BUTTON_SIZE: Float = 32f
        const val BUTTON_PADDING: Float = 4f
        const val BUTTON_BAR_HEIGHT: Float = BUTTON_SIZE + BUTTON_PADDING * 2

        const val SELECTION_BORDER: Float = 4f
        private const val SELECTION_NUMBER_FORMAT_STRING = "%.1f"

        private const val MSG_SEPARATOR = " - "
        private const val ZERO_BEAT_SYMBOL = "♩"
        private const val AUTOSAVE_MESSAGE_TIME_MS = 10000L

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
        val ARROWS: List<String> = listOf("▲", "▼", "△", "▽", "➡")
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
        val CUE_PATTERN_COLOR: Color = Color(0.65f, 0.65f, 0.65f, 1f)
    }

    fun createRemix(): Remix {
        return Remix(camera, this)
    }

    val camera: OrthographicCamera by lazy {
        val c = OrthographicCamera()
        resizeCamera(c)
        c.position.x = 0f
        c.update()
        c
    }

    val pickerSelection: PickerSelection = PickerSelection()
    var remix: Remix = createRemix()
        set(value) {
            field.dispose()
            field = value
            autosaveFile = null
            resetAutosaveTimer()
        }
    var theme: Theme = DarkTheme()
    val stage: EditorStage = EditorStage(
            null, stageCamera, main, this)
    val batch: SpriteBatch
        get() = main.batch
    val subbeatSection = SubbeatSection()
    var snap: Float = 0.25f
    var selection: List<Entity> = listOf()
        set(value) {
            field.forEach { it.isSelected = false }
            field = value
            field.forEach { it.isSelected = true }
        }
    var currentTool: Tool = Tool.NORMAL
    private val mouseVector: Vector2 = Vector2()
        get() {
            field.set(remix.camera.getInputX(), remix.camera.getInputY())
            return field
        }
    private var wasStretchCursor = false
    var timeUntilAutosave: Float = -1f
    var autosaveFrequency: Int = 0
        private set
    private var autosaveFile: FileHandle? = null
    private var autosaveState: Pair<Boolean, Long>? = null

    fun resetAutosaveTimer() {
        autosaveFrequency = main.preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE,
                                                        InfoScreen.DEFAULT_AUTOSAVE_TIME)
                .coerceIn(InfoScreen.timers.first(), InfoScreen.timers.last())

        timeUntilAutosave = 60f * autosaveFrequency
    }

    fun prepAutosaveFile(baseFile: FileHandle) {
        autosaveFile = baseFile.sibling(baseFile.nameWithoutExtension() + ".autosave.${RHRE3.REMIX_FILE_EXTENSION}")
    }

    sealed class ClickOccupation {

        interface TrackerBased {
            var finished: Boolean
            var final: Float
        }

        object None : ClickOccupation()

        class Playback(val editor: Editor)
            : ClickOccupation(), ReversibleAction<Remix>, TrackerBased {
            val old = editor.remix.playbackStart
            override var finished: Boolean = false
            override var final: Float = Float.NaN
                set(value) {
                    if (!java.lang.Float.isNaN(field)) {
                        error("Attempt to set value to $value when already set to $field")
                    }
                    field = value
                }

            override fun redo(context: Remix) {
                if (final == Float.NaN)
                    error("Final value was NaN which is impossible")
                editor.remix.playbackStart = final
            }

            override fun undo(context: Remix) {
                editor.remix.playbackStart = old
            }
        }

        class Music(val editor: Editor, val middleClick: Boolean)
            : ClickOccupation(), ReversibleAction<Remix>, TrackerBased {
            val old = editor.remix.musicStartSec
            override var finished: Boolean = false
            override var final: Float = Float.NaN
                set(value) {
                    if (!java.lang.Float.isNaN(field)) {
                        error("Attempt to set value to $value when already set to $field")
                    }
                    field = value
                }

            override fun redo(context: Remix) {
                if (final != final)
                    error("Final value was NaN which is impossible")
                editor.remix.musicStartSec = final
            }

            override fun undo(context: Remix) {
                editor.remix.musicStartSec = old
            }
        }

        class CreatingSelection(val editor: Editor,
                                val startPoint: Vector2,
                                val isAdd: Boolean) : ClickOccupation() {
            val oldSelection = editor.selection.toList()
            val rectangle = Rectangle()

            fun updateRectangle() {
                val startX = startPoint.x
                val startY = startPoint.y
                val width = editor.remix.camera.getInputX() - startX
                val height = editor.remix.camera.getInputY() - startY

                if (width < 0) {
                    val abs = Math.abs(width)
                    rectangle.x = startX - abs
                    rectangle.width = abs
                } else {
                    rectangle.x = startX
                    rectangle.width = width
                }

                if (height < 0) {
                    val abs = Math.abs(height)
                    rectangle.y = startY - abs
                    rectangle.height = abs
                } else {
                    rectangle.y = startY
                    rectangle.height = height
                }
            }

        }

        class SelectionDrag(val editor: Editor,
                            val mouseOffset: Vector2,
                            val isNewOrCopy: Boolean,
                            val previousSelection: List<Entity>,
                            val stretchType: StretchRegion)
            : ClickOccupation() {

            val oldBounds: List<Rectangle> = copyBounds(editor.selection)

            val isStretching: Boolean by lazy { !isNewOrCopy && stretchType != StretchRegion.NONE }

            private val selection: List<Entity>
                get() = editor.selection

            companion object {
                fun copyBounds(selection: List<Entity>): List<Rectangle> =
                        selection.map { Rectangle(it.bounds) }
            }

            val left: Float
                get() = selection.minBy { it.bounds.x }?.bounds?.x ?: error("Nothing in selection")

            val right: Float
                get() {
                    val right = selection.maxBy { it.bounds.x + it.bounds.width } ?: error("Nothing in selection")
                    return right.bounds.x + right.bounds.width
                }

            val top: Float
                get() {
                    val highest = selection.maxBy { it.bounds.y } ?: error("Nothing in selection")
                    return highest.bounds.y + highest.bounds.height
                }

            val bottom: Float
                get() = selection.minBy { it.bounds.y }?.bounds?.y ?: error("Nothing in selection")

            val width: Float by lazy {
                right - left
            }

            val height: Int by lazy {
                Math.round(top - bottom)
            }

            fun setFirstPosition(x: Float, y: Float) {
                // reducing object creation due to rapid calling
                val first = selection.first()
                val oldFirstPosX = first.bounds.x
                val oldFirstPosY = first.bounds.y
                first.updateBounds {
                    bounds.setPosition(x, y)
                }

                selection.forEachIndexed { index, entity ->
                    if (index == 0)
                        return@forEachIndexed
                    entity.updateBounds {
                        bounds.x = (bounds.x - oldFirstPosX) + x
                        bounds.y = (bounds.y - oldFirstPosY) + y
                    }
                }
            }

            fun setPositionRelativeToMouse(snap: Float = editor.snap, intY: Boolean = true) {
                val y = editor.remix.camera.getInputY() - mouseOffset.y
                setFirstPosition(MathHelper.snapToNearest(editor.remix.camera.getInputX() - mouseOffset.x, snap),
                                 if (intY) Math.round(y).toFloat() else y)
            }

            fun isPlacementValid(): Boolean {
                if (isInDeleteZone())
                    return false
                if (top > TRACK_COUNT)
                    return false

                // EXCEPTIONS for special entities
                if (selection.any { it is EndEntity } && editor.remix.entities.filter { it is EndEntity }.size > 1)
                    return false

                return editor.remix.entities.filter { it !in selection }.all {
                    selection.all { sel ->
                        !sel.bounds.overlaps(it.bounds)
                    }
                }
            }

            fun isInDeleteZone(): Boolean = editor.camera.getInputY() < 0f && bottom < -0.5f

        }
    }

    var clickOccupation: ClickOccupation = ClickOccupation.None
        private set(value) {
            field = value
            updateMessageLabel()
        }

    fun toScaleX(float: Float): Float =
            (float / RHRE3.WIDTH) * camera.viewportWidth

    fun toScaleY(float: Float): Float =
            (float / RHRE3.HEIGHT) * camera.viewportHeight

    fun BitmapFont.scaleFont() {
        this.setUseIntegerPositions(false)
        this.data.setScale(camera.viewportWidth / main.defaultCamera.viewportWidth,
                           camera.viewportHeight / main.defaultCamera.viewportHeight)
    }

    fun BitmapFont.unscaleFont() {
        this.setUseIntegerPositions(true)
        this.data.setScale(1f)
    }

    private fun setSubbeatSectionToMouse() {
        subbeatSection.enabled = true
        subbeatSection.start = Math.floor(remix.camera.getInputX().toDouble()).toFloat()
        subbeatSection.end = subbeatSection.start
    }

    fun getBeatRange(): IntRange =
            Math.round((camera.position.x - camera.viewportWidth / 2 * camera.zoom) / toScaleX(
                    ENTITY_WIDTH)) - 4..(Math.round(
                    (camera.position.x + camera.viewportWidth / 2 * camera.zoom) / toScaleX(ENTITY_WIDTH)) + 4)

    fun getStretchRegionForStretchable(beat: Float, entity: Entity): StretchRegion {
        if (entity !is IStretchable)
            return StretchRegion.NONE

        if (!entity.isStretchable)
            return StretchRegion.NONE

        if (beat in entity.bounds.x..entity.bounds.x + IStretchable.STRETCH_AREA) {
            return StretchRegion.LEFT
        }

        val right = entity.bounds.x + entity.bounds.width

        if (beat in right - IStretchable.STRETCH_AREA..right) {
            return StretchRegion.RIGHT
        }

        return StretchRegion.NONE
    }

    fun canStretchEntity(mouseVector: Vector2, it: Entity): Boolean {
        return it is IStretchable && it.isSelected &&
                mouseVector.y in it.bounds.y..it.bounds.y + it.bounds.height &&
                getStretchRegionForStretchable(mouseVector.x, it) != StretchRegion.NONE
    }

    /**
     * Pre-stage render.
     */
    fun render() {
        val bgColour = theme.background
        Gdx.gl.glClearColor(bgColour.r, bgColour.g, bgColour.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.position.y = 1f
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()

        val beatRange = getBeatRange()
        val font = main.defaultFont
        val trackYOffset = toScaleY(-TRACK_LINE / 2f)

        font.scaleFont()

        // horizontal track lines
        run trackLines@ {
            batch.color = theme.trackLine
            val startX = beatRange.start.toFloat()
            val width = beatRange.endInclusive.toFloat() - startX
            for (i in 0..TRACK_COUNT) {
                batch.fillRect(startX, trackYOffset + i.toFloat(), width,
                               toScaleY(TRACK_LINE))
            }
            batch.setColor(1f, 1f, 1f, 1f)
        }

        remix.entities.forEach {
            if (it.bounds.x + it.bounds.width >= beatRange.start && it.bounds.x <= beatRange.endInclusive) {
                it.render(batch)
            }
        }

        // beat lines
        run beatLines@ {
            for (i in beatRange) {
                batch.color = theme.trackLine
                if (remix.timeSignatures.getMeasurePart(i.toFloat()) > 0) {
                    batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.25f)
                }

                val xOffset = toScaleX(TRACK_LINE) / -2
                batch.fillRect(i.toFloat() + xOffset, trackYOffset, toScaleX(TRACK_LINE),
                               TRACK_COUNT + toScaleY(TRACK_LINE))

                val flashAnimation = subbeatSection.flashAnimation > 0
                val actuallyInRange = (subbeatSection.enabled && i in subbeatSection.start..subbeatSection.end)
                if (flashAnimation || actuallyInRange) {
                    batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b,
                                   theme.trackLine.a * 0.35f *
                                           if (!actuallyInRange) subbeatSection.flashAnimation else 1f)
                    for (j in 1 until Math.round(1f / snap)) {
                        batch.fillRect(i.toFloat() + snap * j + xOffset, trackYOffset, toScaleX(TRACK_LINE),
                                       TRACK_COUNT + toScaleY(TRACK_LINE))
                    }
                }
            }
            batch.setColor(1f, 1f, 1f, 1f)

            if (subbeatSection.flashAnimation > 0) {
                subbeatSection.flashAnimation -= Gdx.graphics.deltaTime / subbeatSection.flashAnimationSpeed
                if (subbeatSection.flashAnimation < 0)
                    subbeatSection.flashAnimation = 0f
            }
        }

        // trackers (playback start, music, others)
        run trackers@ {
            val font = main.defaultBorderedFont
            val oldFontColor = font.color

            fun getTrackerTime(beat: Float): String {
                val sec = Math.abs(remix.tempos.beatsToSeconds(beat))
                return Localization["tracker.any.time",
                        "%.3f".format(beat),
                        (if (beat < 0) "-" else "") +
                                "%1$02d:%2$06.3f".format((sec / 60).toInt(), sec % 60)]
            }

            fun renderAboveTracker(textKey: String?, controlKey: String?, units: Int, beat: Float, color: Color,
                                   triangleHeight: Float = 0.4f) {
                val triangleWidth = toScaleX(triangleHeight * ENTITY_HEIGHT)
                val x = beat - toScaleX(TRACK_LINE * 1.5f) / 2
                val y = trackYOffset
                val height = (TRACK_COUNT + 1.25f + 1.2f * units) + toScaleY(TRACK_LINE)
                batch.setColor(color.toFloatBits())
                batch.fillRect(x, y, toScaleX(TRACK_LINE * 1.5f),
                               height - triangleHeight / 2)
                batch.draw(AssetRegistry.get<Texture>("tracker_right_tri"),
                           x, y + height - triangleHeight, triangleWidth, triangleHeight)

                font.scaleFont()
                font.scaleMul(0.75f)
                font.color = batch.color
                if (textKey != null) {
                    font.drawCompressed(batch, Localization[textKey], x - 1.05f, y + height, 1f, Align.right)
                }
                font.drawCompressed(batch, getTrackerTime(beat), x + triangleWidth + 0.025f, y + height, 1f, Align.left)

                if (controlKey != null) {
                    val line = font.lineHeight
                    font.scaleMul(0.75f)
                    font.drawCompressed(batch, Localization[controlKey], x - 1.05f, y + height - line, 1f, Align.right)
                }

                font.scaleFont()
            }

            renderAboveTracker("tracker.music", "tracker.music.controls",
                               1, remix.tempos.secondsToBeats(remix.musicStartSec), theme.trackers.musicStart)
            renderAboveTracker("tracker.playback", "tracker.playback.controls",
                               0, remix.playbackStart, theme.trackers.playback)

            if (stage.tapalongMarkersEnabled) {
                val tapalong = stage.tapalongStage
                tapalong.seconds.forEach {
                    val beat = remix.tempos.secondsToBeats((it.remixSec ?: return@forEach) + remix.musicStartSec)
                    if (beat in beatRange) {
                        renderAboveTracker(null, null,
                                           1, beat,
                                           theme.trackLine)
                    }
                }
            }

            if (remix.playState != PlayState.STOPPED) {
                renderAboveTracker(null, null, 0, remix.beat,
                                   theme.trackers.playback, 0f)
            }

            font.color = oldFontColor
            font.unscaleFont()
        }

        // beat numbers
        run beatNumbers@ {
            for (i in beatRange) {
                font.color = theme.trackLine
                val width = ENTITY_WIDTH * 0.4f
                val x = i - width / 2f
                val y = TRACK_COUNT + toScaleY(TRACK_LINE + TRACK_LINE) + font.capHeight
                val text = if (i == 0) ZERO_BEAT_SYMBOL else "${Math.abs(i)}"
                font.drawCompressed(batch, text,
                                    x, y, width, Align.center)
                if (i < 0) {
                    val textWidth = font.getTextWidth(text, width, false)
                    font.drawCompressed(batch, "-", x - textWidth / 2f, y, ENTITY_WIDTH * 0.2f, Align.right)
                }

                val measureNum = remix.timeSignatures.getMeasure(i.toFloat())
                if (measureNum >= 1 && remix.timeSignatures.getMeasurePart(i.toFloat()) == 0) {
                    font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
                    font.drawCompressed(batch, "$measureNum",
                                        x, y + font.lineHeight, width, Align.center)
                }
            }
            font.setColor(1f, 1f, 1f, 1f)
        }

        // bottom trackers
        run trackers@ {
            val font = main.defaultBorderedFont
            font.scaleFont()

            val triHeight = 0.5f
            val triWidth = toScaleX(triHeight * ENTITY_HEIGHT)
            val triangle = AssetRegistry.get<Texture>("tracker_tri")
            val tool = currentTool
            val selectedTracker: Tracker? = if (!tool.isTrackerRelated) null else when (tool) {
                Tool.BPM -> getTrackerOnMouse<TempoChange>()
                Tool.MUSIC_VOLUME -> getTrackerOnMouse<MusicVolumeChange>()
                Tool.TIME_SIGNATURE -> getTrackerOnMouse<TimeSignature>()
                else -> error("Tool $tool not supported")
            }

            remix.trackers.forEachIndexed { cindex, container ->
                val level = remix.trackers.size - cindex - 1
                val y = 0f - (level * font.lineHeight) - toScaleY(TRACK_LINE * 2)
                container.getBackingMap().values.forEachIndexed { index, tracker: Tracker ->
                    if (tracker.beat in beatRange) {
                        val oldBatch = batch.packedColor
                        val trackerColor = if (tracker == selectedTracker) Color.WHITE else tracker.getColor(theme)
                        val lineWidth = 1f

                        batch.color = trackerColor
                        batch.fillRect(tracker.beat - toScaleX(lineWidth / 2), y, toScaleX(lineWidth),
                                       -y - toScaleY(TRACK_LINE))
                        batch.draw(triangle, tracker.beat - triWidth / 2, y - triHeight, triWidth, triHeight)
                        batch.setColor(oldBatch)

                        val oldFontColor = font.color
                        font.color = trackerColor
                        font.draw(batch, tracker.getRenderText(),
                                  tracker.beat + triWidth / 2, y - triHeight + font.capHeight + toScaleY(2f))
                        font.color = oldFontColor
                    }
                }
            }

            font.unscaleFont()
        }

        // render selection box + delete zone
        run selectionRectDrawAndDeleteZone@ {
            val clickOccupation = clickOccupation
            when (clickOccupation) {
                is ClickOccupation.SelectionDrag -> {
                    val oldColor = batch.packedColor
                    val mouseX = remix.camera.getInputX()
                    val mouseY = remix.camera.getInputY()
                    val alpha = (1f - mouseY).coerceIn(0.5f + MathHelper.getTriangleWave(2f) * 0.125f, 1f)
                    val left = remix.camera.position.x - remix.camera.viewportWidth / 2

                    batch.setColor(1f, 0f, 0f, 0.25f * alpha)
                    batch.fillRect(left, 0f,
                                   remix.camera.viewportWidth, -remix.camera.viewportHeight)
                    batch.setColor(oldColor)

                    val deleteFont = main.defaultFontLarge
                    deleteFont.scaleFont()
                    deleteFont.scaleMul(0.5f)
                    deleteFont.setColor(0.75f, 0.5f, 0.5f, alpha)

                    deleteFont.drawCompressed(batch, Localization["editor.delete"], left, -1f + font.capHeight / 2,
                                              remix.camera.viewportWidth, Align.center)

                    deleteFont.setColor(1f, 1f, 1f, 1f)
                    deleteFont.unscaleFont()
                }
                is ClickOccupation.CreatingSelection -> {
                    val oldColor = batch.packedColor
                    val rect = clickOccupation.rectangle

                    batch.color = theme.selection.selectionFill
                    batch.fillRect(rect)

                    batch.color = theme.selection.selectionBorder
                    batch.drawRect(rect, toScaleX(SELECTION_BORDER), toScaleY(SELECTION_BORDER))

                    val widthStr = SELECTION_NUMBER_FORMAT_STRING.format(rect.width)
                    val heightStr = SELECTION_NUMBER_FORMAT_STRING.format(rect.height)

                    val oldFontColor = font.color
                    font.color = theme.selection.selectionBorder

                    val toScaleX = toScaleX(SELECTION_BORDER * 1.5f)
                    val toScaleY = toScaleY(SELECTION_BORDER * 1.5f)

                    if (rect.height - toScaleY * 2 >= font.capHeight) {
                        var defaultX = rect.x + toScaleX
                        var defaultWidth = rect.width - toScaleX * 2
                        val shouldBeLeftAlign = remix.camera.getInputX() < clickOccupation.startPoint.x
                        if (defaultX < remix.camera.position.x - remix.camera.viewportWidth / 2) {
                            defaultX = remix.camera.position.x - remix.camera.viewportWidth / 2
                            defaultWidth = (rect.width + rect.x) - defaultX - toScaleX
                        } else if (defaultX + defaultWidth > remix.camera.position.x + remix.camera.viewportWidth / 2) {
                            defaultWidth = (remix.camera.position.x + remix.camera.viewportWidth / 2) - defaultX
                        }
                        if (rect.width >= font.getTextWidth(widthStr)) {
                            font.drawConstrained(batch, widthStr,
                                                 defaultX,
                                                 rect.y + rect.height - toScaleY,
                                                 defaultWidth,
                                                 font.lineHeight, Align.center)
                        }
                        if (rect.width >= font.getTextWidth(heightStr)) {
                            font.drawConstrained(batch, heightStr,
                                                 defaultX,
                                                 rect.y + rect.height / 2 + font.capHeight / 2,
                                                 defaultWidth,
                                                 font.lineHeight,
                                                 if (shouldBeLeftAlign) Align.left else Align.right)
                        }
                    }
                    font.color = oldFontColor

                    batch.setColor(oldColor)
                }
                else -> {
                }
            }
        }

        font.unscaleFont()
        batch.end()
        batch.projectionMatrix = main.defaultCamera.combined
        batch.begin()

        batch.end()

    }

    fun postStageRender() {

    }

    fun renderUpdate() {
        remix.timeUpdate(Gdx.graphics.deltaTime)

        val shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
        val control = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
                Input.Keys.CONTROL_RIGHT)
        val alt = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)
        val left = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val accelerateCamera = shift || control
        val cameraDelta = toScaleX(ENTITY_WIDTH * 5 * Gdx.graphics.deltaTime * if (accelerateCamera) 5 else 1)
        val mouseVector = mouseVector

        subbeatSection.enabled = false

        if (!stage.isTyping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
                Gdx.input.inputProcessor.scrolled(-1)
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
                Gdx.input.inputProcessor.scrolled(1)
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (shift) {
                    when (remix.playState) {
                        PlayState.STOPPED -> remix.playState = PlayState.PLAYING
                        PlayState.PAUSED -> remix.playState = PlayState.PLAYING
                        PlayState.PLAYING -> remix.playState = PlayState.PAUSED
                    }
                } else {
                    when (remix.playState) {
                        PlayState.STOPPED -> remix.playState = PlayState.PLAYING
                        PlayState.PAUSED -> {
                            remix.playState = PlayState.STOPPED
                            remix.playState = PlayState.PLAYING
                        }
                        PlayState.PLAYING -> remix.playState = PlayState.STOPPED
                    }
                }
            }
        }

        if (remix.playState == PlayState.PLAYING) {
            val halfWidth = remix.camera.viewportWidth / 2
            if (remix.beat !in remix.camera.position.x - halfWidth..remix.camera.position.x + halfWidth) {
                remix.camera.position.x = remix.beat + halfWidth
            }

            if (main.preferences.getBoolean(PreferenceKeys.SETTINGS_CHASE_CAMERA, false)) {
                remix.camera.position.x = remix.beat + remix.camera.viewportWidth * 0.25f
            }
        }

        when (currentTool) {
            Tool.MULTIPART_SPLIT -> {
                updateMessageLabel()
            }
            else -> {
            }
        }

        run stretchCursor@ {
            val shouldStretch = this.selection.size == 1 && remix.entities.any {
                canStretchEntity(mouseVector, it)
            }

            if (wasStretchCursor && !shouldStretch) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
            } else if (!wasStretchCursor && shouldStretch) {
                Gdx.graphics.setCursor(AssetRegistry["cursor_horizontal_resize"])
            }
            wasStretchCursor = shouldStretch
        }

        if (remix.playState != PlayState.STOPPED)
            return

        val autosaveFile = autosaveFile
        if (autosaveFrequency > 0 && autosaveFile != null) {
            timeUntilAutosave -= Gdx.graphics.deltaTime
            if (timeUntilAutosave <= 0) {
                autosaveFrequency = 0
                launch(CommonPool) {
                    try {
                        Remix.saveTo(remix, autosaveFile.file(), true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Gdx.app.postRunnable {
                            autosaveState = false to System.currentTimeMillis()
                        }
                    }
                    Gdx.app.postRunnable {
                        autosaveState = true to System.currentTimeMillis()

                        resetAutosaveTimer()
                        Toolboks.LOGGER.info("Autosaved (frequency every $autosaveFrequency minute(s))")
                    }
                }
            }
        }
        val autosaveState = autosaveState
        if (autosaveState != null) {
            if (System.currentTimeMillis() >= autosaveState.second + AUTOSAVE_MESSAGE_TIME_MS) {
                this.autosaveState = null
            }
            updateMessageLabel()
        }

        if (!stage.isTyping) {
            if (left) {
                camera.position.x -= cameraDelta
            }
            if (right) {
                camera.position.x += cameraDelta
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                camera.position.x = 0f
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                camera.position.x = remix.getLastPoint()
            }
        }

        run clickCheck@ {
            val clickOccupation = clickOccupation
            val tool = currentTool
            if (tool == Tool.NORMAL) {
                val nearestBeat = MathHelper.snapToNearest(camera.getInputX(), snap)
                when (clickOccupation) {
                    is ClickOccupation.Music -> {
                        setSubbeatSectionToMouse()
                        remix.musicStartSec = remix.tempos.beatsToSeconds(nearestBeat)
                    }
                    is ClickOccupation.Playback -> {
                        setSubbeatSectionToMouse()
                        remix.playbackStart = nearestBeat
                    }
                    is ClickOccupation.SelectionDrag -> {
                        if (clickOccupation.isStretching) {
                            val entity = this.selection.first()
                            val oldBound = clickOccupation.oldBounds.first()
                            entity.updateBounds {
                                if (clickOccupation.stretchType == StretchRegion.LEFT) {
                                    val rightSide = oldBound.x + oldBound.width

                                    entity.bounds.x = (nearestBeat).coerceAtMost(rightSide - IStretchable.MIN_STRETCH)
                                    entity.bounds.width = rightSide - entity.bounds.x
                                } else if (clickOccupation.stretchType == StretchRegion.RIGHT) {
                                    entity.bounds.width = (nearestBeat - oldBound.x).coerceAtLeast(
                                            IStretchable.MIN_STRETCH)
                                }
                            }
                        } else {
                            clickOccupation.setPositionRelativeToMouse()
                        }

                        subbeatSection.enabled = true
                        subbeatSection.start = Math.floor(clickOccupation.left.toDouble()).toFloat()
                        subbeatSection.end = clickOccupation.right
                    }
                    is ClickOccupation.CreatingSelection -> {
                        clickOccupation.updateRectangle()
                    }
                    ClickOccupation.None -> {
                        if (selection.isNotEmpty() && !stage.isTyping &&
                                (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) ||
                                        Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))) {
                            remix.entities.removeAll(this.selection)
                            remix.addActionWithoutMutating(GroupedAction(listOf(
                                    EntityRemoveAction(this, this.selection, this.selection.map { Rectangle(it.bounds) }),
                                    EntitySelectionAction(this, this.selection.toList(), listOf())
                                                                               )))
                            this.selection = listOf()
                        }
                    }
                }
            }
        }

        if (currentTool.isTrackerRelated && currentTool != Tool.TIME_SIGNATURE) {
            subbeatSection.enabled = true
            subbeatSection.start = Math.floor(remix.camera.getInputX().toDouble()).toFloat()
            subbeatSection.end = subbeatSection.start + 0.5f
        }

        // undo/redo
        if (control) {
            if (remix.canRedo() &&
                    (Gdx.input.isKeyJustPressed(Input.Keys.Y) ||
                            (shift && Gdx.input.isKeyJustPressed(Input.Keys.Z)))) {
                remix.redo()
            } else if (remix.canUndo() && Gdx.input.isKeyJustPressed(Input.Keys.Z) && !shift) {
                remix.undo()
            }
        }

    }

    fun updateMessageLabel() {
        val label = stage.messageLabel
        val builder = StringBuilder()
        val clickOccupation = clickOccupation
        val autosaveState = autosaveState

        fun StringBuilder.separator(): StringBuilder {
            this.append(MSG_SEPARATOR)
            return this
        }

        if (autosaveState != null) {
            builder.append(Localization["editor.msg.autosave.${if (autosaveState.first) "success" else "failed"}"])
                    .separator()
        }

        if (stage.tapalongStage.visible) {
            builder.append(Localization["editor.tapalong.info"])
        } else {
            when (currentTool) {
                Tool.NORMAL -> {
                    val currentGame: Game? = pickerSelection.currentSelection.getCurrentVariant()?.getCurrentGame()
                    builder.append(currentGame?.name ?: Localization["editor.msg.noGame"])
                    if (selection.isNotEmpty()) {
                        builder.separator().append(
                                Localization["editor.msg.numSelected", this.selection.size.toString()])

                        if (clickOccupation == ClickOccupation.None) {
                            if (selection.all(Entity::supportsCopying)) {
                                builder.separator().append(Localization["editor.msg.copyHint"])
                            }

                            if (selection.areAnyResponseCopyable()) {
                                builder.separator().append(Localization["editor.msg.callResponseHint"])
                            }
                        }

                        if (selection.size == 1) {
                            val first = selection.first()

                            if (first is IStretchable && first.isStretchable) {
                                builder.separator().append(
                                        Localization["editor.msg.stretchable${
                                        if (first is EquidistantEntity)
                                            ".equidistant"
                                        else
                                            (if (first is KeepTheBeatEntity)
                                                ".keepTheBeat"
                                            else
                                                "")}"])
                            }

                            if (first is SubtitleEntity) {
                                builder.separator().append(
                                        Localization["editor.msg.subtitle.${if (stage.subtitleField.visible) "finish" else "edit"}"])
                            }
                        }
                    }

                    if (clickOccupation is ClickOccupation.CreatingSelection) {
                        builder.separator().append(Localization["editor.msg.selectionHint", MSG_SEPARATOR])
                    }
                }
                Tool.MULTIPART_SPLIT -> {
                    builder.append(Localization["editor.msg.multipartSplit"])
                    if (remix.playState == PlayState.STOPPED) {
                        val multipart = getMultipartOnMouse()
                        if (multipart != null) {
                            if (!multipart.canSplitWithoutColliding()) {
                                builder.separator().append(Localization["editor.msg.cannotSplit"])
                            }
                        }
                    }
                }
                Tool.BPM -> {
                    builder.append(Localization["editor.msg.tempoChange"])
                }
                Tool.TIME_SIGNATURE -> {
                    builder.append(Localization["editor.msg.timeSignature"])
                }
                Tool.MUSIC_VOLUME -> {
                    builder.append(Localization["editor.msg.musicVolume"])
                }
            }
        }

        label.text = builder.toString()
    }

    private fun getMultipartOnMouse(): MultipartEntity<*>? {
        val mouseVector = mouseVector
        return remix.entities.firstOrNull { mouseVector in it.bounds } as? MultipartEntity<*>?
    }

    private inline fun <reified T : Tracker> getTrackerOnMouse(): T? {
        remix.trackers.forEach { container ->
            val result = container.getBackingMap().values.firstOrNull {
                it is T && MathUtils.isEqual(it.beat, remix.camera.getInputX(), snap / 2)
            }

            if (result != null) {
                return result as T
            }
        }

        return null
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseVector = mouseVector
        val shift =
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
        val control =
                Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)
        val alt =
                Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT)

        val isMusicTrackerButtonDown = !(shift || alt) &&
                ((!control && button == Input.Buttons.MIDDLE) || (button == Input.Buttons.RIGHT && control))

        val isPlaybackTrackerButtonDown = !isMusicTrackerButtonDown &&
                button == Input.Buttons.RIGHT && !(shift || alt || control)

        val isAnyTrackerButtonDown = isMusicTrackerButtonDown || isPlaybackTrackerButtonDown

        val isDraggingButtonDown = button == Input.Buttons.LEFT && !shift
        val isCopying = isDraggingButtonDown && alt
        val isResponsing = isDraggingButtonDown && alt && control

        if (clickOccupation != ClickOccupation.None || remix.playState != PlayState.STOPPED)
            return false

        if (stage.centreAreaStage.isMouseOver()) {
            val tool = currentTool
            if (tool == Tool.NORMAL) {
                val firstEntityInMouse: Entity? = remix.entities.firstOrNull { mouseVector in it.bounds }
                if (button == Input.Buttons.RIGHT && firstEntityInMouse != null && firstEntityInMouse is SubtitleEntity) {
                    val field = stage.subtitleField
                    field.visible = true
                    field.text = firstEntityInMouse.subtitle
                    field.hasFocus = true
                    updateMessageLabel()
                } else if (isAnyTrackerButtonDown) {
                    clickOccupation = if (isMusicTrackerButtonDown) {
                        ClickOccupation.Music(this, button == Input.Buttons.MIDDLE)
                    } else {
                        ClickOccupation.Playback(this)
                    }
                } else if (isDraggingButtonDown) {
                    if (remix.entities.any { mouseVector in it.bounds && it.isSelected }) {
                        val inBounds = this.selection
                        val newSel = if (isResponsing && inBounds.areAnyResponseCopyable()) {
                            inBounds.mapNotNull {
                                it as ModelEntity<*>
                                val datamodel = it.datamodel
                                datamodel as ResponseModel
                                if (datamodel.responseIDs.isNotEmpty()) {
                                    val id = datamodel.responseIDs.random()
                                    val entity = GameRegistry.data.objectMap[id]?.createEntity(remix) ?:
                                            error("ID $id not found in game registry when making response copy")

                                    entity.updateBounds {
                                        entity.bounds.setPosition(it.bounds.x, it.bounds.y)
                                        if (entity is IStretchable) {
                                            entity.bounds.width = it.bounds.width
                                        }
                                    }

                                    entity
                                } else {
                                    null
                                }
                            }
                        } else if (isCopying && inBounds.all(Entity::supportsCopying) && !isResponsing) {
                            inBounds.map { it.copy() }
                        } else {
                            inBounds
                        }
                        val first = newSel.first()
                        val oldSel = this.selection
                        val mouseOffset = Vector2(remix.camera.getInputX() - first.bounds.x,
                                                  remix.camera.getInputY() - first.bounds.y)
                        val stretchRegion = if (newSel.size == 1 && first is IStretchable && !isCopying)
                            getStretchRegionForStretchable(remix.camera.getInputX(), first) else StretchRegion.NONE
                        val newClick = ClickOccupation.SelectionDrag(this, mouseOffset,
                                                                     isCopying, oldSel, stretchRegion)
                        if (isCopying) {
                            this.selection = newSel
                            remix.entities.addAll(newSel)
                        }

                        this.clickOccupation = newClick
                    } else {
                        val clickOccupation = clickOccupation
                        if (clickOccupation == ClickOccupation.None) {
                            // begin selection rectangle
                            val newClick = ClickOccupation.CreatingSelection(this, Vector2(mouseVector), shift)
                            this.clickOccupation = newClick
                        }
                    }
                }
            } else if (tool == Tool.MULTIPART_SPLIT) {
                if (isDraggingButtonDown) {
                    val firstMultipart: MultipartEntity<*>? = getMultipartOnMouse()
                    if (firstMultipart != null) {
                        if (firstMultipart.canSplitWithoutColliding()) {
                            remix.mutate(firstMultipart.createSplittingAction())
                        }
                    }
                }
            } else if (tool.isTrackerRelated) {
                val tracker: Tracker? = when (tool) {
                    Tool.BPM -> getTrackerOnMouse<TempoChange>()
                    Tool.MUSIC_VOLUME -> getTrackerOnMouse<MusicVolumeChange>()
                    Tool.TIME_SIGNATURE -> getTrackerOnMouse<TimeSignature>()
                    else -> error("Tool $tool not supported")
                }
                val beat = MathHelper.snapToNearest(remix.camera.getInputX(), snap)
                val canPlace = if (tool == Tool.TIME_SIGNATURE) MathUtils.isEqual(Math.round(beat).toFloat(), beat,
                                                                                  snap / 2) else true

                if (button == Input.Buttons.RIGHT && tracker != null) {
                    remix.mutate(
                            when (tool) {
                                Tool.BPM -> {
                                    TrackerExistenceAction(remix, remix.tempos, tracker as TempoChange, true)
                                }
                                Tool.MUSIC_VOLUME -> {
                                    TrackerExistenceAction(remix, remix.musicVolumes, tracker as MusicVolumeChange,
                                                           true)
                                }
                                Tool.TIME_SIGNATURE -> {
                                    TrackerExistenceAction(remix, remix.timeSignatures, tracker as TimeSignature, true)
                                }
                                else -> error("Tracker removal not supported: $tool")
                            })
                } else if (button == Input.Buttons.LEFT && tracker == null && canPlace) {
                    remix.mutate(
                            when (tool) {
                                Tool.BPM -> {
                                    TrackerExistenceAction(remix, remix.tempos,
                                                           TempoChange(beat, remix.tempos.tempoAt(beat)), false)
                                }
                                Tool.MUSIC_VOLUME -> {
                                    TrackerExistenceAction(remix, remix.musicVolumes,
                                                           MusicVolumeChange(beat, remix.musicVolumes.getVolume(beat)),
                                                           false)
                                }
                                Tool.TIME_SIGNATURE -> {
                                    TrackerExistenceAction(remix, remix.timeSignatures,
                                                           TimeSignature(beat.toInt(),
                                                                         remix.timeSignatures.getTimeSignature(
                                                                                 beat)?.upper ?: 4), false)
                                }
                                else -> error("Tracker placement not supported: $tool")
                            })
                }
            }
        } else if (stage.patternAreaStage.isMouseOver() && currentTool == Tool.NORMAL && isDraggingButtonDown) {
            // only for new
            val datamodel = pickerSelection.currentSelection.getCurrentVariant()?.getCurrentPlaceable() ?: return true
            val entity = datamodel.createEntity(remix)

            when (entity) {
                is CueEntity -> {
                    entity.datamodel.loadSounds()
                }
                is MultipartEntity -> {
                    entity.loadInternalSounds()
                }
            }

            val oldSelection = this.selection
            this.selection = listOf(entity)
            val selection = ClickOccupation.SelectionDrag(this, Vector2(0f, 0f),
                                                          true, oldSelection, StretchRegion.NONE)
            selection.setPositionRelativeToMouse()

            remix.entities += entity

            this.clickOccupation = selection
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val clickOccupation = clickOccupation
        if (clickOccupation is ClickOccupation.Music &&
                (button == (if (clickOccupation.middleClick) Input.Buttons.MIDDLE else Input.Buttons.RIGHT))) {
            remix.addActionWithoutMutating(clickOccupation)
            clickOccupation.final = remix.musicStartSec
            this.clickOccupation = ClickOccupation.None
            return true
        } else if (clickOccupation is ClickOccupation.Playback &&
                button == Input.Buttons.RIGHT) {
            remix.addActionWithoutMutating(clickOccupation)
            clickOccupation.final = remix.playbackStart
            this.clickOccupation = ClickOccupation.None
            return true
        } else if (clickOccupation is ClickOccupation.SelectionDrag) {
            val validPlacement = clickOccupation.isPlacementValid()
            val deleting = clickOccupation.isInDeleteZone()

            /*
             Placement = drag from picker or copy
             Movement  = moving existing entities

             Outcomes:

             Correct placement -> results in a place+selection action
             Invalid placement -> remove, restore old selection

             Correct movement  -> results in a move action
             Invalid movement  -> return
             Delete movement   -> remove+selection action
             */

            if (clickOccupation.isNewOrCopy) {
                if (validPlacement) {
                    // place+selection action
                    remix.addActionWithoutMutating(GroupedAction(listOf(
                            EntityPlaceAction(this, this.selection),
                            EntitySelectionAction(this, clickOccupation.previousSelection, this.selection)
                                                                       )))
                } else {
                    // delete silently
                    remix.entities.removeAll(selection)
                    // restore original selection
                    selection = clickOccupation.previousSelection
                }
            } else {
                if (validPlacement) {
                    // move action
                    remix.addActionWithoutMutating(EntityMoveAction(this, this.selection, clickOccupation.oldBounds))
                } else if (deleting) {
                    // remove+selection action
                    remix.entities.removeAll(this.selection)
                    remix.addActionWithoutMutating(GroupedAction(listOf(
                            EntityRemoveAction(this, this.selection, clickOccupation.oldBounds),
                            EntitySelectionAction(this, clickOccupation.previousSelection, listOf())
                                                                       )))
                    this.selection = listOf()
                } else {
                    // revert positions silently
                    clickOccupation.oldBounds.forEachIndexed { index, rect ->
                        selection[index].updateBounds {
                            bounds.set(rect)
                        }
                    }
                }
            }

            this.clickOccupation = ClickOccupation.None
            return true
        } else if (clickOccupation is ClickOccupation.CreatingSelection &&
                (button == Input.Buttons.LEFT || button == Input.Buttons.RIGHT)) {
            /*
            Selections are now actions and can be undone
            Note that a selection change will also have to occur when you drag new things - this is handled
             */

            if (button == Input.Buttons.LEFT) {
                // finish selection as ACTION
                clickOccupation.updateRectangle()
                val selectionRect = clickOccupation.rectangle
                val newCaptured: List<Entity> = remix.entities.filter { it.bounds.overlaps(selectionRect) }
                val newSelection: List<Entity> =
                        if (clickOccupation.isAdd) {
                            this.selection.toList() + newCaptured
                        } else {
                            newCaptured
                        }
                if (!this.selection.containsAll(newSelection) ||
                        (newSelection.size != this.selection.size)) {
                    remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
                }
            }

            this.clickOccupation = ClickOccupation.None
        }

        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        when (keycode) {
            in Input.Keys.NUM_0..Input.Keys.NUM_9 -> {
                if (stage.isTyping || clickOccupation != ClickOccupation.None || stage.tapalongStage.visible)
                    return false
                val number = (if (keycode == Input.Keys.NUM_0) 10 else keycode - Input.Keys.NUM_0) - 1
                if (number in 0 until Tool.VALUES.size) {
                    currentTool = Tool.VALUES[number]
                    stage.updateSelected()
                    return true
                }
            }
        }

        return false
    }

    override fun scrolled(amount: Int): Boolean {
        if (remix.playState != PlayState.STOPPED)
            return false

        val tool = currentTool
        if (tool == Tool.NORMAL && selection.isNotEmpty()) {
            val oldPitches = selection.map { (it as? IRepitchable)?.semitone ?: 0 }

            selection.forEach {
                if (it is IRepitchable && it.canBeRepitched) {
                    val current = it.semitone
                    val new = current + -amount
                    if (new in IRepitchable.RANGE) {
                        it.semitone = new
                    }
                }
            }

            remix.addActionWithoutMutating(EntityRepitchAction(this, selection, oldPitches))
        } else if (tool.isTrackerRelated) {
            val tracker: Tracker = when (tool) {
                Tool.BPM -> getTrackerOnMouse<TempoChange>()
                Tool.MUSIC_VOLUME -> getTrackerOnMouse<MusicVolumeChange>()
                Tool.TIME_SIGNATURE -> getTrackerOnMouse<TimeSignature>()
                else -> error("Tool scroll for $tool not supported")
            } ?: return false

            val shift =
                    Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
            val control =
                    Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)

            tracker.onScroll(remix, -amount, shift, control)
        }

        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun dispose() {
    }

    private fun resizeCamera(camera: OrthographicCamera) {
        camera.viewportWidth = RHRE3.WIDTH / ENTITY_WIDTH
        camera.viewportHeight = RHRE3.HEIGHT / ENTITY_HEIGHT
        camera.update()
    }

    fun resize(width: Int, height: Int) {
        stage.updatePositions()
        resizeCamera(camera)
    }
}