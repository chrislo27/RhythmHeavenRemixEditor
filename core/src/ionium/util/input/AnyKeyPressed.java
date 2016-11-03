package ionium.util.input;

import com.badlogic.gdx.Gdx;
import ionium.util.Utils;

public class AnyKeyPressed {

	private static final int[] SINGLE_KEY = new int[1];

	public static boolean isAKeyJustPressed(int[] keys) {
		for (int i : keys) {
			if (Gdx.input.isKeyJustPressed(i)) return true;
		}

		return false;
	}

	public static boolean isAKeyPressed(int[] keys) {
		for (int i : keys) {
			if (Gdx.input.isKeyPressed(i)) return true;
		}

		return false;
	}

	public static boolean isAKeyJustReleased(int[] keys) {
		for (int i : keys) {
			if (Utils.isKeyJustReleased(i)) return true;
		}

		return false;
	}

	public static KeyPressType getKeyPressType(int[] keys) {
		if (isAKeyJustPressed(keys)) {
			return KeyPressType.JUST_PRESSED;
		} else if (isAKeyPressed(keys)) {
			return KeyPressType.PRESSED;
		} else if (isAKeyJustReleased(keys)) {
			return KeyPressType.JUST_RELEASED;
		}

		return KeyPressType.NONE;
	}

	public enum KeyPressType {
		JUST_PRESSED, PRESSED, NONE, JUST_RELEASED;
	}

}
