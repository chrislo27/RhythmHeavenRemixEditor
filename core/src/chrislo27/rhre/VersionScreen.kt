package chrislo27.rhre

import chrislo27.rhre.version.VersionChecker
import chrislo27.rhre.version.VersionState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization
import java.time.format.DateTimeFormatter


class VersionScreen(m: Main) : Updateable<Main>(m) {

	private val titlesAvailable: Int = 5
	private var titleType: Int = 0
	private val MAX_LINES = 12
	private var content: String = ""
	private var line = 0
		set(value) {
			field = value

			if (VersionChecker.releaseObject != null) {
				content = ""
				for (i in line.coerceIn(
						0..Math.max(0, VersionChecker.releaseObject!!.bodyLines.size - MAX_LINES - 1))
						..Math.min(line + MAX_LINES, VersionChecker.releaseObject!!.bodyLines.size - 1)) {
					content += VersionChecker.releaseObject!!.bodyLines[i] + "\n"
				}
			}
		}

	private val input = object : InputAdapter() {
		override fun scrolled(amount: Int): Boolean {
			line += amount
			line = line.coerceIn(0, Math.max(0, VersionChecker.releaseObject!!.bodyLines.size - MAX_LINES - 1))
			return true
		}
	}

	private val formatter = DateTimeFormatter.ofPattern("MMM'.' d',' yyyy hh:mm a")

	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.data.setScale(0.75f)
		Main.drawCompressed(main.biggerFont, main.batch,
							Localization.get("versionScreen.title$titleType"),
							main.camera.viewportWidth * 0.05f,
							main.camera.viewportHeight * 0.85f + main.biggerFont.capHeight,
							main.camera.viewportWidth * 0.9f, Align.left)
		main.biggerFont.data.setScale(1f)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch, Localization.get(
				"versionScreen.${if (VersionChecker.versionState == VersionState.UP_TO_DATE) "already" else "available"}",
				VersionChecker.releaseObject!!.tag_name,
				ionium.templates.Main.version),
					   main.camera.viewportWidth * 0.05f, main.camera.viewportHeight * 0.75f)
		main.font.draw(main.batch, Localization.get("versionScreen.coolInfo",
													((VersionChecker.releaseObject!!.assets!!.first().size) / 1048576.0),
													VersionChecker.releaseObject!!.assets!!.first().download_count,
													VersionChecker.releaseObject!!.publishedTime!!.format(formatter))
				.format("%.3f"),
					   main.camera.viewportWidth * 0.05f, main.camera.viewportHeight * 0.75f - main.font.lineHeight)

		var releaseTitleY = main.camera.viewportHeight * 0.75f - main.font.lineHeight * 2.5f
		main.font.draw(main.batch,
					   "[#DDDDDD]" + VersionChecker.releaseObject!!.name + "[]",
					   main.camera.viewportWidth * 0.05f, releaseTitleY,
					   main.camera.viewportWidth * 0.9f, Align.left, true)
		releaseTitleY -= main.font.lineHeight
		main.font.data.setScale(0.75f)
		main.font.draw(main.batch,
					   "[LIGHT_GRAY]$content[]",
					   main.camera.viewportWidth * 0.05f, releaseTitleY,
					   main.camera.viewportWidth * 0.9f, Align.left, true)
		main.font.data.setScale(1f)

		val scrollY = main.font.capHeight * 4.5f
		main.font.data.setScale(0.5f)
		main.font.draw(main.batch, Localization.get("versionScreen.scroll"), main.camera.viewportWidth * 0.05f,
					   scrollY)
		main.font.data.setScale(1f)
		main.font.draw(main.batch, Localization.get("versionScreen.goToPage"), main.camera.viewportWidth * 0.05f,
					   main.font.capHeight * 3.5f)
		main.font.draw(main.batch, Localization.get("info.back"), main.camera.viewportWidth * 0.05f,
					   main.font.capHeight * 2)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			line = (line + 1).coerceIn(0, Math.max(0, VersionChecker.releaseObject!!.bodyLines.size - MAX_LINES - 1))
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			line = (line - 1).coerceIn(0, Math.max(0, VersionChecker.releaseObject!!.bodyLines.size - MAX_LINES - 1))
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
		line = 0
		if (titlesAvailable > 1) {
			val old = titleType
			while (titleType == old) {
				titleType = MathUtils.random(0, titlesAvailable - 1)
			}
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