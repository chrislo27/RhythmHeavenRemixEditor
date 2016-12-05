package chrislo27.rhre

import chrislo27.rhre.track.MusicData
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


class MusicScreen(m: Main) : Updateable<Main>(m) {

	@Volatile
	private var picker: FileChooser = object : FileChooser() {
		init {
			val fileFilter = FileNameExtensionFilter(
					"Supported sound files (.wav, .ogg, .mp3)", "wav", "ogg", "mp3")

			currentDirectory = File(System.getProperty("user.home"), "Desktop")
			fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
			dialogTitle = "Select a music file"
			addChoosableFileFilter(fileFilter)
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

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch,
					   Localization.get(
							   "musicScreen.current") + " " + (es.editor.remix?.music?.file?.name() ?: Localization.get(
							   "musicScreen.noMusic")),
					   Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.525f)

		main.font.draw(main.batch, Localization.get("musicScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 4)

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

						es.editor.remix.music = MusicData(Gdx.audio.newMusic(handle), handle)
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
	}

	override fun hide() {
		closePicker()
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {

	}
}