package chrislo27.rhre

import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class SaveScreen(m: Main) : Updateable<Main>(m) {

	@Volatile
	private var picker: FileChooser = object : FileChooser() {
		init {
			val fileFilter = FileNameExtensionFilter(
					"RHRE2 remix file", "rhre2")

			currentDirectory = File(System.getProperty("user.home"), "Desktop")
			fileSelectionMode = JFileChooser.FILES_ONLY
			dialogTitle = "Select a directory to save in"
			setFileFilter(fileFilter)
		}
	}

	private var currentThread: Thread? = null

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("saveScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch,
					   Localization.get(
							   "saveScreen.current") + " " + (picker.selectedFile?.path ?: Localization.get(
							   "saveScreen.noSave")),
					   Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.525f, Gdx.graphics.width * 0.9f, Align.left, true)

		main.font.draw(main.batch, Localization.get("warning.remixOverwrite"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 4)
		main.font.draw(main.batch, Localization.get("saveScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 2)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
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
				val result: Int = picker.showSaveDialog(null)

				when (result) {
					JFileChooser.APPROVE_OPTION -> {
						if (picker.selectedFile.extension != "rhre2") {
							picker.selectedFile = File(picker.selectedFile.canonicalPath + ".rhre2")
						}

						val handle = FileHandle(picker.selectedFile)
						val es = ScreenRegistry.get("editor", EditorScreen::class.java)

						val obj: RemixObject = Remix.writeToObject(es.editor.remix)

						picker.selectedFile.createNewFile()

						val json: String = GsonBuilder().create().toJson(obj)

						handle.writeString(json, false, "UTF-8")
					}
				}

				main.screen = ScreenRegistry.get("editor")
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

class LoadScreen(m: Main) : Updateable<Main>(m) {

	@Volatile
	private var picker: FileChooser = object : FileChooser() {
		init {
			val fileFilter = FileNameExtensionFilter(
					"RHRE2 remix file", "rhre2")

			currentDirectory = File(System.getProperty("user.home"), "Desktop")
			fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
			dialogTitle = "Select a remix file to load"
			setFileFilter(fileFilter)
		}
	}

	private var currentThread: Thread? = null

	@Volatile
	private var remixObj: RemixObject? = null

	@Volatile
	private var missingContent: String = ""

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("loadScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		if (remixObj != null) {
			main.font.draw(main.batch, Localization.get("loadScreen.remixInfo", "${remixObj!!.entities.size}",
														"${remixObj!!.bpmChanges.size}"), Gdx.graphics.width * 0.05f,
						   Gdx.graphics.height * 0.75f, Gdx.graphics.width * 0.9f,
						   Align.left, true)

			if (remixObj!!.version != Main.version) {
				main.font.draw(main.batch,
							   Localization.get("loadScreen.versionMismatch", remixObj!!.version ?: "NO VERSION!",
												Main.version),
							   Gdx.graphics.width * 0.05f,
							   Gdx.graphics.height * 0.45f + main.font.capHeight * 0.5f + main.font.lineHeight * 2,
							   Gdx.graphics.width * 0.9f,
							   Align.left, true)
			}

			if (!missingContent.isEmpty()) {
				main.font.draw(main.batch,
							   Localization.get("loadScreen.missingContent", missingContent),
							   Gdx.graphics.width * 0.05f,
							   Gdx.graphics.height * 0.3f + main.font.capHeight * 0.5f + main.font.lineHeight * 2,
							   Gdx.graphics.width * 0.9f,
							   Align.left, true)
			} else {
				main.font.draw(main.batch, Localization.get("loadScreen.confirm"),
							   Gdx.graphics.width * 0.05f,
							   Gdx.graphics.height * 0.175f + main.font.capHeight * 0.5f)
			}
		}
		main.font.draw(main.batch, Localization.get("warning.remixOverwrite"), Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.1f + main.font.capHeight * 0.5f)

		main.font.draw(main.batch, Localization.get("loadScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 2)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && missingContent.isEmpty()) {
			if (remixObj != null) {
				val es = ScreenRegistry.get("editor", EditorScreen::class.java)
				es.editor.remix = Remix.readFromObject(remixObj!!)

				main.screen = ScreenRegistry.get("editor")
			}
		}
	}

	private fun closePicker() {
		picker.isVisible = false
		currentThread?.interrupt()
		currentThread = null
		remixObj = null
		missingContent = ""
	}

	private fun showPicker() {
		val thread: Thread = object : Thread() {
			override fun run() {
				super.run()

				picker.isVisible = true
				val result: Int = picker.showOpenDialog(null)

				when (result) {
					JFileChooser.APPROVE_OPTION -> {
						val gson: Gson = GsonBuilder().create()
						val handle = FileHandle(picker.selectedFile)

						val obj: RemixObject = gson.fromJson(handle.readString("UTF-8"), RemixObject::class.java)

						remixObj = obj

						missingContent = obj.entities.filter { entity ->
							if (entity.isPattern) {
								GameRegistry.instance().getPatternRaw(entity.id) == null
							} else {
								GameRegistry.instance().getCueRaw(entity.id) == null
							}
						}.map {it.id}.joinToString(separator = ", ", transform = {"[LIGHT_GRAY]$it[]"})
					}
					else -> {
						main.screen = ScreenRegistry.get("editor")
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

class NewScreen(m: Main) : Updateable<Main>(m) {


	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("newScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch, Localization.get("newScreen.confirm"), Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.35f + main.font.capHeight * 0.5f)
		main.font.draw(main.batch, Localization.get("warning.remixOverwrite"), Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.25f + main.font.capHeight * 0.5f)
		main.font.draw(main.batch, Localization.get("newScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 2)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			val es = ScreenRegistry.get("editor", EditorScreen::class.java)

			es.editor.remix = Remix()

			main.screen = ScreenRegistry.get("editor")
		}
	}

	override fun tickUpdate() {

	}

	override fun getDebugStrings(array: Array<String>?) {

	}

	override fun resize(width: Int, height: Int) {

	}

	override fun show() {

	}

	override fun hide() {

	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {

	}

}
