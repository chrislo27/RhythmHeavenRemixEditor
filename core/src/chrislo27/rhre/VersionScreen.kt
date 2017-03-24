package chrislo27.rhre

import chrislo27.rhre.version.VersionChecker
import chrislo27.rhre.version.VersionState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.util.Utils
import ionium.util.i18n.Localization
import ionium.util.render.StencilMaskUtil
import java.time.format.DateTimeFormatter


class VersionScreen(m: Main) : NewUIScreen(m) {
	override var icon: String = "ui_update"
	override var title: String = "versionScreen.title0"
	override var bottomInstructions: String = "versionScreen.goToPage"

	private val titlesAvailable: Int = 5
	private var titleType: Int = 0
	private val SCROLL_FACTOR = 0.25f
	private var scrollAmount: Float = 0f
	private var textHeight: Float = 0f

	private val input by lazy {
		object : InputAdapter() {
			override fun scrolled(amount: Int): Boolean {
				scrollText(amount.toFloat())
				return true
			}
		}
	}

	private fun scrollText(amount: Float) {
		scrollAmount += amount * SCROLL_FACTOR
		scrollAmount = scrollAmount.coerceIn(0f, 1f)
	}

	private val formatter by lazy{ DateTimeFormatter.ofPattern("MMM'.' d',' yyyy hh:mm a")}

	override fun render(delta: Float) {
		super.render(delta)

		main.batch.begin()

		val startX = main.camera.viewportWidth * 0.5f - BG_WIDTH * 0.5f
		val startY = main.camera.viewportHeight * 0.5f - BG_HEIGHT * 0.5f

		main.font.setColor(1f, 1f, 1f, 1f)

		main.batch.setColor(1f, 1f, 1f, 1f)
		ionium.templates.Main.fillRect(main.batch, startX + PADDING + BG_WIDTH * 0.2f + 8,
									   startY + BG_HEIGHT * 0.15f + 6,
									   3f, BG_HEIGHT * 0.65f - 6)

		Main.drawCompressed(main.font, main.batch,
							Localization.get("versionScreen.currentlyUsing") + "\n${ionium.templates.Main.version}",
							startX + PADDING, startY + BG_HEIGHT * 0.75f,
							BG_WIDTH * 0.2f, Align.center)

		Main.drawCompressed(main.font, main.batch,
							(if (VersionChecker.versionState == VersionState.AVAILABLE)
								"[#00FF00]" + Localization.get("versionScreen.newAvailable") + "[]"
							else
								Localization.get("versionScreen.currentRelease"))
									+ "\n${ionium.templates.Main.githubVersion}",
							startX + PADDING, startY + BG_HEIGHT * 0.55f,
							BG_WIDTH * 0.2f, Align.center)

		main.font.draw(main.batch,
					   Localization.get("versionScreen.scroll"),
					   startX + PADDING, startY + BG_HEIGHT * 0.3f,
					   BG_WIDTH * 0.2f, Align.center, true)

		Main.drawCompressed(main.font, main.batch,
							Localization.get("versionScreen.coolInfo",
											 ((VersionChecker.releaseObject!!.assets!!.firstOrNull()?.size ?: 0) / 1048576.0),
											 VersionChecker.releaseObject!!.assets!!.firstOrNull()?.download_count,
											 VersionChecker.releaseObject!!.publishedTime!!.format(formatter)).format(
									"%.3f"),
							startX + BG_WIDTH * 0.25f, startY + BG_HEIGHT * 0.8f,
							BG_WIDTH * 0.75f - PADDING, Align.left)

		var releaseTitleY = startY + BG_HEIGHT * 0.8f - main.font.lineHeight
		main.font.draw(main.batch,
					   "[#DDDDDD]" + VersionChecker.releaseObject!!.name + "[]",
					   startX + BG_WIDTH * 0.25f, releaseTitleY,
					   BG_WIDTH * 0.75f - PADDING, Align.left, true)
		releaseTitleY -= main.font.lineHeight

		main.font.data.setScale(0.75f)
		val heightToWorkWith: Float = releaseTitleY - (startY + main.font.capHeight * 8f)

		main.batch.end()
		StencilMaskUtil.prepareMask()
		main.shapes.projectionMatrix = main.camera.combined
		main.shapes.begin(ShapeRenderer.ShapeType.Filled)
		main.shapes.rect(startX + BG_WIDTH * 0.25f, releaseTitleY,
						 BG_WIDTH * 0.75f - PADDING, -(heightToWorkWith + 8))
		main.shapes.end()

		main.batch.begin()
		StencilMaskUtil.useMask()

		main.font.draw(main.batch,
					   "[LIGHT_GRAY]${VersionChecker.releaseObject?.body}[]",
					   startX + BG_WIDTH * 0.25f,
					   releaseTitleY + ((textHeight * (if (textHeight < heightToWorkWith) 0 else 1) - heightToWorkWith) * scrollAmount),
					   BG_WIDTH * 0.75f - PADDING, Align.left, true)
		main.font.data.setScale(1f)

		main.batch.flush()
		StencilMaskUtil.resetMask()

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
			scrollText(Gdx.graphics.deltaTime / SCROLL_FACTOR * 2)
		}
		if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
			scrollText(-Gdx.graphics.deltaTime / SCROLL_FACTOR * 2)
		}

		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && VersionChecker.versionState != VersionState.GETTING
				&& VersionChecker.versionState != VersionState.FAILED) {
			Gdx.net.openURI(VersionChecker.releaseObject!!.html_url)
		}
	}

	override fun tickUpdate() {
	}

	override fun getDebugStrings(array: Array<String>?) {
	}

	override fun resize(width: Int, height: Int) {
	}

	override fun show() {
		scrollAmount = 0f
		main.font.data.setScale(0.75f)
		textHeight = Utils.getHeightWithWrapping(main.font, VersionChecker.releaseObject?.body,
												 BG_WIDTH * 0.75f - PADDING)
		main.font.data.setScale(1f)
		if (titlesAvailable > 1) {
			val old = titleType
			while (titleType == old) {
				titleType = MathUtils.random(0, titlesAvailable - 1)
			}

			title = "versionScreen.title$titleType"
		}

		(Gdx.input.inputProcessor as InputMultiplexer).addProcessor(input)
	}

	override fun hide() {
		(Gdx.input.inputProcessor as InputMultiplexer).removeProcessor(input)
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun dispose() {
	}
}