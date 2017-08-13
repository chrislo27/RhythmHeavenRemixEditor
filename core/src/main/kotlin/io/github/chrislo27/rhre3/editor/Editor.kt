package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.action.EntitySelectionAction
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.oopsies.ReversibleAction
import io.github.chrislo27.rhre3.theme.DarkTheme
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*


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

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
        val ARROWS: List<String> = listOf("▲", "▼", "△", "▽", "➡")
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
        val CUE_PATTERN_COLOR: Color = Color(0.65f, 0.65f, 0.65f, 1f)
    }

    val camera: OrthographicCamera by lazy {
        val c = OrthographicCamera()
        resizeCamera(c)
        c.position.x = 0f
        c.update()
        c
    }

    val pickerSelection: PickerSelection = PickerSelection()
    var remix: Remix = Remix(camera, this)
    val stage: EditorStage = EditorStage(
            null, stageCamera, main, this)
    val batch: SpriteBatch
        get() = main.batch
    var theme: Theme = DarkTheme()
    val subbeatSection = SubbeatSection()
    var snap: Float = 0.25f
    var selection: List<Entity> = listOf()
        set(value) {
            field.forEach { it.isSelected = false }
            field = value
            field.forEach { it.isSelected = true }
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
                if (final == Float.NaN)
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
                            val oldBounds: List<Rectangle>,
                            val isNewOrCopy: Boolean,
                            val oldSelection: List<Entity>)
            : ClickOccupation() {

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

                return editor.remix.entities.filter { it !in selection }.all {
                    selection.all { sel ->
                        !sel.bounds.overlaps(it.bounds)
                    }
                }
            }

            fun isInDeleteZone(): Boolean = top <= 0f

        }
    }

    private var clickOccupation: ClickOccupation = ClickOccupation.None

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
                    ENTITY_WIDTH)) - 1..(Math.round(
                    (camera.position.x + camera.viewportWidth / 2 * camera.zoom) / toScaleX(ENTITY_WIDTH)) + 1)

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
                // TODO time signature based
                if (i % 4 == 0) {
                    batch.color = theme.trackLine
                } else {
                    batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.25f)
                }
                batch.fillRect(i.toFloat(), trackYOffset, toScaleX(TRACK_LINE),
                               TRACK_COUNT + toScaleY(TRACK_LINE))

                if (subbeatSection.enabled && i in subbeatSection.start..subbeatSection.end) {
                    batch.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.35f)
                    for (j in 1 until Math.round(1f / snap)) {
                        batch.fillRect(i.toFloat() + snap * j, trackYOffset, toScaleX(TRACK_LINE),
                                       TRACK_COUNT + toScaleY(TRACK_LINE))
                    }
                }
            }
            batch.setColor(1f, 1f, 1f, 1f)
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
                val x = beat
                val y = trackYOffset
                val height = (TRACK_COUNT + 1.25f + 1.2f * units) + toScaleY(TRACK_LINE)
                batch.setColor(color.toFloatBits())
                batch.fillRect(beat, y, toScaleX(TRACK_LINE * 1.5f),
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
                val text = "${Math.abs(i)}"
                font.drawCompressed(batch, text,
                                    x, y, width, Align.center)
                if (i < 0) {
                    val textWidth = font.getTextWidth(text, width, false)
                    font.drawCompressed(batch, "-", x - textWidth / 2f, y, ENTITY_WIDTH * 0.2f, Align.right)
                }

                // TODO time signature based
                if (Math.floorMod(i, 4) == 0) {
                    val measureNum = i / 4 + 1
                    if (measureNum >= 1) {
                        font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
                        font.drawCompressed(batch, "$measureNum",
                                            x, y + font.lineHeight, width, Align.center)
                    }
                }
            }
            font.setColor(1f, 1f, 1f, 1f)
        }

        // render selection box
        run selectionRectDraw@ {
            val clickOccupation = clickOccupation
            if (clickOccupation is ClickOccupation.CreatingSelection) {
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
                if (rect.height >= font.capHeight) {
                    var defaultX = rect.x + toScaleX(SELECTION_BORDER * 1.5f)
                    var defaultWidth = rect.width - toScaleX(SELECTION_BORDER * 1.5f) * 2
                    val shouldBeLeftAlign = remix.camera.getInputX() < clickOccupation.startPoint.x
                    if (defaultX < remix.camera.position.x - remix.camera.viewportWidth / 2) {
                        defaultX = remix.camera.position.x - remix.camera.viewportWidth / 2
                        defaultWidth = (rect.width + rect.x) - defaultX - toScaleX(SELECTION_BORDER * 1.5f)
                    } else if (defaultX + defaultWidth > remix.camera.position.x + remix.camera.viewportWidth / 2) {
                        defaultWidth = (remix.camera.position.x + remix.camera.viewportWidth / 2) - defaultX
                    }
                    if (rect.width >= font.getTextWidth(widthStr)) {
                        font.drawConstrained(batch, widthStr,
                                             defaultX,
                                             rect.y + rect.height - toScaleY(SELECTION_BORDER * 1.5f),
                                             defaultWidth,
                                             Math.min(font.lineHeight, rect.height), Align.center)
                    }
                    if (rect.width >= font.getTextWidth(heightStr)) {
                        font.drawConstrained(batch, heightStr,
                                             defaultX,
                                             rect.y + rect.height / 2 + font.capHeight / 2,
                                             defaultWidth,
                                             Math.min(font.lineHeight, rect.height),
                                             if (shouldBeLeftAlign) Align.left else Align.right)
                    }
                }
                font.color = oldFontColor

                batch.setColor(oldColor)
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
        val shift = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)
        val control = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
                Input.Keys.CONTROL_RIGHT)
        val alt = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(
                Input.Keys.ALT_RIGHT)
        val left = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val accelerateCamera = shift || control
        val cameraDelta = toScaleX(ENTITY_WIDTH * 5 * Gdx.graphics.deltaTime * if (accelerateCamera) 5 else 1)

        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP)) {
            Gdx.input.inputProcessor.scrolled(-1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN)) {
            Gdx.input.inputProcessor.scrolled(1)
        }

        run camera@ {
            if (left) {
                camera.position.x -= cameraDelta
            }
            if (right) {
                camera.position.x += cameraDelta
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                camera.position.x = 0f
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                // TODO go to end?
            }
        }

        run clickCheck@ {
            val clickOccupation = clickOccupation
            subbeatSection.enabled = false
            when (clickOccupation) {
                is ClickOccupation.Music -> {
                    setSubbeatSectionToMouse()
                    remix.musicStartSec = remix.tempos.beatsToSeconds(
                            MathHelper.snapToNearest(camera.getInputX(), snap))
                }
                is ClickOccupation.Playback -> {
                    setSubbeatSectionToMouse()
                    remix.playbackStart = MathHelper.snapToNearest(camera.getInputX(), snap)
                }
                is ClickOccupation.SelectionDrag -> {
                    clickOccupation.setPositionRelativeToMouse()

                    subbeatSection.enabled = true
                    subbeatSection.start = Math.floor(clickOccupation.left.toDouble()).toFloat()
                    subbeatSection.end = clickOccupation.right
                }
                is ClickOccupation.CreatingSelection -> {
                    clickOccupation.updateRectangle()
                }
            }
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

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
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

        val isDraggingButtonDown = button == Input.Buttons.LEFT && !(control || shift)
        val isCopying = isDraggingButtonDown && alt

        if (clickOccupation != ClickOccupation.None)
            return false

        if (stage.centreAreaStage.isMouseOver()) {
            if (isAnyTrackerButtonDown) {
                clickOccupation = if (isMusicTrackerButtonDown) {
                    ClickOccupation.Music(this, button == Input.Buttons.MIDDLE)
                } else {
                    ClickOccupation.Playback(this)
                }
            } else {
                val mouse = Vector2(remix.camera.getInputX(), remix.camera.getInputY())
                if (remix.entities.any { mouse in it.bounds && it.isSelected }) {
                    // begin selection move
                } else {
                    val clickOccupation = clickOccupation
                    if (clickOccupation == ClickOccupation.None) {
                        // begin selection rectangle
                        val newClick = ClickOccupation.CreatingSelection(this, mouse, shift)
                        this.clickOccupation = newClick
                    }
                }
            }
        } else if (stage.pickerStage.isMouseOver()) {
            // only for new
            val datamodel = pickerSelection.currentSelection.getCurrentVariant().getCurrentPlaceable() ?: return true
            val entity = datamodel.createEntity(remix)

            val selection = ClickOccupation.SelectionDrag(this, Vector2(0f, 0f),
                                                          listOf(Rectangle(entity.bounds)),
                                                          true, this.selection.toList())
            this.selection = listOf(entity)
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
                } else {
                    // delete silently
                    remix.entities.removeAll(selection)
                    // restore original selection
                    selection = clickOccupation.oldSelection
                }
            } else {
                if (validPlacement) {
                    // move action
                } else if (deleting) {
                    // remove+selection action
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
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
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