package ionium.projection;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class CoordinateF implements Poolable {

	public float x = 0;
	public float y = 0;

	public CoordinateF() {

	}

	public CoordinateF(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public CoordinateF set(float x, float y) {
		this.x = x;
		this.y = y;

		return this;
	}

	public CoordinateF setX(float x) {
		this.x = x;

		return this;
	}

	public CoordinateF setY(float y) {
		this.y = y;

		return this;
	}

	public CoordinateF translateToDepth(float width, float height, Vector3 depth) {
		x += (width * depth.x);
		y += (height * depth.z);

		return this;
	}

	@Override
	public void reset() {
	}

	public static class CoordFPool extends Pool<CoordinateF> {

		public static final CoordFPool pool = new CoordFPool(256);

		public CoordFPool(int max) {
			super(max);
		}

		@Override
		protected CoordinateF newObject() {
			return new CoordinateF();
		}

	}

}
