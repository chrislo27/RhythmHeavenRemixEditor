package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.screen.AssetLoadingScreen
import ionium.util.DebugSetting
import ionium.util.MathHelper
import ionium.util.i18n.Localization

class LoadingScreen(m: Main) : AssetLoadingScreen(m) {

	private val WIDTH: Float = 270f + 30
	private val HEIGHT: Float = 370f + 30
	private var finish: Boolean = false

	private var animationTime: Float = 0f
	private val animationSpeed: Float = 1f

	override fun render(delta: Float) {
		super.render(delta)

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		// 1f - MathHelper.getSawtoothWave(System.currentTimeMillis(), 2.5f)
		var time: Float = animationTime * 0.8f
//		time = 0f

		val ratioDown: Float = -14f / 25f

		fun draw(letter: String, x: Float, yStart: Float, delay: Float = 0f) {
			val height: Float = Interpolation.circleOut.apply((time - delay).coerceIn(0.0f..1.0f))

			main.batch.draw(AssetRegistry.getTexture("logo_$letter"),
							(main.camera.viewportWidth * 0.5f - WIDTH * 0.5f) + x,
							(yStart + x * ratioDown + 128) * height - 128)

			if (animationTime in 3.0..4.0) {
				val wave = MathHelper.getTriangleWave(((animationTime - 3) * 1000).toLong(), 1f)
				val rainbow: Color = ionium.templates.Main.getRainbow(
						((animationTime - 2.5f) * 1000).toLong() + (delay * 0.5f * 1000).toLong(), -1.0f,
						wave * 1f)
				main.batch.setColor(rainbow.r, rainbow.g, rainbow.b, wave)

				main.batch.draw(AssetRegistry.getTexture("logo_$letter"),
								(main.camera.viewportWidth * 0.5f - WIDTH * 0.5f) + x,
								(yStart + x * ratioDown + 128) * height - 128)

				main.batch.setColor(1f, 1f, 1f, 1f)
			}

		}

		draw("r", 0f, main.camera.viewportHeight * 0.6f)
		draw("e", 40f, main.camera.viewportHeight * 0.6f, 0.05f)
		draw("m", 82f, main.camera.viewportHeight * 0.6f, 0.05f * 2)
		draw("i", 116f, main.camera.viewportHeight * 0.6f, 0.05f * 3)
		draw("x", 144f, main.camera.viewportHeight * 0.6f, 0.05f * 4)

		draw("e", 0f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 5)
		draw("d", 40f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 6)
		draw("i", 76f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 7)
		draw("t", 106f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 8)
		draw("o", 140f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 9)
		draw("r", 180f, main.camera.viewportHeight * 0.6f - 96f, 0.05f * 10)

		val alpha: Float = Interpolation.circleOut.apply((time - 1.0f).coerceIn(0.0f..1.0f))
		main.batch.setColor(1f, 1f, 1f, alpha)
		main.batch.draw(AssetRegistry.getTexture("logo_2nd"), main.camera.viewportWidth * 0.5f + WIDTH * 0.45f * alpha,
						main.camera.viewportHeight * 0.6f - 64f, 128f, 128f)
		main.batch.setColor(1f, 1f, 1f, 1f)

		val manager = AssetRegistry.instance().assetManager
		val barWidth = Gdx.graphics.width * 0.5f

		ionium.templates.Main.drawRect(main.batch, Gdx.graphics.width * 0.25f - 4,
									   Gdx.graphics.height * 0.15f - 8f - 4f, barWidth + 8,
									   (16 + 8).toFloat(),
									   2f)

		ionium.templates.Main.fillRect(main.batch, Gdx.graphics.width * 0.25f,
									   Gdx.graphics.height * 0.15f - 8,
									   barWidth * manager.progress, 16f)

		if (manager.assetNames.size > 0) {
			Main.drawCompressed((main as Main).font, main.batch, output.lastMsg,
								Gdx.graphics.width * 0.5f - Gdx.graphics.width * 0.95f * 0.5f,
								Gdx.graphics.height * 0.15f - 20, Gdx.graphics.width * 0.95f, Align.center)
		}

		val outOf = "" + manager.loadedAssets + " / " + (manager.loadedAssets + manager.queuedAssets)
		Main.drawCompressed((main as Main).font, main.batch, outOf,
							Gdx.graphics.width * 0.5f - Gdx.graphics.width * 0.95f * 0.5f,
							Gdx.graphics.height * 0.15f - 50, Gdx.graphics.width * 0.95f, Align.center)

		val percent = String.format("%.0f", manager.progress * 100f) + "%"
		Main.drawCompressed((main as Main).font, main.batch, percent,
							Gdx.graphics.width * 0.5f - Gdx.graphics.width * 0.95f * 0.5f,
							Gdx.graphics.height * 0.15f - 75, Gdx.graphics.width * 0.95f, Align.center)

		if (manager.progress >= 1) {
			(main as Main).font.data.setScale(0.5f)
			Main.drawCompressed((main as Main).font, main.batch, Localization.get("loading.skip"),
								Gdx.graphics.width * 0.5f - barWidth * 0.5f,
								Gdx.graphics.height * 0.15f + 25, barWidth, Align.center)
			(main as Main).font.data.setScale(1f)
		}

		main.batch.end()

		animationTime += animationSpeed * delta
	}

	override fun renderUpdate() {
		super.renderUpdate()

		if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(
				Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			finish = true
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			animationTime = 0f
		}
	}

	override fun canFinishLoading(): Boolean {
		return super.canFinishLoading() && (finish || (animationTime >= 4.5f && !DebugSetting.debug))
	}

	override fun getDebugStrings(array: Array<String>?) {
		super.getDebugStrings(array)
		array?.add("Press R to restart animation")
	}

}
