package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.util.MathHelper
import ionium.util.Utils
import ionium.util.i18n.Localization

class TapalongScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_tapper"
	override var title: String = "editor.button.tapalong"
	override var bottomInstructions: String = "tapalong.instructions"

	private var lastTapTime: Long = System.currentTimeMillis()
	private var averageBpm: Double = 0.0
	private var combinedTotal: Long = 0
	private var numOfTaps: Long = 0

	private val AUTO_RESET_TIME: Long = 3 * 1000

	init {
		instructionsParams = arrayOf("${AUTO_RESET_TIME / 1000}")
	}

	override fun render(delta: Float) {
		super.render(delta)

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		main.font.setColor(1f, 1f, 1f, 1f)

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.draw(main.batch,
							 if (numOfTaps == 1L)
								 Localization.get("tapalong.firstBeat")
							 else
								 Math.round(averageBpm).toString(),
							 startX + BG_WIDTH * 0.5f, startY + BG_HEIGHT * 0.5f + main.biggerFont.capHeight, 0f,
							 Align.center, false)

		val scale = if (System.currentTimeMillis() - lastTapTime < 150L) {
			1f + (1f - ((System.currentTimeMillis() - lastTapTime) / 150f)) * 0.5f
		} else {
			1f
		}
		val size = 128f * scale
		main.batch.draw(AssetRegistry.getTexture("ui_beattapper"),
						startX + BG_WIDTH * 0.5f - size * 0.5f,
						startY + BG_HEIGHT * 0.2f + 64f - size * 0.5f,
						size, size)

		if (Utils.isButtonJustPressed(Input.Buttons.LEFT) && MathHelper.isPointInRectangle(
				startX + BG_WIDTH * 0.5f - size * 0.5f,
				startY + BG_HEIGHT * 0.2f + 64f - size * 0.5f,
				size, size, main.getInputX() * 1f, main.camera.viewportHeight - main.getInputY())) {
			handleBeatInput()
		}

		main.font.draw(main.batch, Localization.get("tapalong.average", "$averageBpm"),
					   startX + BG_WIDTH * 0.2f,
					   startY + BG_HEIGHT * 0.35f, 0f, Align.center, false)
		main.font.draw(main.batch, Localization.get("tapalong.numOfTaps", "$numOfTaps"),
					   startX + BG_WIDTH * 0.8f,
					   startY + BG_HEIGHT * 0.35f, 0f, Align.center, false)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			handleBeatInput()
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) reset()

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) main.screen = ScreenRegistry.get("editor")
	}

	private fun handleBeatInput() {
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

	private fun reset() {
		// reset
		numOfTaps = 0
		combinedTotal = 0
		averageBpm = 0.0
	}

	private fun calcAvg() {
		if (numOfTaps <= 1) {
			averageBpm = 0.0
			return@calcAvg
		}

		val averageSec: Double = (((combinedTotal.toDouble() / (numOfTaps - 1)) / 1000.0))

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
