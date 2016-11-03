package ionium.util;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class CoordPool {

	private static CoordPool instance;

	private CoordPool() {
	}

	private final Pool<Coordinate> coordPool = new Pool<Coordinate>(128) {

		public Coordinate newObject() {
			return new Coordinate(0, 0);
		}
	};

	public static CoordPool instance() {
		if (instance == null) {
			instance = new CoordPool();
			instance.loadResources();
		}
		return instance;
	}

	public static Coordinate obtain() {
		return instance().getPool().obtain();
	}

	public static void free(Coordinate c) {
		instance().getPool().free(c);
	}

	public static void freeAll(Array<Coordinate> array) {
		instance().getPool().freeAll(array);
	}

	public Pool<Coordinate> getPool() {
		return coordPool;
	}

	private void loadResources() {

	}

}
