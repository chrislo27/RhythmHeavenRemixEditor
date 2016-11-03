package ionium.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class BiObjectMap<K, V> {

	private ObjectMap<K, V> valuesMap = new ObjectMap<>();
	private ObjectMap<V, K> reverseMap = new ObjectMap<>();

	private Array<V> allValues = new Array<>();
	private Array<K> allKeys = new Array<>();

	public BiObjectMap() {

	}

	public V getValue(K key) {
		return valuesMap.get(key);
	}

	public K getKey(V value) {
		return reverseMap.get(value);
	}

	public Array<K> getAllKeys() {
		return allKeys;
	}

	public Array<V> getAllValues() {
		return allValues;
	}

	public void put(K key, V value) {
		valuesMap.put(key, value);
		reverseMap.put(value, key);

		allKeys.add(key);
		allValues.add(value);
	}

	public V remove(K key) {
		if (!valuesMap.containsKey(key)) return null;

		V value = valuesMap.get(key);

		valuesMap.remove(key);
		reverseMap.remove(value);

		allValues.removeValue(value, false);
		allKeys.removeValue(key, false);

		return value;
	}

}
