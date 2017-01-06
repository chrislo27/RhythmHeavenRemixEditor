package chrislo27.rhre.init

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import ionium.animation.Animation
import ionium.registry.handler.IAssetLoader
import ionium.util.AssetMap
import java.util.*

class VisualAssetLoader : IAssetLoader {

	override fun addManagedAssets(manager: AssetManager) {
		manager.load(AssetMap.add("visual_bouncyRoad", "images/visual/bouncy_road.png"), Texture::class.java)
	}

	override fun addUnmanagedTextures(textures: HashMap<String, Texture>) {
	}

	override fun addUnmanagedAnimations(animations: HashMap<String, Animation>) {
	}

}
