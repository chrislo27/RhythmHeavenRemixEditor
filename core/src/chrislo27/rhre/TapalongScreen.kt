package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization

const val AUTO_RESET_TIME: Long = 2 * 1000
const val MAX_SAMPLES: Int = 512

class TapalongScreen(m: Main) : Updateable<Main>(m) {

	private var lastTapTime: Long = System.currentTimeMillis()

	private var averageBpm: Float = 0f
	private val timeBetween: MutableList<Long> = mutableListOf()

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 if (timeBetween.size == 1) Localization.get("tapalong.firstBeat") else Math.round(
									 averageBpm).toString(),
							 Gdx.graphics.width * 0.5f,
							 Gdx.graphics.height * 0.575f + main.biggerFont.capHeight, 0f, Align.center, false)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch, Localization.get("tapalong.realBpm", "$averageBpm", "${timeBetween.size}"),
					   Gdx.graphics.width * 0.025f,
					   Gdx.graphics.height * 0.5f, Gdx.graphics.width * 0.95f, Align.center, true)

		main.font.draw(main.batch, Localization.get("tapalong.resetTime", (AUTO_RESET_TIME / 1000), MAX_SAMPLES),
					   Gdx.graphics.width * 0.025f,
					   Gdx.graphics.height * 0.25f, Gdx.graphics.width * 0.95f, Align.center, true)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if (System.currentTimeMillis() - AUTO_RESET_TIME > lastTapTime) {
				reset()
			}

			timeBetween.add(System.currentTimeMillis())
			lastTapTime = System.currentTimeMillis()

			if (timeBetween.size > MAX_SAMPLES) {
				timeBetween.removeAt(0)
			}

			calcAvg()
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset()

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) main.screen = ScreenRegistry.get("editor")
	}

	private fun reset() {
		// reset
		timeBetween.clear()
		averageBpm = 0f
	}

	private fun calcAvg() {
		if (timeBetween.size <= 1) {
			averageBpm = 0f
			return@calcAvg
		}

		val deltas: MutableList<Long> = mutableListOf()

		timeBetween.forEachIndexed { index, long ->
			if (index == 0) return@forEachIndexed

			deltas.add(long - timeBetween[index - 1])
		}

		val averageSec: Float = ((deltas.average() / 1000f).toFloat())

		averageBpm = 60 / averageSec
	}

	override fun tickUpdate() {

	}

	override fun getDebugStrings(array: Array<String>?) {

	}

	override fun resize(width: Int, height: Int) {

	}

	override fun show() {
		reset()
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
