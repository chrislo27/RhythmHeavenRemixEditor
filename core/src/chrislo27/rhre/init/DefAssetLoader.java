package chrislo27.rhre.init;

import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.registry.Series;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import ionium.animation.Animation;
import ionium.registry.handler.IAssetLoader;
import ionium.util.AssetMap;

import java.util.HashMap;

public class DefAssetLoader implements IAssetLoader {
	@Override
	public void addManagedAssets(AssetManager manager) {
		manager.load(AssetMap.add("icon_selector_fever", "images/selector/fever.png"), Texture.class);
		manager.load(AssetMap.add("icon_selector_tengoku", "images/selector/tengoku.png"), Texture.class);
		manager.load(AssetMap.add("icon_selector_ds", "images/selector/ds.png"), Texture.class);
		manager.load(AssetMap.add("inspectionIcon", "images/inspection.png"), Texture.class);

		for (Editor.Tool t : Editor.Tool.values()) {
			manager.load(AssetMap.add("tool_icon_" + t.name(), "images/tool/" + t.name() + ".png"), Texture.class);
		}

		for (Series s : Series.values()) {
			manager.load(AssetMap.add("series_icon_" + s.name(), "images/series/" + s.name() + ".png"), Texture.class);
		}

		manager.load(AssetMap.add("ptr_whole", "images/ptr/full.png"), Texture.class);

		manager.load(AssetMap.add("ui_language", "images/ui/language.png"), Texture.class);
		manager.load(AssetMap.add("ui_audacity", "images/ui/audacity.png"), Texture.class);
		manager.load(AssetMap.add("ui_infobutton", "images/ui/info.png"), Texture.class);
		manager.load(AssetMap.add("ui_beattapper", "images/ui/icons/beattapper.png"), Texture.class);
		manager.load(AssetMap.add("ui_cuenumber", "images/ui/icons/cuenumber.png"), Texture.class);
		manager.load(AssetMap.add("ui_folder", "images/ui/icons/folder.png"), Texture.class);
		manager.load(AssetMap.add("ui_info", "images/ui/icons/info.png"), Texture.class);
		manager.load(AssetMap.add("ui_newremix", "images/ui/icons/newremix.png"), Texture.class);
		manager.load(AssetMap.add("ui_save", "images/ui/icons/save.png"), Texture.class);
		manager.load(AssetMap.add("ui_songchoose", "images/ui/icons/songchoose.png"), Texture.class);
		manager.load(AssetMap.add("ui_tapper", "images/ui/icons/tapper.png"), Texture.class);
		manager.load(AssetMap.add("ui_tempochnumber", "images/ui/icons/tempochnumber.png"), Texture.class);
		manager.load(AssetMap.add("ui_update", "images/ui/icons/update.png"), Texture.class);
		manager.load(AssetMap.add("ui_warn", "images/ui/icons/warn.png"), Texture.class);
	}

	@Override
	public void addUnmanagedTextures(HashMap<String, Texture> textures) {
		textures.put("logo_2nd", new Texture("images/logo/2ND.png"));
		textures.put("logo_d", new Texture("images/logo/D.png"));
		textures.put("logo_e", new Texture("images/logo/E.png"));
		textures.put("logo_i", new Texture("images/logo/I.png"));
		textures.put("logo_m", new Texture("images/logo/M.png"));
		textures.put("logo_o", new Texture("images/logo/O.png"));
		textures.put("logo_r", new Texture("images/logo/R.png"));
		textures.put("logo_t", new Texture("images/logo/T.png"));
		textures.put("logo_x", new Texture("images/logo/X.png"));
		textures.put("ui_bg", new Texture("images/ui/bg.png"));
	}

	@Override
	public void addUnmanagedAnimations(HashMap<String, Animation> animations) {

	}
}
