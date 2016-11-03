package ionium.registry.classmap;

import java.util.HashMap;

/**
 * Class maps match a class to a string key. They are generally used for re-instantiation.
 * 
 *
 * @param <T>
 */
public final class ClassMap<T> {

	private HashMap<String, Class<? extends T>> keyToValue = new HashMap<>();
	private HashMap<Class<? extends T>, String> valueToKey = new HashMap<>();

	public void register(Class<? extends T> clazz, String name) {
		keyToValue.put(name, clazz);
		valueToKey.put(clazz, name);
	}

	public Class<? extends T> getValue(String key) {
		return keyToValue.get(key);
	}

	public String getKey(Class<? extends T> clazz) {
		return valueToKey.get(clazz);
	}

}
