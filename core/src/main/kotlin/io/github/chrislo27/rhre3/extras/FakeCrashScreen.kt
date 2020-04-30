package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.screen.HidesVersionText
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.logging.SysOutPiper
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.ui.UIPalette
import java.io.PrintWriter
import java.io.StringWriter


class FakeCrashScreen(main: RHRE3Application, val throwable: Throwable, val lastScreen: Screen?)
    : ToolboksScreen<RHRE3Application, FakeCrashScreen>(main), HidesVersionText {

    override val stage: Stage<FakeCrashScreen> = Stage(null, main.defaultCamera, 1280f, 720f)

    private var crashIcon: Texture? = null

    init {
        val palette = main.uiPalette

        fun label(pal: UIPalette = palette, st: Stage<FakeCrashScreen> = stage): TextLabel<FakeCrashScreen> = TextLabel(pal, st, st).apply {
            this.isLocalizationKey = false
            this.textWrapping = false
        }
        
        stage.elements += label(palette.copy(ftfont = main.defaultFontLargeFTF)).apply {
            this.text = "Well, that happened."
            this.textAlign = Align.left
            this.fontScaleMultiplier = 0.9f
            this.location.set(screenX = 0.2f, screenWidth = 1f /*0.75f*/, screenY = 0.8f, screenHeight = 0.2f)
        }
        try {
            val icon = Texture("images/icon/crash_icon.png")
            crashIcon = icon
            stage.elements += ImageLabel(palette, stage, stage).apply {
                this.image = TextureRegion(icon)
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.location.set(screenX = 0.1f, screenY = 0.9f, screenWidth = 0f, screenHeight = 0f,
                                  pixelWidth = 128f, pixelHeight = 128f, pixelX = -64f, pixelY = -64f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toolboks.LOGGER.warn("Failed to load crash screen icon")
        }

        stage.elements += label().apply {
            this.location.set(screenY = 0.5f, screenHeight = 0.3f)
            this.fontScaleMultiplier = 0.9f
            this.text = "You got a little too excited there, huh?\n\nThe Quiz Show television program has crashed, but we're able to display this crash info screen.\nIf you can, take a screenshot of this screen as it contains useful info for the developer.\nConsider not doing, er, \"that\" next time."
        }

        stage.elements += label().apply {
            this.location.set(screenY = 0.05f, screenHeight = 0.45f, screenX = 0.1f, screenWidth = 0.875f)
            this.fontScaleMultiplier = 0.85f
            this.textAlign = Align.topLeft
            this.textWrapping = true
            this.text = "Last screen: ${lastScreen?.javaClass?.canonicalName}\nLog file: ${if (RHRE3.portableMode) "./.rhre3/logs/" else "~/.rhre3/logs"}/${SysOutPiper.logFile.name}\nException: [#FF6B68]${StringWriter().apply {
                val pw = PrintWriter(this)
                throwable.printStackTrace(pw)
                pw.flush()
            }.toString().replace("\t", "    ")}[]"
        }

        stage.elements += label().apply {
            this.location.set(screenWidth = 0.5f, screenHeight = 0.03333f, pixelHeight = 8f, pixelWidth = -6f)
            this.textAlign = Align.left
            this.fontScaleMultiplier = 0.75f
            this.text = "When you're done, you can press [CYAN]ESC[] to return."
            this.background = true
        }

        stage.elements += label().apply {
            this.location.set(screenX = 0.5f, screenWidth = 0.5f, screenHeight = 0.03333f, pixelX = 6f, pixelWidth = -6f, pixelHeight = 8f)
            this.textAlign = Align.right
            this.fontScaleMultiplier = 0.75f
            this.text = RHRE3.VERSION.toString()
            this.background = true
        }
    }

    override fun render(delta: Float) {
        if (main.batch.isDrawing) {
            main.batch.end()
        }
        super.render(delta)
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = ScreenRegistry["info"]
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
        crashIcon?.dispose()
    }

}