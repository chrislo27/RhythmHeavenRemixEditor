package chrislo27.rhre.init

import chrislo27.rhre.editor.Editor
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.Texture
import ionium.animation.Animation
import ionium.registry.handler.IAssetLoader
import ionium.util.AssetMap
import java.util.*

class DefAssetLoader : IAssetLoader {
    override fun addManagedAssets(manager: AssetManager) {
        manager.load(AssetMap.add("icon_selector_fever", "images/selector/fever.png"), Texture::class.java)
        manager.load(AssetMap.add("icon_selector_tengoku", "images/selector/tengoku.png"), Texture::class.java)
        manager.load(AssetMap.add("icon_selector_ds", "images/selector/ds.png"), Texture::class.java)

        for (t in Editor.Tool.values()) {
            manager.load(AssetMap.add("tool_icon_" + t.name, "images/tool/" + t.name + ".png"), Texture::class.java)
        }

        manager.load(AssetMap.add("baristron", "images/baristron.png"), Texture::class.java)
        manager.load(AssetMap.add("rhre2icon_16", "images/icon/icon16.png"), Texture::class.java)
        manager.load(AssetMap.add("rhre2icon_32", "images/icon/icon32.png"), Texture::class.java)
        manager.load(AssetMap.add("rhre2icon_64", "images/icon/icon64.png"), Texture::class.java)
        manager.load(AssetMap.add("rhre2icon_128", "images/icon/icon128.png"), Texture::class.java)

        manager.load(AssetMap.add("cannery_music_jingle", "credits/cannery/music/jingle.ogg"), Music::class.java)
        manager.load(AssetMap.add("cannery_music_song", "credits/cannery/music/Cannery.ogg"), Music::class.java)
        manager.load(AssetMap.add("cannery_music_practice", "credits/cannery/music/practice.ogg"), Music::class.java)
        manager.load(AssetMap.add("cannery_tex_titlecard", "credits/cannery/titlecard/cannerytitle.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_bg", "credits/cannery/images/background.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_conveyor", "credits/cannery/images/conveyor.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_closed_can", "credits/cannery/images/closed_can.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_open_can", "credits/cannery/images/open_can.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_beacon_flash", "credits/cannery/images/beacon_flash.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_beacon", "credits/cannery/images/beacon.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_box", "credits/cannery/images/box.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_pipe", "credits/cannery/images/pipe.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_pipe_junction", "credits/cannery/images/pipe_junction.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_pipe_v", "credits/cannery/images/pipe_v.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("cannery_tex_whoosh", "credits/cannery/images/whoosh.png"),
                     Texture::class.java)

        manager.load(AssetMap.add("playyan_walking", "images/playyan/walking.png"),
                     Texture::class.java)
        manager.load(AssetMap.add("playyan_jumping", "images/playyan/jumping_26.png"),
                     Texture::class.java)

        manager.load(AssetMap.add("ui_language", "images/ui/language.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_audacity", "images/ui/audacity.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_infobutton", "images/ui/info.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_beattapper", "images/ui/icons/beattapper.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_cuenumber", "images/ui/icons/cuenumber.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_folder", "images/ui/icons/folder.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_info", "images/ui/icons/info.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_newremix", "images/ui/icons/newremix.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_save", "images/ui/icons/save.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_songchoose", "images/ui/icons/songchoose.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_tapper", "images/ui/icons/tapper.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_tempochnumber", "images/ui/icons/tempochnumber.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_update", "images/ui/icons/update.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_warn", "images/ui/icons/warn.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_script", "images/ui/icons/script.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_resetwindow", "images/ui/icons/resetwindow.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_fullscreen", "images/ui/icons/fullscreen.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_palette", "images/ui/icons/palette.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_metronome", "images/ui/icons/metronome.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_inspections", "images/ui/icons/inspections.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_biginspections", "images/ui/icons/biginspections.png"), Texture::class.java)
        manager.load(AssetMap.add("ui_presentation_mode", "images/ui/icons/presentation_mode.png"), Texture::class.java)
    }

    override fun addUnmanagedTextures(textures: HashMap<String, Texture>) {
        textures.put("logo_2nd", Texture("images/logo/2ND.png"))
        textures.put("logo_d", Texture("images/logo/D.png"))
        textures.put("logo_e", Texture("images/logo/E.png"))
        textures.put("logo_i", Texture("images/logo/I.png"))
        textures.put("logo_m", Texture("images/logo/M.png"))
        textures.put("logo_o", Texture("images/logo/O.png"))
        textures.put("logo_r", Texture("images/logo/R.png"))
        textures.put("logo_t", Texture("images/logo/T.png"))
        textures.put("logo_x", Texture("images/logo/X.png"))
        textures.put("ui_bg", Texture("images/ui/bg.png"))
    }

    override fun addUnmanagedAnimations(animations: HashMap<String, Animation>) {

    }
}
