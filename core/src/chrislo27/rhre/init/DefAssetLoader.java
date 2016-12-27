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
	}

	@Override
	public void addUnmanagedTextures(HashMap<String, Texture> textures) {

	}

	@Override
	public void addUnmanagedAnimations(HashMap<String, Animation> animations) {

	}
}
