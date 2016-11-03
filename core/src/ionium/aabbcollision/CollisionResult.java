package ionium.aabbcollision;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;

public class CollisionResult implements Poolable {

	public Vector2 normal = new Vector2();
	public Vector2 newPosition = new Vector2();
	public boolean didCollide = false;
	public int stepsTaken = 0;

	public CollisionResult() {

	}

	@Override
	public void reset() {
		normal.setZero();
		newPosition.setZero();
		didCollide = false;
		stepsTaken = 0;
	}

}
