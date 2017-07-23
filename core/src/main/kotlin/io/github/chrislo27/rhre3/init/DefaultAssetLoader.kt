package io.github.chrislo27.rhre3.init

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.toolboks.registry.AssetRegistry


class DefaultAssetLoader : AssetRegistry.IAssetLoader {

    override fun addManagedAssets(manager: AssetManager) {
        AssetRegistry.loadAsset<Texture>("ui_bg", "images/ui/bg.png")

        AssetRegistry.loadAsset<Texture>("ui_icon_updatesfx", "images/ui/icons/update_sfx.png")
    }

    override fun addUnmanagedAssets(assets: MutableMap<String, Any>) {
        run {
            val sizes: List<Int> = listOf(512, 256, 128, 64, 32, 24, 16)
            sizes.forEach {
                assets[AssetRegistry.bindAsset("rhre3_icon_$it", "images/icon/$it.png").first] = Texture(
                        "images/icon/$it.png")
            }
        }
    }

}