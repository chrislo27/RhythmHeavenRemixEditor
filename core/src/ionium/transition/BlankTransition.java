package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import ionium.templates.Main;

public class BlankTransition implements Transition {

	float time = 0;
	final Color color;

	public BlankTransition(float duration, Color color) {
		this.color = color;
		time = duration;
	}

	@Override
	public boolean finished() {
		return time <= 0;
	}

	@Override
	public void render(Main main) {
		time -= Gdx.graphics.getDeltaTime();

		Gdx.gl.glClearColor(color.r, color.g, color.b, color.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	@Override
	public void tickUpdate(Main main) {
	}

}
