package chrislo27.rhre

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.editor.EditorStageSetup
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.utils.Array
import ionium.screen.Updateable
import ionium.stage.Stage

class EditorScreen(m: Main) : Updateable<Main>(m) {

	val editor: Editor by lazy {
		Editor(main)
	}
	var stage: Stage? = null
		private set
	private var stageSetup: EditorStageSetup? = null
	private var first = true

	override fun render(delta: Float) {
		attemptMakeEditor()

		editor.render(main.batch)
		stage?.render(main.batch)

	}

	override fun renderUpdate() {
		editor.inputUpdate()
		editor.renderUpdate()
	}

	override fun tickUpdate() {

	}

	override fun getDebugStrings(array: Array<String>) {

	}

	override fun resize(width: Int, height: Int) {
		stage?.onResize(main.camera.viewportWidth.toInt(), main.camera.viewportHeight.toInt())
	}

	private fun attemptMakeEditor() {
		if (stageSetup == null) {
			stageSetup = EditorStageSetup(this)
			stage = stageSetup!!.stage
		}
	}

	override fun show() {
		attemptMakeEditor()

		if (Gdx.input.inputProcessor is InputMultiplexer) {
			val plex = Gdx.input.inputProcessor as InputMultiplexer

			stage!!.addSelfToInputMultiplexer(plex)
			plex.addProcessor(editor)
		}

		stage!!.onResize(main.camera.viewportWidth.toInt(), main.camera.viewportHeight.toInt())

		if (first) {
			first = false
			if (main.oldSize.third) {
				Gdx.graphics.setFullscreenMode(Gdx.graphics.displayMode)
			} else {
				println("resizing: " + main.oldSize)
				Gdx.graphics.setWindowedMode(main.oldSize.first, main.oldSize.second)
			}
		}
	}

	override fun hide() {
		if (Gdx.input.inputProcessor is InputMultiplexer && stage != null) {
			Gdx.app.postRunnable {
				val plex = Gdx.input.inputProcessor as InputMultiplexer

				stage!!.removeSelfFromInputMultiplexer(plex)
				plex.removeProcessor(editor)
			}
		}
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {
		editor.dispose()
	}
}
