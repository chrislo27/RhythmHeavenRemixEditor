package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.tulskiy.musique.audio.Encoder
import com.tulskiy.musique.audio.formats.flac.FLACEncoder
import com.tulskiy.musique.audio.formats.ogg.VorbisEncoder
import de.sciss.jump3r.Main
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.screen.ExportRemixScreen.ExportFileType.FLAC
import io.github.chrislo27.rhre3.screen.ExportRemixScreen.ExportFileType.MP3
import io.github.chrislo27.rhre3.screen.ExportRemixScreen.ExportFileType.OGG_VORBIS
import io.github.chrislo27.rhre3.screen.ExportRemixScreen.ExportFileType.WAV
import io.github.chrislo27.rhre3.screen.UploadRemixScreen.Companion.MAX_PICOSONG_BYTES
import io.github.chrislo27.rhre3.screen.UploadRemixScreen.Companion.PICOSONG_AUP_URL
import io.github.chrislo27.rhre3.screen.UploadRemixScreen.Companion.PICOSONG_TOS_URL
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsMusic
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.*
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*
import io.github.chrislo27.toolboks.util.gdxutils.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.core.UGen
import net.beadsproject.beads.ugens.Clock
import net.beadsproject.beads.ugens.DelayTrigger
import net.beadsproject.beads.ugens.RangeLimiter
import net.beadsproject.beads.ugens.RecordToFile
import org.xiph.libvorbis.vorbis_comment
import java.io.File
import java.util.*
import javax.sound.sampled.AudioFileFormat
import javax.sound.sampled.AudioFormat
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


class ExportRemixScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, ExportRemixScreen>(main) {

    private val editorScreen: EditorScreen by lazy { ScreenRegistry.getNonNullAsType<EditorScreen>("editor") }
    private val editor: Editor
        get() = editorScreen.editor
    override val stage: GenericStage<ExportRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val remix: Remix
        get() = editor.remix

    @Volatile
    private var isChooserOpen = false
        set(value) {
            field = value
            setBackButtonEnabled()
        }
    @Volatile
    private var isExporting = false
        set(value) {
            field = value
            setBackButtonEnabled()
        }
    private var partial = false
    private var isCapableOfExporting = false
    private val mainLabel: TextLabel<ExportRemixScreen>
    private val picosongButton: Button<ExportRemixScreen>
    private var picosongFunc: (() -> UploadRemixScreen)? = null
    private val folderButton: Button<ExportRemixScreen>
    private var folderFile: File? = null
    private val readyButton: Button<ExportRemixScreen>
    private lateinit var uploadImmediatelyButton: Button<ExportRemixScreen>
    private val selectionStage: SelectionStage

    private enum class ExportFileType(val extension: String) {
        WAV("wav"), MP3("mp3"), OGG_VORBIS("ogg"), FLAC("flac");

        companion object {
            val VALUES: List<ExportFileType> by lazy { values().toList() }
            val EXTENSIONS: List<String> by lazy { VALUES.map(ExportFileType::extension) }
        }
    }

    private fun setBackButtonEnabled() {
        stage.backButton.enabled = !isChooserOpen && !isExporting
    }

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_export_big"))
        stage.titleLabel.text = "screen.export.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isChooserOpen && !isExporting) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        val palette = main.uiPalette

        stage.centreStage.elements += object : TextLabel<ExportRemixScreen>(palette, stage.centreStage,
                                                                            stage.centreStage) {
            override fun frameUpdate(screen: ExportRemixScreen) {
                super.frameUpdate(screen)
                this.visible = isChooserOpen
            }
        }.apply {
            this.location.set(screenHeight = 0.1f)
            this.textAlign = Align.center
            this.isLocalizationKey = true
            this.text = "closeChooser"
            this.visible = false
        }

        mainLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenHeight = 0.8f, screenY = 0.2f)
            this.textAlign = Align.center
            this.isLocalizationKey = false
            this.text = ""
        }
        stage.centreStage.elements += mainLabel

        selectionStage = SelectionStage(palette, stage.centreStage, stage.centreStage.camera).apply {
            this.location.set(screenHeight = 0.1f, screenY = 0.1f)

        }
        stage.centreStage.elements += selectionStage

        picosongButton = object : Button<ExportRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                picosongFunc?.let {
                    main.screen = it.invoke()
                }
                visible = false
                picosongFunc = null
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.export.picosong"
            })

            this.visible = false

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }
        stage.bottomStage.elements += picosongButton

        readyButton = object : Button<ExportRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                openPicker()
                updateLabels()

                this.visible = false
                uploadImmediatelyButton.visible = false
                selectionStage.visible = false
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.export.fileChooserTitle"
            })

            this.visible = false

            this.location.set(screenX = 0.225f, screenWidth = 0.55f)
        }
        stage.bottomStage.elements += readyButton

        uploadImmediatelyButton = object : Button<ExportRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                this.visible = false
                readyButton.visible = false
                selectionStage.visible = false

                GlobalScope.launch {
                    val exportFile = File.createTempFile("rhre3-quickupload-", ".mp3").apply {
                        deleteOnExit()
                    }
                    export(exportFile, MP3, false, exportOptions = ExportOptions.QUICKUPLOAD)

                    val fileSize = exportFile.length()

                    Gdx.app.postRunnable {
                        if (fileSize > MAX_PICOSONG_BYTES) {
                            this@ExportRemixScreen.stage.backButton.enabled = true
                            this@ExportRemixScreen.mainLabel.text = Localization["screen.export.uploadImmediately.cannot"]
                        } else {
                            main.screen = UploadRemixScreen(main, exportFile, exportFile.name, true)
                        }
                    }

                    // Analytics
                    AnalyticsHandler.track("Export Remix",
                                           mapOf(
                                                   "sfxDatabase" to GameRegistry.data.version,
                                                   "fileType" to MP3.extension,
                                                   "fileSize" to exportFile.length(),
                                                   "partial" to partial,
                                                   "uploadImmediately" to true
                                                ))
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.export.uploadImmediately"
            })

            this.visible = false

            this.location.set(screenX = 0.8f, screenWidth = 0.2f)
        }
        stage.bottomStage.elements += uploadImmediatelyButton

        folderButton = object : Button<ExportRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                val ff = folderFile
                if (ff != null) {
                    Gdx.net.openURI("file:///${(ff.takeUnless { it.isFile } ?: ff.parentFile).absolutePath}")
                }
            }
        }.apply {
            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
            })

            this.visible = false

            this.location.set(this@ExportRemixScreen.stage.backButton.location)
            this.location.set(screenX = 1f - this.location.screenWidth)
        }
        stage.bottomStage.elements += folderButton

        stage.updatePositions()
        updateLabels(null)
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            stage.onBackButtonClick()
        }
    }

    @Synchronized
    private fun export(file: File, fileType: ExportFileType, playSuccessDing: Boolean,
                       startSeconds: Float = selectionStage.percentToSeconds(selectionStage.startPercent),
                       endSeconds: Float = selectionStage.percentToSeconds(selectionStage.endPercent),
                       exportOptions: ExportOptions = RHRE3.exportOptions) {
        if (isExporting || !isCapableOfExporting)
            return
        isExporting = true
        BeadsSoundSystem.isRealtime = false

        fun addBead(function: () -> Unit): Bead {
            return object : Bead() {
                override fun messageReceived(message: Bead?) {
                    function()
                }
            }
        }

        val maxProgressStages: Int = when (fileType) {
            WAV -> 1
            MP3 -> 2
            OGG_VORBIS -> 2
            FLAC -> 2
        }

        fun updateProgress(localization: String, localPercent: Int, stage: Int) {
            mainLabel.text = Localization["screen.export.progress",
                    Localization["screen.export.progress.$localization"],
                    "$localPercent",
                    stage,
                    maxProgressStages,
                    "${Math.round((localPercent + (stage - 1) * 100f) / (maxProgressStages * 100f) * 100)}"]
        }

        // prepare
        val context = BeadsSoundSystem.audioContext
        BeadsSoundSystem.stop()
        context.stop()
        context.out.pause(true)
        context.out.clearDependents()

        // prep recorder
        val recorderFile = if (fileType != WAV)
            File.createTempFile("rhre3-export-tmp-${System.currentTimeMillis()}", ".wav").apply {
                this.deleteOnExit()
            } else file
        val deleteAfterFile: File? = if (fileType != WAV) recorderFile else null
        val recorder = RecordToFile(context, 2, recorderFile, AudioFileFormat.Type.WAVE)
        val limiter = RangeLimiter(context, 2)
        recorder.addInput(limiter)
        limiter.addInput(context.out)

        val oldStart = remix.playbackStart

        @Synchronized
        fun finalize(success: Boolean) {
            recorder.kill()
            limiter.kill()
            BeadsSoundSystem.stop()
            synchronized(context.out) {
                context.out.clearInputConnections()
                context.out.clearDependents()
                context.out.gain = 1f
                context.out.removeDependent(limiter)
            }
            remix.playbackStart = oldStart
            remix.playState = PlayState.STOPPED

            if (success) {
                val commentTag = "Made with Rhythm Heaven Remix Editor ${RHRE3.VERSION}"
                when (fileType) {
                    WAV -> {
                        // nothing
                    }
                    MP3 -> {
                        val preArgs = listOf("--ignore-tag-errors", "-b", exportOptions.bitrateKbps.toString(), "--resample", (exportOptions.sampleRate / 1000f).toString())
                        val args = (preArgs + (if (exportOptions.madeWithComment) listOf("--tc", commentTag) else listOf()) + listOf(recorderFile.path, file.path)).toTypedArray()
                        val main = Main()
                        main.support.addPropertyChangeListener("progress") { event ->
                            val percent: Int = (event.newValue as? Int) ?: 0
                            updateProgress("mp3", percent, 2)
                        }
                        main.run(args)
                    }
                    OGG_VORBIS, FLAC -> {
                        val pair: Pair<Encoder, String> = when (fileType) {
                            OGG_VORBIS -> VorbisEncoder() to "oggvorbis"
                            FLAC -> FLACEncoder() to "flac"
                            else -> error("Unsupported encoder for file type $fileType")
                        }
                        val encoder: Encoder = pair.first
                        val audioFormat = AudioFormat(context.sampleRate, 16, 2, true, false)
                        if (encoder is VorbisEncoder) {
                            if (!encoder.open(file, audioFormat, VorbisEncoder.DEFAULT_BITRATE, vorbis_comment().apply {
                                        if (exportOptions.madeWithComment) {
                                            vorbis_comment_add_tag("Comments", commentTag)
                                        }
                                    }))
                                error("Failed to open $fileType encoder")
                        } else {
                            if (!encoder.open(file, audioFormat))
                                error("Failed to open $fileType encoder")
                        }

                        val buffer: ByteArray = ByteArray(8192)
                        val stream = recorderFile.inputStream()
                        val fileSize = recorderFile.length()
                        var bytesRead = 0L
                        stream.skip(44L)
                        var bytes = stream.read(buffer)

                        fun updateLabel() {
                            updateProgress(pair.second, (bytesRead.toDouble() / fileSize * 100).roundToInt(), 2)
                        }

                        while (bytes >= 4) {
                            bytesRead += bytes
                            encoder.encode(buffer, bytes)
                            bytes = stream.read(buffer)

                            updateLabel()
                        }

                        bytesRead = fileSize
                        updateLabel()

                        stream.close()
                        encoder.close()
                    }
                }

                deleteAfterFile?.delete()
            }

            BeadsSoundSystem.isRealtime = true
            BeadsSoundSystem.resume()

            if (success) {
                (GameRegistry.data.objectMap["mrUpbeatWii/applause"] as? Cue)?.takeIf { playSuccessDing }
            } else {
                (GameRegistry.data.objectMap["mountainManeuver/toot"] as? Cue)
            }?.sound?.sound?.play(loop = false, pitch = 1f, rate = 1f, volume = 1f)
                    ?: (if (!playSuccessDing && success) Unit else Toolboks.LOGGER.warn("Export SFX (success=$success) not found"))

            if (playSuccessDing) {
                folderFile = file
                folderButton.visible = true
            }

            isExporting = false

            if (!playSuccessDing) {
                stage.backButton.enabled = false
            }
        }

        try {
            // prep triggers
            val startMs = Math.min(remix.musicStartSec.toDouble(), remix.tempos.beatsToSeconds(remix.entities.minBy { it.bounds.x }?.bounds?.x ?: 0.0f).toDouble()).toFloat() * 1000.0
            val endMs = endSeconds * 1000.0
            val durationMs = endMs - startMs

            // reset things
            remix.playbackStart = remix.tempos.secondsToBeats(startMs.toFloat() / 1000f)
            remix.playState = PlayState.PLAYING
            remix.playState = PlayState.STOPPED

            // music trigger
            if (remix.music != null) {
                val music = BeadsMusic((remix.music!!.music as BeadsMusic).audio)
                music.stop()

                val musicStartMs = (remix.musicStartSec * 1000) - startMs

                context.out.addDependent(DelayTrigger(context, musicStartMs, addBead {
                    music.also(BeadsMusic::play)
                }))

                // volumes
                context.out.addDependent(Clock(context, 10f).apply {
                    addMessageListener(addBead {
                        music.setVolume(remix.musicVolumes.volumeAt(remix.beat))
                    })
                })
            }

            if (startSeconds * 1000 <= startMs) {
                context.out.addDependent(recorder)
            } else {
                // Start recording at a certain time
                context.out.addDependent(DelayTrigger(context, startSeconds * 1000 - startMs, addBead {
                    context.out.addDependent(recorder)
                }))
            }

            // SFX timing
            // The clock resolution will change depending on the tempo in the remix at any point in time
            // The theoretical coarsest resolution should be 1/32 beats per clock cycle (0.03125) (based on snap divisions)
            // But, to be safe, the coarsest ideal resolution will be 1/50 beats per clock cycle
            // The computed ms interval value should be between 0.1 and 10 ms to ensure precision
            val clockTimerControl: UGen = object : UGen(context, 1) {
                override fun calculateBuffer() {
                    // NO-OP
                }

                override fun setValue(value: Float) {
                    // NO-OP, always computed
                }

                override fun getValueDouble(): Double {
                    // To compute:
                    // Find out how many ms are in a beat, based on the tempo.
                    val tempo = remix.tempos.tempoAtSeconds(remix.seconds)
                    val msPerBeat = (1.0 / tempo) * 60_000
                    // Target resolution is 1/50 beats per cycle, so divide by 50
                    return (msPerBeat / 50.0).coerceIn(0.1, 10.0)
                    // In theory, the 0.1 ms threshold will not be met. The maximum BPM is 600, which results in a timing of 2 ms.
                }

                override fun getValue(i: Int, j: Int): Float {
                    return getValueDouble(i, j).toFloat()
                }

                override fun getValue(): Float {
                    return valueDouble.toFloat()
                }

                override fun getValueDouble(i: Int, j: Int): Double {
                    return valueDouble
                }
            }
            context.out.addDependent(Clock(context, clockTimerControl).apply {
                addMessageListener(addBead {
                    remix.seconds = (context.time + startMs).toFloat() / 1000.0f
                    remix.entities.forEach {
                        remix.entityUpdate(it)
                    }
                })
            })
            // Update labels at 60 fps
            context.out.addDependent(Clock(context, 1000f / 60).apply {
                addMessageListener(addBead {
                    val percent = Math.round(context.time / (endMs - startMs) * 100).coerceIn(0, 100).toInt()
                    updateProgress("pcm", percent, 1)
                })
            })

            // scale gain
            context.out.gain = 0.5f

            // run!
            context.out.pause(false)
            context.runForNMillisecondsNonRealTime(durationMs.coerceAtLeast(1.0))
        } catch (e: Exception) {
            e.printStackTrace()
            finalize(false)
            throw e
        }

        try {
            finalize(true)
        } catch (e: Exception) {
            e.printStackTrace()
            finalize(false)
            throw e
        }
    }

    @Synchronized
    private fun openPicker() {
        if (!isChooserOpen && !isExporting && isCapableOfExporting) {
            GlobalScope.launch {
                isChooserOpen = true
                Gdx.app.postRunnable {
                    mainLabel.text = Localization["screen.export.uploadHint"]
                }
                val filters = listOf(FileChooserExtensionFilter(Localization["screen.export.fileFilter", "MP3, OGG, FLAC, WAV"], "*.mp3", "*.ogg", "*.flac", "*.wav"))
                FileChooser.saveFileChooser(Localization["screen.export.fileChooserTitle"], attemptRememberDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT) ?: getDefaultDirectory(), null, filters, filters.first()) { file ->
                    isChooserOpen = false
                    Gdx.app.postRunnable {
                        mainLabel.text = ""
                    }
                    if (file != null) {
                        val newInitialDirectory = if (!file.isDirectory) file.parentFile else file
                        persistDirectory(main, PreferenceKeys.FILE_CHOOSER_EXPORT, newInitialDirectory)
                        GlobalScope.launch {
                            DiscordHelper.updatePresence(PresenceState.Exporting)
                            try {
                                val correctFile = if (file.extension.toLowerCase(Locale.ROOT) !in ExportFileType.EXTENSIONS)
                                    file.parentFile.resolve("${file.name}.mp3")
                                else
                                    file
                                val fileType = ExportFileType.VALUES.firstOrNull {
                                    it.extension == file.extension.toLowerCase(Locale.ROOT)
                                } ?: MP3

                                export(correctFile, fileType, true)

                                val canUploadToPicosong = fileType == MP3 && correctFile.length() <= MAX_PICOSONG_BYTES

                                Gdx.app.postRunnable {
                                    mainLabel.text = Localization[
                                            if (fileType == MP3)
                                                (if (correctFile.length() <= MAX_PICOSONG_BYTES)
                                                    "screen.export.success.picosong.yes"
                                                else "screen.export.success.picosong.no")
                                            else "screen.export.success", PICOSONG_AUP_URL, PICOSONG_TOS_URL]

                                    picosongButton.visible = canUploadToPicosong
                                    picosongFunc = if (canUploadToPicosong) {
                                        val tempFile = File.createTempFile("rhre3exportupload", ".mp3").apply {
                                            deleteOnExit()
                                        }
                                        val originalName = correctFile.name
                                        correctFile.copyTo(tempFile, true);
                                        {
                                            UploadRemixScreen(main, tempFile, originalName, true)
                                        }
                                    } else {
                                        null
                                    }
                                }

                                // Analytics
                                AnalyticsHandler.track("Export Remix",
                                                       mapOf(
                                                               "sfxDatabase" to GameRegistry.data.version,
                                                               "fileType" to fileType.extension,
                                                               "fileSize" to correctFile.length(),
                                                               "partial" to partial,
                                                               "uploadImmediately" to false
                                                            ))
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                updateLabels(t)
                                isExporting = false
                            }
                        }
                    } else {
                        stage.onBackButtonClick()
                    }
                }
            }
        }
    }

    private fun updateLabels(throwable: Throwable? = null) {
        val label = mainLabel
        val hasEndRemix = remix.duration < Float.POSITIVE_INFINITY
        val canOmitEndRemix = main.preferences.getBoolean(PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST, false)
        readyButton.visible = false
        uploadImmediatelyButton.visible = false
        picosongButton.visible = false
        picosongFunc = null
        folderButton.visible = false
        folderFile = null
        selectionStage.visible = false
        isCapableOfExporting = hasEndRemix || canOmitEndRemix
        if (!isCapableOfExporting) {
            if (!hasEndRemix) {
                label.text = Localization["screen.export.cannot", Localization["screen.export.needsEndRemix"] + "\n[LIGHT_GRAY]${Localization[Series.OTHER.localization]} ➡ ${GameRegistry.data.specialGame.name} ➡ ${GameRegistry.data.objectMap[GameRegistry.END_REMIX_ENTITY_ID]?.name ?: "End Remix"}[]"]
            }
        } else {
            if (throwable == null) {
                label.text = "${Localization["screen.export.prepare"]}\n\n${Localization["screen.export.uploadHint"]}"
                readyButton.visible = true
                uploadImmediatelyButton.visible = true
                selectionStage.visible = true
            } else {
                label.text = Localization["screen.export.failed", throwable::class.java.canonicalName]
            }
        }
    }

    override fun show() {
        super.show()
        updateLabels()
    }

    override fun hide() {
        super.hide()
        BeadsSoundSystem.stop()
        picosongButton.visible = false
        picosongFunc = null
        folderButton.visible = false
        folderFile = null
    }

    override fun dispose() {
    }

    override fun tickUpdate() {
    }

    inner class SelectionStage(palette: UIPalette, parent: UIElement<ExportRemixScreen>?, camera: OrthographicCamera)
        : Stage<ExportRemixScreen>(parent, camera) {

        private val remix: Remix
            get() = editor.remix

        val leftLabel: TextLabel<ExportRemixScreen>
        val rightLabel: TextLabel<ExportRemixScreen>

        val remixStartSeconds: Float = Math.min(remix.musicStartSec.toDouble(), remix.tempos.beatsToSeconds(remix.entities.minBy { it.bounds.x }?.bounds?.x ?: 0.0f).toDouble()).toFloat()
        val remixEndSeconds: Float = remix.tempos.beatsToSeconds(if (remix.duration == Float.POSITIVE_INFINITY) remix.lastPoint else remix.duration)
        private val remixAbsDuration = remixEndSeconds - remixStartSeconds

        private val playbackStartSeconds = remix.tempos.beatsToSeconds(remix.playbackStart)
        var startPercent: Float = if (remixAbsDuration > 0f && playbackStartSeconds in remixStartSeconds..remixEndSeconds - 1f) (secondsToPercent(playbackStartSeconds)).coerceIn(0f, 1f) else 0f
        var endPercent: Float = 1f

        init {
            val timeLabelWidth = 0.2f

            leftLabel = TextLabel(palette, this, this.stage).apply {
                this.location.set(screenX = 0f, screenWidth = timeLabelWidth)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.right
            }
            rightLabel = TextLabel(palette, this, this.stage).apply {
                this.location.set(screenX = 1f - timeLabelWidth, screenWidth = timeLabelWidth)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.left
            }

            elements += leftLabel
            elements += rightLabel
            elements += TextLabel(palette, this, this.stage).apply {
                this.location.set(screenY = -1f)
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.export.durationControls"
                this.fontScaleMultiplier = 0.75f
            }

            elements += object : UIElement<ExportRemixScreen>(this, this) {
                val color = Color.valueOf("26AB57")
                override fun render(screen: ExportRemixScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                    val loc = this.location
                    val x = loc.realX
                    val y = loc.realY
                    val width = loc.realWidth
                    val height = loc.realHeight
                    val lineThickness = 4f
                    val selectionPercent = endPercent - startPercent

                    // background
                    batch.setColor(0f, 0f, 0f, 0.8f)
                    batch.fillRect(x, y, width, height)

                    // bar
                    val barFullWidth = width - lineThickness * 2f
                    val barWidth = barFullWidth * selectionPercent
                    val barX = x + lineThickness + barFullWidth * startPercent
                    batch.setColor(color.r, color.g, color.b, 1f)
                    batch.fillRect(barX, y, barWidth, height)
                    // bar darker outline
                    val arrowTex = AssetRegistry.get<Texture>("entity_stretchable_arrow")
                    val arrowHeight = height - lineThickness * 2f
                    val arrowheadWidth = Math.min(barWidth / 2f, height * 0.5f)
                    batch.setColor(0f, 0f, 0f, 0.2f)
                    batch.draw(arrowTex, barX + arrowheadWidth, y + lineThickness, barWidth - arrowheadWidth * 2, arrowHeight, arrowTex.width / 2, 0, arrowTex.width / 2, arrowTex.height, false, false)
                    batch.draw(arrowTex, barX, y + lineThickness, arrowheadWidth, arrowHeight, 0, 0, arrowTex.width / 2, arrowTex.height, false, false)
                    batch.draw(arrowTex, barX + barWidth, y + lineThickness, -arrowheadWidth, arrowHeight, 0, 0, arrowTex.width / 2, arrowTex.height, false, false)

                    // border
                    batch.setColor(1f, 1f, 1f, 1f)
                    batch.drawRect(x, y, width, height, lineThickness)

                    if (wasClickedOn) {
                        var inputPercent = ((stage.camera.getInputX() - x - lineThickness) / barFullWidth).coerceIn(0f, 1f)

                        if (Gdx.input.isShiftDown() && !Gdx.input.isControlDown() && !Gdx.input.isAltDown()) {
                            inputPercent = secondsToPercent(remix.tempos.beatsToSeconds(remix.tempos.secondsToBeats(percentToSeconds(inputPercent)).roundToInt().toFloat()).coerceIn(remixStartSeconds, remixEndSeconds)).coerceIn(0f, 1f)
                        }

                        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                            if (inputPercent < endPercent) {
                                startPercent = inputPercent
                                updateLabels()
                            }
                        } else if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
                            if (inputPercent > startPercent) {
                                endPercent = inputPercent
                                updateLabels()
                            }
                        }
                    }
                }

                override fun canBeClickedOn(): Boolean {
                    return true
                }

                override fun keyTyped(character: Char): Boolean {
                    val sup = super.keyTyped(character)
                    if (!sup) {
                        if (character == 'r') {
                            startPercent = 0f
                            endPercent = 1f
                            updateLabels()
                            return true
                        }
                    }

                    return sup
                }
            }.apply {
                this.location.set(screenX = timeLabelWidth, screenWidth = 1f - timeLabelWidth * 2)
            }

            updateLabels()
        }

        fun percentToSeconds(percent: Float): Float = MathUtils.lerp(remixStartSeconds, remixEndSeconds, percent)
        fun secondsToPercent(seconds: Float): Float = (seconds - remixStartSeconds) / (remixEndSeconds - remixStartSeconds)

        fun updateLabels() {
            val startSeconds = percentToSeconds(startPercent)
            val endSeconds = percentToSeconds(endPercent)
            leftLabel.text = "♩ ${remix.tempos.secondsToBeats(startSeconds).toInt()} (${secondsToText(startSeconds.toInt())}) "
            rightLabel.text = " ♩ ${remix.tempos.secondsToBeats(endSeconds).roundToInt()} (${secondsToText(endSeconds.roundToInt())})"
            partial = startPercent != 0f || endPercent != 1f
        }

        private fun secondsToText(seconds: Int): String {
            val abs = seconds.absoluteValue
            val sec = abs % 60
            return "${if (seconds < 0) "-" else ""}${abs / 60}:${if (sec < 10) "0" else ""}${Math.max(0, sec)}"
        }

    }
}