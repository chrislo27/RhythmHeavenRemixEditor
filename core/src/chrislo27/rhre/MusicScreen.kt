package chrislo27.rhre

import chrislo27.rhre.track.MusicData
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


class MusicScreen(m: Main) : Updateable<Main>(m), InputProcessor {
	private var failedToLoad: String? = null

	override fun scrolled(amount: Int): Boolean {
		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		if (es.editor.remix?.music != null) {
			val music: Music = es.editor.remix?.music?.music!!

			var vol: Int = (music.volume * 100).toInt()

			vol += -amount * if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(
					Input.Keys.CONTROL_RIGHT)) 5 else 1

			vol = MathUtils.clamp(vol, 0, 100)

			es.editor.remix.musicVolume = vol / 100f
			music.volume = es.editor.remix.musicVolume
		}

		return false
	}

	@Volatile
	private var picker: FileChooser = object : FileChooser() {
		init {
			val fileFilter = FileNameExtensionFilter(
					"Supported sound files (.wav, .ogg, .mp3)", "wav", "ogg", "mp3")

			currentDirectory = File(System.getProperty("user.home"), "Desktop")
			fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
			dialogTitle = "Select a music file"
			setFileFilter(fileFilter)
		}
	}

	private var currentThread: Thread? = null

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("musicScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)

		if (failedToLoad != null) {
			failedToLoad as String
			main.font.setColor(1f, 0f, 0f, 1f)
			main.font.draw(main.batch,
						   failedToLoad!!,
						   Gdx.graphics.width * 0.05f,
						   Gdx.graphics.height * 0.8f)
		}

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch,
					   Localization.get(
							   "musicScreen.current") + " " + (es.editor.remix?.music?.file?.name() ?: Localization.get(
							   "musicScreen.noMusic")),
					   Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.525f)

		main.font.draw(main.batch,
					   Localization.get("musicScreen.volume", "${(es.editor.remix.musicVolume * 100).toInt()}"),
					   Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.485f)

		main.font.draw(main.batch, Localization.get("musicScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 6)

		main.batch.end()
	}

	override fun renderUpdate() {


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
						val handle: FileHandle = FileHandle(picker.selectedFile)
						val es = ScreenRegistry.get("editor", EditorScreen::class.java)

						try {
							es.editor.remix.music = MusicData(Gdx.audio.newMusic(handle), handle)
						} catch (e: Exception) {
							es.editor.remix.music?.music?.dispose()
							failedToLoad = e.toString()
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