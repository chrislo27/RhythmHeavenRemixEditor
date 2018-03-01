package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.ClickOccupation.TrackerResize
import io.github.chrislo27.rhre3.editor.action.*
import io.github.chrislo27.rhre3.editor.picker.PickerSelection
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.EditorStage.DirtyType
import io.github.chrislo27.rhre3.editor.view.ViewType
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.areAnyResponseCopyable
import io.github.chrislo27.rhre3.entity.model.*
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.entity.model.special.ShakeEntity
import io.github.chrislo27.rhre3.oopsies.ActionGroup
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameMetadata
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.screen.InfoScreen
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.getValues
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.PlaybackCompletion
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.track.timesignature.TimeSigValueChange
import io.github.chrislo27.rhre3.track.timesignature.TimeSignature
import io.github.chrislo27.rhre3.track.timesignature.TimeSignatureAction
import io.github.chrislo27.rhre3.track.tracker.Tracker
import io.github.chrislo27.rhre3.track.tracker.TrackerAction
import io.github.chrislo27.rhre3.track.tracker.TrackerValueChange
import io.github.chrislo27.rhre3.track.tracker.musicvolume.MusicVolumeChange
import io.github.chrislo27.rhre3.track.tracker.tempo.TempoChange
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.roundToInt


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

        const val SELECTION_BORDER: Float = 2f

        private const val MSG_SEPARATOR = " - "
        private const val NEGATIVE_SYMBOL = "-"
        private const val ZERO_BEAT_SYMBOL = "♩"
        private const val SELECTION_RECT_ADD = "+"
        private const val SELECTION_RECT_INVERT = "±"
        private const val SONG_SUBTITLE_TRANSITION = 0.5f

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
        val ARROWS: List<String> = listOf("▲", "▼", "△", "▽", "➡")
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
        val CUE_PATTERN_COLOR: Color = Color(0.65f, 0.65f, 0.65f, 1f)

        private val THREE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.000", DecimalFormatSymbols())
        private val TRACKER_TIME_FORMATTER = DecimalFormat("00.000", DecimalFormatSymbols())
        private val TRACKER_MINUTES_FORMATTER = DecimalFormat("00", DecimalFormatSymbols())
        val ONE_DECIMAL_PLACE_FORMATTER = DecimalFormat("0.0", DecimalFormatSymbols())
    }

    data class TimedString(val str: String, var time: Float, var out: Boolean) {
        fun goIn() {
            out = false
            time = 0f
        }
    }

    private enum class AutosaveResult(val localization: String, val timed: Boolean = false) {
        NONE(""),
        DOING("editor.msg.autosave.progress"),
        SUCCESS("editor.msg.autosave.success", true),
        FAILED("editor.msg.autosave.failed", true)
    }

    private data class AutosaveState(val result: AutosaveResult, var time: Float)

    fun createRemix(): Remix {
        return Remix(camera, this)
    }

    val camera: OrthographicCamera by lazy {
        OrthographicCamera().also { c ->
            resizeCameraToEntityScale(c)
            c.position.x = 0f
            c.update()
        }
    }
    private val staticCamera: OrthographicCamera by lazy {
        OrthographicCamera().also { c ->
            resizeCameraToPixelScale(c)
            c.position.x = c.viewportWidth / 2f
            c.position.y = c.viewportHeight / 2f
            c.update()
        }
    }

    val pickerSelection: PickerSelection = PickerSelection()
    var remix: Remix = createRemix()
        set(value) {
            field.dispose()
            field = value

            autosaveFile = null
            lastSaveFile = null
            resetAutosaveTimer()

            camera.position.x = field.playbackStart
            camera.update()

            field.entities.forEach { it.updateInterpolation(true) }

            resetAllSongSubtitles()
            stage.updateSelected(DirtyType.SEARCH_DIRTY)
        }
    var theme: Theme = LoadedThemes.currentTheme
    val stage: EditorStage = EditorStage(
            null, stageCamera, main, this)
    val batch: SpriteBatch
        get() = main.batch
    val subbeatSection = SubbeatSection()
    var snap: Float = 0.25f
    val views: EnumSet<ViewType> = EnumSet.noneOf(ViewType::class.java)
    var selection: List<Entity> = listOf()
        set(value) {
            field.forEach { it.isSelected = false }
            field = value
            field.forEach { it.isSelected = true }
        }
    var currentTool: Tool = Tool.SELECTION
        set(value) {
            field = value
            remix.recomputeCachedData()
        }
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
    var lastSaveFile: FileHandle? = null
        private set
    @Volatile
    private var autosaveState: AutosaveState = AutosaveState(AutosaveResult.NONE, 0f)
    private var cachedPlaybackStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""
    private var cachedMusicStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""
    private val songSubtitles = mutableMapOf("songArtist" to TimedString("", SONG_SUBTITLE_TRANSITION, false),
                                             "songTitle" to TimedString("", SONG_SUBTITLE_TRANSITION, false))
    var songArtist: TimedString by songSubtitles
        private set
    var songTitle: TimedString by songSubtitles
        private set

    fun resetAutosaveTimer() {
        autosaveFrequency = main.preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE,
                                                        InfoScreen.DEFAULT_AUTOSAVE_TIME)
                .coerceIn(InfoScreen.timers.first(), InfoScreen.timers.last())

        timeUntilAutosave = 60f * autosaveFrequency
    }

    fun setFileHandles(baseFile: FileHandle) {
        lastSaveFile = baseFile
        autosaveFile = baseFile.sibling(baseFile.nameWithoutExtension() + ".autosave.${RHRE3.REMIX_FILE_EXTENSION}")
    }

    var clickOccupation: ClickOccupation = ClickOccupation.None
        private set(value) {
            field = value
            updateMessageLabel()
        }

    init {
        Localization.listeners += {
            updateMessageLabel()

            cachedPlaybackStart = Float.POSITIVE_INFINITY to ""
            cachedMusicStart = Float.POSITIVE_INFINITY to ""
        }
    }

    fun toScaleX(float: Float): Float =
            (float / RHRE3.WIDTH) * camera.viewportWidth

    fun toScaleY(float: Float): Float =
            (float / RHRE3.HEIGHT) * camera.viewportHeight

    fun BitmapFont.scaleFont(camera: OrthographicCamera) {
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

        if (beat in entity.bounds.x..Math.min(entity.bounds.x + IStretchable.STRETCH_AREA,
                                              entity.bounds.x + entity.bounds.width / 2f)) {
            return StretchRegion.LEFT
        }

        val right = entity.bounds.x + entity.bounds.width

        if (beat in Math.max(right - IStretchable.STRETCH_AREA, right - entity.bounds.width / 2f)..right) {
            return StretchRegion.RIGHT
        }

        return StretchRegion.NONE
    }

    fun canStretchEntity(mouseVector: Vector2, it: Entity): Boolean {
        return it is IStretchable && it.isSelected &&
                mouseVector.y in it.bounds.y..it.bounds.y + it.bounds.height &&
                getStretchRegionForStretchable(mouseVector.x, it) != StretchRegion.NONE
    }

    fun render() = render(adjustCamera = true, otherUI = true)

    /**
     * Pre-stage render.
     */
    fun render(adjustCamera: Boolean, otherUI: Boolean) {
        val beatRange = getBeatRange()
        val isGameBoundariesInViews = ViewType.GAME_BOUNDARIES in views
        val bgColour = theme.background
        Gdx.gl.glClearColor(bgColour.r, bgColour.g, bgColour.b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)

        run {
            val themeTex: Texture = theme.textureObj ?: return@run
            batch.draw(themeTex, 0f, 0f, main.defaultCamera.viewportWidth, main.defaultCamera.viewportHeight)
        }

        batch.end()

        val oldCameraX = camera.position.x
        val oldCameraY = camera.position.y
        val adjustedCameraX: Float
        val adjustedCameraY: Float
        if (adjustCamera) {
            camera.position.y = 1f
            camera.zoom = MathUtils.lerp(camera.zoom, (if (isGameBoundariesInViews) 1.5f else 1f),
                                         Gdx.graphics.deltaTime * 6.5f)

            if (remix.playState == PlayState.PLAYING && remix.currentShakeEntities.isNotEmpty()) {
                val shakeValue = remix.currentShakeEntities.fold(1f) { acc, it ->
                    acc * ShakeEntity.getShakeIntensity(it.semitone)
                }
                val intensity = 0.125f

                camera.position.y += intensity * MathUtils.randomSign() * MathUtils.random(shakeValue)
                camera.position.x += intensity * MathUtils.randomSign() * MathUtils.random(shakeValue) *
                        (ENTITY_HEIGHT / ENTITY_WIDTH)

                camera.update()

                adjustedCameraX = camera.position.x
                adjustedCameraY = camera.position.y
                camera.position.x = oldCameraX
                camera.position.y = oldCameraY
            } else {
                camera.update()
                adjustedCameraX = oldCameraX
                adjustedCameraY = oldCameraY
            }
        } else {
            adjustedCameraX = oldCameraX
            adjustedCameraY = oldCameraY
        }
        batch.projectionMatrix = camera.combined
        batch.begin()

        val font = main.defaultFont
        val trackYOffset = toScaleY(-TRACK_LINE / 2f)

        font.scaleFont(camera)

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

        // game boundaries view (background)
        if (isGameBoundariesInViews) {
            val squareHeight = TRACK_COUNT.toFloat()
            val squareWidth = squareHeight / (ENTITY_WIDTH / ENTITY_HEIGHT)

            remix.gameSections.values.forEach { section ->
                if (section.startBeat > beatRange.endInclusive || section.endBeat < beatRange.start)
                    return@forEach
                val tex = section.game.icon

                val sectionWidth = section.endBeat - section.startBeat
                val sections = (sectionWidth / squareWidth)
                val wholes = sections.toInt()
                val remainder = sectionWidth % squareWidth

                // track background icons
                batch.setColor(1f, 1f, 1f, 0.25f)
                for (i in 0 until wholes) {
                    batch.draw(tex, section.startBeat + squareWidth * i, 0f,
                               squareWidth, squareHeight)
                }
                batch.draw(tex, section.startBeat + squareWidth * wholes, 0f,
                           remainder, squareHeight,
                           0, 0, (tex.width * (sections - wholes)).toInt(), tex.height,
                           false, false)
            }

            batch.setColor(1f, 1f, 1f, 1f)
        }

        val smoothDragging = main.preferences.getBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, true)
        remix.entities.forEach {
            it.updateInterpolation(!smoothDragging)
            if (it.inRenderRange(beatRange.start.toFloat(), beatRange.endInclusive.toFloat())) {
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

        // waveform
        if (SoundSystem.system == BeadsSoundSystem && remix.playState == PlayState.PLAYING && ViewType.WAVEFORM in views && otherUI) {
            batch.setColor(theme.waveform.r, theme.waveform.g, theme.waveform.b, theme.waveform.a * 0.65f)

            val samplesPerSecond = BeadsSoundSystem.audioContext.sampleRate.toInt()
            val samplesPerFrame = samplesPerSecond / 60
            val fineness: Int = (samplesPerFrame / 1).coerceAtMost(samplesPerFrame)

            val isPresentationMode = stage.presentationModeStage.visible
            val viewportWidth = if (isPresentationMode) camera.viewportWidth * 0.5f else camera.viewportWidth
            val height = if (!isPresentationMode) TRACK_COUNT / 2f else 1f
            val centre = TRACK_COUNT / 2f

            BeadsSoundSystem.audioContext.getValues(BeadsSoundSystem.sampleArray)

            val data = BeadsSoundSystem.sampleArray
            for (x in 0 until fineness) {
                val dataPoint = data[(x.toFloat() / fineness * data.size).toInt()]
                val h = height * (if (isPresentationMode) Math.abs(dataPoint) else dataPoint.coerceIn(-1f, 1f))
                val width = viewportWidth / fineness.toFloat() * camera.zoom
                if (!isPresentationMode) {
                    batch.fillRect((camera.position.x - viewportWidth / 2 * camera.zoom) + x * width,
                                   centre - h / 2, width, h)
                } else {
                    val cameraAdjX = (oldCameraX - adjustedCameraX)
                    val cameraAdjY = (oldCameraY - adjustedCameraY)
                    batch.fillRect((camera.position.x - cameraAdjX - viewportWidth / 2 * camera.zoom) + x * width,
                                   -3f - cameraAdjY, width, h / 2)
                }
            }

            batch.setColor(1f, 1f, 1f, 1f)
        }

        // game boundaries view (dividers)
        if (isGameBoundariesInViews) {
            remix.gameSections.values.forEach { section ->
                if (section.startBeat > beatRange.endInclusive || section.endBeat < beatRange.start)
                    return@forEach
                val icon = section.game.icon
                val sectionWidth = section.endBeat - section.startBeat

                // dividing lines
                val triangle = AssetRegistry.get<Texture>("tracker_right_tri")
                run left@ {
                    batch.color = theme.trackLine
                    font.color = theme.trackLine
                    val x = section.startBeat
                    val height = TRACK_COUNT * 2f + 0.5f
                    val maxTextWidth = 5f
                    batch.fillRect(x, 0f, toScaleX(TRACK_LINE) * 2, height)
                    batch.draw(triangle, x, height - 1f, 0.25f, 1f)

                    for (i in 0 until (sectionWidth / 6f).toInt().coerceAtLeast(1)) {
                        batch.setColor(1f, 1f, 1f, 1f)

                        val left = x + (6f * i).coerceAtLeast(0.125f)

                        batch.draw(icon, left, height - 2f, 0.25f, 1f)
                        font.drawCompressed(batch,
                                            if (stage.presentationModeStage.visible) section.game.group else section.game.name,
                                            left, height - 2.25f,
                                            (sectionWidth - 0.25f).coerceAtMost(maxTextWidth), Align.left)
                    }
                }
                batch.setColor(1f, 1f, 1f, 1f)
                font.setColor(1f, 1f, 1f, 1f)
            }

            batch.setColor(1f, 1f, 1f, 1f)
        }

        // trackers (playback start, music)
        run trackers@ {
            val borderedFont = main.defaultBorderedFont
            val oldFontColor = borderedFont.color

            fun getTrackerTime(time: Float, noBeat: Boolean = false): String {
                val signedSec = if (noBeat) time else remix.tempos.beatsToSeconds(time)
                val sec = Math.abs(signedSec)
                val seconds = (if (signedSec < 0) "-" else "") +
                        TRACKER_MINUTES_FORMATTER.format(
                                (sec / 60).toLong()) + ":" + TRACKER_TIME_FORMATTER.format(
                        sec % 60.0)
                if (noBeat) {
                    return seconds
                }
                return Localization["tracker.any.time",
                        THREE_DECIMAL_PLACES_FORMATTER.format(time.toDouble()), seconds]
            }

            fun renderAboveTracker(textKey: String?, controlKey: String?, units: Int, beat: Float, color: Color,
                                   trackerTime: String = getTrackerTime(beat),
                                   triangleHeight: Float = 0.4f, bpmText: String? = null) {
                val triangleWidth = toScaleX(triangleHeight * ENTITY_HEIGHT)
                val x = beat - toScaleX(TRACK_LINE * 1.5f) / 2
                val y = trackYOffset
                val height = (TRACK_COUNT + 1.25f + 1.2f * units) + toScaleY(TRACK_LINE)
                batch.setColor(color.toFloatBits())
                batch.fillRect(x, y, toScaleX(TRACK_LINE * 1.5f),
                               height - triangleHeight / 2)
                batch.draw(AssetRegistry.get<Texture>("tracker_right_tri"),
                           x, y + height - triangleHeight, triangleWidth, triangleHeight)

                borderedFont.scaleFont(camera)
                borderedFont.scaleMul(0.75f)
                borderedFont.color = batch.color
                if (textKey != null) {
                    borderedFont.drawCompressed(batch, Localization[textKey], x - 1.05f, y + height, 1f, Align.right)
                }
                borderedFont.drawCompressed(batch, trackerTime, x + triangleWidth + 0.025f, y + height, 1f, Align.left)
                if (bpmText != null) {
                    borderedFont.drawCompressed(batch, bpmText,
                                                x + triangleWidth + 0.025f,
                                                y + height + borderedFont.capHeight * 1.25f,
                                                1f, Align.left)
                }

                if (controlKey != null) {
                    val line = borderedFont.lineHeight
                    borderedFont.scaleMul(0.75f)
                    borderedFont.drawCompressed(batch, Localization[controlKey], x - 1.05f, y + height - line, 1f,
                                                Align.right)
                }

                borderedFont.scaleFont(camera)
                batch.setColor(1f, 1f, 1f, 1f)
            }

            if (cachedPlaybackStart.first != remix.tempos.beatsToSeconds(remix.playbackStart)) {
                cachedPlaybackStart = remix.tempos.beatsToSeconds(remix.playbackStart) to getTrackerTime(
                        remix.playbackStart)
            }
            if (cachedMusicStart.first != remix.musicStartSec) {
                cachedMusicStart = remix.musicStartSec to getTrackerTime(remix.musicStartSec, noBeat = true)
            }

            renderAboveTracker("tracker.music", "tracker.music.controls",
                               1, remix.musicStartSec, theme.trackers.musicStart,
                               cachedMusicStart.second)
            renderAboveTracker("tracker.playback", "tracker.playback.controls",
                               0, remix.playbackStart, theme.trackers.playback, cachedPlaybackStart.second)

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
                                   theme.trackers.playback, triangleHeight = 0f,
                                   bpmText = "♩=${ONE_DECIMAL_PLACE_FORMATTER.format(
                                           remix.tempos.tempoAt(remix.beat))}")
            }

            borderedFont.color = oldFontColor
            borderedFont.unscaleFont()
        }

        // beat numbers
        run beatNumbers@ {
            for (i in beatRange) {
                val width = ENTITY_WIDTH * 0.4f
                val x = i - width / 2f
                val y = TRACK_COUNT + toScaleY(TRACK_LINE + TRACK_LINE) + font.capHeight
                val text = if (i == 0) ZERO_BEAT_SYMBOL else "${Math.abs(i)}"
                if (stage.jumpToField.hasFocus && i == stage.jumpToField.text.toIntOrNull() ?: Int.MAX_VALUE) {
                    val glow = MathHelper.getTriangleWave(1f)
                    val sel = theme.selection.selectionBorder
                    font.setColor(MathUtils.lerp(sel.r, 1f, glow), MathUtils.lerp(sel.g, 1f, glow),
                                  MathUtils.lerp(sel.b, 1f, glow), sel.a)
                } else {
                    font.color = theme.trackLine
                }
                font.drawCompressed(batch, text,
                                    x, y, width, Align.center)
                if (i < 0) {
                    val textWidth = font.getTextWidth(text, width, false)
                    font.drawCompressed(batch, NEGATIVE_SYMBOL, x - textWidth / 2f, y, ENTITY_WIDTH * 0.2f, Align.right)
                }

                val measureNum = remix.timeSignatures.getMeasure(i.toFloat())
                if (measureNum >= 1 && remix.timeSignatures.getMeasurePart(i.toFloat()) == 0 && i < remix.duration) {
                    font.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.5f)
                    font.drawCompressed(batch, "$measureNum",
                                        x, y + font.lineHeight, width, Align.center)
                }
            }
            font.setColor(1f, 1f, 1f, 1f)
        }

        // Play-Yan
        if (remix.metronome) {
            val jumpTime = 0.65f
            val beat = if (remix.playState != PlayState.STOPPED) remix.beat else remix.playbackStart
            val beatPercent = (beat + Math.abs(Math.floor(beat.toDouble()) * 2).toFloat()) % 1
            if (beatPercent <= jumpTime && remix.playState != PlayState.STOPPED) {
                val jumpSawtooth = beatPercent / jumpTime
                val jumpTriangle = if (jumpSawtooth <= 0.5f)
                    jumpSawtooth * 2
                else
                    1f - (jumpSawtooth - 0.5f) * 2
                val jumpHeight = (if (jumpSawtooth <= 0.5f)
                    jumpSawtooth * 2
                else
                    jumpTriangle * jumpTriangle) * (if (remix.timeSignatures.getMeasurePart(beat) == 0) 2f else 1f)

                batch.draw(AssetRegistry.get<Texture>("playyan_jumping"), beat,
                           TRACK_COUNT + 1f * jumpHeight, toScaleX(26f), toScaleY(35f),
                           0, 0, 26, 35, false, false)
            } else {
                val step = (MathHelper.getSawtoothWave(0.25f) * 4).toInt()
                batch.draw(AssetRegistry.get<Texture>("playyan_walking"), beat,
                           TRACK_COUNT * 1f,
                           toScaleX(26f), toScaleY(35f),
                           step * 26, 0, 26, 35, false, false)
            }
        }

        // time signatures
        run timeSignatures@ {
            val timeSignatures = remix.timeSignatures
            val bigFont = main.timeSignatureFont
            val heightOfTrack = TRACK_COUNT.toFloat() - toScaleY(TRACK_LINE) * 2f
            val inputBeat = Math.floor(remix.camera.getInputX().toDouble()).toInt()
            bigFont.scaleFont(camera)
            bigFont.scaleMul((heightOfTrack * 0.5f - 0.1f) / bigFont.capHeight)

            timeSignatures.map.values.forEach { tracker ->
                val x = tracker.beat
                val startY = 0f + toScaleY(TRACK_LINE)

                if (currentTool == Tool.TIME_SIGNATURE && tracker.beat == inputBeat) {
                    bigFont.color = theme.selection.selectionBorder
                } else {
                    bigFont.setColor(theme.trackLine.r, theme.trackLine.g, theme.trackLine.b, theme.trackLine.a * 0.75f)
                }

                val lowerWidth = bigFont.getTextWidth(tracker.lowerText, 1f, false)
                val upperWidth = bigFont.getTextWidth(tracker.upperText, 1f, false)
                val biggerWidth = Math.max(lowerWidth, upperWidth)

                bigFont.drawCompressed(batch, tracker.lowerText,
                                       x + biggerWidth * 0.5f - lowerWidth * 0.5f,
                                       startY + bigFont.capHeight,
                                       1f, Align.left)
                bigFont.drawCompressed(batch, tracker.upperText,
                                       x + biggerWidth * 0.5f - upperWidth * 0.5f,
                                       startY + heightOfTrack,
                                       1f, Align.left)
            }

            bigFont.setColor(1f, 1f, 1f, 1f)
            bigFont.unscaleFont()
        }

        // bottom trackers
        run trackers@ {
            val borderedFont = main.defaultBorderedFont
            borderedFont.scaleFont(camera)

            val triHeight = 0.5f
            val triWidth = toScaleX(triHeight * ENTITY_HEIGHT)
            val triangle = AssetRegistry.get<Texture>("tracker_tri")
            val rightTriangle = AssetRegistry.get<Texture>("tracker_right_tri_bordered")
            val tool = currentTool
            val clickOccupation = clickOccupation
            val toolIsTrackerBased = tool.isTrackerRelated
            val clickIsTrackerResize = clickOccupation is TrackerResize
            val currentTracker: Tracker<*>? = getTrackerOnMouse(tool.trackerClass?.java, true)

            fun renderTracker(layer: Int, color: Color, text: String, beat: Float, width: Float, slope: Int) {
                val heightPerLayer = 0.75f
                val y = 0f - (layer + 1) * heightPerLayer
                val height = 0f - y

                // background
                batch.setColor(color.r, color.g, color.b, color.a * 0.35f)
                val batchColor = batch.packedColor
                val fadedColor = Color.toFloatBits(color.r, color.g, color.b, color.a * 0.025f)
                if (slope == 0 || width == 0f) {
                    batch.fillRect(beat, y, width, height)
                } else {
                    batch.drawQuad(beat, y + height, batchColor,
                                   beat + width, y + height, fadedColor,
                                   beat + width, y, fadedColor,
                                   beat, y, batchColor)
                }

                batch.drawRect(beat, y, width, height, toScaleX(2f), toScaleY(2f))

                // lines
                batch.color = color
                val lineWidth = toScaleX(TRACK_LINE)
                batch.fillRect(beat, y, lineWidth, height)
                batch.fillRect(beat + width, y, -lineWidth, height)

                // triangle
                if (width == 0f) {
                    batch.draw(triangle, beat - triWidth * 0.5f, y, triWidth, triHeight)
                } else {
                    batch.draw(rightTriangle, beat + width, y, -triWidth * 0.75f, triHeight)
                    batch.draw(rightTriangle, beat, y, triWidth * 0.75f, triHeight)
                }

                // text
                borderedFont.color = color
                borderedFont.drawCompressed(batch, text, beat + triWidth * 0.5f,
                                            y + heightPerLayer * 0.5f + borderedFont.capHeight * 0.5f,
                                            2f, Align.left)

                batch.setColor(1f, 1f, 1f, 1f)
            }

            fun Tracker<*>.render() {
                renderTracker(container.renderLayer,
                              if (toolIsTrackerBased && currentTracker === this
                                      && !clickIsTrackerResize && remix.playState == PlayState.STOPPED)
                                  Color.WHITE else getColour(theme),
                              text, beat, width, getSlope())
            }

            remix.trackersReverseView.forEach {
                it.map.values.forEach {
                    if ((clickOccupation !is TrackerResize || clickOccupation.tracker !== it) && it !== currentTracker && (it.beat in beatRange || it.endBeat in beatRange)) {
                        it.render()
                    }
                }
            }

            if (clickOccupation is TrackerResize) {
                renderTracker(clickOccupation.renderLayer,
                              if (clickOccupation.isPlacementValid()) Color.WHITE else Color.RED,
                              clickOccupation.text,
                              clickOccupation.beat, clickOccupation.width, clickOccupation.tracker.getSlope())
            } else {
                currentTracker?.render()
            }

            borderedFont.unscaleFont()
        }

        // render selection box + delete zone
        if (otherUI) {
            val clickOccupation = clickOccupation
            when (clickOccupation) {
                is ClickOccupation.SelectionDrag -> {
                    val oldColor = batch.packedColor
                    val y = if (clickOccupation.isBottomSpecial) -1f else 0f
                    val mouseY = remix.camera.getInputY()
                    val alpha = (1f + y - mouseY).coerceIn(0.5f + MathHelper.getTriangleWave(2f) * 0.125f, 1f)
                    val left = remix.camera.position.x - remix.camera.viewportWidth / 2 * remix.camera.zoom

                    batch.setColor(1f, 0f, 0f, 0.25f * alpha)
                    batch.fillRect(left, y,
                                   remix.camera.viewportWidth * remix.camera.zoom,
                                   -remix.camera.viewportHeight * remix.camera.zoom)
                    batch.setColor(oldColor)

                    val deleteFont = main.defaultFontLarge
                    deleteFont.scaleFont(camera)
                    deleteFont.scaleMul(0.5f)
                    deleteFont.setColor(0.75f, 0.5f, 0.5f, alpha)

                    deleteFont.drawCompressed(batch, Localization["editor.delete"], left, y + -1f + font.capHeight / 2,
                                              remix.camera.viewportWidth * remix.camera.zoom, Align.center)

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

                    run text@ {
                        val oldFontColor = font.color
                        font.color = theme.selection.selectionBorder

                        val toScaleX = toScaleX(SELECTION_BORDER * 1.5f)
                        val toScaleY = toScaleY(SELECTION_BORDER * 1.5f)
                        val shift = Gdx.input.isShiftDown()
                        val control = Gdx.input.isControlDown()

                        val bigFont = main.defaultFontLarge
                        val oldBigFontColor = bigFont.color
                        bigFont.scaleFont(camera)

                        // AND or XOR strings
                        if (rect.height - toScaleY * 2 >= bigFont.capHeight
                                && !(shift && control) && (shift || control)) {
                            bigFont.color = theme.selection.selectionBorder
                            bigFont.color.a *= 0.25f * MathHelper.getTriangleWave(2f) + 0.35f
                            bigFont.drawCompressed(batch, if (shift) SELECTION_RECT_ADD else SELECTION_RECT_INVERT,
                                                   rect.x + toScaleX, rect.y + rect.height / 2 + bigFont.capHeight / 2,
                                                   rect.width - toScaleX * 2, Align.center)
                        }

                        // dimension strings
                        if (rect.height - toScaleY * 2 >= font.capHeight) {
                            val widthStr = ONE_DECIMAL_PLACE_FORMATTER.format(rect.width.toDouble())
                            val heightStr = ONE_DECIMAL_PLACE_FORMATTER.format(rect.height.toDouble())

                            var defaultX = rect.x + toScaleX
                            var defaultWidth = rect.width - toScaleX * 2
                            val shouldBeLeftAlign = remix.camera.getInputX() < clickOccupation.startPoint.x
                            if (defaultX < remix.camera.position.x - remix.camera.viewportWidth / 2 * remix.camera.zoom) {
                                defaultX = remix.camera.position.x - remix.camera.viewportWidth / 2 * remix.camera.zoom
                                defaultWidth = (rect.width + rect.x) - defaultX - toScaleX
                            } else if (defaultX + defaultWidth > remix.camera.position.x + remix.camera.viewportWidth / 2) {
                                defaultWidth = (remix.camera.position.x + remix.camera.viewportWidth / 2) - defaultX
                            }
                            if (rect.width - toScaleX * 2 >= font.getTextWidth(widthStr)) {
                                font.drawCompressed(batch, widthStr,
                                                    defaultX,
                                                    rect.y + rect.height - toScaleY,
                                                    defaultWidth, Align.center)
                            }
                            if (rect.width - toScaleX * 2 >= font.getTextWidth(heightStr)) {
                                font.drawCompressed(batch, heightStr,
                                                    defaultX,
                                                    rect.y + rect.height / 2 + font.capHeight / 2,
                                                    defaultWidth,
                                                    if (shouldBeLeftAlign) Align.left else Align.right)
                            }
                        }

                        bigFont.unscaleFont()
                        bigFont.color = oldBigFontColor
                        font.color = oldFontColor
                    }

                    batch.setColor(oldColor)
                }
                else -> {
                }
            }
        }

        font.unscaleFont()
        batch.end()

        // static camera
        batch.projectionMatrix = staticCamera.combined
        batch.begin()
        font.scaleFont(staticCamera)

        if (otherUI) {
            // midi visualization w/ glee club
            if (remix.midiInstruments > 0 && ViewType.GLEE_CLUB in views) {
                // each cell is 88x136 with a 1 px black border
                val texture = AssetRegistry.get<Texture>("glee_club")
                val width = 86
                val cellHeight = 134
                val height = cellHeight - 15
                val renderWidth = width * 0.65f

                val startX = (staticCamera.viewportWidth / 2f - renderWidth * remix.midiInstruments / 2f).coerceAtLeast(
                        0f)
                val startY = (stage.centreAreaStage.location.realY / Gdx.graphics.height) * staticCamera.viewportHeight

                for (i in 0 until remix.midiInstruments) {
                    val playingEntity: Entity? = remix.entities.find {
                        it.playbackCompletion == PlaybackCompletion.PLAYING && it is CueEntity && it.instrument == i + 1
                    }
                    val isPlaying = remix.playState != PlayState.STOPPED && playingEntity != null
                    val animation = if (isPlaying) {
                        MathUtils.lerp(0f, 3f, MathHelper.getSawtoothWave(0.2f)).roundToInt().coerceAtMost(2)
                    } else {
                        MathHelper.getSawtoothWave(0.2f).roundToInt()
                    }

                    batch.draw(texture, startX + i * (if (startX <= 0f)
                        staticCamera.viewportWidth / remix.midiInstruments
                    else renderWidth), startY, width.toFloat(),
                               height * (if (isPlaying && playingEntity != null && playingEntity is IRepitchable)
                                   (Semitones.getALPitch(playingEntity.semitone)) else 1f),
                               1 + (animation * (width + 2)),
                               1 + (if (isPlaying) ((cellHeight + 2) * (if (isPlaying && playingEntity != null && playingEntity is IRepitchable && playingEntity.semitone > IRepitchable.DEFAULT_RANGE.endInclusive) 4 else 3)) else 0),
                               width, height, false, false)
                }
            }

            // song subtitles
            run {
                val camera = staticCamera
                font.scaleMul(camera.zoom)
                val texture = AssetRegistry.get<Texture>("ui_songtitle")
                val scale = 1.15f
                val texWidth = texture.width.toFloat() * scale * camera.zoom
                val texHeight = texture.height.toFloat() * scale * camera.zoom
                val startX = 0f

                fun renderBar(timedString: TimedString, bottom: Boolean) {
                    val rawPercent = Interpolation.circle.apply(
                            (timedString.time / SONG_SUBTITLE_TRANSITION).coerceIn(0f, 1f))
                    val xPercent: Float = if (timedString.out) rawPercent else 1f - rawPercent
                    val x = startX + if (!bottom) texWidth * xPercent - texWidth else camera.viewportWidth * camera.zoom - xPercent * texWidth
                    val y = (camera.viewportHeight * 0.35f * camera.zoom) - if (bottom) texHeight * 1.1f else 0f

                    batch.draw(texture, x, y, texWidth, texHeight,
                               0, 0, texture.width, texture.height,
                               bottom, bottom)
                    font.setColor(1f, 1f, 1f, 1f)
                    val padding = 8f * camera.zoom
                    val textWidth = (texture.width.toFloat() - texture.height) * scale * camera.zoom - padding * 2
                    val triangleWidth = texture.height.toFloat() * scale * camera.zoom
                    font.drawCompressed(batch, timedString.str,
                                        (if (!bottom) x else x + triangleWidth) + padding,
                                        y + font.capHeight + texHeight / 2 - font.capHeight / 2,
                                        textWidth,
                                        if (bottom) Align.left else Align.right)
                }

                renderBar(songTitle, false)
                renderBar(songArtist, true)

                font.scaleMul(1f / camera.zoom)
            }

            // autosave indicator
            if (autosaveState.result != AutosaveResult.NONE) {
                val autosaveState = autosaveState
                val borderedFont = main.defaultBorderedFont
                borderedFont.scaleFont(staticCamera)
                borderedFont.scaleMul(0.85f)

                val alpha = if (autosaveState.result.timed) (autosaveState.time * 3f).coerceIn(0f, 1f) else 1f

                borderedFont.setColor(1f, 1f, 1f, alpha)
                batch.setColor(1f, 1f, 1f, alpha)

                val startX = 6f
                val startY = (stage.centreAreaStage.location.realY / Gdx.graphics.height) * staticCamera.viewportHeight + 6f
                val height = 32f
                val width = 32f

                borderedFont.drawCompressed(batch, Localization[autosaveState.result.localization],
                                            startX, startY + height * 0.5f + borderedFont.capHeight * 0.5f,
                                            staticCamera.viewportWidth - width, Align.left)

                borderedFont.setColor(1f, 1f, 1f, 1f)
                borderedFont.unscaleFont()
                batch.setColor(1f, 1f, 1f, 1f)
            }
        }

        font.unscaleFont()
        batch.end()

        // reset camera proj matrix to normal
        batch.projectionMatrix = main.defaultCamera.combined
    }

    fun postStageRender() {

    }

    fun renderUpdate() {
        remix.timeUpdate(Gdx.graphics.deltaTime)
        if (remix.playState == PlayState.PLAYING) {
            songSubtitles.values.forEach { it.time += Gdx.graphics.deltaTime }
        }

        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()
//        val alt = Gdx.input.isAltDown()
        val left = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)
        val right = !stage.isTyping && Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)
        val accelerateCamera = shift || control
        val cameraDelta = toScaleX(ENTITY_WIDTH * 5 * Gdx.graphics.deltaTime * if (accelerateCamera) 5 else 1)
        val mouseVector = mouseVector

        subbeatSection.enabled = false

        if (!stage.isTyping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                Gdx.input.inputProcessor.scrolled(-1)
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                Gdx.input.inputProcessor.scrolled(1)
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (shift) {
                    when (remix.playState) {
                        PlayState.STOPPED, PlayState.PAUSED -> remix.playState = PlayState.PLAYING
                        PlayState.PLAYING -> remix.playState = PlayState.PAUSED
                    }
                } else {
                    when (remix.playState) {
                        PlayState.STOPPED, PlayState.PAUSED -> remix.playState = PlayState.PLAYING
                        PlayState.PLAYING -> remix.playState = PlayState.STOPPED
                    }
                }
            }
        }

        if (remix.playState == PlayState.PLAYING) {
            val halfWidth = remix.camera.viewportWidth / 2 * remix.camera.zoom
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
                        autosaveState = AutosaveState(AutosaveResult.DOING, 0f)
                        Remix.saveTo(remix, autosaveFile.file(), true)
                        autosaveState = AutosaveState(AutosaveResult.SUCCESS, 3f)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        autosaveState = AutosaveState(AutosaveResult.FAILED, 15f)
                    }
                    Gdx.app.postRunnable {
                        resetAutosaveTimer()
                        Toolboks.LOGGER.info("Autosaved (frequency every $autosaveFrequency minute(s))")
                    }
                }
            }
        }

        if (autosaveState.time > 0) {
            autosaveState.time -= Gdx.graphics.deltaTime
        }

        if (Toolboks.debugMode && Gdx.input.isKeyJustPressed(Input.Keys.J)) {
            timeUntilAutosave = 0f
        }

        if (!stage.isTyping) {
            if (left) {
                camera.position.x -= cameraDelta
                camera.update()
            }
            if (right) {
                camera.position.x += cameraDelta
                camera.update()
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                camera.position.x = 0f
                camera.update()
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                camera.position.x = remix.getLastEntityPoint()
                camera.update()
            }

            if (control && clickOccupation == ClickOccupation.None) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                    main.screen = ScreenRegistry.getNonNull("newRemix")
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
                    main.screen = ScreenRegistry.getNonNull("openRemix")
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                    main.screen = ScreenRegistry.getNonNull("saveRemix")
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
//                    remix.mutate(EntitySelectionAction(this, selection.toList(), remix.entities.toList()))
                }
            }

            if (Toolboks.debugMode) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                }
            }
        }

        run clickCheck@ {
            val clickOccupation = clickOccupation
            val tool = currentTool
            val nearestSnap = MathHelper.snapToNearest(camera.getInputX(), snap)
            if (tool == Tool.SELECTION) {
                when (clickOccupation) {
                    is ClickOccupation.Music -> {
                        setSubbeatSectionToMouse()
                        remix.musicStartSec = nearestSnap
                    }
                    is ClickOccupation.Playback -> {
                        setSubbeatSectionToMouse()
                        remix.playbackStart = nearestSnap
                    }
                    is ClickOccupation.SelectionDrag -> {
                        if (clickOccupation.isStretching) {
                            val entity = this.selection.first()
                            val oldBound = clickOccupation.oldBounds.first()
                            entity.updateBounds {
                                if (clickOccupation.stretchType == StretchRegion.LEFT) {
                                    val rightSide = oldBound.x + oldBound.width

                                    entity.bounds.x = (nearestSnap).coerceAtMost(rightSide - IStretchable.MIN_STRETCH)
                                    entity.bounds.width = rightSide - entity.bounds.x
                                } else if (clickOccupation.stretchType == StretchRegion.RIGHT) {
                                    entity.bounds.width = (nearestSnap - oldBound.x).coerceAtLeast(
                                            IStretchable.MIN_STRETCH)
                                }
                            }
                        } else {
                            clickOccupation.setPositionRelativeToMouse()
                        }

                        subbeatSection.enabled = true
                        subbeatSection.start = Math.floor(clickOccupation.left.toDouble()).toFloat()
                        subbeatSection.end = clickOccupation.right

                        updateMessageLabel()
                    }
                    is ClickOccupation.CreatingSelection -> {
                        clickOccupation.updateRectangle()
                    }
                    ClickOccupation.None -> {
                        if (selection.isNotEmpty() && !stage.isTyping &&
                                (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) ||
                                        Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE))) {
                            remix.entities.removeAll(this.selection)
                            remix.addActionWithoutMutating(ActionGroup(listOf(
                                    EntityRemoveAction(this, this.selection,
                                                       this.selection.map { Rectangle(it.bounds) }),
                                    EntitySelectionAction(this, this.selection.toList(), listOf())
                                                                             )))
                            this.selection = listOf()
                        }
                    }
                    is TrackerResize -> {
                        // handled below
                    }
                }
            } else if (tool.isTrackerRelated) {
                if (clickOccupation is TrackerResize) {
                    clickOccupation.updatePosition(nearestSnap)
                }
            } else if (tool == Tool.SFX_VOLUME) {
                updateMessageLabel()
            }
        }

        if (currentTool.isTrackerRelated && currentTool != Tool.TIME_SIGNATURE) {
            subbeatSection.enabled = true
            subbeatSection.start = Math.floor(remix.camera.getInputX().toDouble()).toFloat()
            subbeatSection.end = subbeatSection.start + 0.5f
        }

        // undo/redo
        if (control && clickOccupation == ClickOccupation.None) {
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

        fun StringBuilder.separator(): StringBuilder {
            this.append(MSG_SEPARATOR)
            return this
        }

        if (stage.tapalongStage.visible) {
            builder.append(Localization["editor.tapalong.info"])
        } else {
            when (currentTool) {
                Tool.SELECTION -> {
                    val currentGame: Game? = if (pickerSelection.filter.areGamesEmpty) null else pickerSelection.filter.currentGame
                    builder.append(currentGame?.let {
                        if (Toolboks.debugMode) {
                            it.name + " [LIGHT_GRAY](${it.id})[]"
                        } else {
                            it.name
                        }
                    } ?: Localization["editor.msg.noGame"])
                    if (selection.isNotEmpty()) {
                        builder.separator().append(
                                Localization["editor.msg.numSelected", this.selection.size.toString()])

                        if (clickOccupation == ClickOccupation.None) {
                            if (selection.any { it is IRepitchable && it.canBeRepitched }) {
                                builder.separator().append(Localization["editor.msg.repitch"])
                            }

                            if (selection.all(Entity::supportsCopying)) {
                                builder.separator().append(Localization["editor.msg.copyHint"])
                            }

                            if (selection.areAnyResponseCopyable()) {
                                builder.separator().append(Localization["editor.msg.callResponseHint"])
                            }
                        } else if (clickOccupation is ClickOccupation.SelectionDrag) {
                            if (!clickOccupation.isPlacementValid()) {
                                if (clickOccupation.isInDeleteZone()) {
                                    builder.separator().append(Localization["editor.msg.deletingSelection"])
                                } else {
                                    builder.separator().append(Localization["editor.msg.invalidPlacement"])
                                }
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

                            if (first is IEditableText) {
                                builder.separator().append(
                                        Localization["editor.msg.editabletext.${if (stage.entityTextField.visible) "finish" else "edit"}"])
                            }
                        }
                    }

                    if (clickOccupation is ClickOccupation.CreatingSelection) {
                        builder.separator().append(Localization["editor.msg.selectionHint"])
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
                Tool.TIME_SIGNATURE -> {
                    builder.append(Localization["editor.msg.timeSignature"])
                }
                Tool.TEMPO_CHANGE -> {
                    builder.append(Localization["editor.msg.tempoChange"])
                }
                Tool.MUSIC_VOLUME -> {
                    builder.append(Localization["editor.msg.musicVolume"])
                }
                Tool.SFX_VOLUME -> {
                    builder.append(Localization["editor.msg.sfxVolume"])
                    val entity = getEntityOnMouse()
                    if (entity != null && entity is IVolumetric && entity.isVolumetric) {
                        builder.separator().append(Localization["editor.msg.sfxVolume.volume", entity.volumePercent])
                    }
                }
            }
        }

        label.text = builder.toString()
    }

    private fun getMultipartOnMouse(): MultipartEntity<*>? {
        val mouseVector = mouseVector
        return remix.entities.firstOrNull { mouseVector in it.bounds } as? MultipartEntity<*>?
    }

    private fun getEntityOnMouse(): Entity? {
        val mouseVector = mouseVector
        return remix.entities.firstOrNull { mouseVector in it.bounds }
    }

    fun songTitle(text: String?) {
        if (text == null) {
            songTitle.goIn()
        } else {
            songTitle = TimedString(text, 0f, true)
        }
    }

    fun songArtist(text: String?) {
        if (text == null) {
            songArtist.goIn()
        } else {
            songArtist = TimedString(text, 0f, true)
        }
    }

    fun resetAllSongSubtitles() {
        songSubtitles.values.forEach {
            it.time = SONG_SUBTITLE_TRANSITION
            it.out = false
        }
    }

    fun updateRecentsList(gameUsed: Game?) {
        val list = GameMetadata.recents

        if (gameUsed != null) {
            list.remove(gameUsed)
            list.add(0, gameUsed)

            val byGroup = list.distinctBy(Game::gameGroup)
            if (byGroup.size > GameMetadata.MAX_RECENTLY_USED) {
                list.retainAll(byGroup.take(GameMetadata.MAX_RECENTLY_USED))
            }
        } else {
            list.clear()
        }

        stage.recentsFilter.shouldUpdate = true
        stage.updateSelected()
    }

    fun getTrackerOnMouse(klass: Class<out Tracker<*>>?, obeyY: Boolean): Tracker<*>? {
        if (klass == null || (obeyY && remix.camera.getInputY() > 0f))
            return null
        val mouseX = remix.camera.getInputX()
        remix.trackers.forEach {
            val result = it.map.values.firstOrNull {
                if (it::class.java != klass)
                    false
                else if (it.isZeroWidth)
                    MathUtils.isEqual(mouseX, it.beat, snap * 0.5f)
                else
                    mouseX in it.beat..it.endBeat
            }

            if (result != null)
                return result
        }

        return null
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val mouseVector = mouseVector
        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()
        val alt = Gdx.input.isAltDown()

        val isMusicTrackerButtonDown = !(shift || alt) &&
                ((!control && button == Input.Buttons.MIDDLE) || (button == Input.Buttons.RIGHT && control))

        val isPlaybackTrackerButtonDown = !isMusicTrackerButtonDown &&
                button == Input.Buttons.RIGHT && !(shift || alt || control)

        val isAnyTrackerButtonDown = isMusicTrackerButtonDown || isPlaybackTrackerButtonDown

        val isDraggingButtonDown = button == Input.Buttons.LEFT
        val isCopying = isDraggingButtonDown && alt
        val isResponsing = isDraggingButtonDown && alt && control

        if (clickOccupation != ClickOccupation.None || remix.playState != PlayState.STOPPED)
            return false

        if (stage.centreAreaStage.isMouseOver() && stage.centreAreaStage.visible) {
            val tool = currentTool
            if (tool == Tool.SELECTION) {
                val firstEntityInMouse: Entity? = remix.entities.firstOrNull { mouseVector in it.bounds }
                if (button == Input.Buttons.RIGHT && firstEntityInMouse != null && firstEntityInMouse is IEditableText) {
                    if (firstEntityInMouse in this.selection) {
                        val field = stage.entityTextField
                        field.visible = true
                        field.text = firstEntityInMouse.text
                        field.canInputNewlines = firstEntityInMouse.canInputNewlines
                        field.hasFocus = true
                        updateMessageLabel()
                    }
                } else if (isAnyTrackerButtonDown) {
                    clickOccupation = if (isMusicTrackerButtonDown) {
                        ClickOccupation.Music(this, button == Input.Buttons.MIDDLE)
                    } else {
                        ClickOccupation.Playback(this)
                    }
                } else if (isDraggingButtonDown) {
                    if (remix.entities.any { mouseVector in it.bounds && it.isSelected } && !(control && !alt)) {
                        val inBounds = this.selection
                        val newSel = if (isResponsing && inBounds.areAnyResponseCopyable()) {
                            inBounds.mapNotNull {
                                it as ModelEntity<*>
                                val datamodel = it.datamodel
                                datamodel as ResponseModel
                                if (datamodel.responseIDs.isNotEmpty()) {
                                    val id = datamodel.responseIDs.random()
                                    val entity = GameRegistry.data.objectMap[id]?.createEntity(remix, null) ?: error(
                                            "ID $id not found in game registry when making response copy")

                                    entity.updateBounds {
                                        entity.bounds.setPosition(it.bounds.x, it.bounds.y)
                                        if (entity is IStretchable && entity.isStretchable) {
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

                        val newClick = ClickOccupation.SelectionDrag(this, first, mouseOffset,
                                                                     false, isCopying, oldSel, stretchRegion)

                        if (isCopying) {
                            this.selection = newSel
                            remix.entities.addAll(newSel)
                        }

                        newSel.forEach {
                            it.updateInterpolation(true)
                        }

                        this.clickOccupation = newClick
                    } else {
                        val clickOccupation = clickOccupation
                        if (clickOccupation == ClickOccupation.None) {
                            // begin selection rectangle
                            val newClick = ClickOccupation.CreatingSelection(this, Vector2(mouseVector))
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
            } else if (tool == Tool.TIME_SIGNATURE) {
                val beat = Math.floor(remix.camera.getInputX().toDouble()).toInt()
                val timeSig: TimeSignature? = remix.timeSignatures.getTimeSignature(
                        beat.toFloat())?.takeIf { it.beat == beat }

                if (button == Input.Buttons.RIGHT && timeSig != null) {
                    remix.mutate(TimeSignatureAction(remix, timeSig, true))
                } else if (button == Input.Buttons.LEFT && timeSig == null) {
                    remix.mutate(
                            TimeSignatureAction(remix,
                                                TimeSignature(remix.timeSignatures, beat,
                                                              remix.timeSignatures.getTimeSignature(beat.toFloat())
                                                                      ?.divisions ?: 4), false))
                }
            } else if (tool.isTrackerRelated) {
                val tracker: Tracker<*>? = getTrackerOnMouse(tool.trackerClass!!.java, true)
                val mouseX = remix.camera.getInputX()
                val beat = MathHelper.snapToNearest(mouseX, snap)

                if (button == Input.Buttons.RIGHT && tracker != null) {
                    remix.mutate(TrackerAction(tracker, true))
                } else if (button == Input.Buttons.LEFT) {
                    if (tracker != null) {
                        val left = MathUtils.isEqual(tracker.beat, mouseX, snap * 0.5f)
                        val right = MathUtils.isEqual(tracker.endBeat, mouseX, snap * 0.5f)
                        if (left || right) {
                            clickOccupation = TrackerResize(tracker, mouseX - tracker.beat, left)
                        }
                    } else if (getTrackerOnMouse(tool.trackerClass.java, false) == null) {
                        val tr = when (tool) {
                            Tool.TEMPO_CHANGE -> {
                                TempoChange(remix.tempos, beat, 0f, remix.tempos.tempoAt(beat))
                            }
                            Tool.MUSIC_VOLUME -> {
                                MusicVolumeChange(remix.musicVolumes, beat,
                                                  0f,
                                                  Math.round(remix.musicVolumes.volumeAt(beat) * 100)
                                                          .coerceIn(0, MusicVolumeChange.MAX_VOLUME))
                            }
                            else -> error(
                                    "Tracker creation not supported for tool $tool")
                        }
                        val action: TrackerAction = TrackerAction(tr, false)
                        remix.mutate(action)
                    }
                }
            }
        } else if (stage.patternAreaStage.isMouseOver() && stage.patternAreaStage.visible
                && currentTool == Tool.SELECTION && isDraggingButtonDown) {
            // only for new
            val datamodel: Datamodel = if (pickerSelection.filter.areDatamodelsEmpty) return true else pickerSelection.filter.currentDatamodel
            val entity = datamodel.createEntity(remix, null)

            if (entity is ILoadsSounds) {
                entity.loadSounds()
            }

            if (Toolboks.debugMode) {
                entity.datamodel.game.objects.forEach { obj ->
                    if (obj is Cue) {
                        obj.loadSounds()
                    }
                }
            }

            val oldSelection = this.selection
            this.selection = listOf(entity)
            val selection = ClickOccupation.SelectionDrag(this, this.selection.first(), Vector2(0f, 0f),
                                                          true, false, oldSelection, StretchRegion.NONE)
            selection.setPositionRelativeToMouse()
            entity.updateInterpolation(true)

            remix.entities += entity

            this.clickOccupation = selection
        }

        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()
        val alt = Gdx.input.isAltDown()

        val clickOccupation = clickOccupation
        if (clickOccupation is ClickOccupation.Music &&
                (button == (if (clickOccupation.middleClick) Input.Buttons.MIDDLE else Input.Buttons.RIGHT))) {
            if (remix.musicStartSec != clickOccupation.old) {
                clickOccupation.final = remix.musicStartSec
                remix.addActionWithoutMutating(clickOccupation)
            }
            this.clickOccupation = ClickOccupation.None
            return true
        } else if (clickOccupation is ClickOccupation.Playback &&
                button == Input.Buttons.RIGHT) {
            if (remix.playbackStart != clickOccupation.old) {
                clickOccupation.final = remix.playbackStart
                remix.addActionWithoutMutating(clickOccupation)
            }
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
                    remix.addActionWithoutMutating(ActionGroup(listOf(
                            EntityPlaceAction(this, this.selection),
                            EntitySelectionAction(this, clickOccupation.previousSelection, this.selection)
                                                                     )))
                    if (clickOccupation.isNew && !clickOccupation.isCopy) {
                        updateRecentsList(
                                (this.selection.first { it is ModelEntity<*> } as ModelEntity<*>).datamodel.game)
                    }
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
                    remix.addActionWithoutMutating(ActionGroup(listOf(
                            EntityRemoveAction(this, this.selection, clickOccupation.oldBounds),
                            EntitySelectionAction(this, clickOccupation.previousSelection, listOf())
                                                                     )))
                    this.selection = listOf()
                } else {
                    // revert positions silently
                    clickOccupation.oldBounds.forEachIndexed { index, rect ->
                        val entity = selection[index]
                        entity.updateBounds {
                            entity.bounds.set(rect)
                        }
                    }
                }
            }

            this.clickOccupation = ClickOccupation.None
            remix.recomputeCachedData()
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
                val newCaptured: List<Entity> = remix.entities.filter { it.bounds.intersects(selectionRect) }
                val newSelection: List<Entity> =
                        if (shift && !control && !alt) {
                            this.selection.toList() + newCaptured
                        } else if (control && !shift && !alt) {
                            mutableListOf<Entity>().also { list ->
                                list.addAll(this.selection)
                                newCaptured.forEach {
                                    if (it in list) {
                                        list -= it
                                    } else {
                                        list += it
                                    }
                                }
                            }
                        } else {
                            newCaptured
                        }
                if (!this.selection.containsAll(newSelection) ||
                        (newSelection.size != this.selection.size)) {
                    remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
                }
            }

            this.clickOccupation = ClickOccupation.None
        } else if (clickOccupation is TrackerResize) {
            clickOccupation.normalizeWidth()
            if (button == Input.Buttons.LEFT && clickOccupation.isPlacementValid() &&
                    (clickOccupation.tracker.beat != clickOccupation.beat || clickOccupation.tracker.width != clickOccupation.width)) {
                val copy = clickOccupation.tracker.createResizeCopy(clickOccupation.beat, clickOccupation.width)
                remix.mutate(ActionGroup(
                        TrackerAction(clickOccupation.tracker, true),
                        TrackerAction(copy, false)
                                        ))
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
        if (remix.playState != PlayState.STOPPED) {
            return false
        }

        val selection = selection
        val tool = currentTool
        val control = Gdx.input.isControlDown()
        val shift = Gdx.input.isShiftDown()
        if (tool == Tool.SELECTION && selection.isNotEmpty() && !shift) {
            val repitchables = selection.filter { it is IRepitchable && it.canBeRepitched }
            val oldPitches = repitchables.map { (it as IRepitchable).semitone }

            val anyChanged = selection.fold(false) { acc, it ->
                if (it is IRepitchable && it.canBeRepitched) {
                    val current = it.semitone
                    val new = current + -amount * (if (control) 2 else 1)
                    if (new in it.range) {
                        it.semitone = new
                        return@fold true
                    }
                }
                acc
            }

            if (anyChanged) {
                val lastAction: EntityRepitchAction? = remix.getUndoStack().peekFirst() as? EntityRepitchAction?

                if (lastAction != null && lastAction.entities.containsAll(repitchables)) {
                    lastAction.reloadNewPitches()
                } else {
                    remix.addActionWithoutMutating(EntityRepitchAction(this, repitchables, oldPitches))
                }
            }

            return true
        } else if (tool == Tool.TIME_SIGNATURE && !shift) {
            val timeSig = remix.timeSignatures.getTimeSignature(remix.camera.getInputX())
            if (timeSig != null) {
                val change = -amount * (if (control) 5 else 1)
                val newDivisions = (timeSig.divisions + change)
                        .coerceIn(TimeSignature.LOWER_LIMIT, TimeSignature.UPPER_LIMIT)
                if ((change < 0 && timeSig.divisions > TimeSignature.LOWER_LIMIT) || (change > 0 && timeSig.divisions < TimeSignature.UPPER_LIMIT)) {
                    val lastAction: TimeSigValueChange? = remix.getUndoStack().peekFirst() as? TimeSigValueChange?
                    val result = TimeSignature(remix.timeSignatures, timeSig.beat, newDivisions)

                    if (lastAction != null && lastAction.current === timeSig) {
                        lastAction.current = result
                        lastAction.redo(remix)
                    } else {
                        remix.mutate(TimeSigValueChange(timeSig, result))
                    }
                    return true
                }
            }
        } else if (tool.isTrackerRelated) {
            val tracker = getTrackerOnMouse(tool.trackerClass?.java, true)
            if (tracker != null) {
                val result = tracker.scroll(-amount, control, shift)

                if (result != null) {
                    val lastAction: TrackerValueChange? = remix.getUndoStack().peekFirst() as? TrackerValueChange?

                    if (lastAction != null && lastAction.current === tracker) {
                        lastAction.current = result
                        lastAction.redo(remix)
                    } else {
                        remix.mutate(TrackerValueChange(tracker, result))
                    }
                }

                return true
            }
        } else if (tool == Tool.SFX_VOLUME) {
            val entity = getEntityOnMouse()
            if (entity != null && entity is IVolumetric && entity.isVolumetric) {
                val change = -amount * (if (control) 10 else 1)
                val oldVol = entity.volumePercent
                val newVol = (oldVol + change).coerceIn(entity.volumeRange)

                if (oldVol != newVol) {
                    val lastAction: EntityRevolumeAction? = remix.getUndoStack().peekFirst() as? EntityRevolumeAction?

                    entity.volumePercent = newVol

                    if (lastAction != null && entity in lastAction.entities) {
                        lastAction.reloadNewVolumes()
                    } else {
                        remix.addActionWithoutMutating(EntityRevolumeAction(this, listOf(entity), listOf(oldVol)))
                    }

                }
            }
        }


        if (shift) {
            // Camera scrolling left/right (CTRL/SHIFT+CTRL)
            val amt = (amount * if (control) 5f else 1f)
            camera.position.x += amt
            camera.update()

            return true
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
//        val file = autosaveFile ?: return
//        try {
//            val name = file.nameWithoutExtension() + "_CLOSE.${RHRE3.REMIX_FILE_EXTENSION}"
//            Toolboks.LOGGER.info("Attempting end of program save at $name")
//            Remix.saveTo(remix, file.sibling(name).file(), true)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
    }

    private fun resizeCameraToEntityScale(camera: OrthographicCamera) {
        camera.viewportWidth = RHRE3.WIDTH / ENTITY_WIDTH
        camera.viewportHeight = RHRE3.HEIGHT / ENTITY_HEIGHT
        camera.update()
    }

    private fun resizeCameraToPixelScale(camera: OrthographicCamera) {
        camera.viewportWidth = RHRE3.WIDTH.toFloat()
        camera.viewportHeight = RHRE3.HEIGHT.toFloat()
        camera.update()
    }

    fun resize() {
        resizeCameraToEntityScale(camera)
        resizeCameraToPixelScale(staticCamera)
        stage.updatePositions()
    }

    fun getDebugString(): String? {
//        val click = clickOccupation
        val range = getBeatRange()
        return "e: ${remix.entities.count {
            it.inRenderRange(range.start.toFloat(), range.endInclusive.toFloat())
        }} / ${remix.entities.size}\n" +
                "tr: ${remix.trackers.sumBy {
                    it.map.values.count { it.beat in range || it.endBeat in range }
                }} / ${remix.trackers.sumBy { it.map.values.size }}\n" +
                "pos: ♩${THREE_DECIMAL_PLACES_FORMATTER.format(remix.beat)} / ${THREE_DECIMAL_PLACES_FORMATTER.format(
                        remix.seconds)}\nbpm: ♩=${remix.tempos.tempoAtSeconds(
                        remix.seconds)}\nmvol: ${remix.musicVolumes.volumeAt(
                        remix.beat)}\nmidi: ${remix.midiInstruments}\nautosave: $timeUntilAutosave / $autosaveFrequency min"
    }
}