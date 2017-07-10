package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksGame
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.i18n.NamedLocale
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import java.util.*


class RHRE3Application(logger: Logger, logToFile: Boolean)
    : ToolboksGame(logger, logToFile, RHRE3.VERSION, Pair(1280, 720), false) {

    companion object {
        val languages: List<NamedLocale> =
                listOf(
                        NamedLocale("English", Locale("")),
                        NamedLocale("Français (French)", Locale("fr")),
                        NamedLocale("Español (Spanish)", Locale("es"))
                      )
    }

    private val defaultFontLargeKey = "default_font_large"
    private val defaultBorderedFontLargeKey = "default_bordered_font_large"

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
            fonts.loadUnloaded()
        }
    }

    override fun postRender() {
        super.postRender()

        batch.begin()

        batch.setColor(0.6f, 1f, 0.2f, 1f)
        batch.fillRect(60f, 60f, 1000f, 600f)
        batch.setColor(1f, 1f, 1f, 1f)

        defaultFont.draw(batch, "RHRE3 first pass", 50f, 50f)
        defaultBorderedFontLarge.draw(batch, "RHRE3 first pass LARGE font", 50f, 500f)

        batch.end()
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