package chrislo27.rhre.lazysound

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
		return LazySound(file)
	}
}

class LazySoundLoaderParameter : AssetLoaderParameters<LazySound>()