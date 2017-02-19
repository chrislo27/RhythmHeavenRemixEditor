package chrislo27.rhre.lazysound

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable


class LazySound(handle: FileHandle) : Disposable {

	companion object {
		var forceLoadNow: Boolean = false
	}

	val sound: Sound by lazy {
//		val t = System.nanoTime()
		val s = Gdx.audio.newSound(handle)
		isLoaded = true
//		println("loaded $handle in ${(System.nanoTime() - t) / 1000000f} ms")
		return@lazy s
	}

	var isLoaded: Boolean = false
		private set

	override fun dispose() {
		if (isLoaded)
			sound.dispose()
	}
}