package chrislo27.rhre

import chrislo27.rhre.registry.Game
import chrislo27.rhre.registry.GameRegistry
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.MathHelper
import ionium.util.Utils
import ionium.util.i18n.Localization


class SoundboardScreen(m: Main) : Updateable<Main>(m), InputProcessor {

	open inner class Key(val keycode: Int, val x: Float, val y: Float, val width: Float = 1f, val height: Float = 1f,
						 val keyString: String = Input.Keys.toString(keycode)) {

		open fun isPressed(): Boolean = Gdx.input.isKeyPressed(keycode)

		open fun render(main: Main, prefs: Preferences, set: Int) {
			main.batch.setColor(0.25f, 0.25f, 0.25f, 0.5f)
			if (isPressed())
				main.batch.setColor(0.25f, 0.5f, 0.5f, 0.5f)
			ionium.templates.Main.fillRect(main.batch, x, y, width, height)
			ionium.templates.Main.drawRect(main.batch, x, y, width, height, 0.05f)

			main.font.draw(main.batch, keyString, x + 0.15f,
						   y + height - 0.15f)

			main.batch.setColor(1f, 1f, 1f, 1f)
			val game = getGame(set)
			if (game != null) {
				main.batch.draw(AssetRegistry.getTexture("gameIcon_" + game.id), x + 1 - 0.05f - 0.5f, y + 0.05f,
								0.5f, 0.5f)
			}
		}

		fun getGame(set: Int): Game? = GameRegistry.instance()[prefs.getString("game_" + set + "_" + keycode)]

		open fun onKeyDown() {

		}

	}

	inner class NumKey(keycode: Int, x: Float, y: Float, width: Float = 1f, height: Float = 1f,
							keyString: String = Input.Keys.toString(keycode)) :
			Key(keycode, x, y,
				width, height,
				keyString) {
		override fun isPressed(): Boolean {
			return keycode - (Input.Keys.NUM_0 + setNum) == 0
		}
	}

	inner class NumpadKey(keycode: Int, x: Float, y: Float, width: Float = 1f, height: Float = 1f,
					   keyString: String = Input.Keys.toString(keycode)) :
			Key(keycode, x, y,
				width, height,
				keyString) {
		
	}

	val keys: Map<Int, Key>
	val camera = OrthographicCamera()
	val prefs: Preferences = Gdx.app.getPreferences("RHRE2_soundboard_keymappings")
	private var setNum: Int = 1
	private val picker = Vector3()

	init {
		camera.setToOrtho(false, 16f, 9f)
		camera.position.set(camera.viewportWidth / 2f - 1, 1f, 0f)
		camera.update()

		keys = mutableMapOf()

		fun add(key: Key) {
			keys.put(key.keycode, key)
		}

		// QWERTY
		add(Key(Input.Keys.Q, 0f, 2f))
		add(Key(Input.Keys.W, 1f, 2f))
		add(Key(Input.Keys.E, 2f, 2f))
		add(Key(Input.Keys.R, 3f, 2f))
		add(Key(Input.Keys.T, 4f, 2f))
		add(Key(Input.Keys.Y, 5f, 2f))
		add(Key(Input.Keys.U, 6f, 2f))
		add(Key(Input.Keys.I, 7f, 2f))
		add(Key(Input.Keys.O, 8f, 2f))
		add(Key(Input.Keys.P, 9f, 2f))
		add(Key(Input.Keys.A, 0f + (1f / 3f), 1f))
		add(Key(Input.Keys.S, 1f + (1f / 3f), 1f))
		add(Key(Input.Keys.D, 2f + (1f / 3f), 1f))
		add(Key(Input.Keys.F, 3f + (1f / 3f), 1f))
		add(Key(Input.Keys.G, 4f + (1f / 3f), 1f))
		add(Key(Input.Keys.H, 5f + (1f / 3f), 1f))
		add(Key(Input.Keys.J, 6f + (1f / 3f), 1f))
		add(Key(Input.Keys.K, 7f + (1f / 3f), 1f))
		add(Key(Input.Keys.L, 8f + (1f / 3f), 1f))
		add(Key(Input.Keys.Z, 0f + (2f / 3f), 0f))
		add(Key(Input.Keys.X, 1f + (2f / 3f), 0f))
		add(Key(Input.Keys.C, 2f + (2f / 3f), 0f))
		add(Key(Input.Keys.V, 3f + (2f / 3f), 0f))
		add(Key(Input.Keys.B, 4f + (2f / 3f), 0f))
		add(Key(Input.Keys.N, 5f + (2f / 3f), 0f))
		add(Key(Input.Keys.M, 6f + (2f / 3f), 0f))
		add(NumpadKey(Input.Keys.NUMPAD_7, 11f, 2f, keyString = "7"))
		add(NumpadKey(Input.Keys.NUMPAD_8, 12f, 2f, keyString = "8"))
		add(NumpadKey(Input.Keys.NUMPAD_9, 13f, 2f, keyString = "9"))
		add(NumpadKey(Input.Keys.NUMPAD_4, 11f, 1f, keyString = "4"))
		add(NumpadKey(Input.Keys.NUMPAD_5, 12f, 1f, keyString = "5"))
		add(NumpadKey(Input.Keys.NUMPAD_6, 13f, 1f, keyString = "6"))
		add(NumpadKey(Input.Keys.NUMPAD_1, 11f, 0f, keyString = "1"))
		add(NumpadKey(Input.Keys.NUMPAD_2, 12f, 0f, keyString = "2"))
		add(NumpadKey(Input.Keys.NUMPAD_3, 13f, 0f, keyString = "3"))
		add(NumpadKey(Input.Keys.NUMPAD_0, 11f, -1f, 2f, keyString = "0"))
//		add(Key(Input.Keys., 13f, -1f, keyString = "."))
		add(NumKey(Input.Keys.NUM_1, 0f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_2, 1f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_3, 2f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_4, 3f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_5, 4f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_6, 5f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_7, 6f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_8, 7f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_9, 8f - 0.5f, 3f))
		add(NumKey(Input.Keys.NUM_0, 9f - 0.5f, 3f))
	}

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		camera.update()
		main.batch.projectionMatrix = camera.combined
		main.batch.begin()

		main.font.setColor(1f, 1f, 1f, 1f)
		main.font.data.setScale(1f / 80f)
		main.font.setUseIntegerPositions(false)

		keys.values.forEach {
			it.render(main, prefs, setNum)
		}
		main.batch.setColor(1f, 1f, 1f, 1f)
		main.font.data.setScale(1f)
		main.font.setUseIntegerPositions(true)

		main.batch.projectionMatrix = main.camera.combined

		main.font.data.setScale(0.5f)
		main.font.draw(main.batch, Localization.get("soundboard.return"), 4f, 4 + main.font.capHeight)
		main.font.draw(main.batch, Localization.get("soundboard.controls"), 4f,
					   4 + main.font.capHeight + main.font.lineHeight)
		main.font.data.setScale(1f)
		main.batch.end()
	}

	override fun renderUpdate() {
		camera.unproject(picker.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
		val touchedKey: Key? = keys.values.firstOrNull {
			MathHelper.intersects(it.x, it.y, it.width, it.height, picker.x, picker.y, 0f, 0f)
		}
		if (Utils.isButtonJustPressed(Input.Buttons.LEFT)) {

		} else if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)){
//			prefs.remove()
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
			return
		}
	}

	override fun tickUpdate() {
	}

	override fun getDebugStrings(array: Array<String>?) {
	}

	override fun resize(width: Int, height: Int) {
	}

	override fun show() {
		if (Gdx.input.inputProcessor is InputMultiplexer) {
			val plex = Gdx.input.inputProcessor as InputMultiplexer

			plex.addProcessor(this)
		}
	}

	override fun hide() {
		prefs.flush()
		if (Gdx.input.inputProcessor is InputMultiplexer) {
			Gdx.app.postRunnable {
				val plex = Gdx.input.inputProcessor as InputMultiplexer

				plex.removeProcessor(this)
			}
		}
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun dispose() {
		prefs.flush()
	}

	// InputProcessor

	override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return false
	}

	override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
		return false
	}

	override fun keyTyped(character: Char): Boolean {
		return false
	}

	override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
		return false
	}

	override fun scrolled(amount: Int): Boolean {
		return false
	}

	override fun keyUp(keycode: Int): Boolean {
		return false
	}

	override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
		return false
	}

	override fun keyDown(keycode: Int): Boolean {
		if (keycode >= Input.Keys.NUM_0 && keycode <= Input.Keys.NUM_9) {
			setNum = keycode - Input.Keys.NUM_0
			return true
		}

		if (keycode >= Input.Keys.NUMPAD_0 && keycode <= Input.Keys.NUMPAD_9) {
			keys[keycode]?.onKeyDown() ?: return false

			return true
		}

		return false
	}

}

