package ionium.util;

import com.badlogic.gdx.utils.Pool.Poolable;

public class Coordinate implements Poolable {

	public static final Coordinate global = new Coordinate(0, 0);

	int x = 0;
	int y = 0;

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public Coordinate setPosition(int x, int y) {
		setX(x);
		setY(y);
		return this; // chaining
	}

	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public Coordinate copy() {
		return new Coordinate(x, y);
	}

	@Override
	public void reset() {
		x = 0;
		y = 0;
	}

	public String toString() {
		return "[" + getX() + ", " + getY() + "]";
	}

	public boolean equals(Object obj) {
		if (obj instanceof Coordinate) {
			Coordinate c = (Coordinate) obj;
			if (c.getX() == x && c.getY() == y) {
				return true;
			} else return false;
		} else return false;
	}

}
