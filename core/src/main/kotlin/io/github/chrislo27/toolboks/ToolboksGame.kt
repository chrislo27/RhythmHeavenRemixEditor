package io.github.chrislo27.toolboks

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.toolboks.Toolboks.StageOutlineMode.ALL
import io.github.chrislo27.toolboks.Toolboks.StageOutlineMode.NONE
import io.github.chrislo27.toolboks.Toolboks.StageOutlineMode.ONLY_VISIBLE
import io.github.chrislo27.toolboks.font.FontHandler
import io.github.chrislo27.toolboks.font.FreeTypeFont
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.logging.Logger
import io.github.chrislo27.toolboks.logging.SysOutPiper
import io.github.chrislo27.toolboks.oshi.OSHI
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.tick.TickController
import io.github.chrislo27.toolboks.tick.TickHandler
import io.github.chrislo27.toolboks.util.MemoryUtils
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.isKeyJustReleased
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import io.github.chrislo27.toolboks.version.Version
import java.io.File
import java.text.NumberFormat
import kotlin.system.measureNanoTime

/**
 * This class is the base of all games. [ResizeAction] and its other size parameters are behaviours for how resizing works.
 * This is generally important for fonts that scale up.
 */
abstract class ToolboksGame(val logger: Logger, val logToFile: File?,
                            val version: Version,
                            val emulatedSize: Pair<Int, Int>, val resizeAction: ResizeAction,
                            val minimumSize: Pair<Int, Int>)
    : Game(), TickHandler {

    companion object {

        lateinit var smallTexture: Texture
            private set

    }

    val versionString: String = version.toString()
    val defaultFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_font"
    val defaultBorderedFontKey: String = "${Toolboks.TOOLBOKS_ASSET_PREFIX}default_bordered_font"
    private val numberFormatInstance = NumberFormat.getIntegerInstance()

    lateinit var originalResolution: Pair<Int, Int>
        private set
    val tickController: TickController = TickController()
    val defaultCamera: OrthographicCamera = OrthographicCamera()
    lateinit var fonts: FontHandler
        private set
    lateinit var batch: SpriteBatch
        private set
    lateinit var shapeRenderer: ShapeRenderer
        private set

    val defaultFont: BitmapFont
        get() = fonts[defaultFontKey].font!!
    val defaultBorderedFont: BitmapFont
        get() = fonts[defaultBorderedFontKey].font!!

    open val inputMultiplexer = InputMultiplexer()

    private var memoryDeltaTime: Float = 0f
    private var lastMemory: Long = 0L
    var memoryDelta: Long = 0L
    private var shouldToggleDebugAfterPress = true
    val startTimeMillis: Long = System.currentTimeMillis()
    private val disposeCalls: MutableList<Runnable> = mutableListOf()

    /**
     * Should include the version
     */
    abstract fun getTitle(): String

    abstract val programLaunchArguments: List<String>

    override fun create() {
        if (logToFile != null) {
            SysOutPiper.pipe(programLaunchArguments, this, logToFile)
        }
        Toolboks.LOGGER = logger

        originalResolution = Pair(Gdx.graphics.width, Gdx.graphics.height)
        resetCamera()
        tickController.init(this)

        val pixmap: Pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(1f, 1f, 1f, 1f)
        pixmap.fill()
        smallTexture = Texture(pixmap)
        pixmap.dispose()

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        fonts = FontHandler(this)
        fonts[defaultFontKey] = createDefaultFont()
        fonts[defaultBorderedFontKey] = createDefaultBorderedFont()
        fonts.loadAll(defaultCamera.viewportWidth, defaultCamera.viewportHeight)

        Gdx.input.inputProcessor = inputMultiplexer
    }

    /**
     * This function handles camera updates, debug keystrokes, and clearing.
     */
    open fun preRender() {
        defaultCamera.update()
        batch.projectionMatrix = defaultCamera.combined
        shapeRenderer.projectionMatrix = defaultCamera.combined
        tickController.update()

        // render update
        if (Gdx.input.isKeyJustReleased(Toolboks.DEBUG_KEY)) {
            if (shouldToggleDebugAfterPress) {
                val old = Toolboks.debugMode
                Toolboks.debugMode = !old
                onDebugChange(old, !old)
                Toolboks.LOGGER.debug("Switched debug mode to ${!old}")
            }
            shouldToggleDebugAfterPress = true
        }
        if (Gdx.input.isKeyPressed(Toolboks.DEBUG_KEY)) {
            var pressed = true
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                val nano = measureNanoTime {
                    Localization.reloadAll(true)
                    Localization.logMissingLocalizations()
                }
                Toolboks.LOGGER.debug("Reloaded I18N from files in ${nano / 1_000_000.0} ms")
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                Toolboks.stageOutlines = if (Gdx.input.isShiftDown()) {
                    if (Toolboks.stageOutlines == ONLY_VISIBLE) ALL else ONLY_VISIBLE
                } else if (Toolboks.stageOutlines == NONE) ALL else NONE
                Toolboks.LOGGER.debug("Toggled stage outlines to ${Toolboks.stageOutlines}")
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
                System.gc()
            } else if (checkDebugKeybind()) {

            } else {
                pressed = false
            }

            if (shouldToggleDebugAfterPress && pressed) {
                shouldToggleDebugAfterPress = false
            }
        }
        if (screen != null) {
            (screen as? ToolboksScreen<*, *>)?.renderUpdate()
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        Gdx.gl.glClearDepthf(1f)
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)
    }

    /**
     * This is called after the main [render] function is called.
     */
    open fun postRender() {

    }

    protected open fun onDebugChange(old: Boolean, new: Boolean) {
    }

    protected open fun checkDebugKeybind(): Boolean {
        return false
    }

    /**
     * The default render function. This calls [preRender], then `super.[render]`, then [postRender].
     * The debug overlay is also rendered at this time.
     */
    override fun render() {
        try {
            measureNanoTime {
                preRender()
                super.render()
                postRender()

                memoryDeltaTime += Gdx.graphics.deltaTime
                if (memoryDeltaTime >= 1f) {
                    memoryDeltaTime = 0f
                    val heap = Gdx.app.nativeHeap
                    memoryDelta = heap - lastMemory
                    lastMemory = heap
                }

                if (Toolboks.debugMode) {
                    val font = defaultBorderedFont
                    batch.begin()
                    font.data.setScale(0.75f)

                    val fps = Gdx.graphics.framesPerSecond
                    val string =
                            """FPS: [${if (fps <= 10) "RED" else if (fps < 30) "YELLOW" else "WHITE"}]$fps[]
Debug mode: ${Toolboks.DEBUG_KEY_NAME}
  While holding ${Toolboks.DEBUG_KEY_NAME}: I - Reload L10N | S - Stage outlines (SHIFT for only visible) | G - gc
Version: $versionString
Memory: ${numberFormatInstance.format(Gdx.app.nativeHeap / 1024)} / ${numberFormatInstance.format(
                                    MemoryUtils.maxMemory)} KB (${numberFormatInstance.format(memoryDelta / 1024)} KB/s)
CPU: ${if (!OSHI.isInitialized) "OSHI not yet inited by SysOutPiper" else OSHI.sysInfo.hardware.processor.name}

Screen: ${screen?.javaClass?.canonicalName}
${getDebugString()}
${(screen as? ToolboksScreen<*, *>)?.getDebugString() ?: ""}"""

                    font.setColor(1f, 1f, 1f, 1f)
                    font.drawCompressed(batch, string, 8f, defaultCamera.viewportHeight - 8f, defaultCamera.viewportWidth - 16f,
                                        Align.left)

                    font.data.setScale(1f)
                    batch.end()
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Gdx.app.exit()
        }
    }

    /**
     * This returns a string to be put in the debug overlay above the current screen's debug string. By default this is
     * an empty string.
     */
    open fun getDebugString(): String {
        return ""
    }

    override fun tickUpdate(tickController: TickController) {
        if (screen != null) {
            (screen as? ToolboksScreen<*, *>)?.tickUpdate()
        }
    }

    /**
     * This will reset the camera + reload fonts if the resize action is [ResizeAction.KEEP_ASPECT_RATIO]. It then calls
     * the super-method last.
     */
    override fun resize(width: Int, height: Int) {
        val lastCameraDimensions = defaultCamera.viewportWidth to defaultCamera.viewportHeight
        resetCamera()
        if (resizeAction == ResizeAction.KEEP_ASPECT_RATIO &&
                (defaultCamera.viewportWidth to defaultCamera.viewportHeight) != lastCameraDimensions) {
            val nano = measureNanoTime {
                fonts.loadAll(defaultCamera.viewportWidth, defaultCamera.viewportHeight)
            }
            Toolboks.LOGGER.info("Reloaded all ${fonts.fonts.size} fonts in ${nano / 1_000_000.0} ms")
        }
        super.resize(width, height)
    }

    override fun setScreen(screen: Screen?) {
        val current = getScreen()
        super.setScreen(screen)
        Toolboks.LOGGER.debug("Changed screens from ${current?.javaClass?.name} to ${screen?.javaClass?.name}")
    }

    override fun dispose() {
        Toolboks.LOGGER.info("Starting dispose call")

        super.dispose()

        disposeCalls.forEach {
            try {
                it.run()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        batch.dispose()
        shapeRenderer.dispose()
        fonts.dispose()
        smallTexture.dispose()

        ScreenRegistry.dispose()
        AssetRegistry.dispose()

        Toolboks.LOGGER.info("Dispose call finished, goodbye!")
    }

    fun addDisposeCall(runnable: Runnable) {
        disposeCalls += runnable
    }

    fun removeDisposeCall(runnable: Runnable) {
        disposeCalls -= runnable
    }

    abstract fun createDefaultFont(): FreeTypeFont

    abstract fun createDefaultBorderedFont(): FreeTypeFont

    fun resetCamera() {
        when (resizeAction) {
            ResizeAction.ANY_SIZE -> defaultCamera.setToOrtho(false, Gdx.graphics.width.toFloat(),
                                                              Gdx.graphics.height.toFloat())
            ResizeAction.LOCKED -> defaultCamera.setToOrtho(false, emulatedSize.first.toFloat(),
                                                            emulatedSize.second.toFloat())
            ResizeAction.KEEP_ASPECT_RATIO -> {
                val width: Float
                val height: Float

                if (Gdx.graphics.width < Gdx.graphics.height) {
                    width = Gdx.graphics.width.toFloat()
                    height = (emulatedSize.second.toFloat() / emulatedSize.first) * width
                } else {
                    height = Gdx.graphics.height.toFloat()
                    width = (emulatedSize.first.toFloat() / emulatedSize.second) * height
                }

                defaultCamera.setToOrtho(false, width, height)
            }
        }
        if (defaultCamera.viewportWidth < minimumSize.first || defaultCamera.viewportHeight < minimumSize.second) {
            Toolboks.LOGGER.info("Camera too small, forcing it at minimum")
            defaultCamera.setToOrtho(false, minimumSize.first.toFloat(), minimumSize.second.toFloat())
        }
        defaultCamera.update()
        Toolboks.LOGGER.info(
                "Resizing camera as $resizeAction, window is ${Gdx.graphics.width} x ${Gdx.graphics.height}, camera is ${defaultCamera.viewportWidth} x ${defaultCamera.viewportHeight}")
    }
}
