package io.github.chrislo27.rhre3.init

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Pixmap
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
        AssetRegistry.loadAsset<Texture>("ui_selector", "images/selector/generic.png")

        AssetRegistry.loadAsset<Texture>("ui_bg", "images/ui/bg.png")

        AssetRegistry.loadAsset<Texture>("tracker_right_tri", "images/ui/tracker_right_triangle.png")
        AssetRegistry.loadAsset<Texture>("tracker_tri", "images/ui/tracker_triangle.png")

        AssetRegistry.loadAsset<Texture>("tool_selection", "images/tool/selection.png")
        AssetRegistry.loadAsset<Texture>("tool_bpm", "images/tool/bpm.png")
        AssetRegistry.loadAsset<Texture>("tool_multipart_split", "images/tool/multipart_split.png")
        AssetRegistry.loadAsset<Texture>("tool_time_signature", "images/tool/time_signature.png")
        AssetRegistry.loadAsset<Texture>("tool_music_volume", "images/tool/music_volume.png")

        AssetRegistry.loadAsset<Texture>("ui_icon_update", "images/ui/icons/update.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_credits", "images/ui/icons/credits.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_updatesfx", "images/ui/icons/update_sfx.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_palette", "images/ui/icons/palette.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_info_button", "images/ui/icons/info_button.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_info", "images/ui/icons/info.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_folder", "images/ui/icons/folder.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_resetwindow", "images/ui/icons/resetwindow.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_fullscreen", "images/ui/icons/fullscreen.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_warn", "images/ui/icons/warn.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_language", "images/ui/icons/language3.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_metronome", "images/ui/icons/metronome.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_back", "images/ui/icons/back.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_new_button", "images/ui/icons/new_button.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_load_button", "images/ui/icons/load_button.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_save_button", "images/ui/icons/save_button.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_newremix", "images/ui/icons/newremix.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_saveremix", "images/ui/icons/saveremix.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_songchoose", "images/ui/icons/songchoose.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_music_button", "images/ui/icons/music_button.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_music_button_muted", "images/ui/icons/music_button_muted.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_uncheckedbox", "images/ui/checkbox/unchecked.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_checkedbox", "images/ui/checkbox/checked.png")
        AssetRegistry.loadAsset<Texture>("ui_icon_xcheckedbox", "images/ui/checkbox/x.png")

        AssetRegistry.loadAsset<Texture>("wakame", "images/wakame.png")
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
        assets["cursor_horizontal_resize"] =
                Gdx.graphics.newCursor(Pixmap(Gdx.files.internal("images/cursor/horizontalResize.png")),
                                       16, 8)
    }

}