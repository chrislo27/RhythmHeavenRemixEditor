package chrislo27.rhre

import chrislo27.rhre.script.ScriptSandbox
import chrislo27.rhre.util.FileChooser
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.util.i18n.Localization
import org.luaj.vm2.LuaError
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class ScriptLoadingScreen(m: Main) : NewUIScreen(m) {

	override var icon: String = "ui_script"
	override var title: String = "scriptScreen.title"
	override var bottomInstructions: String = "scriptScreen.instructions"

	private val picker: FileChooser by lazy {
		object : FileChooser() {
			init {
				val fileFilter = FileNameExtensionFilter(
						"Lua script file", "lua")

				val defLoc = Gdx.files.local("scripts/")
				defLoc.mkdirs()

				currentDirectory = attemptRememberDirectory(main, "lastScriptDirectory") ?: defLoc.file()
				fileSelectionMode = JFileChooser.FILES_ONLY
				dialogTitle = "Select a music file"
				setFileFilter(fileFilter)
			}
		}
	}

	private var currentThread: Thread? = null
	@Volatile private var loadState = LoadState.WAITING
	@Volatile private var currentScript: FileHandle? = null

	enum class LoadState {
		WAITING, LOADED, FAILED_TO_LOAD, EXECUTED
	}

	override fun render(delta: Float) {
		super.render(delta)

		val es = ScreenRegistry.get("editor", EditorScreen::class.java)

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		main.font.setColor(1f, 1f, 1f, 1f)

		Main.drawCompressed(main.font, main.batch, Localization.get("scriptScreen.experimental"), startX + PADDING,
							startY + BG_HEIGHT * 0.75f,
							BG_WIDTH - PADDING * 2,
							Align.center)

		main.font.draw(main.batch,
					   when (loadState) {
						   LoadState.WAITING -> {
							   ""
						   }
						   LoadState.LOADED -> {
							   Localization.get("scriptScreen.loaded", currentScript?.path())
						   }
						   LoadState.FAILED_TO_LOAD -> {
							   Localization.get("loadScreen.failed")
						   }
						   LoadState.EXECUTED -> {
							   Localization.get("scriptScreen.executed")
						   }
					   },
					   startX + PADDING,
					   startY + BG_HEIGHT * 0.55f,
					   BG_WIDTH - PADDING * 2,
					   Align.center, true)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(
				Input.Keys.ENTER) && loadState == LoadState.LOADED && currentScript != null && currentScript!!.exists()) {
			val es = ScreenRegistry.get("editor", EditorScreen::class.java)
			try {
				ScriptSandbox.runScriptInRemix(es.editor.remix!!, currentScript!!.readString("UTF-8"))
				loadState = LoadState.EXECUTED
			} catch (e: LuaError) {
				loadState = LoadState.FAILED_TO_LOAD
				e.printStackTrace()
			}
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
						persistDirectory(main, "lastScriptDirectory", picker.currentDirectory)
						val handle: FileHandle = FileHandle(picker.selectedFile)
						val es = ScreenRegistry.get("editor", EditorScreen::class.java)

						currentScript = null
						try {
							val chunk = ScriptSandbox.parseChunk(es.editor.remix, handle.readString("UTF-8"))
							currentScript = handle
							loadState = LoadState.LOADED
						} catch (e: Exception) {
							loadState = LoadState.FAILED_TO_LOAD
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
	}

	override fun hide() {
		closePicker()
		loadState = LoadState.WAITING
	}

}
