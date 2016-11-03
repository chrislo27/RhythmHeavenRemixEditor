package ionium.util.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import ionium.registry.AssetRegistry;
import ionium.templates.Main;

public class Gears {

	private Sprite big, small;

	public Gears(Main main) {
		big = new Sprite(AssetRegistry.instance().getUnmanagedTextures().get("gear"));
		small = new Sprite(AssetRegistry.instance().getUnmanagedTextures().get("gear"));

		reset();
	}

	public Gears reset() {
		big.setSize(128, 128);
		small.setSize(64, 64);

		big.setOriginCenter();
		small.setOriginCenter();

		big.setRotation(5);
		small.setRotation(0);
		return this;
	}

	public float smallSpeed = 0.25f;
	public float bigSpeed = 0.125f;

	public void render(Main main, float x, float y) {
		big.setPosition(x, y);
		small.setPosition(x + (118), y);

		big.draw(main.batch);
		small.draw(main.batch);
	}

	public void update(float speed) {
		big.rotate(360 * Gdx.graphics.getDeltaTime() * bigSpeed * speed);
		small.rotate(-(360 * Gdx.graphics.getDeltaTime() * smallSpeed * speed));
	}
}
