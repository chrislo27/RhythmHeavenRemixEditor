package chrislo27.rhre.entity;

import chrislo27.rhre.track.Remix;
import com.badlogic.gdx.math.Rectangle;

public class Entity {

	public static final int PX_HEIGHT = 48;
	public static final int PX_WIDTH = PX_HEIGHT * 2;

	public final Rectangle bounds = new Rectangle();
	public final Remix remix;

	public Entity(Remix remix) {
		this.remix = remix;
	}

	public void onStart() {

	}

	public void onEnd() {

	}

	public void onWhile() {

	}

}
