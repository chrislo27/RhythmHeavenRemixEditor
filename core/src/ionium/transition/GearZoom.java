package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import ionium.registry.AssetRegistry;
import ionium.registry.ScreenRegistry;
import ionium.templates.Main;
import ionium.util.MathHelper;
import ionium.util.Utils;
import ionium.util.render.StencilMaskUtil;

public class GearZoom implements Transition {

	/**
	 * How much smaller the inner part of the gear is compared to the entire texture
	 */
	private static final float GEAR_HOLE_SIZE = 178f / 512f;

	private float elapsed = 0;
	private float duration = 0;

	/**
	 * A start transition with a gear zooming out, revealing the next screen underneath.
	 * @param seconds
	 */
	public GearZoom(float seconds) {
		duration = seconds;
	}

	@Override
	public boolean finished() {
		return elapsed >= duration;
	}

	@Override
	public void render(Main main) {
		float percentage = Interpolation.pow2In.apply(elapsed / duration);
		TransitionScreen ts = ScreenRegistry.get("ionium_transition", TransitionScreen.class);

		// old screen is already rendered
		// render gear, and then stencil mask the next one

		float radius;
		if (Gdx.graphics.getWidth() >= Gdx.graphics.getHeight()) {
			radius = percentage * Gdx.graphics.getWidth() * 0.5f;
		} else {
			radius = percentage * Gdx.graphics.getHeight() * 0.5f;
		}

		float texSize = (radius * 2) / GEAR_HOLE_SIZE;

		Utils.drawRotatedCentered(main.batch, AssetRegistry.getTexture("gear"),
				Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f, texSize, texSize,
				MathHelper.getSawtoothWave(2) * 360, true);

		// stencil in the next screen
		main.batch.end();
		StencilMaskUtil.prepareMask();

		main.shapes.begin(ShapeType.Filled);
		main.shapes.circle(Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f, radius);
		main.shapes.end();

		StencilMaskUtil.useMask();

		main.batch.begin();
		main.batch.setColor(0, 0, 0, 1);
		Main.fillRect(main.batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		main.batch.setColor(1, 1, 1, 1);
		main.batch.end();

		if (ts.nextScreen != null) {
			ts.nextScreen.render(Gdx.graphics.getDeltaTime());
		}

		StencilMaskUtil.resetMask();
		main.batch.begin();

		elapsed += Gdx.graphics.getDeltaTime();
		elapsed = Math.min(elapsed, duration);
	}

	@Override
	public void tickUpdate(Main main) {
	}

}
