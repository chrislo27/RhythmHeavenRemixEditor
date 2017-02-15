package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization


class D4JAdvertisingScreen(m: Main) : Updateable<Main>(m) {

	private var time: Float = 0f

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.data.setScale(0.75f)
		main.biggerFont.draw(main.batch,
							 Localization.get("d4jScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)
		main.biggerFont.data.setScale(1f)

		main.font.setColor(1f, 1f, 1f, 1f)
		main.font.draw(main.batch,
					   Localization.get("d4jScreen.content"),
					   Gdx.graphics.width * 0.05f,
					   Gdx.graphics.height * 0.75f,
					   Gdx.graphics.width * 0.9f,
					   Align.left, true)

		main.font.draw(main.batch,
					   Localization.get("d4jScreen.controls"),
					   Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 3.5f,
					   Gdx.graphics.width * 0.9f,
					   Align.left, true)

		main.font.draw(main.batch,
					   Localization.get("d4jScreen.return"),
					   Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 2,
					   Gdx.graphics.width * 0.9f,
					   Align.left, true)

		main.batch.end()
	}

	override fun renderUpdate() {
		time += Gdx.graphics.deltaTime

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && (time >= 3f || !main.jumpToD4J)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
			Gdx.net.openURI("https://github.com/austinv11/Discord4J")
		}
	}

	override fun tickUpdate() {
	}

	override fun getDebugStrings(array: Array<String>?) {
	}

	override fun resize(width: Int, height: Int) {
	}

	override fun show() {
		main.jumpToD4J = false
		main.preferences.putBoolean("jumpToD4J", main.jumpToD4J)
		main.preferences.flush()
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
