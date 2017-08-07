package io.github.chrislo27.rhre3.init

import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.toolboks.registry.AssetRegistry


class DefaultAssetLoader : AssetRegistry.IAssetLoader {

    override fun addManagedAssets(manager: AssetManager) {
        Series.VALUES.forEach {
            AssetRegistry.loadAsset<Texture>(it.textureId, it.texturePath)
        }
        AssetRegistry.loadAsset<Texture>("ui_selector_fever", "images/selector/fever.png")
        AssetRegistry.loadAsset<Texture>("ui_selector_tengoku", "images/selector/tengoku.png")
        AssetRegistry.loadAsset<Texture>("ui_selector_ds", "images/selector/ds.png")

        AssetRegistry.loadAsset<Texture>("ui_bg", "images/ui/bg.png")

        AssetRegistry.loadAsset<Texture>("ui_icon_updatesfx", "images/ui/icons/update_sfx.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_palette", "images/ui/icons/palette.png")
    }

    override fun addUnmanagedAssets(assets: MutableMap<String, Any>) {
        run {
            val sizes: List<Int> = listOf(512, 256, 128, 64, 32, 24, 16)
            sizes.forEach {
                assets[AssetRegistry.bindAsset("rhre3_icon_$it", "images/icon/$it.png").first] = Texture(
                        "images/icon/$it.png")
            }
        }
        assets[AssetRegistry.bindAsset("ui-icons", "images/ui/ui-icons.pack").first] =
                TextureAtlas("images/ui/ui-icons.pack")
    }

}