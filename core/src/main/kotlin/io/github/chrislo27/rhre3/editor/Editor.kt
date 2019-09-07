package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.SharedLibraryLoader
import com.fasterxml.jackson.core.JsonParseException
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.editor.CameraBehaviour.FOLLOW_PLAYBACK
import io.github.chrislo27.rhre3.editor.CameraBehaviour.PAN_OVER_SMOOTH
import io.github.chrislo27.rhre3.editor.ClickOccupation.TrackerResize
import io.github.chrislo27.rhre3.editor.action.*
import io.github.chrislo27.rhre3.editor.picker.PickerSelection
import io.github.chrislo27.rhre3.editor.rendering.*
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.EditorStage.DirtyType
import io.github.chrislo27.rhre3.editor.view.ViewType
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.entity.areAnyResponseCopyable
import io.github.chrislo27.rhre3.entity.model.*
import io.github.chrislo27.rhre3.entity.model.cue.CueEntity
import io.github.chrislo27.rhre3.entity.model.multipart.EquidistantEntity
import io.github.chrislo27.rhre3.entity.model.multipart.KeepTheBeatEntity
import io.github.chrislo27.rhre3.entity.model.special.PlayalongEntity
import io.github.chrislo27.rhre3.entity.model.special.ShakeEntity
import io.github.chrislo27.rhre3.entity.model.special.SubtitleEntity
import io.github.chrislo27.rhre3.entity.model.special.TextureEntity
import io.github.chrislo27.rhre3.midi.MidiHandler
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.oopsies.ActionGroup
import io.github.chrislo27.rhre3.patternstorage.ClipboardStoredPattern
import io.github.chrislo27.rhre3.patternstorage.StoredPattern
import io.github.chrislo27.rhre3.patternstorage.toEntityList
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.screen.*
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.GameGroup
import io.github.chrislo27.rhre3.sfxdb.GameMetadata
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.Cue
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.EditorRemix
import io.github.chrislo27.rhre3.track.GameSection
import io.github.chrislo27.rhre3.track.PlayState.PAUSED
import io.github.chrislo27.rhre3.track.PlayState.PLAYING
import io.github.chrislo27.rhre3.track.PlayState.STOPPED
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
import io.github.chrislo27.rhre3.util.RectanglePool
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.rhre3.util.scaleFont
import io.github.chrislo27.rhre3.util.unscaleFont
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.*


class Editor(val main: RHRE3Application, stageCamera: OrthographicCamera, attachMidiListeners: Boolean)
    : Disposable, InputProcessor {

    companion object {
        const val ENTITY_HEIGHT: Float = 48f
        const val ENTITY_WIDTH: Float = ENTITY_HEIGHT * 4

        const val ICON_SIZE: Float = 32f
        const val ICON_PADDING: Float = 6f
        const val ICON_COUNT_X: Int = 13
        const val ICON_COUNT_Y: Int = 4

        // Technically, there is only an absolute minimum of 1, these limits below are just for the UI
        val MIN_TRACK_COUNT: Int = 5
        val MAX_TRACK_COUNT: Int = 10
        val DEFAULT_TRACK_COUNT: Int = 5
        const val TRACK_LINE_THICKNESS: Float = 2f
        const val PATTERN_COUNT: Int = 5

        const val MESSAGE_BAR_HEIGHT: Int = 28
        const val BUTTON_SIZE: Float = 32f
        const val BUTTON_PADDING: Float = 4f

        const val SELECTION_BORDER: Float = 2f

        internal const val MSG_SEPARATOR = " - "
        internal const val NEGATIVE_SYMBOL = "-"
        internal const val ZERO_BEAT_SYMBOL = "♩"
        internal const val SELECTION_RECT_ADD = "+"
        internal const val SELECTION_RECT_INVERT = "±"
        private const val SONG_SUBTITLE_TRANSITION = 0.5f
        const val VOLUME_CHAR = "\uE13C" // Only works with built-in font

        val TRANSLUCENT_BLACK: Color = Color(0f, 0f, 0f, 0.5f)
        val ARROWS: List<String> = listOf("▲", "▼", "△", "▽", "➡")
        val SELECTED_TINT: Color = Color(0.65f, 1f, 1f, 1f)
        val CUE_PATTERN_COLOR: Color = Color(0.65f, 0.65f, 0.65f, 1f)

        internal val THREE_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.000", DecimalFormatSymbols())
        internal val TRACKER_TIME_FORMATTER = DecimalFormat("00.000", DecimalFormatSymbols())
        internal val TRACKER_MINUTES_FORMATTER = DecimalFormat("00", DecimalFormatSymbols())
        val ONE_DECIMAL_PLACE_FORMATTER = DecimalFormat("0.0", DecimalFormatSymbols())
        val TWO_DECIMAL_PLACES_FORMATTER = DecimalFormat("0.00", DecimalFormatSymbols())

        val DEFAULT_CAMERA_BEHAVIOUR: CameraBehaviour = FOLLOW_PLAYBACK
        var cameraBehaviour: CameraBehaviour = DEFAULT_CAMERA_BEHAVIOUR
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

    enum class ScrollMode(val msgLocalization: String, val buttonLocalization: String, val icon: String) {
        PITCH("editor.msg.repitch", "editor.scrollMode.pitch", "ui_icon_scroll_pitch"),
        VOLUME("editor.msg.changeVolume", "editor.scrollMode.volume", "ui_icon_sfx_volume");

        companion object {

            val VALUES: List<ScrollMode> = values().toList()

        }
    }

    private data class AutosaveState(val result: AutosaveResult, var time: Float)

    fun createRemix(addListeners: Boolean = true): Remix {
        return EditorRemix(main, this).apply {
            if (addListeners) {
                playStateListeners += { old, new ->
                    when (new) {
                        STOPPED -> {
                            resetAllSongSubtitles()
                            DiscordHelper.updatePresence(PresenceState.InEditor)
                            stage.patternPreviewButton.visible = editor.pickerSelection.filter != editor.stage.storedPatternsFilter
                            stage.patternPreviewButton.stop()
                        }
                        PAUSED -> {
                            DiscordHelper.updatePresence(PresenceState.InEditor)
                            stage.patternPreviewButton.visible = false
                            stage.patternPreviewButton.stop()
                        }
                        PLAYING -> {
                            stage.patternPreviewButton.visible = false
                            stage.patternPreviewButton.stop()

                            if (old == STOPPED) {
                                if (stage.tapalongStage.visible) {
                                    stage.tapalongStage.reset()
                                }
                                if (stage.playalongStage.visible) {
                                    stage.playalongStage.reset()
                                }
                            }

                            val durationSeconds = tempos.beatsToSeconds(lastPoint) - seconds
                            if (durationSeconds > 5f) {
                                if (midiInstruments > 0) {
                                    DiscordHelper.updatePresence(PresenceState.Elapsable.PlayingMidi(durationSeconds))
                                } else if (stage.playalongStage.visible) {
                                    DiscordHelper.updatePresence(PresenceState.PlayingAlong)
                                }
                            }

                        }
                    }
                }
                playStateListeners += { old, new ->
                    // Cannot use method reference due to initialization
                    stage.playalongStage.playStateListener(old, new)
                }
            }
        }
    }

    val camera: OrthographicCamera by lazy {
        OrthographicCamera().also { c ->
            resizeCameraToEntityScale(c)
            c.position.x = 0f
            c.update()
        }
    }
    val staticCamera: OrthographicCamera by lazy {
        OrthographicCamera().also { c ->
            resizeCameraToPixelScale(c)
            c.position.x = c.viewportWidth / 2f
            c.position.y = c.viewportHeight / 2f
            c.update()
        }
    }

    var cameraPan: CameraPan? = null

    val pickerSelection: PickerSelection = PickerSelection()
    var remix: Remix = createRemix()
        set(value) {
            field.dispose()
            field = value

            autosaveFile = null
            lastSaveFile = null
            cameraPan = null
            resetAutosaveTimer()
            selection = listOf()

            camera.position.x = field.playbackStart
            camera.update()

            field.entities.forEach { it.updateInterpolation(true) }

            resetAllSongSubtitles()
            stage.updateSelected(DirtyType.SEARCH_DIRTY)
            val wasPlayalongVisible = stage.playalongStage.visible
            stage.playalongStage.visible = false // Update playalong stage
            stage.playalongStage.visible = wasPlayalongVisible
        }
    var theme: Theme = LoadedThemes.currentTheme
    var scrollMode: ScrollMode = ScrollMode.PITCH
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
            field.set(camera.getInputX(), camera.getInputY())
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
    var cachedPlaybackStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""
    var cachedMusicStart: Pair<Float, String> = Float.POSITIVE_INFINITY to ""
    private val songSubtitles = mutableMapOf("songArtist" to TimedString("", SONG_SUBTITLE_TRANSITION, false),
                                             "songTitle" to TimedString("", SONG_SUBTITLE_TRANSITION, false))
    var songArtist: TimedString by songSubtitles
        private set
    var songTitle: TimedString by songSubtitles
        private set
    val stage: EditorStage = EditorStage(null, stageCamera, main, this)

    data class Particle(val color: Color, var x: Float, var y: Float, var veloX: Float, var veloY: Float, var accelX: Float, var accelY: Float, var width: Float, var height: Float, var expiry: Float)

    val particles: MutableList<Particle> = mutableListOf()

    data class MiningProgress(val entity: ModelEntity<*>, var progress: Float, var miningTime: Float, var timeSinceDigSound: Float = 100f)

    var miningProgress: MiningProgress? = null

    internal val buildingNotes = mutableMapOf<MidiHandler.MidiReceiver.Note, BuildingNote>()

    val glassEffect: GlassEffect = GlassEffect(main, this)

    fun resetAutosaveTimer() {
        autosaveFrequency = main.preferences.getInteger(PreferenceKeys.SETTINGS_AUTOSAVE,
                                                        InfoScreen.DEFAULT_AUTOSAVE_TIME)
                .coerceIn(InfoScreen.autosaveTimers.first(), InfoScreen.autosaveTimers.last())

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
        Localization.addListener {
            updateMessageLabel()

            cachedPlaybackStart = Float.POSITIVE_INFINITY to ""
            cachedMusicStart = Float.POSITIVE_INFINITY to ""
        }

        camera.position.y = calculateNormalCameraY()
        camera.update()

        if (attachMidiListeners) {
            MidiHandler.noteListeners += EditorMidiListener(this)
        }
    }

    fun toScaleX(float: Float): Float =
            (float / RHRE3.WIDTH) * camera.viewportWidth

    fun toScaleY(float: Float): Float =
            (float / RHRE3.HEIGHT) * camera.viewportHeight

    private fun setSubbeatSectionToMouse() {
        subbeatSection.enabled = true
        subbeatSection.start = floor(camera.getInputX().toDouble()).toFloat()
        subbeatSection.end = subbeatSection.start
    }

    fun getBeatRange(): IntRange =
            ((camera.position.x - camera.viewportWidth / 2 * camera.zoom) / toScaleX(
                    ENTITY_WIDTH)).roundToInt() - 4..(((camera.position.x + camera.viewportWidth / 2 * camera.zoom) / toScaleX(ENTITY_WIDTH)).roundToInt() + 4)

    fun getStretchRegionForStretchable(beat: Float, entity: Entity): StretchRegion {
        if (entity !is IStretchable)
            return StretchRegion.NONE

        if (!entity.isStretchable)
            return StretchRegion.NONE

        if (beat in entity.bounds.x..min(entity.bounds.x + IStretchable.STRETCH_AREA, entity.bounds.x + entity.bounds.width / 2f)) {
            return StretchRegion.LEFT
        }

        val right = entity.bounds.x + entity.bounds.width

        if (beat in max(right - IStretchable.STRETCH_AREA, right - entity.bounds.width / 2f)..right) {
            return StretchRegion.RIGHT
        }

        return StretchRegion.NONE
    }

    fun canStretchEntity(mouseVector: Vector2, it: Entity): Boolean {
        return it is IStretchable && it.isSelected &&
                mouseVector.y in it.bounds.y..it.bounds.y + it.bounds.height &&
                getStretchRegionForStretchable(mouseVector.x, it) != StretchRegion.NONE
    }

    fun getApparentPlaybackTrackerPos(): Float {
        return if (stage.playalongStage.visible) {
            remix.tempos.secondsToBeats(remix.seconds - (if (remix.playalong.allTouchScreen) {
                remix.playalong.calibratedMouseOffset
            } else remix.playalong.calibratedKeyOffset))
        } else remix.beat
    }

    private fun calculateNormalCameraY(): Float = 1f + (remix.trackCount - MIN_TRACK_COUNT) / 10f * 3.25f

    private fun calculatePlayalongCameraY(): Float = calculateNormalCameraY() // 4f + remix.trackCount

    fun render() = render(updateDelta = true, otherUI = true, noGlassEffect = false)

    /**
     * Pre-stage renderWithGlass.
     */
    fun render(updateDelta: Boolean, otherUI: Boolean, noGlassEffect: Boolean, disableThemeUsesMenu: Boolean = false) {
        val isGameBoundariesInViews = ViewType.GAME_BOUNDARIES in views
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        batch.begin()
        batch.setColor(1f, 1f, 1f, 1f)

        val themeUsesMenu = !disableThemeUsesMenu && main.preferences.getBoolean(PreferenceKeys.THEME_USES_MENU, false)
        val useGlassEffect = !noGlassEffect && glassEffect.fboSupported && main.preferences.getBoolean(PreferenceKeys.SETTINGS_GLASS_ENTITIES, true)
        if (themeUsesMenu && useGlassEffect) {
            glassEffect.renderBackground()
        }

        this.renderBackground(batch, main.shapeRenderer, main.defaultCamera, true, disableThemeUsesMenu)

        batch.end()

        val oldCameraX = camera.position.x
        val oldCameraY = camera.position.y
        val adjustedCameraX: Float
        val adjustedCameraY: Float
        val monsterGoal = remix.playalong.isMonsterGoalActive && stage.playalongStage.visible
        if (updateDelta) {
            val transitionTime = Gdx.graphics.deltaTime / 0.15f
            val cameraYNormal = calculateNormalCameraY()
            val cameraZoomNormal = (if (isGameBoundariesInViews) 1.5f else 1f) + (remix.trackCount - MIN_TRACK_COUNT) / 10f
            val cameraYPlayalong = calculatePlayalongCameraY()
            val cameraZoomPlayalong = if (monsterGoal) remix.playalong.getMonsterGoalCameraZoom() else 1f
            val isPlayalong = stage.playalongStage.visible
            val cameraY = if (!isPlayalong) cameraYNormal else cameraYPlayalong
            val cameraZoom = if (!isPlayalong) cameraZoomNormal else cameraZoomPlayalong
            camera.position.y = MathUtils.lerp(camera.position.y, cameraY, transitionTime)
            camera.zoom = MathUtils.lerp(camera.zoom, cameraZoom, transitionTime)

            val cameraPan = cameraPan
            if (cameraPan != null) {
                cameraPan.update(Gdx.graphics.deltaTime, camera)
                if (cameraPan.done) {
                    this.cameraPan = null
                }
            }

            if (remix.playState == PLAYING && remix.currentShakeEntities.isNotEmpty()) {
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

        val beatRange = getBeatRange()
        val beatRangeStartFloat = beatRange.first.toFloat()
        val beatRangeEndFloat = beatRange.last.toFloat()
        val font = main.defaultFont
        val trackYOffset = toScaleY(-TRACK_LINE_THICKNESS / 2f)
        val isPresentationMode = stage.presentationModeStage.visible

        font.scaleFont(camera)

        // horizontal track lines
        this.renderHorizontalTrackLines(batch, beatRangeStartFloat, beatRangeEndFloat - beatRangeStartFloat, trackYOffset)

        // game boundaries view (background)
        if (isGameBoundariesInViews) {
            this.renderGameBoundaryBg(batch, beatRange)
        }

        // entities
        val smoothDragging = main.preferences.getBoolean(PreferenceKeys.SETTINGS_SMOOTH_DRAGGING, true)
        remix.entities.forEach {
            if (it !is TextureEntity) {
                it.updateInterpolation(!smoothDragging)
            }
        }
        if (selection.isNotEmpty()) {
            val clickOccupation = clickOccupation
            if (clickOccupation is ClickOccupation.SelectionDrag) {
                val oldColor = batch.packedColor
                val rect = RectanglePool.obtain()
                rect.set(clickOccupation.lerpLeft, clickOccupation.lerpBottom, clickOccupation.lerpRight - clickOccupation.lerpLeft, clickOccupation.lerpTop - clickOccupation.lerpBottom)

                batch.color = theme.selection.selectionFill
                batch.fillRect(rect)
                batch.color = theme.selection.selectionBorder
                batch.drawRect(rect, toScaleX(SELECTION_BORDER), toScaleY(SELECTION_BORDER))

                batch.packedColor = oldColor
                RectanglePool.free(rect)
            }
        }

        if (themeUsesMenu && useGlassEffect) {
            main.shapeRenderer.projectionMatrix = camera.combined
            main.shapeRenderer.prepareStencilMask(batch) {
                begin(ShapeRenderer.ShapeType.Filled)
                remix.entities.forEach {
                    if (it is ModelEntity<*> && it !is TextureEntity && it.glassEffect) {
                        if (it.inRenderRange(beatRangeStartFloat, beatRangeEndFloat) && !(it is PlayalongEntity && stage.playalongStage.hideIndicators && remix.playState == PLAYING)) {
                            rect(it.bounds.x, it.bounds.y, it.bounds.width, it.bounds.height)
                        }
                    }
                }
                end()
            }.useStencilMask {
                batch.draw(glassEffect.fboRegion, camera.position.x - camera.viewportWidth / 2f * camera.zoom, camera.position.y - camera.viewportHeight / 2f * camera.zoom, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom)
            }
            main.shapeRenderer.projectionMatrix = main.defaultCamera.combined
        }
        remix.entities.forEach {
            if (it !is TextureEntity) {
                if (it is ModelEntity<*> && isPresentationMode && it.datamodel.hideInPresentationMode)
                    return@forEach
                if (it.inRenderRange(beatRangeStartFloat, beatRangeEndFloat) && !(it is PlayalongEntity && stage.playalongStage.hideIndicators && remix.playState == PLAYING)) {
                    if (it is ModelEntity<*>) {
                        it.renderWithGlass(this, batch, themeUsesMenu && useGlassEffect)
                        this.renderMining(batch, it)
                    } else {
                        it.render(this, batch)
                    }
                }
            }
        }

        // beat lines
        this.renderBeatLines(batch, beatRange, trackYOffset, updateDelta)

        // beat numbers
        this.renderBeatNumbers(batch, beatRange, font)

        // Texture entities get rendered here
        remix.entities.forEach {
            if (it is TextureEntity) {
                it.updateInterpolation(!smoothDragging)
                if (it.inRenderRange(beatRangeStartFloat, beatRangeEndFloat)) {
                    it.render(this, batch)
                    this.renderMining(batch, it)
                }
            }
        }

        // Stripe board in invalid positions
        if (selection.isNotEmpty()) {
            this.renderStripeBoard(batch, main.shapeRenderer)
        }

        // Playalong
        if (stage.playalongStage.visible) {
            val alpha = if (stage.playalongStage.hideIndicators) (if (remix.playState == STOPPED) 0.5f else 0f) else 1f
            this.renderPlayalong(batch, beatRange, alpha)
        }

        // waveform
        if (remix.playState == PLAYING && ViewType.WAVEFORM in views && otherUI) {
            this.renderWaveform(batch, oldCameraX, oldCameraY, adjustedCameraX, adjustedCameraY)
        }

        // game boundaries view (dividers)
        if (isGameBoundariesInViews) {
            this.renderGameBoundaryDividers(batch, beatRange, font)
        }

        this.renderTopTrackers(batch, beatRange, trackYOffset)

        // Play-Yan
        if (remix.metronome) {
            this.renderPlayYan(batch)
        }

        // time signatures
        this.renderTimeSignatures(batch, beatRange)

        // bottom trackers
        this.renderBottomTrackers(batch, beatRange)

        // renderWithGlass selection box, delete zone, sfx vol, ruler
        if (otherUI) {
            main.shapeRenderer.projectionMatrix = camera.combined
            this.renderOtherUI(batch, main.shapeRenderer, beatRange, font)
            main.shapeRenderer.projectionMatrix = main.defaultCamera.combined
        }

        if (monsterGoal) {
            main.shapeRenderer.projectionMatrix = camera.combined
            this.renderPlayalongMonsterGoal(batch, main.shapeRenderer)
            main.shapeRenderer.projectionMatrix = main.defaultCamera.combined
        }

        this.renderParticles(batch)

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

                val startX = (staticCamera.viewportWidth / 2f - renderWidth * remix.midiInstruments / 2f - renderWidth / 4f).coerceAtLeast(
                        0f)
                val startY = (stage.centreAreaStage.location.realY / Gdx.graphics.height) * staticCamera.viewportHeight

                val playingEntities = remix.entities.filter { it.playbackCompletion == PlaybackCompletion.PLAYING }

                for (i in 0 until remix.midiInstruments) {
                    val playingEntity: Entity? = playingEntities.find {
                        it is CueEntity && it.instrument == i + 1
                    }
                    val isPlaying = remix.playState != STOPPED && playingEntity != null
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
                               1 + (if (isPlaying) ((cellHeight + 2) * (if (isPlaying && playingEntity != null && playingEntity is IRepitchable && playingEntity.semitone > IRepitchable.DEFAULT_RANGE.last) 4 else 3)) else 0),
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
                    val y = (camera.viewportHeight * 0.365f * camera.zoom) - if (bottom) texHeight * 1.1f else 0f

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

            if (remix.tempos.secondsMap.isEmpty()) {
                val borderedFont = main.defaultBorderedFont
                borderedFont.scaleFont(staticCamera)
                borderedFont.scaleMul(1f)

                if (remix.playState != STOPPED) {
                    // This shouldn't be called since remixes aren't supposed to be playable without tempo changes
                    this.renderImplicitTempo(batch)
                }

                borderedFont.setColor(1f, 1f, 1f, 1f)

                val startX = 6f
                val startY = (stage.centreAreaStage.location.realY / Gdx.graphics.height) * staticCamera.viewportHeight + 6f
                val height = 32f
                val width = 32f

                borderedFont.drawCompressed(batch, Localization["editor.noTempo"],
                                            startX, startY + height * 0.5f + borderedFont.capHeight * 0.5f,
                                            staticCamera.viewportWidth - width, Align.center)

                borderedFont.unscaleFont()
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
        if (remix.playState == PLAYING) {
            songSubtitles.values.forEach { it.time += Gdx.graphics.deltaTime }
            if (RHRE3.midiRecording) {
                buildingNotes.values.forEach { bn ->
                    val ent = bn.entity
                    ent.updateBounds {
                        ent.bounds.width = remix.beat - ent.bounds.x
                    }
                }
            }
        } else if (remix.playState == STOPPED) {
            if (RHRE3.midiRecording && buildingNotes.isNotEmpty()) {
                buildingNotes.clear()
            }
        }

        val shift = Gdx.input.isShiftDown()
        val control = Gdx.input.isControlDown()
        val alt = Gdx.input.isAltDown()
        val cameraLeft = !stage.isTyping && (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
        val cameraRight = !stage.isTyping && (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
        val accelerateCamera = shift xor control
        val cameraDelta = toScaleX(ENTITY_WIDTH * 5 * Gdx.graphics.deltaTime * if (accelerateCamera) 5 else 1)
        val mouseVector = mouseVector

        subbeatSection.enabled = false

        if (!stage.isTyping) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                Gdx.input.inputProcessor.scrolled(-1)
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || (Gdx.input.isKeyJustPressed(Input.Keys.S) && !control)) {
                Gdx.input.inputProcessor.scrolled(1)
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (shift) {
                    when (remix.playState) {
                        STOPPED, PAUSED -> remix.playState = PLAYING
                        PLAYING -> remix.playState = PAUSED
                    }
                } else {
                    when (remix.playState) {
                        STOPPED, PAUSED -> remix.playState = PLAYING
                        PLAYING -> remix.playState = STOPPED
                    }
                }
            }
        }

        if (remix.playState == PLAYING) {
            val camBehav = cameraBehaviour
            if (stage.playalongStage.visible || camBehav == FOLLOW_PLAYBACK) {
                // Use linear time to prevent nauseation
                camera.position.x = remix.tempos.linearSecondsToBeats(remix.seconds - (if (stage.playalongStage.visible) remix.playalong.calibratedKeyOffset else 0f)) + camera.viewportWidth * 0.25f
            } else {
                val smooth = camBehav == PAN_OVER_SMOOTH
                val halfWidth = camera.viewportWidth / 2 * camera.zoom
                if (remix.beat !in camera.position.x - halfWidth..camera.position.x + halfWidth && cameraPan == null) {
                    val target = remix.beat + halfWidth
                    if (smooth) {
                        cameraPan = CameraPan(camera.position.x, target, 0.2f, Interpolation.exp10Out)
                    } else {
                        camera.position.x = target
                    }
                }
            }
        }

        if (currentTool == Tool.MULTIPART_SPLIT || currentTool.isTrackerRelated) {
            updateMessageLabel()
        }

        run stretchCursor@{
            val clickOccupation = clickOccupation
            val shouldStretch = remix.playState == STOPPED && currentTool == Tool.SELECTION &&
                    ((clickOccupation is ClickOccupation.SelectionDrag && clickOccupation.isStretching) ||
                            (clickOccupation == ClickOccupation.None && this.selection.isNotEmpty() && this.selection.all { it is IStretchable && it.isStretchable } && remix.entities.any {
                                canStretchEntity(mouseVector, it)
                            }))

            if (wasStretchCursor && !shouldStretch) {
                Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow)
                wasStretchCursor = shouldStretch
            } else if (!wasStretchCursor && shouldStretch) {
                Gdx.graphics.setCursor(AssetRegistry["cursor_horizontal_resize"])
                wasStretchCursor = shouldStretch
            }
        }

        if (remix.playState != STOPPED)
            return

        val autosaveFile = autosaveFile
        if (autosaveFrequency > 0 && autosaveFile != null) {
            timeUntilAutosave -= Gdx.graphics.deltaTime
            if (timeUntilAutosave <= 0) {
                autosaveFrequency = 0
                GlobalScope.launch {
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

        if (!stage.isTyping) {
            if (cameraLeft && !(shift && control)) {
                camera.position.x -= cameraDelta
                cameraPan = null
                camera.update()
            }
            if (cameraRight && !(shift && control)) {
                camera.position.x += cameraDelta
                cameraPan = null
                camera.update()
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
                cameraPan = CameraPan(camera.position.x, 0f, 0.25f, Interpolation.exp10Out)
                camera.update()
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
                cameraPan = CameraPan(camera.position.x, remix.getLastEntityPoint(), 0.25f, Interpolation.exp10Out)
                camera.update()
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                cameraPan = CameraPan(camera.position.x, remix.musicStartSec, 0.25f, Interpolation.exp10Out)
                camera.update()
            }

            if (clickOccupation == ClickOccupation.None) {
                if (control && !alt && !shift) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
                        main.screen = ScreenRegistry.getNonNull("newRemix")
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
                        val screen = ScreenRegistry.getNonNull("openRemix")
                        main.screen = screen
                        (screen as? OpenRemixScreen)?.attemptOpenPicker()
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                        main.screen = ScreenRegistry.getNonNull("saveRemix")
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        if (!shift && !alt) {
                            // Export screen
                            main.screen = ExportRemixScreen(main)
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
                        // Select following
                        val selectionMinX: Float = this.selection.minBy { it.bounds.x }?.bounds?.x ?: remix.playbackStart
                        val newSelection = remix.entities.toList().filter { it.bounds.x >= selectionMinX }
                        if (!this.selection.containsAll(newSelection) || (newSelection.size != this.selection.size)) {
                            remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
                            updateMessageLabel()
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                        // Select preceding
                        val selectionMaxX: Float = this.selection.maxBy { it.bounds.maxX }?.bounds?.maxX ?: remix.playbackStart
                        val newSelection = remix.entities.toList().filter { it.bounds.maxX <= selectionMaxX }
                        if (!this.selection.containsAll(newSelection) || (newSelection.size != this.selection.size)) {
                            remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
                            updateMessageLabel()
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
                        // Select between
                        val selectionMinX: Float = this.selection.minBy { it.bounds.x }?.bounds?.x ?: 0f
                        val selectionMaxX: Float = this.selection.maxBy { it.bounds.maxX }?.bounds?.maxX ?: 0f
                        val newSelection = remix.entities.toList().filter { it.bounds.x >= selectionMinX && it.bounds.maxX <= selectionMaxX }
                        if (!this.selection.containsAll(newSelection) || (newSelection.size != this.selection.size)) {
                            remix.mutate(EntitySelectionAction(this, this.selection, newSelection))
                            updateMessageLabel()
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
                        // Copy
                        if (this.selection.isNotEmpty()) {
                            Gdx.app.clipboard.contents = PatternStoreScreen.entitiesToJson(remix, selection.toList(), prettyPrinted = false)
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
                        // Cut
                        if (this.selection.isNotEmpty()) {
                            Gdx.app.clipboard.contents = PatternStoreScreen.entitiesToJson(remix, selection.toList(), prettyPrinted = false)

                            remix.entities.removeAll(this.selection)
                            remix.addActionWithoutMutating(ActionGroup(listOf(
                                    EntityRemoveAction(this, this.selection,
                                                       this.selection.map { Rectangle(it.bounds) }),
                                    EntitySelectionAction(this, this.selection.toList(), listOf())
                                                                             )))
                            this.selection = listOf()

                            updateMessageLabel()
                        }
                    } else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
                        // Jump to clipboard section in stored patterns
                        stage.selectInPicker(ClipboardStoredPattern)
                    }
                } else if (control && shift && !alt) {
                    if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                        // Select all
                        val newSelection = remix.entities.toList()
                        val oldSelection = selection.toList()
                        if (newSelection.size != oldSelection.size || !newSelection.containsAll(oldSelection)) {
                            remix.mutate(EntitySelectionAction(this, oldSelection, newSelection))
                            updateMessageLabel()
                        }
                    }
                }
            }
        }

        run clickCheck@{
            val clickOccupation = clickOccupation
            val tool = currentTool
            val nearestSnap = MathHelper.snapToNearest(camera.getInputX(), snap)
            if (tool == Tool.SELECTION) {
                when (clickOccupation) {
                    is ClickOccupation.Music -> {
                        setSubbeatSectionToMouse()
                        remix.musicStartSec = if (Gdx.input.isShiftDown()) camera.getInputX() else nearestSnap
                    }
                    is ClickOccupation.Playback -> {
                        setSubbeatSectionToMouse()
                        remix.playbackStart = nearestSnap
                    }
                    is ClickOccupation.SelectionDrag -> {
                        if (clickOccupation.isStretching) {
                            val rootEntity = clickOccupation.clickedOn
                            val rootBound = clickOccupation.oldBounds.getValue(rootEntity)

                            fun stretch(entity: Entity) {
                                val oldBound = clickOccupation.oldBounds.getValue(entity)
                                entity.updateBounds {
                                    if (clickOccupation.stretchType == StretchRegion.LEFT) {
                                        val oldRightSide = oldBound.x + oldBound.width

                                        entity.bounds.x = (nearestSnap - (rootBound.x - oldBound.x)).coerceAtMost(oldRightSide - IStretchable.MIN_STRETCH)
                                        entity.bounds.width = oldRightSide - entity.bounds.x
                                    } else if (clickOccupation.stretchType == StretchRegion.RIGHT) {
                                        entity.bounds.width = (nearestSnap - oldBound.x - (rootBound.maxX - oldBound.maxX)).coerceAtLeast(
                                                IStretchable.MIN_STRETCH)
                                    }
                                }
                            }

                            stretch(rootEntity)
                            this.selection.forEach { entity ->
                                if (entity === rootEntity) return@forEach
                                stretch(entity)
                            }
                        } else {
                            clickOccupation.setPositionRelativeToMouse()
                        }

                        subbeatSection.enabled = true
                        subbeatSection.start = floor(clickOccupation.left.toDouble()).toFloat()
                        subbeatSection.end = clickOccupation.right

                        updateMessageLabel()
                    }
                    is ClickOccupation.CreatingSelection -> {
                        clickOccupation.updateRectangle()
                        updateMessageLabel()
                    }
                    ClickOccupation.None -> {
                        if (selection.isNotEmpty() && !stage.isTyping) {
                            if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) || Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
                                remix.entities.removeAll(this.selection)
                                selection.filterIsInstance<ModelEntity<*>>().forEach { explodeEntity(it) }
                                remix.addActionWithoutMutating(ActionGroup(listOf(
                                        EntityRemoveAction(this, this.selection,
                                                           this.selection.map { Rectangle(it.bounds) }),
                                        EntitySelectionAction(this, this.selection.toList(), listOf())
                                                                                 )))
                                this.selection = listOf()

                                updateMessageLabel()
                            } else if (Gdx.input.isKeyJustPressed(Input.Keys.INSERT) || Gdx.input.isKeyJustPressed(Input.Keys.P)) {
                                main.screen = PatternStoreScreen(main, this, null, selection.toList())
                            }
                        }

                        if (Gdx.input.isKeyJustPressed(Input.Keys.TAB)) {
                            if (!control && !alt && !shift) {
                                cycleScrollMode(if (shift) -1 else 1)
                                updateMessageLabel()
                            }
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
            }
        }

        if (currentTool.showSubbeatLines) {
            subbeatSection.enabled = true
            subbeatSection.start = floor(camera.getInputX().toDouble()).toFloat()
            subbeatSection.end = subbeatSection.start + 0.5f
        }

        if (currentTool == Tool.PICKAXE) {
            val onEntity = getEntityOnMouse() as? ModelEntity<*>?
            if (onEntity != null && Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                val mining = miningProgress
                if (mining != null && mining.entity != onEntity) {
                    miningProgress = null
                }
                val current = miningProgress ?: MiningProgress(onEntity, 0f, MathUtils.lerp(0.15f, 0.9f, onEntity.bounds.area() / 2f).coerceIn(0.05f, 0.9f)).apply {
                    miningProgress = this
                }

                current.progress += Gdx.graphics.deltaTime / current.miningTime
                if (current.timeSinceDigSound >= 1f / 5f) {
                    current.timeSinceDigSound = 0f
                    AssetRegistry.get<LazySound>("""pickaxe_dig${MathUtils.random(1, 6)}""").sound.play(0.75f, 0.5f, 0f)
                } else {
                    current.timeSinceDigSound += Gdx.graphics.deltaTime
                }

                if (current.progress >= 1f) {
                    val themeUsesMenu = main.preferences.getBoolean(PreferenceKeys.THEME_USES_MENU, false)
                    val useGlassEffect = glassEffect.fboSupported && main.preferences.getBoolean(PreferenceKeys.SETTINGS_GLASS_ENTITIES, true)
                    explodeEntity(onEntity, true)
                    AssetRegistry.get<LazySound>("""pickaxe_destroy_${if (themeUsesMenu && useGlassEffect) "glass${MathUtils.random(1, 3)}" else "stone${MathUtils.random(1, 4)}"}""").sound.play()
                    remix.mutate(EntityRemoveAction(this, listOf(onEntity), listOf(Rectangle(onEntity.bounds))))
                    this.selection = this.selection - listOf(onEntity)
                }
            } else {
                miningProgress = null
            }
        } else {
            miningProgress = null
        }

        // undo/redo
        if (control && clickOccupation == ClickOccupation.None) {
            if (remix.canRedo() &&
                    (Gdx.input.isKeyJustPressed(Input.Keys.Y) ||
                            (shift && Gdx.input.isKeyJustPressed(Input.Keys.Z)))) {
                remix.redo()
                updateMessageLabel()
            } else if (remix.canUndo() && Gdx.input.isKeyJustPressed(Input.Keys.Z) && !shift) {
                remix.undo()
                updateMessageLabel()
            }
        }

    }

    fun updateMessageLabel() {
        val messageLabel = stage.messageLabel
        val controlsLabel = stage.controlsLabel
        val msgBuilder = StringBuilder()
        val ctrlBuilder = StringBuilder()
        val clickOccupation = clickOccupation
        val scrollMode = scrollMode

        fun StringBuilder.separator(): StringBuilder {
            if (this.isNotEmpty())
                this.append(MSG_SEPARATOR)
            return this
        }

        @Suppress("CascadeIf")
        if (stage.tapalongStage.visible) {
            msgBuilder.append(Localization["editor.tapalong.info"])
        } else if (stage.playalongStage.visible) {
            msgBuilder.append(Localization["editor.playalong.info"])
            ctrlBuilder.append(Playalong.playalongControls.toInputString())
        } else {
            when (currentTool) {
                Tool.SELECTION -> {
                    val currentGame: Game? = if (pickerSelection.filter.areGamesEmpty) null else pickerSelection.filter.currentGame
                    if (selection.isNotEmpty()) {
                        msgBuilder.append(
                                Localization["editor.msg.numSelected", this.selection.size.toString()])

                        if (clickOccupation == ClickOccupation.None) {
                            msgBuilder.separator().append(Localization["editor.msg.changeScrollMode"])

                            when (scrollMode) {
                                Editor.ScrollMode.PITCH -> {
                                    if (selection.any { it is IRepitchable && it.canBeRepitched }) {
                                        msgBuilder.separator().append(
                                                Localization[scrollMode.msgLocalization, Localization["editor.msg.scroll"]])
                                    }
                                }
                                Editor.ScrollMode.VOLUME -> {
                                    if (selection.any { it is IVolumetric && it.isVolumetric }) {
                                        msgBuilder.separator().append(
                                                Localization[scrollMode.msgLocalization, Localization["editor.msg.scroll"]])
                                    }
                                }
                            }

                            if (selection.all(Entity::supportsCopying)) {
                                ctrlBuilder.separator().append(Localization["editor.msg.copyHint"])
                            }

                            if (selection.areAnyResponseCopyable()) {
                                ctrlBuilder.separator().append(
                                        Localization[if (SharedLibraryLoader.isMac) "editor.msg.callResponseHint.mac" else "editor.msg.callResponseHint"])
                            }

                            if (selection.size == 1) {
                                val first = selection.first()

                                if (first is IEditableText) {
                                    ctrlBuilder.separator().append(
                                            Localization[if (stage.entityTextField.visible) "editor.msg.editabletext.finish" else "editor.msg.editabletext.edit"])
                                } else if (first is TextureEntity) {
                                    ctrlBuilder.separator().append(Localization["editor.msg.textureentity"])
                                }
                            }
                            if (selection.isNotEmpty()) {
                                if (selection.all { it is IStretchable && it.isStretchable }) {
                                    val allEquidistant = selection.all { it is EquidistantEntity }
                                    val allKtB = selection.all { it is KeepTheBeatEntity }
                                    ctrlBuilder.separator().append(Localization[if (allEquidistant) "editor.msg.stretchable.equidistant" else if (allKtB) "editor.msg.stretchable.keepTheBeat" else "editor.msg.stretchable"])
                                }
                            }
                        } else if (clickOccupation is ClickOccupation.SelectionDrag) {
                            if (!clickOccupation.isPlacementValid()) {
                                if (clickOccupation.isInDeleteZone()) {
                                    msgBuilder.separator().append(Localization[if (pickerSelection.filter == stage.storedPatternsFilter && stage.pickerStage.isMouseOver() && !stage.patternAreaStage.isMouseOver()) "editor.msg.storingSelection" else "editor.msg.deletingSelection"])
                                } else {
                                    msgBuilder.separator().append(Localization["editor.msg.invalidPlacement"])
                                }
                            }
                        }
                    } else {
                        msgBuilder.append(currentGame?.let {
                            if (Toolboks.debugMode) {
                                it.name + " [LIGHT_GRAY](${it.id})[]"
                            } else {
                                it.name
                            }
                        } ?: Localization["editor.msg.noGame"])
                    }

                    if (clickOccupation is ClickOccupation.CreatingSelection) {
                        val selectionMode = getSelectionMode()
                        val newSelectionCount = remix.entities.count { selectionMode.wouldEntityBeIncluded(it, clickOccupation.rectangle, remix.entities, this.selection) } - (if (selectionMode == SelectionMode.ADD) (this.selection.size) else 0)
                        if (newSelectionCount > 0) {
                            ctrlBuilder.separator().append(Localization[if (selectionMode == SelectionMode.ADD) {
                                "editor.msg.selectionHint.count.add"
                            } else "editor.msg.selectionHint.count", newSelectionCount])
                        }
                        ctrlBuilder.separator().append(Localization["editor.msg.selectionHint"])
                    }
                }
                Tool.MULTIPART_SPLIT -> {
                    ctrlBuilder.append(Localization["editor.msg.multipartSplit"])
                    if (remix.playState == STOPPED) {
                        val multipart = getMultipartOnMouse()
                        if (multipart != null) {
                            if (!multipart.canSplitWithoutColliding()) {
                                msgBuilder.separator().append(Localization["editor.msg.cannotSplit"])
                            }
                        }
                    }
                }
                Tool.TIME_SIGNATURE -> {
                    ctrlBuilder.append(Localization["editor.msg.timeSignature"])
                }
                Tool.TEMPO_CHANGE -> {
                    ctrlBuilder.append(Localization["editor.msg.tempoChange"])
                    val tracker = getTrackerOnMouse(TempoChange::class.java, true) as? TempoChange
                    if (tracker != null) {
                        var totalWidth = ((tracker.container.map as NavigableMap).higherEntry(tracker.endBeat)?.value?.beat ?: remix.lastPoint) - tracker.beat
                        if (totalWidth < 0f) {
                            totalWidth = Float.POSITIVE_INFINITY
                        }
                        if (tracker.isZeroWidth) {
                            msgBuilder.append(Localization["editor.msg.tracker.info", tracker.text, totalWidth])
                        } else {
                            msgBuilder.append(Localization["editor.msg.tracker.info.stretch", TempoChange.getFormattedText(tracker.previousBpm), tracker.text, tracker.width, totalWidth])
                        }
                    }
                }
                Tool.MUSIC_VOLUME -> {
                    ctrlBuilder.append(Localization["editor.msg.musicVolume"])
                    val tracker = getTrackerOnMouse(MusicVolumeChange::class.java, true) as? MusicVolumeChange
                    if (tracker != null) {
                        var totalWidth = ((tracker.container.map as NavigableMap).higherEntry(tracker.endBeat)?.value?.beat ?: remix.lastPoint) - tracker.beat
                        if (totalWidth < 0f) {
                            totalWidth = Float.POSITIVE_INFINITY
                        }
                        if (tracker.isZeroWidth) {
                            msgBuilder.append(Localization["editor.msg.tracker.info", tracker.text, totalWidth])
                        } else {
                            msgBuilder.append(Localization["editor.msg.tracker.info.stretch", MusicVolumeChange.getFormattedText(tracker.previousVolume), tracker.text, tracker.width, totalWidth])
                        }
                    }
                }
                Tool.SWING -> {
                    ctrlBuilder.append(Localization["editor.msg.swing"])
                }
                Tool.RULER -> {
                    ctrlBuilder.append(Localization["editor.msg.rulerTool"])
                }
                Tool.PICKAXE -> {
                    msgBuilder.append("Mining away...")
                }
            }
        }

        messageLabel.text = msgBuilder.toString()
        controlsLabel.text = ctrlBuilder.toString()
    }

    fun getHoverText(): String {
        val output: MutableList<String> = mutableListOf()
        val entity = getEntityOnMouse()

        if (remix.playState == STOPPED && entity != null && clickOccupation == ClickOccupation.None && !stage.playalongStage.visible && currentTool != Tool.PICKAXE) {
            if (entity is ModelEntity<*> && entity.needsNameTooltip && entity.renderText.isNotEmpty()) {
                output += entity.renderText
            }
            if (main.advancedOptions && entity is SubtitleEntity && entity.subtitle.isNotBlank()) {
                val charCount = entity.subtitle.count { it != ' ' && it != '-' && it != '(' && it != ')' && it != '\n' }
                val duration = remix.tempos.linearBeatsToSeconds(entity.bounds.x + entity.bounds.width) - remix.tempos.linearBeatsToSeconds(entity.bounds.x)
                output += "${(charCount / duration).roundToInt()} CPS"
            }
            val inSelection = entity in selection
            if (inSelection) {
                if (scrollMode == ScrollMode.VOLUME) {
                    if (entity is IVolumetric && (entity.isVolumetric || entity.volumePercent != IVolumetric.DEFAULT_VOLUME)) {
                        output += Localization["editor.msg.volume", entity.volumePercent]
                    }
                } else if (scrollMode == ScrollMode.PITCH) {
                    if (entity is IRepitchable && (entity.canBeRepitched || entity.semitone != 0)) {
                        val semitoneText = (entity as? ModelEntity<*>)?.getTextForSemitone(entity.semitone) ?: Semitones.getSemitoneName(entity.semitone)
                        output += if (!entity.showPitchOnTooltip) {
                            semitoneText
                        } else {
                            Localization["editor.msg.pitch", semitoneText]
                        }
                    }
                }
            }
            val hoverText = entity.getHoverText(inSelection)
            if (hoverText != null && hoverText.isNotEmpty()) {
                output += hoverText
            }

            if (ModdingUtils.moddingToolsEnabled && entity is ModelEntity<*>) {
                val str = SFXDatabase.moddingMetadata.currentData.joinToStringFromData(entity.datamodel, entity)
                if (str.isNotEmpty()) {
                    output += str
                }
            }
        }

        if (Toolboks.debugMode) {
            val str = StringBuilder()

            if (entity != null) {
                str.append(entity::class.java.simpleName)
                if (entity is ModelEntity<*>) {
                    str.append('\n').append(entity.datamodel.id)
                }
            }

            if (str.isNotEmpty()) {
                output += "[LIGHT_GRAY]$str[]"
            }
        }

        return output.joinToString(separator = "\n")
    }

    fun getMultipartOnMouse(): MultipartEntity<*>? {
        return getEntityOnMouse() as? MultipartEntity<*>
    }

    fun getEntityOnMouse(): Entity? {
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

    fun cycleScrollMode(dir: Int): ScrollMode {
        val last = scrollMode
        val allValues = ScrollMode.VALUES
        val indexOfCurrent = allValues.indexOf(last)

        if (indexOfCurrent == -1) {
            scrollMode = allValues.first()
        } else if (dir > 0) {
            val nextIndex = indexOfCurrent + 1

            scrollMode = if (nextIndex >= allValues.size) {
                allValues.first()
            } else {
                allValues[nextIndex]
            }
        } else if (dir < 0) {
            val nextIndex = indexOfCurrent - 1

            scrollMode = if (nextIndex < 0) {
                allValues.last()
            } else {
                allValues[nextIndex]
            }
        }

        return last
    }

    fun getTrackerOnMouse(klass: Class<out Tracker<*>>?, obeyY: Boolean): Tracker<*>? {
        if (klass == null || (obeyY && camera.getInputY() > 0f))
            return null
        val mouseX = camera.getInputX()
        remix.trackers.forEach { container ->
            val result = container.map.values.firstOrNull {
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
        val isResponsing = isDraggingButtonDown && alt && (control || shift)

        if (clickOccupation != ClickOccupation.None || remix.playState != STOPPED)
            return false

        if (stage.centreAreaStage.isMouseOver() && stage.playalongStage.visible) {
            val firstEntityInMouse: Entity? = remix.entities.firstOrNull { mouseVector in it.bounds }
            if (isPlaybackTrackerButtonDown && firstEntityInMouse == null) {
                clickOccupation = ClickOccupation.Playback(this)
            }
        } else if (stage.centreAreaStage.isMouseOver() && stage.centreAreaStage.visible) {
            val tool = currentTool
            if (tool == Tool.SELECTION) {
                val firstEntityInMouse: Entity? = remix.entities.firstOrNull { mouseVector in it.bounds }
                if (button == Input.Buttons.RIGHT && firstEntityInMouse != null && selection.size == 1) {
                    if (firstEntityInMouse in this.selection) {
                        if (firstEntityInMouse is IEditableText) {
                            val field = stage.entityTextField
                            field.visible = true
                            field.text = firstEntityInMouse.text
                            field.canInputNewlines = firstEntityInMouse.canInputNewlines
                            field.hasFocus = true
                            updateMessageLabel()
                        } else if (firstEntityInMouse is TextureEntity) {
                            // Open file chooser
                            main.screen = TexEntChooserScreen(main, firstEntityInMouse)
                            updateMessageLabel()
                        }
                    }
                } else if (isMusicTrackerButtonDown && firstEntityInMouse != null && firstEntityInMouse is ModelEntity<*>) {
                    // Jump to that game in the picker
                    stage.selectInPicker(firstEntityInMouse.datamodel)
                } else if (isAnyTrackerButtonDown && firstEntityInMouse == null) {
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
                                    val entity = SFXDatabase.data.objectMap[id]?.createEntity(remix, null) ?: error(
                                            "ID $id not found in SFX database when making response copy")

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
                        val oldSel = this.selection.toList()
                        val clickedOn = getEntityOnMouse().takeIf { it in oldSel } ?: first
                        val mouseOffset = Vector2(camera.getInputX() - first.bounds.x,
                                                  camera.getInputY() - first.bounds.y)
                        val stretchRegion = if (newSel.isNotEmpty() && newSel.all { it is IStretchable && it.isStretchable } && !isCopying)
                            getStretchRegionForStretchable(camera.getInputX(), clickedOn) else StretchRegion.NONE

                        val newClick = ClickOccupation.SelectionDrag(this, first, clickedOn, mouseOffset,
                                                                     false, isCopying, oldSel, stretchRegion)

                        if (isCopying) {
                            this.selection = newSel
                            val notIn = newSel.filter { it !in remix.entities }
                            if (notIn.isNotEmpty()) remix.entities.addAll(notIn)
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
                val inputX = camera.getInputX()
                val inputBeat = floor(inputX.toDouble() / snap).toFloat() * snap
                val timeSig: TimeSignature? = remix.timeSignatures.getTimeSignature(inputBeat)?.takeIf { MathUtils.isEqual(inputBeat, it.beat) }

                if (button == Input.Buttons.RIGHT && timeSig != null) {
                    remix.mutate(TimeSignatureAction(remix, timeSig, true))
                } else if (button == Input.Buttons.LEFT && timeSig == null) {
                    val timeSigAt = remix.timeSignatures.getTimeSignature(inputBeat)
                    remix.mutate(TimeSignatureAction(remix,
                                                     TimeSignature(remix.timeSignatures, inputBeat,
                                                                   timeSigAt?.beatsPerMeasure ?: TimeSignature.DEFAULT_NOTE_UNIT,
                                                                   timeSigAt?.beatUnit ?: TimeSignature.DEFAULT_NOTE_UNIT), false))
                }
            } else if (tool.isTrackerRelated) {
                val tracker: Tracker<*>? = getTrackerOnMouse(tool.trackerClass!!.java, true)
                val mouseX = camera.getInputX()
                val beat = MathHelper.snapToNearest(mouseX, snap)

                if (button == Input.Buttons.RIGHT && tracker != null) {
                    remix.mutate(TrackerAction(tracker, true))
                } else if (button == Input.Buttons.LEFT) {
                    if (tracker != null && tracker.allowsResize) {
                        val left = MathUtils.isEqual(tracker.beat, mouseX, snap * 0.5f)
                        val right = MathUtils.isEqual(tracker.endBeat, mouseX, snap * 0.5f)
                        if (left || right) {
                            clickOccupation = TrackerResize(tracker, mouseX - tracker.beat, left)
                        }
                    } else if (getTrackerOnMouse(tool.trackerClass.java, false) == null) {
                        val tr = when (tool) {
                            Tool.TEMPO_CHANGE -> {
                                val tempoScale = if (shift && !alt) 0.5f else if (!shift && alt) 2f else 1f
                                TempoChange(remix.tempos, beat, (remix.tempos.tempoAt(beat) * tempoScale).coerceIn(TempoChange.MIN_TEMPO, TempoChange.MAX_TEMPO), remix.tempos.swingAt(beat), 0f)
                            }
                            Tool.MUSIC_VOLUME -> {
                                MusicVolumeChange(remix.musicVolumes, beat,
                                                  0f,
                                                  (remix.musicVolumes.volumeAt(beat) * 100).roundToInt()
                                                          .coerceIn(0, MusicVolumeChange.MAX_VOLUME))
                            }
                            else -> error("Tracker creation not supported for tool $tool")
                        }
                        val action: TrackerAction = TrackerAction(tr, false)
                        remix.mutate(action)
                    }
                }
            } else if (tool == Tool.RULER) {
                val clickOccupation = clickOccupation
                if (clickOccupation == ClickOccupation.None) {
                    if (button == Input.Buttons.LEFT) {
                        // Begin ruler
                        val newClick = ClickOccupation.RulerMeasuring(this, Vector2(MathHelper.snapToNearest(mouseVector.x, if (shift) 0f else snap), mouseVector.y))
                        this.clickOccupation = newClick
                    } else if (button == Input.Buttons.RIGHT) {
                        val firstEntityInMouse: ModelEntity<*>? = remix.entities.firstOrNull { mouseVector in it.bounds && it is ModelEntity<*> } as ModelEntity<*>?
                        firstEntityInMouse?.let {
                            val id = it.datamodel.id
                            Gdx.app.clipboard.contents = id
                            Toolboks.LOGGER.info("Copied [$id] to clipboard")
                        }
                    }
                }
            }
        } else if (stage.patternAreaStage.isMouseOver() && stage.patternAreaStage.visible
                && currentTool == Tool.SELECTION && isDraggingButtonDown) {
            // only for new/stored pattern
            val isStored = pickerSelection.filter == stage.storedPatternsFilter
            val entities: List<Entity>

            if (!isStored) {
                val datamodel: Datamodel = pickerSelection.filter.currentDatamodel ?: return true
                val entity = datamodel.createEntity(remix, null)

                if (Toolboks.debugMode) {
                    entity.datamodel.game.objects.forEach { obj ->
                        if (obj is Cue) {
                            obj.loadSounds()
                        }
                    }
                }

                entities = listOf(entity)
            } else {
                val pattern: StoredPattern = stage.storedPatternsFilter.currentPattern ?: return true
                entities = when (pattern) {
                    is ClipboardStoredPattern -> {
                        try {
                            pattern.toEntityList(remix)
                        } catch (jpe: JsonParseException) {
                            // The clipboard was not valid JSON at all
                            Toolboks.LOGGER.warn("Failed to paste from clipboard: ${jpe::class.java.canonicalName}")
                            listOf<Entity>()
                        } catch (cce: ClassCastException) {
                            // Expected ArrayNode, got something else
                            Toolboks.LOGGER.warn("Failed to paste from clipboard: ${cce::class.java.canonicalName}")
                            listOf<Entity>()
                        } catch (e: Exception) {
                            // Something else went wrong
                            Toolboks.LOGGER.warn("Failed to paste from clipboard: ${e::class.java.canonicalName}")
                            e.printStackTrace()
                            listOf<Entity>()
                        }
                    }
                    else -> {
                        try {
                            pattern.toEntityList(remix)
                        } catch (e: Exception) {
                            Toolboks.LOGGER.warn("Failed to instantiate stored pattern ${pattern.name}: ${e::class.java.canonicalName}")
                            e.printStackTrace()
                            listOf<Entity>()
                        }
                    }
                }
            }

            if (entities.isNotEmpty()) {
                entities.forEach { entity ->
                    if (entity is ILoadsSounds) {
                        entity.loadSounds()
                    }
                }

                val oldSelection = this.selection.toList()
                this.selection = entities.toList()
                val first = this.selection.first()
                val selection = ClickOccupation.SelectionDrag(this, first, first, Vector2(0f, 0f),
                                                              true, false, oldSelection, StretchRegion.NONE)
                selection.setPositionRelativeToMouse()
                entities.forEach {
                    it.updateInterpolation(true)
                }

                remix.entities.addAll(entities)

                this.clickOccupation = selection
            }
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
            val storing = deleting && pickerSelection.filter == stage.storedPatternsFilter && stage.pickerStage.isMouseOver() && !stage.patternAreaStage.isMouseOver()

            /*
             Placement = drag from picker or copy
             Movement  = moving existing entities

             Outcomes:

             New or copy:
             Correct placement -> results in a place+selection action
             Invalid placement -> remove, restore old selection

             Existing:
             Correct movement  -> results in a move action
             Invalid movement  -> return
             Delete movement   -> remove+selection action
             */

            fun storePattern() {
                main.screen = PatternStoreScreen(main, this, null, selection.toList())
            }

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
                    if (storing) {
                        storePattern()
                    }

                    // delete silently
                    remix.entities.removeAll(selection)
                    selection.filterIsInstance<ModelEntity<*>>().forEach { explodeEntity(it) }
                    // restore original selection
                    selection = clickOccupation.previousSelection
                }
            } else {
                if (validPlacement) {
                    // move action
                    val sel = this.selection.toList()
                    remix.addActionWithoutMutating(EntityMoveAction(this, sel, sel.map { clickOccupation.oldBounds.getValue(it) }))
                } else if (deleting && !storing) {
                    // remove+selection action
                    remix.entities.removeAll(this.selection)
                    selection.filterIsInstance<ModelEntity<*>>().forEach { explodeEntity(it) }
                    val sel = this.selection.toList()
                    remix.addActionWithoutMutating(ActionGroup(listOf(
                            EntityRemoveAction(this, this.selection, sel.map { clickOccupation.oldBounds.getValue(it) }),
                            EntitySelectionAction(this, clickOccupation.previousSelection, listOf())
                                                                     )))
                    this.selection = listOf()
                } else {
                    if (storing) {
                        storePattern()
                    }

                    // revert positions silently
                    clickOccupation.oldBounds.forEach { (entity, rect) ->
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
                val newSelection: List<Entity> = getSelectionMode().createNewSelection(remix.entities.toList(), this.selection.toList(), selectionRect)
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
        } else if (clickOccupation is ClickOccupation.RulerMeasuring && button == Input.Buttons.LEFT) {
            this.clickOccupation = ClickOccupation.None
        }

        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode in Input.Keys.NUM_0..Input.Keys.NUM_9 || keycode == Input.Keys.R) {
            if (stage.isTyping || clickOccupation != ClickOccupation.None || stage.tapalongStage.visible)
                return false
            val number = (if (keycode == Input.Keys.NUM_0) 10 else keycode - Input.Keys.NUM_0) - 1
            if (!Gdx.input.isControlDown() && !Gdx.input.isAltDown() && !Gdx.input.isShiftDown()) {
                if (keycode == Input.Keys.R) {
                    currentTool = Tool.RULER
                    stage.updateSelected()
                    return true
                }
                if (number in 0 until Tool.VALUES.size) {
                    currentTool = Tool.VALUES[number]
                    stage.updateSelected()
                    return true
                }
            }
        }

        return false
    }

    fun explodeRegion(rect: Rectangle, c: Color) {
        val color = c.cpy()
        val expiry = 4f
        val scale = 0.25f
        val numX = (rect.width / (scale / 4f)).roundToInt()
        for (x in 0..numX) {
            val numY = (rect.height / scale).roundToInt()
            for (y in 0..numY) {
                if (((x * numY + y) % 3) > 1) continue
                particles += Particle(color, rect.x + (x.toFloat() / numX) * rect.width, rect.y + (y.toFloat() / numY) * rect.height,
                                      MathUtils.random(0.125f, 1f) * MathUtils.randomSign(), MathUtils.random(2.5f, 5f),
                                      0f, -20f, scale / 4f, scale, expiry)
            }
        }
    }

    fun explodeEntity(e: ModelEntity<*>, doExplode: Boolean = main.advancedOptions && main.preferences.getBoolean(PreferenceKeys.ADVOPT_EXPLODING_ENTITIES, false)) {
        if (!doExplode) return
        val themeUsesMenu = main.preferences.getBoolean(PreferenceKeys.THEME_USES_MENU, false)
        val useGlassEffect = glassEffect.fboSupported && main.preferences.getBoolean(PreferenceKeys.SETTINGS_GLASS_ENTITIES, true)
        val alpha = if (themeUsesMenu && useGlassEffect) 0.5f else 1f
        val color = e.getRenderColor(this, theme).cpy().apply {
            a *= alpha
        }
        val borderC = color.cpy().apply {
            r = (r - 0.25f).coerceAtLeast(0f)
            g = (g - 0.25f).coerceAtLeast(0f)
            b = (b - 0.25f).coerceAtLeast(0f)
            // not affected by alpha multiplication
        }
        val expiry = 4f
        val scale = 0.25f
        val numX = (e.bounds.width / (scale / 4f)).roundToInt()
        for (x in 0..numX) {
            val numY = (e.bounds.height / scale).roundToInt()
            for (y in 0..numY) {
                if (((x * numY + y) % 3) > 1) continue
                val isBorder = x == 0 || x == numX || y == 0 || y == numY
                particles += Particle(if (isBorder) borderC else color, e.bounds.x + (x.toFloat() / numX) * e.bounds.width, e.bounds.y + (y.toFloat() / numY) * e.bounds.height,
                                      MathUtils.random(0.125f, 1f) * MathUtils.randomSign(), MathUtils.random(2.5f, 5f),
                                      0f, -20f, scale / 4f, scale, expiry)
            }
        }
    }

    fun getSelectionMode(): SelectionMode {
        return when {
            !Gdx.input.isAltDown() && !Gdx.input.isControlDown() && Gdx.input.isShiftDown() -> SelectionMode.ADD
            !Gdx.input.isAltDown() && Gdx.input.isControlDown() && !Gdx.input.isShiftDown() -> SelectionMode.INVERT
            else -> SelectionMode.REPLACE
        }
    }

    fun changePitchOfSelection(change: Int, delta: Boolean, canExceedLimits: Boolean, selection: List<Entity>) {
        val repitchables = selection.filter { it is IRepitchable && it.canBeRepitched }
        val oldPitches = repitchables.map { (it as IRepitchable).semitone }

        val anyChanged = selection.fold(false) { acc, it ->
            if (it is IRepitchable && it.canBeRepitched) {
                val current = it.semitone
                val new = if (delta) (current + change) else change
                if (!canExceedLimits) {
                    val semitoneRange = it.semitoneRange
                    if (it.rangeWrapsAround) {
                        it.semitone = semitoneRange.first + Math.floorMod(new - semitoneRange.first, semitoneRange.last - semitoneRange.first + 1)
                        if (it.semitone == current) acc else true
                    } else {
                        when {
                            new in semitoneRange -> {
                                it.semitone = new
                                true
                            }
                            semitoneRange.last in (current + 1)..(new - 1) -> {
                                it.semitone = semitoneRange.last
                                true
                            }
                            semitoneRange.first in (new + 1)..(current - 1) -> {
                                it.semitone = semitoneRange.first
                                true
                            }
                            else -> acc
                        }
                    }
                } else {
                    it.semitone = new
                    true
                }
            } else acc
        }

        if (anyChanged) {
            val lastAction: EntityRepitchAction? = remix.getUndoStack().peekFirst() as? EntityRepitchAction

            if (lastAction != null && lastAction.entities.containsAll(repitchables)) {
                lastAction.reloadNewPitches()
            } else {
                remix.addActionWithoutMutating(EntityRepitchAction(this, repitchables, oldPitches))
            }
        }
    }

    override fun scrolled(amount: Int): Boolean {
        if (remix.playState != STOPPED) {
            return false
        }

        val selection = selection
        val tool = currentTool
        val control = Gdx.input.isControlDown()
        val shift = Gdx.input.isShiftDown()
        if (tool == Tool.SELECTION && selection.isNotEmpty() && !shift) {
            when (scrollMode) {
                Editor.ScrollMode.PITCH -> {
                    changePitchOfSelection(-amount * (if (control) 2 else 1), true, false, selection)
                }
                Editor.ScrollMode.VOLUME -> {
                    val volumetrics = selection.filter { it is IVolumetric && it.isVolumetric }
                    val oldVolumes: List<Int> = volumetrics.map { (it as IVolumetric).volumePercent }
                    val changeAmount = -amount * (if (control) 25 else 5)

                    val anyChanged = selection.fold(false) { acc, it ->
                        if (it is IVolumetric && it.isVolumetric) {
                            val current = it.volumePercent
                            val new = current + changeAmount
                            when {
                                new in it.volumeRange -> {
                                    it.volumePercent = new
                                    true
                                }
                                it.volumeRange.last in (current + 1)..(new - 1) -> {
                                    it.volumePercent = it.volumeRange.last
                                    true
                                }
                                it.volumeRange.first in (new + 1)..(current - 1) -> {
                                    it.volumePercent = it.volumeRange.first
                                    true
                                }
                                else -> acc
                            }
                        } else acc
                    }

                    if (anyChanged) {
                        val lastAction: EntityRevolumeAction? = remix.getUndoStack().peekFirst() as? EntityRevolumeAction

                        if (lastAction != null && lastAction.entities.containsAll(volumetrics)) {
                            lastAction.reloadNewVolumes()
                        } else {
                            remix.addActionWithoutMutating(EntityRevolumeAction(this, volumetrics, oldVolumes))
                        }
                    }
                }
            }

            return true
        } else if (tool == Tool.TIME_SIGNATURE) {
            val inputX = camera.getInputX()
            val timeSig = remix.timeSignatures.getTimeSignature(inputX)
            val inputBeat = floor(inputX.toDouble() / snap).toFloat() * snap
            if (timeSig != null && MathUtils.isEqual(inputBeat, timeSig.beat)) {
                if (!shift) {
                    val change = -amount * (if (control) 5 else 1)
                    val newDivisions = (timeSig.beatsPerMeasure + change)
                            .coerceIn(TimeSignature.LOWER_BEATS_PER_MEASURE, TimeSignature.UPPER_BEATS_PER_MEASURE)
                    if ((change < 0 && timeSig.beatsPerMeasure > TimeSignature.LOWER_BEATS_PER_MEASURE) || (change > 0 && timeSig.beatsPerMeasure < TimeSignature.UPPER_BEATS_PER_MEASURE)) {
                        val lastAction: TimeSigValueChange? = remix.getUndoStack().peekFirst() as? TimeSigValueChange?
                        val result = TimeSignature(remix.timeSignatures, timeSig.beat, newDivisions, timeSig.beatUnit)

                        if (lastAction != null && lastAction.current === timeSig) {
                            lastAction.current = result
                            lastAction.redo(remix)
                        } else {
                            remix.mutate(TimeSigValueChange(timeSig, result))
                        }
                        return true
                    }
                } else if (shift && !control) {
                    val change = -amount
                    val index = TimeSignature.NOTE_UNITS.indexOf(timeSig.beatUnit).takeUnless { it == -1 } ?: TimeSignature.NOTE_UNITS.indexOf(TimeSignature.DEFAULT_NOTE_UNIT)
                    val newUnits = TimeSignature.NOTE_UNITS[(index + change).coerceIn(0, TimeSignature.NOTE_UNITS.size - 1)]
                    if (newUnits != timeSig.beatUnit) {
                        val lastAction: TimeSigValueChange? = remix.getUndoStack().peekFirst() as? TimeSigValueChange?
                        val result = TimeSignature(remix.timeSignatures, timeSig.beat, timeSig.beatsPerMeasure, newUnits)

                        if (lastAction != null && lastAction.current === timeSig) {
                            lastAction.current = result
                            lastAction.redo(remix)
                        } else {
                            remix.mutate(TimeSigValueChange(timeSig, result))
                        }
                        return true
                    }
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
        } else if (tool == Tool.SWING) {
            val tracker = getTrackerOnMouse(TempoChange::class.java, true) as? TempoChange?
            if (tracker != null) {
                val result = tracker.scrollSwing(-amount, control, shift)

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
        }

        if (shift && tool != Tool.TIME_SIGNATURE) {
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

    fun getGamesUsedInRemix(): String {
        remix.recomputeCachedData()
        val games: List<GameGroup> = remix.gameSections.values.map(GameSection::gameGroup).distinct()
        return if (games.isEmpty()) "" else games.joinToString(transform = GameGroup::name)
    }

    fun getDebugString(): String? {
//        val click = clickOccupation
        val range = getBeatRange()
        val str = StringBuilder(100)
        val debugKey = Input.Keys.toString(Toolboks.DEBUG_KEY)
        val rangeStartF = range.first.toFloat()
        val rangeEndF = range.last.toFloat()
        val duration = remix.duration
        str.apply {
            append("Camera: [")
            append(THREE_DECIMAL_PLACES_FORMATTER.format(camera.position.x))
            append(", ")
            append(THREE_DECIMAL_PLACES_FORMATTER.format(camera.position.y))
            append(", ")
            append(THREE_DECIMAL_PLACES_FORMATTER.format(camera.zoom))
            append(" zoom]\n")
            append("  Pan: ")
            append(cameraPan)
            append("\n")

            append("Rendered objects (visible/total):\n  Entities: ")
            append(remix.entities.count {
                it.inRenderRange(rangeStartF, rangeEndF)
            })
            append(" / ")
            append(remix.entities.size).append("\n")

            append("  Trackers: ")
            append(remix.trackers.sumBy { container ->
                container.map.values.count { it.beat.roundToInt() in range || it.endBeat.roundToInt() in range }
            })
            append(" / ")
            append(remix.trackers.sumBy { it.map.values.size }).append("\n")

            append("Pos.: ♩")
            append(THREE_DECIMAL_PLACES_FORMATTER.format(remix.beat))
            append(" / ")
            append(THREE_DECIMAL_PLACES_FORMATTER.format(remix.seconds)).append("\n")

            append("Dur.: ♩")
            append(if (duration.isInfinite()) "$duration" else THREE_DECIMAL_PLACES_FORMATTER.format(duration))
            append(" / ")
            val signedSec = remix.tempos.beatsToSeconds(duration)
            val sec = abs(signedSec)
            val seconds = if (signedSec.isInfinite()) "$signedSec" else ((if (signedSec < 0) "-" else "") + Editor.TRACKER_MINUTES_FORMATTER.format((sec / 60).toLong()) + ":" + Editor.TRACKER_TIME_FORMATTER.format(sec % 60.0))
            append(seconds).append("\n")

            val timeSig = remix.timeSignatures.getTimeSignature(remix.beat)
            val timeSigStr = if (timeSig != null) ("${timeSig.beatsPerMeasure}/${timeSig.beatUnit} (${remix.timeSignatures.getMeasurePart(remix.beat)}, ${remix.timeSignatures.getMeasure(remix.beat)})") else "none"
            append("Time Sig.: ")
            append(timeSigStr)
            append("\n")

            append("Tempo: ♩=")
            append(remix.tempos.tempoAtSeconds(remix.seconds)).append("\n")

            append("  Swing: ")
            val swing = remix.tempos.swingAtSeconds(remix.seconds)
            append(swing.ratio).append("%, ").append(swing.division).append("\n")

            append("Music Vol.: ")
            append((remix.musicVolumes.volumeAt(remix.beat) * 100).roundToInt()).append("%\n")

            append("Track Count: ")
            append(remix.trackCount).append("\n")

            // metadata
            append("MIDI Instrs.: ")
            append(remix.midiInstruments).append("\n")

            append("Textures: ")
            append(remix.textureCache.size).append("\n")

            append("Autosave Timer: ")
            append(timeUntilAutosave)
            append(" sec / ")
            append(autosaveFrequency)
            append(" min\n")

            append("Modifier Keys: ")
            if (Gdx.input.isControlDown())
                append("[CTRL]")
            if (Gdx.input.isShiftDown())
                append("[SHIFT]")
            if (Gdx.input.isAltDown())
                append("[ALT]")
            append("\n")
        }

        return str.toString()
    }
}