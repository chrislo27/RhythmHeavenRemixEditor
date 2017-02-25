package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization

const val AUTO_RESET_TIME: Long = 3 * 1000

class TapalongScreen(m: Main) : Updateable<Main>(m) {

	private var lastTapTime: Long = System.currentTimeMillis()
	private var averageBpm: Float = 0f
	private var combinedTotal: Long = 0
	private var numOfTaps: Long = 0

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 if (numOfTaps == 1L) Localization.get("tapalong.firstBeat") else Math.round(
									 averageBpm).toString(),
							 Gdx.graphics.width * 0.5f,
							 Gdx.graphics.height * 0.575f + main.biggerFont.capHeight, 0f, Align.center, false)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch, Localization.get("tapalong.realBpm", "$averageBpm", "$numOfTaps"),
					   Gdx.graphics.width * 0.025f,
					   Gdx.graphics.height * 0.5f, Gdx.graphics.width * 0.95f, Align.center, true)

		main.font.draw(main.batch, Localization.get("tapalong.resetTime", (AUTO_RESET_TIME / 1000)),
					   Gdx.graphics.width * 0.025f,
					   Gdx.graphics.height * 0.25f, Gdx.graphics.width * 0.95f, Align.center, true)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if (System.currentTimeMillis() - AUTO_RESET_TIME > lastTapTime) {
				reset()
			}

			numOfTaps++

			if (numOfTaps > 1) {
				combinedTotal += (System.currentTimeMillis() - lastTapTime)
			}

			lastTapTime = System.currentTimeMillis()

			calcAvg()
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset()

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) main.screen = ScreenRegistry.get("editor")
	}

	private fun reset() {
		// reset
		numOfTaps = 0
		combinedTotal = 0
		averageBpm = 0f
	}

	private fun calcAvg() {
		if (numOfTaps <= 1) {
			averageBpm = 0f
			return@calcAvg
		}

		val averageSec: Float = (((combinedTotal.toDouble() / (numOfTaps - 1)) / 1000f)).toFloat()

		averageBpm = 60 / averageSec
	}

	override fun tickUpdate() {

	}

	override fun getDebugStrings(array: Array<String>?) {

	}

	override fun resize(width: Int, height: Int) {

	}

	override fun show() {
		val es: EditorScreen = ScreenRegistry.get("editor", EditorScreen::class.java)
		es.editor.remix.music?.music?.isLooping = true
		es.editor.remix.music?.music?.play()
	}

	override fun hide() {
		val es: EditorScreen = ScreenRegistry.get("editor", EditorScreen::class.java)
		es.editor.remix.music?.music?.isLooping = false
		es.editor.remix.music?.music?.stop()
	}

	override fun pause() {

	}

	override fun resume() {

	}

	override fun dispose() {

	}
}
