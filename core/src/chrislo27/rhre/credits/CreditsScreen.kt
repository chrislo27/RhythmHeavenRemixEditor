package chrislo27.rhre.credits

import chrislo27.rhre.Main
import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.json.persistent.RemixObject
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.track.MusicData
import chrislo27.rhre.track.PlayingState
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.HideVersionText
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.Utils
import ionium.util.i18n.Localization

class CreditsScreen(m: Main) : Updateable<Main>(m), HideVersionText {

	enum class State(val length: Float = -1f) {
		TITLE_CARD(4.5f) {
			override fun end(screen: CreditsScreen) {
				AssetRegistry.getMusic("cannery_music_jingle").stop()
			}
		},
		AFTER_TITLE(4.5f + 0.75f), REMIX(4.5f + 0.75f + 51f) {
			override fun start(screen: CreditsScreen) {
				screen.remix.playingState = PlayingState.PLAYING
			}
		},
		LITTLE_WAIT(4.5f + 0.75f + 51f + 1.0f),
		END;

		companion object {
			val values = values()
		}

		open fun start(screen: CreditsScreen) {

		}

		open fun end(screen: CreditsScreen) {

		}
	}

	inner class ArmFireEntity(remix: Remix, x: Float) : Entity(remix) {
		override val name: String = "credits_armFire"

		override fun copy(): Entity {
			throw UnsupportedOperationException("no can do partner")
		}

		override val isStretchable: Boolean = false
		override val isRepitchable: Boolean = false
		override val id: String = "credits_armFire"
		override val semitone: Int = 0

		override fun render(main: Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		}

		init {
			bounds.x = x
		}

		override fun onStart(delta: Float, intended: Float) {
			super.onStart(delta, intended)
			fire()
		}

	}

	inner class BeaconFlashEntity(remix: Remix, x: Float) : Entity(remix) {

		init {
			bounds.x = x
		}

		override val name: String = "credits_beaconFlash"

		override fun copy(): Entity {
			throw UnsupportedOperationException("no can do partner")
		}

		override val isStretchable: Boolean = false
		override val isRepitchable: Boolean = false
		override val id: String = "credits_beaconFlash"
		override val semitone: Int = 0

		override fun render(main: Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		}

		override fun onStart(delta: Float, intended: Float) {
			super.onStart(delta, intended)
			beaconFlash = 1f
		}

	}

	inner class TextEntity(remix: Remix, x: Float, val cid: Int) : Entity(remix) {

		init {
			bounds.x = x
		}

		override val name: String = "credits_text"

		override fun copy(): Entity {
			throw UnsupportedOperationException("no can do partner")
		}

		override val isStretchable: Boolean = false
		override val isRepitchable: Boolean = false
		override val id: String = "credits_text"
		override val semitone: Int = 0

		override fun render(main: Main, palette: AbstractPalette, batch: SpriteBatch, selected: Boolean) {
		}

		override fun onStart(delta: Float, intended: Float) {
			super.onStart(delta, intended)
			currentString = if (cid < 0) Triple("", "", 0f) else Triple(Credits.list[cid].first,
																		Credits.list[cid].second,
																		cid.toFloat() / (Credits.list.size - 1).coerceAtLeast(
																				1))
		}

	}

	fun Remix.getBeatBounce(): Float {
		val beatDec = beat - beat.toInt()

		if (beatDec <= 0.3f && beat >= 0 && beat < duration) {
			return 1f - beatDec / 0.3f
		} else {
			return 0f
		}
	}

	private var state: State = State.TITLE_CARD
	private var secondsElapsed: Float = 0f

	private var armExtension: Float = 0f
	private var beaconFlash: Float = 0f

	val startMsTime: Long = System.currentTimeMillis()
	var currentString: Triple<String, String, Float> = Triple("", "", 0f)

	val remix: Remix = Remix.readFromJsonObject(
			JsonHandler.fromJson(Gdx.files.internal("credits/cannery/cannery.rhre2").readString("UTF-8"),
								 RemixObject::class.java))

	init {
		remix.music = MusicData(AssetRegistry.getMusic("cannery_music_song"),
								Gdx.files.local("credits/cannery/music/Cannery.ogg"), "Cannery.ogg")
		remix.playbackStart = -1f

		remix.entities.filter { it is PatternEntity && it.id == "cannery_can" }.forEach {
			remix.entities.add(ArmFireEntity(remix, it.bounds.x + 1f))
			remix.entities.add(BeaconFlashEntity(remix, it.bounds.x))
		}

		remix.entities.sortBy { it.bounds.x }

		var i: Int = 0
		remix.entities.filter { it is SoundEntity && (it.id == "cannery/ding" || it.id == "cannery/steam") }.forEach {
			if (it.id == "cannery/ding") {
				remix.entities.add(TextEntity(remix, it.bounds.x, i++))
			} else {
				remix.entities.add(TextEntity(remix, it.bounds.x, -1))
			}
		}

		remix.entities.removeIf { it is SoundEntity && (it.id == "cannery/ding" || it.id == "cannery/steam") }
	}

	private fun fire() {
		armExtension = 1f
	}

	override fun render(delta: Float) {
		val batch = main.batch
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		batch.projectionMatrix = main.camera.combined
		batch.begin()

		if (state == State.TITLE_CARD) {
			batch.draw(AssetRegistry.getTexture("cannery_tex_titlecard"), 0f, 120f, 1280f, 480f)
		} else if (state == State.REMIX) {
			batch.draw(AssetRegistry.getTexture("cannery_tex_bg"), 0f, 0f, main.camera.viewportWidth,
					   main.camera.viewportHeight)

			val conveyorHeight = 136f
			val conveyor = AssetRegistry.getTexture("cannery_tex_conveyor")
			for (x in (((remix.beat % 1f)) * main.camera.viewportWidth * 0.5f - main.camera.viewportWidth * 0.5f).toInt()..main.camera.viewportWidth.toInt() step conveyor.width) {
				batch.draw(conveyor, x * 1f, conveyorHeight)
			}

			val closedCan = AssetRegistry.getTexture("cannery_tex_closed_can")

			remix.entities.forEach {
				if (it is PatternEntity && it.id == "cannery_can") {
					val beatsFromCentre = remix.beat - (it.bounds.x + 1f)
					if (Math.abs(beatsFromCentre) > 2)
						return@forEach

					val tex = if (beatsFromCentre >= 0)
						closedCan
					else AssetRegistry.getTexture("cannery_tex_open_can")
					batch.draw(tex,
							   beatsFromCentre * main.camera.viewportWidth * 0.5f + main.camera.viewportWidth * 0.5f - tex.width * 0.5f,
							   conveyorHeight + conveyor.height)
				}
			}

			batch.draw(AssetRegistry.getTexture("cannery_tex_box"), 0f, conveyorHeight + conveyor.height)
			val boxHeight = conveyorHeight + conveyor.height + AssetRegistry.getTexture("cannery_tex_box").height
			batch.draw(AssetRegistry.getTexture("cannery_tex_beacon"), 128f, boxHeight, 64f,
					   64f * (0.6f + 0.4f * (1f - remix.getBeatBounce())))
			if (beaconFlash > 0) {
				batch.setColor(1f, 1f, 1f, beaconFlash)
				batch.draw(AssetRegistry.getTexture("cannery_tex_beacon_flash"), 128f - 32, boxHeight - 32, 128f, 128f)
				batch.setColor(1f, 1f, 1f, 1f)
			}

			val pipeV = AssetRegistry.getTexture("cannery_tex_pipe_v")
			val pipeJ = AssetRegistry.getTexture("cannery_tex_pipe_junction")
			val pipeH = AssetRegistry.getTexture("cannery_tex_pipe")
			val pipeX = main.camera.viewportWidth * 0.5f - pipeV.width * 0.5f

			for (i in 0..4) {
				batch.draw(pipeV, pipeX,
						   (conveyorHeight + conveyor.height + closedCan.height) + (1f - armExtension) * 192f + i * pipeV.height)
				if (i == 0 && armExtension > 0.8f) {
					batch.draw(AssetRegistry.getTexture("cannery_tex_whoosh"), pipeX,
							   (conveyorHeight + conveyor.height + closedCan.height) + (1f - armExtension) * 192f + i * pipeV.height)
				}
			}

			batch.draw(pipeJ, pipeX, main.camera.viewportHeight - pipeJ.height)
			for (i in 0..2) {
				batch.draw(pipeH, pipeX + pipeV.width + i * pipeH.width, main.camera.viewportHeight - pipeJ.height)
			}

			val textHeight = main.camera.viewportHeight * 0.85f
			val textX = pipeX + pipeJ.width * 1.5f
			val textW = main.camera.viewportWidth - 16f - textX

			var realTH: Float = if (currentString.second.isEmpty()) 0f else Utils.getHeightWithWrapping(main.font,
																										currentString.second,
																										textW) + main.font.capHeight
			val s = Localization.get("info.credits." + currentString.first)
			if (currentString.first.isNotEmpty()) {
				realTH += Utils.getHeight(main.fontBordered, s)
			}

			val realTW: Float = Math.min(textW, Math.max(if (currentString.second.isEmpty()) 0f
														 else Utils.getWidthWithWrapping(main.font,
																						 currentString.second, textW),
														 if (currentString.first.isEmpty()) 0f
														 else Utils.getWidth(main.fontBordered, s)))

			if (realTH > 0) {
				main.batch.setColor(0f, 0f, 0f, 1f)
				ionium.templates.Main.drawRect(main.batch, textX - 6, textHeight + 6, realTW + 12, -(realTH + 12), 2f)
				main.batch.setColor(1f, 1f, 1f, 0.75f)
				ionium.templates.Main.fillRect(main.batch, textX - 4, textHeight + 4, realTW + 8, -(realTH + 8))
				main.batch.setColor(1f, 1f, 1f, 1f)
			}

			if (currentString.first.isNotEmpty()) {
				main.font.color = ionium.templates.Main.getRainbow(startMsTime + (1000L * currentString.third).toLong(),
																   1f,
																   0.75f).mul(.85f, .85f, .85f, 1f)
				main.font.draw(batch, s, textX,
							   textHeight, textW, Align.left, false)
			}
			main.fontBordered.setColor(1f, 1f, 1f, 1f)

			main.font.setColor(0f, 0f, 0f, 1f)
			Colors.put("RAINBOW", ionium.templates.Main.getRainbow(System.currentTimeMillis(), 3f, 0.75f))
			main.font.draw(batch, currentString.second, textX,
						   textHeight - main.fontBordered.lineHeight * 1.25f, textW, Align.left, true)

			if (secondsElapsed >= State.REMIX.length - 1f) {
				batch.setColor(0f, 0f, 0f, (1f - (State.REMIX.length - secondsElapsed)).coerceIn(0.0f, 1.0f))
				ionium.templates.Main.fillRect(batch, 0f, 0f, main.camera.viewportWidth, main.camera.viewportHeight)
				batch.setColor(1f, 1f, 1f, 1f)
			}
		}

		batch.end()
	}

	override fun renderUpdate() {
		remix.update(Gdx.graphics.deltaTime)
		secondsElapsed += Gdx.graphics.deltaTime

		if (armExtension > 0)
			armExtension = (armExtension - Gdx.graphics.deltaTime / 0.3f).coerceIn(0f, 1f)
		if (beaconFlash > 0)
			beaconFlash = (beaconFlash - Gdx.graphics.deltaTime / 0.5f).coerceIn(0f, 1f)

		if (state == State.TITLE_CARD) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
				secondsElapsed = state.length
			}
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			secondsElapsed = Float.MAX_VALUE
		}

		if (state.length > 0f && secondsElapsed >= state.length) {
			state.end(this)
			state = State.values[State.values.indexOf(state) + 1]
			state.start(this)
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
		AssetRegistry.instance().stopAllMusic()
		AssetRegistry.instance().stopAllSound()

		state = State.TITLE_CARD
		state.start(this)
		AssetRegistry.getMusic("cannery_music_jingle").play()
	}

	override fun hide() {
		AssetRegistry.instance().stopAllMusic()
		AssetRegistry.instance().stopAllSound()
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun dispose() {
	}

}
