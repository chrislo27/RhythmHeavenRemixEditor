package ionium.util;

import com.badlogic.gdx.math.Bresenham2;

/**
 * uses libgdx Bresenham2 to raycast (singleton)
 * 
 *
 */
public class Raycaster {

	private static Raycaster instance;

	private Raycaster() {
	}

	public static Raycaster instance() {
		if (instance == null) {
			instance = new Raycaster();
			instance.loadResources();
		}
		return instance;
	}

	private Bresenham2 bre;

	private void loadResources() {
		bre = new Bresenham2();
	}

	public static Bresenham2 get() {
		return instance().bre;
	}

}
