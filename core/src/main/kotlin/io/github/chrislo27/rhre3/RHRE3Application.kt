package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.init.DefaultAssetLoader
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.screen.AssetRegistryLoadingScreen
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.screen.GitUpdateScreen
import io.github.chrislo27.rhre3.screen.RegistryLoadingScreen
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
import net.spookygames.gdx.nativefilechooser.NativeFileChooser
import java.util.*


class RHRE3Application(nativeFileChooser: NativeFileChooser, logger: Logger, logToFile: Boolean)
    : ToolboksGame(logger, logToFile, RHRE3.VERSION,
                   RHRE3.DEFAULT_SIZE, ResizeAction.KEEP_ASPECT_RATIO, RHRE3.MINIMUM_SIZE) {

    companion object {
        val languages: List<NamedLocale> =
                listOf(
                        NamedLocale("English", Locale("")),
                        NamedLocale("Français (French)", Locale("fr")),
                        NamedLocale("Español (Spanish)", Locale("es"))
                      )

        lateinit var nativeFileChooser: NativeFileChooser
            private set
    }

    init {
        Companion.nativeFileChooser = nativeFileChooser
    }

    val defaultFontLargeKey = "default_font_large"
    val defaultBorderedFontLargeKey = "default_bordered_font_large"

    val defaultFontLarge: BitmapFont
        get() = fonts[defaultFontLargeKey].font!!
    val defaultBorderedFontLarge: BitmapFont
        get() = fonts[defaultBorderedFontLargeKey].font!!

    private val fontFileHandle: FileHandle by lazy { Gdx.files.internal("fonts/rodin.otf") }
    private val fontAfterLoadFunction: FreeTypeFont.() -> Unit = {
        this.font!!.setFixedWidthGlyphs("1234567890/\\")
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
            }

            setScreen(ScreenRegistry.getNonNullAsType<AssetRegistryLoadingScreen>("assetLoad")
                              .setNextScreen(
                                      {
                                          addOtherScreens()
                                          loadWindowSettings()
                                          ScreenRegistry[if (RHRE3.skipGitScreen) "registryLoad" else "databaseUpdate"]
                                      }))

        }
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
        persistWindowSettings()
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