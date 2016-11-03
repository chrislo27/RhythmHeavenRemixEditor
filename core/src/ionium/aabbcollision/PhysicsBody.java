package ionium.aabbcollision;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import ionium.util.MathHelper;

public class PhysicsBody implements Poolable {

	public static final PhysicsBody tmp = new PhysicsBody();
	public static final PhysicsBody tmp2 = new PhysicsBody();
	private static final Rectangle pathHitTempRect = new Rectangle();

	public Rectangle bounds = new Rectangle();
	public Vector2 velocity = new Vector2();

	public PhysicsBody() {
		this(0, 0, 1, 1);
	}

	public PhysicsBody(float x, float y, float w, float h) {
		setBounds(x, y, w, h);
	}

	public PhysicsBody setBounds(float x, float y, float w, float h) {
		bounds.set(x, y, w, h);

		return this;
	}

	public PhysicsBody setVelocity(float x, float y) {
		velocity.set(x, y);

		return this;
	}

	public boolean mayBeHitInPath(float timeScale, PhysicsBody other) {
		pathHitTempRect.set(other.getAreaOfTravel(timeScale));

		return bounds.overlaps(pathHitTempRect) || pathHitTempRect.overlaps(bounds);
	}

	public Rectangle getAreaOfTravel(float timeScale) {
		pathHitTempRect.set(bounds.x, bounds.y, velocity.x * timeScale + bounds.width,
				velocity.y * timeScale + bounds.height);
		MathHelper.normalizeRectangle(pathHitTempRect);

		return pathHitTempRect;
	}

	@Override
	public String toString() {
		return "[" + bounds.toString() + ", " + velocity.toString() + "]";
	}

	@Override
	public void reset() {
		setVelocity(0, 0);
	}

}
