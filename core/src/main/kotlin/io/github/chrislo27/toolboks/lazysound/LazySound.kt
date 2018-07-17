package io.github.chrislo27.toolboks.lazysound

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Disposable


/**
 * A lazily loaded sound that can be disposed of and recreated later to save memory.
 */
class LazySound(val handle: FileHandle) : Disposable {

    companion object {
        var loadLazilyWithAssetManager: Boolean = true
        var soundFactory: (FileHandle) -> Sound = { Gdx.audio.newSound(it) }
    }

    private @Volatile var backing: Sound? = null
    @Volatile var disposedOf: Boolean = false
        private set

    val sound: Sound
        get() {
            if (backing == null) {
                load()
            }

            return backing!!
        }

    val isLoaded: Boolean
        get() {
            return backing != null && !disposedOf
        }

    fun load() {
        if (disposedOf) {
            error("Lazy sound already disposed of")
        }
        if (!isLoaded) {
            backing = soundFactory(handle)
        }
    }

    fun unload() {
        if (isLoaded) {
            synchronized(backing!!) {
                backing!!.dispose()
                backing = null
            }
        }
    }

    override fun dispose() {
        unload()
        disposedOf = true
    }

}