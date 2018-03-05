package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.init.DefaultAssetLoader
import io.github.chrislo27.rhre3.registry.GameMetadata
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.screen.*
import io.github.chrislo27.rhre3.soundsystem.SoundSystem
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Themes
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.rhre3.util.JavafxStub
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.rhre3.util.ReleaseObject
import io.github.chrislo27.toolboks.ResizeAction
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.i18n.NamedLocale
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.UIPalette
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.setHSB
import io.github.chrislo27.toolboks.version.Version
import javafx.application.Application
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl.asyncHttpClient
import org.lwjgl.opengl.Display
import java.util.*
import kotlin.concurrent.thread


class RHRE3Application(logger: Logger, logToFile: Boolean)
    : ToolboksGame(logger, logToFile, RHRE3.VERSION,
                   RHRE3.DEFAULT_SIZE, ResizeAction.KEEP_ASPECT_RATIO, RHRE3.MINIMUM_SIZE) {

    companion object {
        lateinit var languages: List<NamedLocale>
            private set

        val httpClient: AsyncHttpClient = asyncHttpClient()

        private const val RAINBOW_STR = "RAINBOW"

        init {
            Colors.put("X", Color.CLEAR)
            Colors.put("PICOSONG", Color.valueOf("26AB57"))
        }
    }

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"
    val timeSignatureFontKey = "time_signature"

    val defaultFontLarge: BitmapFont
        get() = fonts[defaultFontLargeKey].font!!
    val defaultBorderedFontLarge: BitmapFont
        get() = fonts[defaultBorderedFontLargeKey].font!!
    val timeSignatureFont: BitmapFont
        get() = fonts[timeSignatureFontKey].font!!

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
        UIPalette(fonts[defaultFontKey], fonts[defaultFontLargeKey], 1f,
                  Color(1f, 1f, 1f, 1f),
                  Color(0f, 0f, 0f, 0.75f),
                  Color(0.25f, 0.25f, 0.25f, 0.75f),
                  Color(0f, 0.5f, 0.5f, 0.75f))
    }

    lateinit var preferences: Preferences
        private set

    var versionTextWidth: Float = -1f
        private set

    var githubVersion: Version = Version.RETRIEVING
        private set

    private val rainbowColor: Color = Color()

    override val programLaunchArguments: List<String>
        get() = RHRE3.launchArguments

    override fun getTitle(): String =
            "Rhythm Heaven Remix Editor $versionString"

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("RHRE3 $versionString is starting...")
        // 1.8.0_144
        // 9.X.Y(extra)
        val javaVersion = System.getProperty("java.version").trim()
        Toolboks.LOGGER.info("Running on JRE $javaVersion")

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
                characters = "0123456789"
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
        GameMetadata.setPreferencesInstance(preferences)
        if (preferences.getString(PreferenceKeys.LAST_VERSION, null) != RHRE3.VERSION.toString()) {
            preferences.putInteger(PreferenceKeys.TIMES_SKIPPED_UPDATE, 0).flush()
        }
        AnalyticsHandler.initAndIdentify(Gdx.app.getPreferences("RHRE3-analytics"))

        // set the sound system
        SoundSystem.setSoundSystem(BeadsSoundSystem)

        // registry
        AssetRegistry.addAssetLoader(DefaultAssetLoader())

        // load themes
        LoadedThemes.reloadThemes(preferences, true)

        // screens
        run {
            ScreenRegistry += "assetLoad" to AssetRegistryLoadingScreen(this)

            fun addOtherScreens() {
                ScreenRegistry += "databaseUpdate" to GitUpdateScreen(this)
                ScreenRegistry += "registryLoad" to RegistryLoadingScreen(this)
                ScreenRegistry += "editor" to EditorScreen(this)
                ScreenRegistry += "musicSelect" to MusicSelectScreen(this)
                ScreenRegistry += "info" to InfoScreen(this)
                ScreenRegistry += "newRemix" to NewRemixScreen(this)
                ScreenRegistry += "saveRemix" to SaveRemixScreen(this)
                ScreenRegistry += "openRemix" to OpenRemixScreen(this)
                ScreenRegistry += "exportRemix" to ExportRemixScreen(this)
                ScreenRegistry += "editorVersion" to EditorVersionScreen(this)
                ScreenRegistry += "credits" to CreditsScreen(this)
            }

            val nextScreenLambda = {
                defaultCamera.viewportWidth = RHRE3.WIDTH.toFloat()
                defaultCamera.viewportHeight = RHRE3.HEIGHT.toFloat()
                defaultCamera.update()

                addOtherScreens()
                loadWindowSettings()
                ScreenRegistry[if (RHRE3.skipGitScreen) "registryLoad" else "databaseUpdate"]
            }
            setScreen(ScreenRegistry.getNonNullAsType<AssetRegistryLoadingScreen>("assetLoad")
                              .setNextScreen(nextScreenLambda))

        }

        thread(isDaemon = true) {
            Application.launch(JavafxStub::class.java) // start up
        }
        launch(CommonPool) {
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
    }

    override fun preRender() {
        rainbowColor.setHSB(MathHelper.getSawtoothWave(2f), 0.8f, 0.8f)
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

            font.data.setScale(1f)
        }

        super.postRender()
    }

    override fun dispose() {
        super.dispose()
        GameMetadata.persist()
        preferences.putString(PreferenceKeys.LAST_VERSION, RHRE3.VERSION.toString())
        preferences.putString(PreferenceKeys.MIDI_NOTE,
                              preferences.getString(PreferenceKeys.MIDI_NOTE, Remix.DEFAULT_MIDI_NOTE))
        preferences.flush()
        GameRegistry.dispose()
        Themes.dispose()
        persistWindowSettings()
        Gdx.files.local("tmpMusic/").emptyDirectory()
        SoundSystem.allSystems.forEach(SoundSystem::dispose)
        httpClient.close()
        AnalyticsHandler.track("Close Program", mapOf("durationSeconds" to ((System.currentTimeMillis() - startTimeMillis) / 1000L)))
        AnalyticsHandler.dispose()
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

    private fun createDefaultTTFParameter(): FreeTypeFontGenerator.FreeTypeFontParameter {
        return FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            magFilter = Texture.TextureFilter.Nearest
            minFilter = Texture.TextureFilter.Linear
            genMipMaps = true
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