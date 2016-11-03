package ionium.registry.handler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import ionium.animation.Animation;
import ionium.util.AssetMap;

import java.util.HashMap;

public class StockAssetLoader implements IAssetLoader {

	@Override
	public void addManagedAssets(AssetManager manager) {
		manager.load(AssetMap.add("ionium_ui-icons", "images/ui/ui-icons.pack"),
				TextureAtlas.class);
	}

	@Override
	public void addUnmanagedTextures(HashMap<String, Texture> textures) {
		// misc

		// unmanaged textures
		textures.put("gear", new Texture("images/gear.png"));

	}

	@Override
	public void addUnmanagedAnimations(HashMap<String, Animation> animations) {
		// animations
	}

}
