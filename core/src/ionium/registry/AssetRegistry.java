package ionium.registry;

import chrislo27.rhre.lazysound.LazySound;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import ionium.animation.Animation;
import ionium.audio.captioned.Captioned;
import ionium.audio.captioned.CaptionedLoader;
import ionium.registry.handler.IAssetLoader;
import ionium.registry.handler.StockAssetLoader;
import ionium.util.AssetMap;
import ionium.util.GameException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public final class AssetRegistry implements Disposable {

	private static AssetRegistry instance;

	private AssetRegistry() {
	}

	public static AssetRegistry instance() {
		if (instance == null) {
			instance = new AssetRegistry();
			instance.onInstantiate();
		}
		return instance;
	}

	private Iterator<Entry<String, Animation>> animationLoadingIterator;

	private Array<IAssetLoader> loaders = new Array<>();

	private AssetManager manager = new AssetManager();
	private HashMap<String, Texture> unmanagedTextures = new HashMap<>();
	private HashMap<String, Animation> animations = new HashMap<>();
	private HashMap<String, HashMap<String, AtlasRegion>> atlasRegions = new HashMap<>();

	private Texture missingTexture;

	private Array<Sound> tempSoundArray;
	private Array<LazySound> tempLazySoundArray;
	private Array<Music> tempMusicArray;

	private void onInstantiate() {
		manager.setLoader(Captioned.class, new CaptionedLoader(new InternalFileHandleResolver()));

		addAssetLoader(new StockAssetLoader());
	}

	public Array<IAssetLoader> getAllAssetLoaders() {
		return loaders;
	}

	public AssetManager getAssetManager() {
		return manager;
	}

	public HashMap<String, Texture> getUnmanagedTextures() {
		return unmanagedTextures;
	}

	public HashMap<String, Animation> getAnimations() {
		return animations;
	}

	public AssetRegistry addAssetLoader(IAssetLoader l) {
		loaders.add(l);

		// add the managed textures to the asset manager, the unmanaged textures are loaded separately
		l.addManagedAssets(manager);
		l.addUnmanagedTextures(unmanagedTextures);
		l.addUnmanagedAnimations(animations);

		return this;
	}

	/**
	 * calls the #update(int) method of the internal AssetManager and also loads an animation from the map if found.
	 * The amount of time the manager gets is half of the given time (bigger half) unless the animations are done loading. 
	 * The animations load for half the given time.
	 * <br>
	 * It is not guaranteed that the time this method blocks will be exactly the number of milliseconds given in the parameter.
	 * @param millis
	 */
	public synchronized void loadManagedAssets(int millis) {
		if (animationLoadingIterator == null) createAnimationLoadingIterator();

		int managerTimeShare = (animationLoadingIterator.hasNext() ? (millis / 2 + millis % 2)
				: millis);
		int animationTimeShare = (manager.getProgress() >= 1 ? millis : millis - managerTimeShare);

		if (manager.getProgress() < 1) {
			manager.update(managerTimeShare);
		}

		if (animationLoadingIterator.hasNext()) {
			long time = System.currentTimeMillis();
			while (true) {
				if (System.currentTimeMillis() - time >= animationTimeShare) break;
				if (!animationLoadingIterator.hasNext()) break;
				animationLoadingIterator.next().getValue().load();
			}
		}
	}

	/**
	 * Optional to call, but you should call this when you're done so it can load some extra things such
	 * as the atlas regions for any TextureAtlas ahead of time
	 */
	public void optionalOnFinish() {
		if (finishedLoading() == false) return;

		Array<String> allNames = manager.getAssetNames();

		for (String s : allNames) {
			if (manager.getAssetType(s) == TextureAtlas.class) {
				if (AssetMap.containsValue(s)) {
					getAllAtlasRegions(AssetMap.getFromValue(s));
				}
			}
		}
	}

	public void loadUnmanagedTextures() {
		for (IAssetLoader l : loaders) {
			l.addUnmanagedTextures(unmanagedTextures);
		}
	}

	private void createAnimationLoadingIterator() {
		if (animationLoadingIterator == null) {
			animationLoadingIterator = animations.entrySet().iterator();
		}
	}

	public boolean finishedLoading() {
		if (animationLoadingIterator == null) createAnimationLoadingIterator();

		return (getAssetManager().getProgress() >= 1
				&& animationLoadingIterator.hasNext() == false);
	}

	@Override
	public void dispose() {
		manager.dispose();

		for (Entry<String, Texture> entry : unmanagedTextures.entrySet()) {
			entry.getValue().dispose();
		}

		for (Entry<String, Animation> entry : animations.entrySet()) {
			entry.getValue().dispose();
		}

		if (missingTexture != null) missingTexture.dispose();
	}

	public static Texture getMissingTexture() {
		if (instance().missingTexture == null) {
			throw new GameException(
					"Missing texture not created yet; forgot to call #createMissingTexture in "
							+ instance().getClass().getSimpleName());
		} else {
			return instance().missingTexture;
		}
	}

	public static void createMissingTexture() {
		if (instance().missingTexture != null) return;

		// generate missing texture
		Pixmap pix = new Pixmap(32, 32, Format.RGBA8888);

		// pink
		pix.setColor(1, 0, 1, 1);
		pix.fillRectangle(0, 0, pix.getWidth() / 2, pix.getHeight() / 2);
		pix.fillRectangle(pix.getWidth() / 2, pix.getHeight() / 2, pix.getWidth() / 2,
				pix.getHeight() / 2);

		// black
		pix.setColor(0, 0, 0, 1);
		pix.fillRectangle(pix.getWidth() / 2, 0, pix.getWidth() / 2, pix.getHeight() / 2);
		pix.fillRectangle(0, pix.getHeight() / 2, pix.getWidth() / 2, pix.getHeight() / 2);

		// set to texture
		instance().missingTexture = new Texture(pix);

		pix.dispose();
	}

	/**
	 * uses the instance() method and AssetMap key to return a Texture. It will attempt to search the unmanaged textures map first.
	 * @param key
	 * @return the Texture, searching unmanaged textures first or null if none is found
	 */
	public static Texture getTexture(String key) {
		if (instance().getUnmanagedTextures().get(key) != null) {
			return instance().getUnmanagedTextures().get(key);
		} else {
			if (AssetMap.get(key) == null) {
				return getMissingTexture();
			}

			return getAsset(key, Texture.class);
		}
	}

	public static Sound getSound(String key) {
		return getAsset(key, Sound.class);
	}

	public static Music getMusic(String key) {
		return getAsset(key, Music.class);
	}

	public static Animation getAnimation(String key) {
		return instance().getAnimations().get(key);
	}

	/**
	 * Gets an asset, putting the key through AssetMap.get first
	 * @param key
	 * @param clz
	 * @return
	 */
	public static <T> T getAsset(String key, Class<T> clz) {
		if (AssetMap.get(key) == null) return null;

		return getAssetByPath(AssetMap.get(key), clz);
	}

	/**
	 * Gets an asset by the path provided
	 * @param path
	 * @param clz
	 * @return
	 */
	public static <T> T getAssetByPath(String path, Class<T> clz) {
		return instance().getAssetManager().get(path, clz);
	}

	public static TiledMap getTiledMap(String key) {
		return getAsset(key, TiledMap.class);
	}

	public static TextureAtlas getTextureAtlas(String key) {
		return getAsset(key, TextureAtlas.class);
	}

	public static HashMap<String, AtlasRegion> getAllAtlasRegions(String key) {
		HashMap<String, AtlasRegion> map = instance().atlasRegions.get(key);

		if (map != null) {
			return map;
		} else {
			map = new HashMap<>();

			Array<AtlasRegion> array = getTextureAtlas(key).getRegions();

			for (AtlasRegion ar : array) {
				map.put(ar.name + (ar.index != -1 ? ar.index : ""), ar);
			}

			instance().atlasRegions.put(key, map);
		}

		return map;
	}

	public static AtlasRegion getAtlasRegion(String atlasKey, String section) {
		return getAllAtlasRegions(atlasKey).get(section);
	}

	public void pauseAllSound() {
		if (tempSoundArray == null) {
			tempSoundArray = manager.getAll(Sound.class, new Array<Sound>());
		}

		if (tempLazySoundArray == null) {
			tempLazySoundArray = manager.getAll(LazySound.class, new Array<>());
		}

		for (Sound s : tempSoundArray) {
			s.pause();
		}

		for (LazySound s : tempLazySoundArray) {
			if (s.isLoaded())
				s.getSound().pause();
		}
	}

	public void resumeAllSound() {
		if (tempSoundArray == null) {
			tempSoundArray = manager.getAll(Sound.class, new Array<Sound>());
		}

		if (tempLazySoundArray == null) {
			tempLazySoundArray = manager.getAll(LazySound.class, new Array<>());
		}

		for (Sound s : tempSoundArray) {
			s.resume();
		}

		for (LazySound s : tempLazySoundArray) {
			if (s.isLoaded())
				s.getSound().resume();
		}
	}

	public void stopAllSound() {
		if (tempSoundArray == null) {
			tempSoundArray = manager.getAll(Sound.class, new Array<Sound>());
		}

		if (tempLazySoundArray == null) {
			tempLazySoundArray = manager.getAll(LazySound.class, new Array<>());
		}

		for (Sound s : tempSoundArray) {
			s.stop();
		}

		for (LazySound s : tempLazySoundArray) {
			if (s.isLoaded())
				s.getSound().stop();
		}
	}

	public void pauseAllMusic() {
		if (tempMusicArray == null) {
			tempMusicArray = manager.getAll(Music.class, new Array<Music>());
		}

		for (Music m : tempMusicArray) {
			m.pause();
		}
	}

	public void resumeAllMusic() {
		if (tempMusicArray == null) {
			tempMusicArray = manager.getAll(Music.class, new Array<Music>());
		}

		for (Music m : tempMusicArray) {
			if (m.getPosition() > 0) m.play();
		}
	}

	public void stopAllMusic() {
		if (tempMusicArray == null) {
			tempMusicArray = manager.getAll(Music.class, new Array<Music>());
		}

		for (Music m : tempMusicArray) {
			m.stop();
		}
	}

}
