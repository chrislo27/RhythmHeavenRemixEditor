package ionium.util;

import java.util.HashMap;

public class AssetMap {

	private static AssetMap instance;

	private AssetMap() {
	}

	public static AssetMap instance() {
		if (instance == null) {
			instance = new AssetMap();
			instance.loadResources();
		}
		return instance;
	}

	private HashMap<String, String> map = new HashMap<>();
	private HashMap<String, String> reverse = new HashMap<>();

	private void loadResources() {
	}

	/**
	 * get name of a path (uses instance())
	 * 
	 * @param key
	 * @return
	 */
	public static String get(String key) {
		return AssetMap.instance().map.get(key);
	}

	public static String getFromValue(String value) {
		return AssetMap.instance.reverse.get(value);
	}

	/**
	 * add a key/value pair
	 * 
	 * @param key
	 * @param value
	 * @return value
	 */
	public static String add(String key, String value) {
		AssetMap.instance().map.put(key, value);
		AssetMap.instance.reverse.put(value, key);
		return value;
	}

	/**
	 * add a key/value pair - parameters swapped!
	 * 
	 * @param value
	 * @param key
	 * @return value
	 */
	public static String addSwap(String value, String key) {
		return add(key, value);
	}

	public static boolean containsKey(String key) {
		return AssetMap.instance().map.containsKey(key);
	}

	public static boolean containsValue(String value) {
		return AssetMap.instance().map.containsValue(value);
	}

}
