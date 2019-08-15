package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.controllers.Controllers
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
import io.github.chrislo27.rhre3.editor.CameraBehaviour
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.init.DefaultAssetLoader
import io.github.chrislo27.rhre3.lc.LC
import io.github.chrislo27.rhre3.midi.MidiHandler
import io.github.chrislo27.rhre3.modding.ModdingGame
import io.github.chrislo27.rhre3.modding.ModdingUtils
import io.github.chrislo27.rhre3.news.ThumbnailFetcher
import io.github.chrislo27.rhre3.patternstorage.PatternStorage
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.screen.*
import io.github.chrislo27.rhre3.sfxdb.GameMetadata
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.stage.bg.Background
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Themes
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.rhre3.util.ReleaseObject
import io.github.chrislo27.rhre3.util.Semitones
import io.github.chrislo27.toolboks.ResizeAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.CloseListener
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.isAltDown
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Dsl.asyncHttpClient
import org.lwjgl.opengl.Display
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import kotlin.concurrent.thread


class RHRE3Application(logger: Logger, logToFile: File?)
    : ToolboksGame(logger, logToFile, RHRE3.VERSION, RHRE3.DEFAULT_SIZE, ResizeAction.KEEP_ASPECT_RATIO, RHRE3.MINIMUM_SIZE), CloseListener {

    companion object {
        lateinit var instance: RHRE3Application
            private set

        val httpClient: AsyncHttpClient = asyncHttpClient(DefaultAsyncHttpClientConfig.Builder()
                                                                  .setThreadFactory {
                                                                      Thread(it).apply {
                                                                          isDaemon = true
                                                                      }
                                                                  }
                                                                  .setFollowRedirect(true)
                                                                  .setCompressionEnforced(true))

        private const val RAINBOW_STR = "RAINBOW"

        init {
            Colors.put("X", Color.CLEAR)
            Colors.put("PICOSONG", Color.valueOf("26AB57"))
        }
    }

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"
    val timeSignatureFontKey = "time_signature"

    val defaultFontFTF: FreeTypeFont
        get() = fonts[defaultFontKey]
    val defaultBorderedFontFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontKey]
    val defaultFontLargeFTF: FreeTypeFont
        get() = fonts[defaultFontLargeKey]
    val defaultBorderedFontLargeFTF: FreeTypeFont
        get() = fonts[defaultBorderedFontLargeKey]
    val timeSignatureFontFTF: FreeTypeFont
        get() = fonts[timeSignatureFontKey]

    val defaultFontLarge: BitmapFont
        get() = defaultFontLargeFTF.font!!
    val defaultBorderedFontLarge: BitmapFont
        get() = defaultBorderedFontLargeFTF.font!!
    val timeSignatureFont: BitmapFont
        get() = timeSignatureFontFTF.font!!

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin_merged.ttf") }
    private val fontAfterLoadFunction: FreeTypeFont.() -> Unit = {
        this.font!!.apply {
            setFixedWidthGlyphs("1234567890")
            data.setLineHeight(lineHeight * 0.9f)
            setUseIntegerPositions(true)
            data.markupEnabled = true
            data.missingGlyph = data.getGlyph('☒')
        }
    }

    val uiPalette: UIPalette by lazy {
        UIPalette(defaultFontFTF, defaultFontLargeFTF, 1f,
                  Color(1f, 1f, 1f, 1f),
                  Color(0f, 0f, 0f, 0.75f),
                  Color(0.25f, 0.25f, 0.25f, 0.75f),
                  Color(0f, 0.5f, 0.5f, 0.75f))
    }

    lateinit var preferences: Preferences
        private set

    var versionTextWidth: Float = -1f
        private set

    @Volatile
    var githubVersion: Version = Version.RETRIEVING
        private set
    @Volatile
    var liveUsers: Int = -1
        private set

    var advancedOptions: Boolean = false
    private var lastWindowed: Pair<Int, Int> = RHRE3.DEFAULT_SIZE.copy()

    private val rainbowColor: Color = Color(1f, 1f, 1f, 1f)

    override val programLaunchArguments: List<String>
        get() = RHRE3.launchArguments

    override fun getTitle(): String =
            "${RHRE3.TITLE} $versionString"

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("${RHRE3.TITLE} $versionString is starting...")
        if (RHRE3.portableMode) {
            Toolboks.LOGGER.info("Running in portable mode")
        }
        // 1.8.0_144
        // 9.X.Y(extra)
        val javaVersion = System.getProperty("java.version").trim()
        Toolboks.LOGGER.info("Running on JRE $javaVersion")

        instance = this

        // localization stuff
        run {
            Localization.loadBundlesFromLangFile()
            Localization.logMissingLocalizations()
        }

        // font stuff
        run {
            fonts[defaultFontLargeKey] = createDefaultLargeFont()
            fonts[defaultBorderedFontLargeKey] = createDefaultLargeBorderedFont()
            fonts[timeSignatureFontKey] = FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter().apply {
                size *= 6
                characters = "0123456789?_+-!&%"
                incremental = false
            }).setAfterLoad {
                this.font!!.apply {
                    setFixedWidthGlyphs("0123456789")
                }
            }
            fonts.loadUnloaded(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
        }

        // preferences
        preferences = Gdx.app.getPreferences("RHRE3")

        AnalyticsHandler.initAndIdentify(Gdx.app.getPreferences("RHRE3-analytics"))
        GameMetadata.setPreferencesInstance(preferences)
        if (preferences.getString(PreferenceKeys.LAST_VERSION, null) != RHRE3.VERSION.toString()) {
            preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0).flush()
        }
        val backgroundPref = preferences.getString(PreferenceKeys.BACKGROUND, Background.defaultBackground.id)
        GenericStage.backgroundImpl = Background.backgroundMap[backgroundPref] ?: Background.defaultBackground
        advancedOptions = preferences.getBoolean(PreferenceKeys.SETTINGS_ADVANCED_OPTIONS, false)
        ModdingUtils.currentGame = ModdingGame.VALUES.find { it.id == preferences.getString(PreferenceKeys.ADVOPT_REF_RH_GAME, ModdingGame.DEFAULT_GAME.id) } ?: ModdingGame.DEFAULT_GAME
        LoadingIcon.usePaddlerAnimation = preferences.getBoolean(PreferenceKeys.PADDLER_LOADING_ICON, false)
        Semitones.pitchStyle = Semitones.PitchStyle.VALUES.find { it.name == preferences.getString(PreferenceKeys.ADVOPT_PITCH_STYLE, "") } ?: Semitones.pitchStyle
        val oldChaseCamera = "settings_chaseCamera"
        if (oldChaseCamera in preferences) {
            // Retroactively apply settings
            val oldSetting = preferences.getBoolean(oldChaseCamera, true)
            Editor.cameraBehaviour = if (oldSetting) CameraBehaviour.FOLLOW_PLAYBACK else CameraBehaviour.PAN_OVER_INSTANT
            // Delete
            preferences.remove(oldChaseCamera)
            preferences.flush()
        } else {
            Editor.cameraBehaviour = CameraBehaviour.MAP.getOrDefault(preferences.getString(PreferenceKeys.SETTINGS_CAMERA_BEHAVIOUR), Editor.DEFAULT_CAMERA_BEHAVIOUR)
        }
        Playalong.loadFromPrefs(preferences)
        Controllers.getControllers() // Initialize

        DiscordHelper.init(enabled = preferences.getBoolean(PreferenceKeys.SETTINGS_DISCORD_RPC_ENABLED, true))
        DiscordHelper.updatePresence(PresenceState.Loading)

        PatternStorage.load()

        // registry
        AssetRegistry.addAssetLoader(DefaultAssetLoader())

        // load themes
        LoadedThemes.reloadThemes(preferences, true)

        // MIDI input
        MidiHandler

        // screens
        run {
            ScreenRegistry += "assetLoad" to AssetRegistryLoadingScreen(this)

            fun addOtherScreens() {
                ScreenRegistry += "databaseUpdate" to GitUpdateScreen(this)
                ScreenRegistry += "sfxdbLoad" to SFXDBLoadingScreen(this)
                ScreenRegistry += "editor" to EditorScreen(this)
                ScreenRegistry += "musicSelect" to MusicSelectScreen(this)
                ScreenRegistry += "info" to InfoScreen(this)
                ScreenRegistry += "newRemix" to NewRemixScreen(this)
                ScreenRegistry += "saveRemix" to SaveRemixScreen(this)
                ScreenRegistry += "openRemix" to OpenRemixScreen(this)
                ScreenRegistry += "recoverRemix" to RecoverRemixScreen(this)
                ScreenRegistry += "editorVersion" to EditorVersionScreen(this)
                ScreenRegistry += "news" to NewsScreen(this)
                ScreenRegistry += "partners" to PartnersScreen(this)
                ScreenRegistry += "advancedOptions" to AdvancedOptionsScreen(this)
            }

            val nextScreenLambda: (() -> ToolboksScreen<*, *>?) = nextScreenLambda@{
                defaultCamera.viewportWidth = RHRE3.WIDTH.toFloat()
                defaultCamera.viewportHeight = RHRE3.HEIGHT.toFloat()
                defaultCamera.update()

                addOtherScreens()
                loadWindowSettings()
                val nextScreen = ScreenRegistry[if (RHRE3.skipGitScreen) "sfxdbLoad" else "databaseUpdate"]
//                if (preferences.getString(PreferenceKeys.LAST_VERSION, null) == null) {
//                    Gdx.net.openURI("https://rhre.readthedocs.io/en/latest/")
//                }
                return@nextScreenLambda nextScreen
            }
            setScreen(ScreenRegistry.getNonNullAsType<AssetRegistryLoadingScreen>("assetLoad")
                              .setNextScreen(nextScreenLambda))

            RemixRecovery.addSelfToShutdownHooks()
            Toolboks.LOGGER.info(
                    "Can recover last remix: ${RemixRecovery.canBeRecovered()}; Should recover: ${RemixRecovery.shouldBeRecovered()}")
        }

        thread(isDaemon = true, name = "Live User Count") {
            Thread.sleep(2500L)
            var failures = 0
            fun failed() {
                failures++
                this.liveUsers = -1
            }
            do {
                try {
                    val req = httpClient.prepareGet("https://zorldo.auroranet.me:10443/rhre3/live")
                            .addHeader("User-Agent", "RHRE ${RHRE3.VERSION}")
                            .addHeader("X-Analytics-ID", AnalyticsHandler.getUUID())
                            .addHeader("X-D-ID", DiscordHelper.currentUser?.userId ?: "null")
                            .addHeader("X-D-U", DiscordHelper.currentUser?.let { "${it.username}#${it.discriminator}" } ?: "null")
                            .execute().get()

                    if (req.statusCode == 200) {
                        val liveUsers = req.responseBody?.trim()?.toIntOrNull()
                        if (liveUsers != null) {
                            failures = 0
                            this.liveUsers = liveUsers.coerceAtLeast(0)
                        } else {
                            Toolboks.LOGGER.warn("Got no integer for return value (got ${req.responseBody})")
                            failed()
                        }
                    } else {
                        Toolboks.LOGGER.warn("Request status code is not 200, got ${req.statusCode}")
                        failed()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    failed()
                }

                Thread.sleep(60_000L * (failures + 1))
            } while (!Thread.interrupted() && !RHRE3.noOnlineCounter)

            if (RHRE3.noOnlineCounter) {
                this.liveUsers = 0
                Toolboks.LOGGER.info("No online counter by request from launch args")
            }
        }

        GlobalScope.launch {
            try {
                val nano = System.nanoTime()
                val obj = JsonHandler.fromJson<ReleaseObject>(
                        httpClient.prepareGet(RHRE3.RELEASE_API_URL).execute().get().responseBody)

                githubVersion = Version.fromStringOrNull(obj.tag_name!!) ?: Version.UNKNOWN
                Toolboks.LOGGER.info(
                        "Fetched editor version from GitHub in ${(System.nanoTime() - nano) / 1_000_000f} ms, is $githubVersion")

                val v = githubVersion
                if (!v.isUnknown) {
                    if (v > RHRE3.VERSION) {
                        preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE,
                                               preferences.getInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0) + 1)
                    } else {
                        preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        LC.all(this)
    }

    override fun exceptionHandler(t: Throwable) {
        val currentScreen = this.screen
        AnalyticsHandler.track("Render Crash", mapOf(
                "throwable" to t::class.java.canonicalName,
                "stackTrace" to StringWriter().apply {
                    val pw = PrintWriter(this)
                    t.printStackTrace(pw)
                    pw.flush()
                }.toString(),
                "currentScreen" to (currentScreen?.javaClass?.canonicalName ?: "null")
                                                    ))
        thread(start = true, isDaemon = true, name = "Crash Report Analytics Flusher") {
            AnalyticsHandler.flush()
        }
        if (currentScreen !is CrashScreen) {
            thread(start = true, isDaemon = true, name = "Crash Remix Recovery") {
                RemixRecovery.saveRemixInRecovery()
            }
            setScreen(CrashScreen(this, t, currentScreen))
        } else {
            super.exceptionHandler(t)
            Gdx.app.exit()
        }
    }

    override fun preRender() {
        rainbowColor.fromHsv(MathHelper.getSawtoothWave(2f) * 360f, 0.8f, 0.8f)
        Colors.put(RAINBOW_STR, rainbowColor)
        super.preRender()
    }

    override fun postRender() {
        val screen = screen
        if (screen !is HidesVersionText || !screen.hidesVersionText) {
            val font = defaultBorderedFont
            font.data.setScale(0.5f)

            if (!githubVersion.isUnknown && githubVersion > RHRE3.VERSION) {
                font.color = Color.ORANGE
            } else {
                font.setColor(1f, 1f, 1f, 1f)
            }

            val oldProj = batch.projectionMatrix
            batch.projectionMatrix = defaultCamera.combined
            batch.begin()
            val layout = font.draw(batch, RHRE3.VERSION.toString(),
                                   0f,
                                   (font.capHeight) + (2f / RHRE3.HEIGHT) * defaultCamera.viewportHeight,
                                   defaultCamera.viewportWidth, Align.right, false)
            versionTextWidth = layout.width
            batch.end()
            batch.projectionMatrix = oldProj
            font.setColor(1f, 1f, 1f, 1f)

            font.data.setScale(1f)
        }

        super.postRender()
    }

    override fun dispose() {
        super.dispose()
        preferences.putString(PreferenceKeys.LAST_VERSION, RHRE3.VERSION.toString())
        preferences.putString(PreferenceKeys.MIDI_NOTE, preferences.getString(PreferenceKeys.MIDI_NOTE, Remix.DEFAULT_MIDI_NOTE))
        preferences.putString(PreferenceKeys.PLAYALONG_CONTROLS, JsonHandler.toJson(Playalong.playalongControls))
        preferences.putString(PreferenceKeys.PLAYALONG_CONTROLLER_MAPPINGS, JsonHandler.toJson(Playalong.playalongControllerMappings))
        preferences.flush()
        try {
            SFXDatabase.dispose()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Themes.dispose()
        ThumbnailFetcher.dispose()
        persistWindowSettings()
        RHRE3.tmpMusic.emptyDirectory()
        BeadsSoundSystem.dispose()
        httpClient.close()
        AnalyticsHandler.track("Close Program",
                               mapOf("durationSeconds" to ((System.currentTimeMillis() - startTimeMillis) / 1000L)))
        AnalyticsHandler.dispose()
        MidiHandler.dispose()
    }

    override fun attemptClose(): Boolean {
        val screenRequestedStop = (screen as? CloseListener)?.attemptClose() == false
        return if (screenRequestedStop) {
            false
        } else {
            // Close warning only if the editor screen has been entered at least once and if the preferences say so
            if (EditorScreen.enteredEditor && preferences.getBoolean(PreferenceKeys.SETTINGS_CLOSE_WARNING, true) && this.screen !is CloseWarningScreen && this.screen !is CrashScreen) {
                Gdx.app.postRunnable {
                    setScreen(CloseWarningScreen(this, this.screen))
                }
                false
            } else {
                true
            }
        }
    }

    fun persistWindowSettings() {
        val isFullscreen = Gdx.graphics.isFullscreen
        if (isFullscreen) {
            preferences.putString(PreferenceKeys.WINDOW_STATE, "fs")
        } else {
            preferences.putString(PreferenceKeys.WINDOW_STATE,
                                  "${(Gdx.graphics.width / Display.getPixelScaleFactor()).toInt()}x${(Gdx.graphics.height / Display.getPixelScaleFactor()).toInt()}")
        }

        Toolboks.LOGGER.info("Persisting window settings as ${preferences.getString(PreferenceKeys.WINDOW_STATE)}")

        preferences.flush()
    }

    fun loadWindowSettings() {
        val str: String = preferences.getString(PreferenceKeys.WINDOW_STATE,
                                                "${RHRE3.WIDTH}x${RHRE3.HEIGHT}").toLowerCase(Locale.ROOT)
        if (str == "fs") {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
        } else {
            val width: Int
            val height: Int
            if (!str.matches("\\d+x\\d+".toRegex())) {
                width = RHRE3.WIDTH
                height = RHRE3.HEIGHT
            } else {
                width = str.substringBefore('x').toIntOrNull()?.coerceAtLeast(160) ?: RHRE3.WIDTH
                height = str.substringAfter('x').toIntOrNull()?.coerceAtLeast(90) ?: RHRE3.HEIGHT
            }

            Gdx.graphics.setWindowedMode(width, height)
        }
    }

    fun attemptFullscreen() {
        lastWindowed = Gdx.graphics.width to Gdx.graphics.height
        Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
    }

    fun attemptEndFullscreen() {
        val last = lastWindowed
        Gdx.graphics.setWindowedMode(last.first, last.second)
    }

    fun attemptResetWindow() {
        Gdx.graphics.setWindowedMode(RHRE3.DEFAULT_SIZE.first, RHRE3.DEFAULT_SIZE.second)
    }

    override fun keyDown(keycode: Int): Boolean {
        val res = super.keyDown(keycode)
        if (!res) {
            if (!Gdx.input.isControlDown() && !Gdx.input.isAltDown()) {
                if (keycode == Input.Keys.F11) {
                    if (!Gdx.input.isShiftDown()) {
                        if (Gdx.graphics.isFullscreen) {
                            attemptEndFullscreen()
                        } else {
                            attemptFullscreen()
                        }
                    } else {
                        attemptResetWindow()
                    }
                    persistWindowSettings()
                    return true
                }
            }
        }
        return res
    }

    private fun createDefaultTTFParameter(): FreeTypeFontGenerator.FreeTypeFontParameter {
        return FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            magFilter = Texture.TextureFilter.Nearest
            minFilter = Texture.TextureFilter.Linear
            genMipMaps = false
            incremental = true
            size = 24
            characters = ""
            hinting = FreeTypeFontGenerator.Hinting.AutoFull
        }
    }

    override fun createDefaultFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter())
                .setAfterLoad(fontAfterLoadFunction)
    }

    override fun createDefaultBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }

    private fun createDefaultLargeBorderedFont(): FreeTypeFont {
        return FreeTypeFont(fontFileHandle, emulatedSize, createDefaultTTFParameter()
                .apply {
                    borderWidth = 1.5f

                    size *= 4
                    borderWidth *= 4
                })
                .setAfterLoad(fontAfterLoadFunction)
    }
}