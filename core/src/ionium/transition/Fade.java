package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import ionium.templates.Main;

public class Fade implements Transition {

	protected static final Color REUSED = new Color(0, 0, 0, 1);

	protected float startingTime = 1f;
	protected float timeLeft = 1f;
	protected int color = Color.rgba8888(Color.BLACK);
	protected boolean fadingOut = true;

	public Fade(boolean fadingOut, int color, float time) {
		this.fadingOut = fadingOut;
		this.color = color;
		timeLeft = time;
		startingTime = time;
	}

	@Override
	public boolean finished() {
		return timeLeft <= 0;
	}

	@Override
	public void render(Main main) {
		REUSED.set(color);

		// 0 to startingTime
		float elapsedTime = (startingTime - timeLeft);
		// 0 going to 1 (alpha)
		float blacken = MathUtils.clamp((elapsedTime / startingTime), 0f, 1f);

		main.batch.setColor(REUSED.r, REUSED.g, REUSED.b,
				(fadingOut ? blacken : 1f - blacken));
		Main.fillRect(main.batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		main.batch.setColor(1, 1, 1, 1);

		if (timeLeft > 0) {
			timeLeft -= Gdx.graphics.getDeltaTime();
			if (timeLeft < 0) timeLeft = 0;
		}
	}

	@Override
	public void tickUpdate(Main main) {
	}

}
