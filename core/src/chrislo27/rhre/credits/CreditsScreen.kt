package chrislo27.rhre.credits

import chrislo27.rhre.Main
import chrislo27.rhre.util.DoNotRenderVersionPlease
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable

class CreditsScreen(m: Main) : Updateable<Main>(m), DoNotRenderVersionPlease {

	enum class State(val length: Float = -1f) {
		TITLE_CARD(4.5f), AFTER_TITLE(4.5f + 0.75f), REMIX, END;

		fun start() {

		}
	}

	private var state: State = State.TITLE_CARD
	private var secondsElapsed: Float = 0f

	override fun render(delta: Float) {
		val batch = main.batch
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		batch.projectionMatrix = main.camera.combined
		batch.begin()

		if (state == State.TITLE_CARD) {
			batch.draw(AssetRegistry.getTexture("cannery_tex_titlecard"), 0f, 120f, 1280f, 480f)
		}

		batch.end()
	}

	override fun renderUpdate() {
		secondsElapsed += Gdx.graphics.deltaTime

		if (state.length > 0f && secondsElapsed > state.length) {
			state = State.values()[State.values().indexOf(state) + 1]
			state.start()
		}

		if (state == State.END) {
			main.screen = ScreenRegistry.get("info")
		}
	}

	override fun tickUpdate() {
	}

	override fun getDebugStrings(array: Array<String>?) {
	}

	override fun resize(width: Int, height: Int) {
	}

	override fun show() {
		state = State.TITLE_CARD
		state.start()
		AssetRegistry.getMusic("cannery_music_jingle").play()
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
