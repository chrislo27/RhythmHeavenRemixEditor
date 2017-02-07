package chrislo27.rhre

import chrislo27.rhre.version.VersionChecker
import chrislo27.rhre.version.VersionState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.ScreenRegistry
import ionium.screen.Updateable
import ionium.util.i18n.Localization


class VersionScreen(m: Main) : Updateable<Main>(m) {
	override fun render(delta: Float) {
		Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

		main.batch.begin()

		main.biggerFont.setColor(1f, 1f, 1f, 1f)
		main.biggerFont.data.setScale(0.75f)
		main.biggerFont.draw(main.batch,
							 Localization.get("versionScreen.title"),
							 Gdx.graphics.width * 0.05f,
							 Gdx.graphics.height * 0.85f + main.biggerFont.capHeight)
		main.biggerFont.data.setScale(1f)

		main.font.setColor(1f, 1f, 1f, 1f)

		main.font.draw(main.batch, Localization.get(
				"versionScreen.${if (VersionChecker.versionState == VersionState.UP_TO_DATE) "already" else "available"}",
				VersionChecker.releaseObject!!.tag_name,
													ionium.templates.Main.version),
					   Gdx.graphics.width * 0.05f, Gdx.graphics.height * 0.75f)
		main.font.draw(main.batch, Localization.get("versionScreen.coolInfo",
													((VersionChecker.releaseObject!!.assets!!.first().size) / 1048576.0),
													VersionChecker.releaseObject!!.assets!!.first().download_count)
				.format("%.3f"),
					   Gdx.graphics.width * 0.05f, Gdx.graphics.height * 0.75f - main.font.lineHeight)

		var releaseTitleY = Gdx.graphics.height * 0.75f - main.font.lineHeight * 2.5f
		main.font.draw(main.batch,
					   "[#DDDDDD]" + VersionChecker.releaseObject!!.name + "[]",
					   Gdx.graphics.width * 0.05f, releaseTitleY,
					   Gdx.graphics.width * 0.9f, Align.left, true)
		releaseTitleY -= main.font.lineHeight
		main.font.data.setScale(0.75f)
		main.font.draw(main.batch,
					   "[LIGHT_GRAY]" + VersionChecker.releaseObject!!.body + "[]",
					   Gdx.graphics.width * 0.05f, releaseTitleY,
					   Gdx.graphics.width * 0.9f, Align.left, true)
		main.font.data.setScale(1f)

		main.font.draw(main.batch, Localization.get("versionScreen.goToPage"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 4)
		main.font.draw(main.batch, Localization.get("versionScreen.return"), Gdx.graphics.width * 0.05f,
					   main.font.capHeight * 2)

		main.batch.end()
	}

	override fun renderUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
			main.screen = ScreenRegistry.get("editor")
		} else if (Gdx.input.isKeyJustPressed(Input.Keys.V) && VersionChecker.versionState != VersionState.GETTING
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