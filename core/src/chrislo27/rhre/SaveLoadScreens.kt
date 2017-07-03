package chrislo27.rhre

import chrislo27.rhre.FileFilters.bothFileFilter
import chrislo27.rhre.FileFilters.bundledFileFilter
import chrislo27.rhre.FileFilters.dataFileFilter
import chrislo27.rhre.FileFilters.midiFileFilter
import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.registry.OldGameRegistry
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.FileChooser
import chrislo27.rhre.util.JsonHandler
import chrislo27.rhre.util.message.IconMessage
import chrislo27.rhre.version.RHRE2Version
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.util.Utils
import ionium.util.i18n.Localization
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.sound.midi.MidiSystem
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

internal object FileFilters {
	val dataFileFilter = FileNameExtensionFilter(
			"Deprecated RHRE2 remix data file (.rhre2)", "rhre2")
	val bundledFileFilter = FileNameExtensionFilter("RHRE2 bundled file (.brhre2)", "brhre2")
	val bothFileFilter = FileNameExtensionFilter("Any RHRE2 compatible file (.brhre2, .rhre2)", "brhre2", "rhre2")
	val midiFileFilter = FileNameExtensionFilter("MIDI file (.mid, .midi)", "mid", "midi")
}

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

class SaveScreen(m: Main) : NewUIScreen(m) {
	companion object {
		const val MAX_ZIP_ATTEMPTS: Int = 4
	}

	override var icon: String = "ui_save"
	override var title: String = "saveScreen.title"
	override var bottomInstructions: String = "saveScreen.back"

	internal val picker: FileChooser by lazy {
		object : FileChooser() {
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
	}

	private var currentThread: Thread? = null

	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.font.setColor(1f, 1f, 1f, 1f)

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		main.font.draw(main.batch,
					   Localization.get(
							   "saveScreen.current") + "\n" + (picker.selectedFile?.path ?: Localization.get(
							   "saveScreen.noSave")),
					   startX + PADDING,
					   startY + BG_HEIGHT * 0.75f,
					   BG_WIDTH - PADDING * 2,
					   Align.left, true)

		Main.drawCompressed(main.font, main.batch, Localization.get("warning.remixOverwrite"),
							startX + PADDING,
							startY + PADDING + main.font.capHeight * 6,
							BG_WIDTH - PADDING * 2, Align.center)

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
						val handle = FileHandle(picker.selectedFile)
						try {
							val obj: RemixObject = Remix.writeToJsonObject(es.editor.remix)

							picker.selectedFile.createNewFile()

							if (picker.fileFilter === dataFileFilter) {
								val json: String = JsonHandler.toJson(obj)
								handle.writeString(json, false, "UTF-8")

								es.editor.file = handle
							} else {
								for (i in 1..MAX_ZIP_ATTEMPTS) {
									var zipFile: ZipFile? = null
									try {
										val zipStream: ZipOutputStream = ZipOutputStream(FileOutputStream(picker.selectedFile))

										Remix.writeToZipStream(es.editor.remix, zipStream)

										zipStream.close()

										zipFile = ZipFile(picker.selectedFile)

										break
									} catch (e: IOException) {
										e.printStackTrace()

										if (i == MAX_ZIP_ATTEMPTS) {
											throw e
										}
									} finally {
										zipFile?.close()
									}
								}
							}

							es.editor.file = FileHandle(picker.selectedFile)
							es.editor.isNormalSave = true
							es.editor.messageHandler.list.add(0,
															  IconMessage(3f, AssetRegistry.getTexture("ui_save"),
																		  Localization.get("editor.saved"),
																		  main, 0.5f, 4f))
						} catch (e: Exception) {
							e.printStackTrace()
							es.editor.messageHandler.list.add(0,
															  IconMessage(5f, AssetRegistry.getTexture("ui_save"),
																		  Localization.get("saveScreen.failed"),
																		  main, 0.5f, 4f))
						}
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

class LoadScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_folder"
	override var title: String = "loadScreen.title"
	override var bottomInstructions: String = "loadScreen.confirm"

	internal val picker: FileChooser by lazy {
		object : FileChooser() {
			init {
				currentDirectory = attemptRememberDirectory(main, "lastLoadDirectory") ?: File(
						System.getProperty("user.home"),
						"Desktop")
				fileSelectionMode = JFileChooser.FILES_ONLY
				dialogTitle = "Select a remix file to load"
				fileFilter = bothFileFilter
				addChoosableFileFilter(bundledFileFilter)
				addChoosableFileFilter(dataFileFilter)
				addChoosableFileFilter(midiFileFilter)
			}
		}
	}

	var shouldShowPicker = true

	private var currentThread: Thread? = null

	@Volatile private var remixObj: RemixObject? = null
	@Volatile private var missingContent: Pair<String, Int> = "" to 0
	@Volatile private var failed = false

	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		main.font.setColor(1f, 1f, 1f, 1f)

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		if (remixObj != null) {
			val iconSize: Float = 128f

			Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.hover"),
								startX + PADDING,
								startY + BG_HEIGHT * 0.795f,
								BG_WIDTH - PADDING * 2, Align.center)

			main.batch.draw(AssetRegistry.getTexture("ui_cuenumber"), startX + BG_WIDTH * 0.2f - iconSize * 0.5f,
							startY + BG_HEIGHT * 0.6f - iconSize * 0.5f, iconSize, iconSize)
			Main.drawCompressed(main.font, main.batch,
								"" + remixObj!!.entities!!.size,
								startX + BG_WIDTH * 0.2f - iconSize * 0.5f,
								startY + BG_HEIGHT * 0.6f - iconSize * 0.5f, iconSize, Align.center)

			main.batch.draw(AssetRegistry.getTexture("ui_tempochnumber"), startX + BG_WIDTH * 0.4f - iconSize * 0.5f,
							startY + BG_HEIGHT * 0.6f - iconSize * 0.5f, iconSize, iconSize)
			Main.drawCompressed(main.font, main.batch,
								"" + remixObj!!.bpmChanges!!.size,
								startX + BG_WIDTH * 0.4f - iconSize * 0.5f,
								startY + BG_HEIGHT * 0.6f - iconSize * 0.5f, iconSize, Align.center)

			if (missingContent.first.isEmpty()) {
				main.biggerFont.setColor(0f, 1f, 0f, 1f)
				val height = Utils.getHeight(main.biggerFont, "OK")
				main.biggerFont.draw(main.batch, "OK",
									 startX + BG_WIDTH * 0.6f,
									 startY + BG_HEIGHT * 0.6f + iconSize * 0.5f - height * 0.5f,
									 0f, Align.center, false)
			} else {
				main.biggerFont.setColor(1f, 0f, 0f, 1f)
				val height = Utils.getHeight(main.biggerFont, "!!")
				main.biggerFont.draw(main.batch, "!!",
									 startX + BG_WIDTH * 0.6f,
									 startY + BG_HEIGHT * 0.6f + iconSize * 0.5f - height * 0.5f,
									 0f, Align.center, false)
			}
			main.font.color = main.biggerFont.color
			Main.drawCompressed(main.font, main.batch,
								Localization.get("loadScreen.missingContent", "" + missingContent.second),
								startX + BG_WIDTH * 0.6f - iconSize * 0.5f,
								startY + BG_HEIGHT * 0.6f - iconSize * 0.5f, iconSize, Align.center)
			main.font.setColor(1f, 1f, 1f, 1f)
			main.biggerFont.setColor(1f, 1f, 1f, 1f)

			if (remixObj!!.versionNumber ?: 0 >= RHRE2Version.VERSION.numericalValue) {
				main.font.setColor(0f, 1f, 0f, 1f)
			} else {
				main.font.color = Color.ORANGE
			}
			Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.version", remixObj!!.version),
								startX + BG_WIDTH * 0.8f - iconSize,
								startY + BG_HEIGHT * 0.6f + main.font.capHeight,
								iconSize * 2, Align.center)
			main.font.setColor(1f, 1f, 1f, 1f)

			val iconNum: Int
			if (main.camera.viewportHeight - main.getInputY() > startY + BG_HEIGHT * 0.6f - iconSize * 0.5f &&
					main.camera.viewportHeight - main.getInputY() < startY + BG_HEIGHT * 0.6f + iconSize * 0.5f) {
				iconNum = Math.floor(
						(main.getInputX() - (startX + BG_WIDTH * 0.2 - iconSize * 0.5f)) / (BG_WIDTH * 0.2f)).toInt()
			} else {
				iconNum = -1
			}

			if (iconNum in 0..3) {
				val text: String =
						when (iconNum) {
							0 -> Localization.get("loadScreen.cueNumber")
							1 -> Localization.get("loadScreen.tempoChangeNumber")
							2 -> if (missingContent.second > 0)
								Localization.get("loadScreen.missingInfo", missingContent.first)
							else
								Localization.get("loadScreen.noneMissing")
							3 -> Localization.get("loadScreen.versionInfo")
							else -> ""
						}

				main.font.draw(main.batch, text, startX + PADDING,
							   startY + PADDING + BG_HEIGHT * 0.375f,
							   BG_WIDTH - PADDING * 2, Align.center, true)
			}

			Main.drawCompressed(main.font, main.batch, Localization.get("warning.remixOverwrite"),
								startX + PADDING,
								startY + PADDING + main.font.capHeight * 6,
								BG_WIDTH - PADDING * 2, Align.center)
		} else {
//			Main.drawCompressed(main.font, main.batch, Localization.get("loadScreen.drag"),
//								startX + PADDING,
//								startY + BG_HEIGHT * 0.55f,
//								BG_WIDTH - PADDING * 2,
//								Align.center)
			if (failed) {
				main.font.draw(main.batch, Localization.get("loadScreen.failed"),
							   startX + PADDING,
							   startY + BG_HEIGHT * 0.55f,
							   BG_WIDTH - PADDING * 2,
							   Align.center, true)
			}
		}

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			if (remixObj != null) {
				val es = ScreenRegistry.get("editor", EditorScreen::class.java)
				es.editor.remix?.music?.dispose()
				es.editor.setRemix(Remix.readFromJsonObject(remixObj!!))
				es.editor.file = remixObj?.fileHandle
				es.editor.camera.position.x = es.editor.remix.playbackStart

				main.screen = es
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
		missingContent = "" to 0
	}

	private fun attemptLoad(file: File, endFunction: (success: Boolean) -> Unit) {
		try {
			persistDirectory(main, "lastLoadDirectory", file.parentFile)

			val obj: RemixObject
			val handle = FileHandle(file)
			if (picker.fileFilter === midiFileFilter) {
				obj = Remix.readFromMidiSequence(MidiSystem.getSequence(file))
			} else if (file.extension == "rhre2") {
				val gson: Gson = GsonBuilder().create()
				obj = gson.fromJson(handle.readString("UTF-8"), RemixObject::class.java)
			} else {
				val zipFile: ZipFile = ZipFile(file)
				obj = Remix.readFromZipStream(zipFile)
			}

			obj.fileHandle = handle
			remixObj = obj

			val missingEntities = obj.entities!!.filter { entity ->
				if (entity.isPattern) {
					OldGameRegistry.getPattern(entity.id!!) == null
				} else {
					OldGameRegistry.getCue(entity.id!!) == null
				}
			}

			obj.entities!!.removeAll(missingEntities)

			val distinct = missingEntities.map { it.id!! }.distinct()

			missingContent = distinct.joinToString(separator = ", ", transform = { it }) to distinct.size

			if (missingContent.second > 0) {
				ionium.templates.Main.logger.warn("Missing content: " + missingContent.first)
			}

			endFunction(true)
		} catch (e: Exception) {
			System.err.println("Failed to load " + file.absolutePath)
			e.printStackTrace()
			failed = true
			endFunction(false)
		}
	}

	private fun showPicker() {
		val thread: Thread = object : Thread() {
			override fun run() {
				super.run()

				picker.isVisible = true
				val result: Int = picker.showOpenDialog(null)

				when (result) {
					JFileChooser.APPROVE_OPTION -> {
						attemptLoad(picker.selectedFile) { success ->
							if (!success) {
								ScreenRegistry.get("save",
												   SaveScreen::class.java).picker.selectedFile = picker.selectedFile
							}
						}
					}
					else -> {
						Gdx.app.postRunnable {
							main.screen = ScreenRegistry.get("editor")
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
		if (shouldShowPicker) showPicker()
	}

	override fun hide() {
		closePicker()
		shouldShowPicker = true
		failed = false
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {

	}

}

class NewScreen(m: Main) : NewUIScreen(m) {

	override var icon: String = "ui_newremix"
	override var title: String = "newScreen.title"
	override var bottomInstructions: String = "newScreen.instructions"

	override fun render(delta: Float) {
		super.render(delta)

//		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f
		val warnSize = 256f
		val warnX = startX + BG_WIDTH * 0.5f - warnSize * 1.125f
		val warnY = startY + BG_HEIGHT * 0.5f - warnSize * 0.5f

		main.batch.draw(AssetRegistry.getTexture("ui_warn"), warnX, warnY, warnSize, warnSize)

		main.font.setColor(1f, 1f, 1f, 1f)

		val textHeight = Utils.getHeightWithWrapping(main.font, Localization.get("warning.remixOverwrite"),
													 warnSize * 1.25f)
		main.font.draw(main.batch, Localization.get("warning.remixOverwrite"),
					   warnX + warnSize, warnY + warnSize * 0.5f + textHeight * 0.5f,
					   warnSize * 1.25f, Align.left, true)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			val es = ScreenRegistry.get("editor", EditorScreen::class.java)

			es.editor.setRemix(Remix())
			es.editor.file = null
			es.editor.camera.position.set(0f, 0f, 0f)

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
