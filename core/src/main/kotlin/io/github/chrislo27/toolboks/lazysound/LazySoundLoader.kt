package io.github.chrislo27.toolboks.lazysound

import com.badlogic.gdx.assets.AssetDescriptor
import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.FileHandleResolver
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.Array


class LazySoundLoader(resolver: FileHandleResolver) : SynchronousAssetLoader<LazySound, LazySoundLoaderParameter>(
		resolver) {
	override fun getDependencies(fileName: String?, file: FileHandle?,
								 parameter: LazySoundLoaderParameter?): Array<AssetDescriptor<Any>>? {
		return null
	}

	override fun load(assetManager: AssetManager?, fileName: String?, file: FileHandle,
					  parameter: LazySoundLoaderParameter?): LazySound {
		val ls = LazySound(file)

		if (!LazySound.loadLazilyWithAssetManager)
			ls.sound

		return ls
	}
}

class LazySoundLoaderParameter : AssetLoaderParameters<LazySound>()
