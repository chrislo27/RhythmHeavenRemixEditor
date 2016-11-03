package ionium.registry.handler;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import ionium.animation.Animation;

import java.util.HashMap;

public interface IAssetLoader {
	
	/**
	 * puts the assets to be loaded in the AssetManager
	 * @param manager
	 */
	public void addManagedAssets(AssetManager manager);
	
	/**
	 * Method for adding textures that will be loaded immediately
	 * @param textures
	 */
	public void addUnmanagedTextures(HashMap<String, Texture> textures);
	
	/**
	 * puts the animations to be loaded in the map
	 * @param animations
	 */
	public void addUnmanagedAnimations(HashMap<String, Animation> animations);
	
}
