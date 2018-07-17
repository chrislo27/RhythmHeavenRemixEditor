package io.github.chrislo27.toolboks.registry

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.lazysound.LazySoundLoader

/**
 * Holds all the assets needed for a game and can be easily disposed of at the end.
 */
object AssetRegistry : Disposable {

    private const val LOAD_STATE_NONE = 0
    private const val LOAD_STATE_LOADING = 1
    private const val LOAD_STATE_DONE = 2

    val manager: AssetManager = AssetManager()
    val unmanagedAssets: MutableMap<String, Any> = mutableMapOf()
    val assetMap: Map<String, String> = mutableMapOf()
    val missingTexture: Texture by lazy {
        val pix = Pixmap(2, 2, Pixmap.Format.RGBA8888)

        pix.setColor(1f, 0f, 1f, 1f)
        pix.drawPixel(0, 0)
        pix.drawPixel(1, 1)

        pix.setColor(0f, 0f, 0f, 1f)
        pix.drawPixel(1, 0)
        pix.drawPixel(0, 1)

        val tex = Texture(pix)
        pix.dispose()
        return@lazy tex
    }

    private val assetLoaders: MutableList<IAssetLoader> = mutableListOf()

    private var loadingState: Int = LOAD_STATE_NONE

    init {
        manager.setLoader(LazySound::class.java, LazySoundLoader(manager.fileHandleResolver))
    }

    fun bindAsset(key: String, file: String): Pair<String, String> {
        if (key.startsWith(Toolboks.TOOLBOKS_ASSET_PREFIX)) {
            throw IllegalArgumentException("$key starts with the Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }
        if (assetMap.containsKey(key)) {
            throw IllegalArgumentException("$key has already been bound to ${assetMap[key]}")
        }

        (assetMap as MutableMap)[key] = file
        return key to file
    }

    internal fun bindToolboksAsset(keyWithoutPrefix: String, file: String): Pair<String, String> {
        val key = Toolboks.TOOLBOKS_ASSET_PREFIX + keyWithoutPrefix
        if (assetMap.containsKey(key)) {
            throw IllegalArgumentException("$key has already been bound to ${assetMap[key]}")
        }

        (assetMap as MutableMap)[key] = file
        return key to file
    }

    inline fun <reified T> loadAsset(key: String, file: String) {
        manager.load(bindAsset(key, file).second, T::class.java)
    }

    internal inline fun <reified T> loadToolboksAsset(key: String, file: String) {
        manager.load(bindToolboksAsset(key, file).second, T::class.java)
    }

    fun addAssetLoader(loader: IAssetLoader) {
        assetLoaders += loader
        val map = mutableMapOf<String, Any>()
        loader.addUnmanagedAssets(map)
        val allStartingWithPrefix = map.keys.filter{ it.startsWith(Toolboks.TOOLBOKS_ASSET_PREFIX) }
        if (allStartingWithPrefix.isNotEmpty()) {
            throw IllegalArgumentException("$allStartingWithPrefix start with the Toolboks asset prefix, which is ${Toolboks.TOOLBOKS_ASSET_PREFIX}")
        }

        unmanagedAssets.putAll(map)
    }

    fun load(delta: Float): Float {
        if (loadingState == LOAD_STATE_NONE) {
            loadingState = LOAD_STATE_LOADING

            assetLoaders.forEach {
                it.addManagedAssets(manager)
            }
        }

        if (manager.update((delta * 1000).coerceIn(0f, Int.MAX_VALUE.toFloat()).toInt())) {
            loadingState = LOAD_STATE_DONE
        }

        return manager.progress
    }

    fun loadBlocking() {
        while (load(Int.MAX_VALUE.toFloat()) < 1f);
    }

    operator fun contains(key: String): Boolean {
        return key in unmanagedAssets || key in assetMap
    }

    inline fun <reified T> containsAsType(key: String): Boolean {
        if (!contains(key))
            return false

        return (unmanagedAssets[key] as T?) != null || manager.isLoaded(assetMap[key], T::class.java)
    }

    inline operator fun <reified T> get(key: String): T {
        val unmanaged = (unmanagedAssets[key] as T?)
        if (unmanaged != null) {
            return unmanaged
        }

        if (assetMap[key] == null) {
            error("Key not found in mappings: $key")
        }

        if (!manager.isLoaded(assetMap[key], T::class.java)) {
            if (T::class === Texture::class) {
                return missingTexture as T
            }
            error("Asset not loaded/found: $key")
        }

        return manager.get(assetMap[key], T::class.java)
    }

    fun stopAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::stop)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded).forEach { it.sound.stop() }
    }

    fun pauseAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::pause)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded).forEach { it.sound.pause() }
    }

    fun resumeAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::resume)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded).forEach { it.sound.resume() }
    }

    override fun dispose() {
        unmanagedAssets.values.filterIsInstance(Disposable::class.java).forEach(Disposable::dispose)
        manager.dispose()
        missingTexture.dispose()
    }

    interface IAssetLoader {

        fun addManagedAssets(manager: AssetManager)

        fun addUnmanagedAssets(assets: MutableMap<String, Any>)

    }

}