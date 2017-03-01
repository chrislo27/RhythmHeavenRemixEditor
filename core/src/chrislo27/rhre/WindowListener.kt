package chrislo27.rhre

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowAdapter


class WindowListener(val main: Main) : Lwjgl3WindowAdapter() {

	override fun filesDropped(files: Array<out String>) {
		if (main.screen is WhenFilesDropped) {
			(main.screen as WhenFilesDropped).onFilesDropped(files.map { Gdx.files.absolute(it) })
		}
	}

}