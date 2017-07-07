package chrislo27.rhre

import chrislo27.rhre.track.MusicData
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.util.i18n.Localization
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


class MusicScreen(m: Main) : NewUIScreen(m), InputProcessor {
    override var icon: String = "ui_songchoose"
    override var title: String = "musicScreen.title"
    override var bottomInstructions: String = "musicScreen.return"

    private var failedToLoad: String? = null

    override fun scrolled(amount: Int): Boolean {
        val es = ScreenRegistry.get("editor", EditorScreen::class.java)

        if (es.editor.remix?.music != null) {
            val music: Music = es.editor.remix?.music?.music!!

            var vol: Int = Math.round(music.volume * 100)

            vol += -amount * (if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
                    Input.Keys.CONTROL_RIGHT)) 5 else 1)

            vol = MathUtils.clamp(vol, 0, 100)

            es.editor.remix.musicVolume = vol / 100f
            music.volume = es.editor.remix.musicVolume

            return true
        }

        return false
    }

    private val picker: FileChooser by lazy {
        object : FileChooser() {
            init {
                val fileFilter = FileNameExtensionFilter(
                        "Supported sound files (.wav, .ogg, .mp3)", "wav", "ogg", "mp3")

                currentDirectory = attemptRememberDirectory(main, "lastMusicDirectory") ?: File(
                        System.getProperty("user.home"), "Desktop")
                fileSelectionMode = JFileChooser.FILES_ONLY
                dialogTitle = "Select a music file"
                setFileFilter(fileFilter)
            }
        }
    }

    private var currentThread: Thread? = null

    override fun render(delta: Float) {
        super.render(delta)

        val es = ScreenRegistry.get("editor", EditorScreen::class.java)

        main.batch.begin()

        val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
        val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

        if (failedToLoad != null) {
            failedToLoad as String
            main.font.setColor(1f, 0f, 0f, 1f)
            main.font.draw(main.batch,
                           failedToLoad!!,
                           startX + PADDING,
                           startY + BG_HEIGHT * 0.55f,
                           BG_WIDTH * 0.75f - PADDING * 2,
                           Align.left, true)
        }

        main.font.setColor(1f, 1f, 1f, 1f)

        main.font.draw(main.batch,
                       Localization.get(
                               "musicScreen.current") + "\n" + (es.editor.remix?.music?.originalFileName ?: Localization.get(
                               "musicScreen.noMusic")),
                       startX + PADDING,
                       startY + BG_HEIGHT * 0.75f,
                       BG_WIDTH * 0.65f - PADDING * 2,
                       Align.left, true)

        main.biggerFont.data.setScale(0.5f)
        main.biggerFont.draw(main.batch,
                             Localization.get("musicScreen.volume",
                                              "\n${Math.round(es.editor.remix.musicVolume * 100)}"),
                             startX + PADDING + BG_WIDTH * 0.65f,
                             startY + BG_HEIGHT * 0.75f,
                             BG_WIDTH * 0.35f - PADDING * 2,
                             Align.left, true)
        main.biggerFont.data.setScale(1f)

        Main.drawCompressed(main.font, main.batch, Localization.get("musicScreen.scroll"),
                            startX + PADDING,
                            startY + PADDING + main.font.capHeight * 6,
                            BG_WIDTH - PADDING * 2, Align.center)

        main.batch.end()
    }

    override fun renderUpdate() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            scrolled(1)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            scrolled(-1)
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = ScreenRegistry.get("editor")
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            val es = ScreenRegistry.get("editor", EditorScreen::class.java)
            es?.editor?.remix?.music = null
            main.screen = ScreenRegistry.get("editor")
        }
    }

    private fun closePicker() {
        picker.isVisible = false
        currentThread?.interrupt()
        currentThread = null
    }

    private fun showPicker() {
        val thread: Thread = object : Thread() {
            override fun run() {
                super.run()

                picker.isVisible = true
                val result: Int = picker.showOpenDialog(null)

                when (result) {
                    JFileChooser.APPROVE_OPTION -> {
                        persistDirectory(main, "lastMusicDirectory", picker.currentDirectory)
                        val handle: FileHandle = FileHandle(picker.selectedFile)
                        val es = ScreenRegistry.get("editor", EditorScreen::class.java)

                        try {
                            val md = MusicData(Gdx.audio.newMusic(handle), handle, handle.name())
                            es.editor.remix.music = md
                        } catch (e: Exception) {
                            failedToLoad = e.toString()
                            e.printStackTrace()
                        }
                    }
                }
            }
        }

        thread.isDaemon = true
        thread.start()

        currentThread = thread
    }

    override fun tickUpdate() {

    }

    override fun getDebugStrings(array: Array<String>?) {

    }

    override fun resize(width: Int, height: Int) {

    }

    override fun show() {
        showPicker()

        Gdx.input.inputProcessor = InputMultiplexer()
        (Gdx.input.inputProcessor as InputMultiplexer).addProcessor(this)
    }

    override fun hide() {
        closePicker()
        failedToLoad = null

        if (Gdx.input.inputProcessor is InputMultiplexer) {
            (Gdx.input.inputProcessor as InputMultiplexer).removeProcessor(this as InputProcessor)
        }
    }

    override fun pause() {

    }

    override fun resume() {

    }

    override fun dispose() {

    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun keyDown(keycode: Int): Boolean {
        return false
    }
}