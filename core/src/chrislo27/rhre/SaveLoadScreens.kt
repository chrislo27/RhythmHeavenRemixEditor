package chrislo27.rhre

import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ionium.registry.ScreenRegistry
import ionium.util.DebugSetting
import ionium.util.i18n.Localization
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

val dataFileFilter = FileNameExtensionFilter(
		"Deprecated RHRE2 remix data file (.rhre2)", "rhre2")
val bundledFileFilter = FileNameExtensionFilter("RHRE2 bundled file (.brhre2)", "brhre2")
val bothFileFilter = FileNameExtensionFilter("Any RHRE2 compatible file (.brhre2, .rhre2)", "brhre2", "rhre2")

internal fun persistDirectory(main: Main, prefName: String, file: File) {
	main.preferences.putString(prefName, file.absolutePath)
	main.preferences.flush()
}

internal fun attemptRememberDirectory(main: Main, prefName: String): File? {
	val f: File = File(main.preferences.getString(prefName, null) ?: return null)

	if (f.exists() && f.isDirectory)
		return f

	return null
}

class SaveScreen(m: Main) : BackgroundedScreen(m) {

	@Volatile
	internal var picker: FileChooser = object : FileChooser() {
		init {
			currentDirectory = attemptRememberDirectory(main, "lastSaveDirectory") ?: File(
					System.getProperty("user.home"),
					"Desktop")
			fileSelectionMode = JFileChooser.FILES_ONLY
			dialogTitle = "Select a directory to save in"
			fileFilter = bundledFileFilter
			addChoosableFileFilter(dataFileFilter)
			addChoosableFileFilter(bothFileFilter)
		}
	}

	private var currentThread: Thread? = null

	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("saveScreen.title"),
							 main.camera.viewportWidth * 0.05f,
							 main.camera.viewportHeight * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch,
					   Localization.get(
							   "saveScreen.current") + " " + (picker.selectedFile?.path ?: Localization.get(
							   "saveScreen.noSave")),
					   main.camera.viewportWidth * 0.05f,
					   main.camera.viewportHeight * 0.5f + main.font.capHeight * 0.5f, main.camera.viewportWidth * 0.9f,
					   Align.left, true)

		Main.drawCompressed(main.font, main.batch, Localization.get("warning.remixOverwrite"),
							main.camera.viewportWidth * 0.05f,
							main.font.capHeight * 4, main.camera.viewportWidth * 0.9f, Align.left)
		Main.drawCompressed(main.font, main.batch, Localization.get("info.back"), main.camera.viewportWidth * 0.05f,
							main.font.capHeight * 2, main.camera.viewportWidth * 0.9f, Align.left)

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
						persistDirectory(main, "lastSaveDirectory", picker.currentDirectory)

						val handle = FileHandle(picker.selectedFile)
						val es = ScreenRegistry.get("editor", EditorScreen::class.java)

						if (picker.fileFilter === dataFileFilter) {
							if (picker.selectedFile.extension != "rhre2") {
								picker.selectedFile = File(picker.selectedFile.canonicalPath + ".rhre2")
							}
						} else if (picker.fileFilter === bundledFileFilter || picker.fileFilter === bothFileFilter) {
							if (picker.selectedFile.extension != "brhre2") {
								picker.selectedFile = File(picker.selectedFile.canonicalPath + ".brhre2")
							}
						}

						val obj: RemixObject = Remix.writeToJsonObject(es.editor.remix)

						picker.selectedFile.createNewFile()

						if (picker.fileFilter === dataFileFilter) {
							val gsonBuilder = GsonBuilder()
							if (DebugSetting.debug)
								gsonBuilder.setPrettyPrinting()
							val json: String = gsonBuilder.create().toJson(obj)
							handle.writeString(json, false, "UTF-8")

							es.editor.file = handle
						} else {
							val zipStream: ZipOutputStream = ZipOutputStream(FileOutputStream(picker.selectedFile))

							Remix.writeToZipStream(es.editor.remix, zipStream)

							zipStream.close()
						}

						es.editor.isNormalSave = true
						es.editor.autosaveMessageShow = 3f
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

class LoadScreen(m: Main) : BackgroundedScreen(m), WhenFilesDropped {

	@Volatile
	internal var picker: FileChooser = object : FileChooser() {
		init {
			currentDirectory = attemptRememberDirectory(main, "lastLoadDirectory") ?: File(
					System.getProperty("user.home"),
					"Desktop")
			fileSelectionMode = JFileChooser.FILES_ONLY
			dialogTitle = "Select a remix file to load"
			fileFilter = bothFileFilter
			addChoosableFileFilter(bundledFileFilter)
			addChoosableFileFilter(dataFileFilter)
		}
	}

	var shouldShowPicker = true

	private var currentThread: Thread? = null

	@Volatile
	private var remixObj: RemixObject? = null

	@Volatile
	private var missingContent: String = ""

	override fun onFilesDropped(list: List<FileHandle>) {
		if (list.size != 1) return

		hidePicker()
		attemptLoad(list.first().file())
	}

	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("loadScreen.title"),
							 main.camera.viewportWidth * 0.05f,
							 main.camera.viewportHeight * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		if (remixObj != null) {
			main.font.draw(main.batch, Localization.get("loadScreen.remixInfo", "${remixObj!!.entities.size}",
														"${remixObj!!.bpmChanges.size}"),
						   main.camera.viewportWidth * 0.05f,
						   main.camera.viewportHeight * 0.85f - main.biggerFont.capHeight * 0.75f,
						   main.camera.viewportWidth * 0.9f,
						   Align.left, true)

			if (remixObj!!.version != ionium.templates.Main.version) {
				main.font.draw(main.batch,
							   Localization.get("loadScreen.versionMismatch", remixObj!!.version ?: "NO VERSION!",
												ionium.templates.Main.version),
							   main.camera.viewportWidth * 0.05f,
							   main.camera.viewportHeight * 0.85f - main.biggerFont.capHeight * 0.75f - main.font.capHeight * 6,
							   main.camera.viewportWidth * 0.9f,
							   Align.left, true)
			}

			if (!missingContent.isEmpty()) {
				main.font.draw(main.batch,
							   Localization.get("loadScreen.missingContent", missingContent),
							   main.camera.viewportWidth * 0.05f,
							   main.camera.viewportHeight * 0.85f - main.biggerFont.capHeight * 0.75f - main.font.capHeight * 13,
							   main.camera.viewportWidth * 0.9f,
							   Align.left, true)
			}

			Main.drawCompressed(main.font, main.batch, Localization.get("warning.remixOverwrite"),
								main.camera.viewportWidth * 0.05f,
								main.camera.viewportHeight * 0.85f - main.biggerFont.capHeight * 0.75f + main.font.capHeight * 1.5f,
								main.camera.viewportWidth * 0.9f, Align.center)
			Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.confirm"),
								main.camera.viewportWidth * 0.05f,
								main.font.capHeight * 4, main.camera.viewportWidth * 0.9f, Align.left)
		} else {
			Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.drag"),
								main.camera.viewportWidth * 0.05f,
								main.camera.viewportHeight * 0.85f - main.biggerFont.capHeight * 0.75f,
								main.camera.viewportWidth * 0.9f, Align.left)
		}

		Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.return"),
							main.camera.viewportWidth * 0.05f,
							main.font.capHeight * 2, main.camera.viewportWidth * 0.9f, Align.left)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			if (remixObj != null) {
				val es = ScreenRegistry.get("editor", EditorScreen::class.java)
				es.editor.remix?.music?.dispose()
				es.editor.remix = Remix.readFromJsonObject(remixObj!!)
				es.editor.file = remixObj?.fileHandle

				main.screen = ScreenRegistry.get("editor")
			}
		}
	}

	fun hidePicker() {
		picker.isVisible = false
		currentThread?.interrupt()
		currentThread = null
	}

	private fun closePicker() {
		hidePicker()
		remixObj = null
		missingContent = ""
	}

	private fun attemptLoad(file: File) {
		persistDirectory(main, "lastLoadDirectory", file.parentFile)

		val obj: RemixObject
		val handle = FileHandle(file)
		if (file.extension == "rhre2") {
			val gson: Gson = GsonBuilder().create()
			obj = gson.fromJson(handle.readString("UTF-8"), RemixObject::class.java)
		} else {
			val zipFile: ZipFile = ZipFile(file)
			obj = Remix.readFromZipStream(zipFile)
		}

		obj.fileHandle = handle
		remixObj = obj

		val missingEntities = obj.entities.filter { entity ->
			if (entity.isPattern) {
				GameRegistry.instance().getPatternRaw(entity.id) == null
			} else {
				GameRegistry.instance().getCueRaw(entity.id) == null
			}
		}

		obj.entities.removeAll(missingEntities)

		missingContent = missingEntities.map { it.id }.distinct().joinToString(separator = ", ",
																			   transform = { "[LIGHT_GRAY]$it[]" })

		ionium.templates.Main.logger.warn("Missing content: " + missingContent)
	}

	private fun showPicker() {
		val thread: Thread = object : Thread() {
			override fun run() {
				super.run()

				picker.isVisible = true
				val result: Int = picker.showOpenDialog(null)

				when (result) {
					JFileChooser.APPROVE_OPTION -> {
						attemptLoad(picker.selectedFile)
						ScreenRegistry.get("save", SaveScreen::class.java).picker.selectedFile = picker.selectedFile
					}
					else -> {
//						main.screen = ScreenRegistry.get("editor")
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
		if (shouldShowPicker) showPicker()
	}

	override fun hide() {
		closePicker()
		shouldShowPicker = true
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {

	}

}

class NewScreen(m: Main) : BackgroundedScreen(m) {


	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 Localization.get("newScreen.title"),
							 main.camera.viewportWidth * 0.05f,
							 main.camera.viewportHeight * 0.85f + main.biggerFont.capHeight)

		main.font.setColor(1f, 1f, 1f, 1f)

		Main.drawCompressed(main.font, main.batch, Localization.get("warning.remixOverwrite"),
							main.camera.viewportWidth * 0.05f,
							main.camera.viewportHeight * 0.5f + main.font.capHeight * 0.5f,
							main.camera.viewportWidth * 0.9f, Align.center)
		Main.drawCompressed(main.font, main.batch, Localization.get("newScreen.confirm"),
							main.camera.viewportWidth * 0.05f,
							main.font.capHeight * 4, main.camera.viewportWidth * 0.9f, Align.left)
		Main.drawCompressed(main.font, main.batch, Localization.get("newScreen.return"),
							main.camera.viewportWidth * 0.05f,
							main.font.capHeight * 2, main.camera.viewportWidth * 0.9f, Align.left)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			val es = ScreenRegistry.get("editor", EditorScreen::class.java)

			es.editor.remix = Remix()
			es.editor.file = null

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
