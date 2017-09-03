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
import io.github.chrislo27.rhre3.init.DefaultAssetLoader
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.screen.*
import io.github.chrislo27.rhre3.theme.Themes
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
import java.util.*
import kotlin.concurrent.thread


class RHRE3Application(logger: Logger, logToFile: Boolean)
    : ToolboksGame(logger, logToFile, RHRE3.VERSION,
                   RHRE3.DEFAULT_SIZE, ResizeAction.KEEP_ASPECT_RATIO, RHRE3.MINIMUM_SIZE) {

    companion object {
        val languages: List<NamedLocale> =
                listOf(
                        NamedLocale("English", Locale("")),
                        NamedLocale("Français (French)", Locale("fr")),
                        NamedLocale("Español (Spanish)", Locale("es")),
                        NamedLocale("Deutsch (German)", Locale("de"))
//                      ,NamedLocale("Italiano (Italian)", Locale("it"))
                      )

        private const val RAINBOW_STR = "RAINBOW"

        init {
            Colors.put("X", Color.CLEAR)
        }
    }

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"

    val defaultFontLarge: BitmapFont
        get() = fonts[defaultFontLargeKey].font!!
    val defaultBorderedFontLarge: BitmapFont
        get() = fonts[defaultBorderedFontLargeKey].font!!

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin.otf") }
    private val fontAfterLoadFunction: FreeTypeFont.() -> Unit = {
        this.font!!.setFixedWidthGlyphs("1234567890")
        this.font!!.data.setLineHeight(this.font!!.lineHeight * 0.6f)
        this.font!!.setUseIntegerPositions(true)
        this.font!!.data.markupEnabled = true
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

    override fun getTitle(): String =
            "Rhythm Heaven Remix Editor $versionString"

    override fun create() {
        super.create()
        Toolboks.LOGGER.info("RHRE3 $versionString is starting...")

        // localization stuff
        run {
            Localization.bundles.apply {
                languages.forEach { add(Localization.createBundle(it)) }
            }
            Localization.logMissingLocalizations()
        }

        // font stuff
        run {
            fonts[defaultFontLargeKey] = createDefaultLargeFont()
            fonts[defaultBorderedFontLargeKey] = createDefaultLargeBorderedFont()
            fonts.loadUnloaded(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
        }

        // preferences
        preferences = Gdx.app.getPreferences("RHRE3")

        // registry
        AssetRegistry.addAssetLoader(DefaultAssetLoader())

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
                ScreenRegistry += "editorVersion" to EditorVersionScreen(this)
                ScreenRegistry += "credits" to CreditsScreen(this)
            }

            setScreen(ScreenRegistry.getNonNullAsType<AssetRegistryLoadingScreen>("assetLoad")
                              .setNextScreen(
                                      {
                                          defaultCamera.viewportWidth = RHRE3.WIDTH.toFloat()
                                          defaultCamera.viewportHeight = RHRE3.HEIGHT.toFloat()
                                          defaultCamera.update()

                                          addOtherScreens()
                                          loadWindowSettings()
                                          ScreenRegistry[if (RHRE3.skipGitScreen) "registryLoad" else "databaseUpdate"]
                                      }))

        }

        thread(isDaemon = true) {
            Application.launch(JavafxStub::class.java) // start up
        }
        launch(CommonPool) {
            try {
                val nano = System.nanoTime()
                val obj = JsonHandler.fromJson<ReleaseObject>(khttp.get(RHRE3.RELEASE_API_URL).text)

                githubVersion = Version.fromStringOrNull(obj.tag_name!!) ?: Version.UNKNOWN
                Toolboks.LOGGER.info(
                        "Fetched editor version from GitHub in ${(System.nanoTime() - nano) / 1_000_000f}, is $githubVersion")
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
        run {
            val font = defaultBorderedFont
            font.data.setScale(0.5f)
            font.setColor(1f, 1f, 1f, 1f)

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

    fun persistWindowSettings() {
        val isFullscreen = Gdx.graphics.isFullscreen
        if (isFullscreen) {
            preferences.putString(PreferenceKeys.WINDOW_STATE, "fs")
        } else {
            preferences.putString(PreferenceKeys.WINDOW_STATE, "${Gdx.graphics.width}x${Gdx.graphics.height}")
        }

        preferences.flush()
    }

    fun loadWindowSettings() {
        val str: String = preferences.getString(PreferenceKeys.WINDOW_STATE, "${RHRE3.WIDTH}x${RHRE3.HEIGHT}")
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
        }
    }

    override fun dispose() {
        super.dispose()
        preferences.flush()
        GameRegistry.dispose()
        Themes.dispose()
        persistWindowSettings()
        Gdx.files.local("tmpMusic/").emptyDirectory()
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