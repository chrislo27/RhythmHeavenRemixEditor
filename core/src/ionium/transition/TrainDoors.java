package ionium.transition;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import ionium.registry.AssetRegistry;
import ionium.templates.Main;

public class TrainDoors implements Transition {

	boolean closing = true;
	boolean complete = false;

	float closePercentage = 0;
	float pausePercentage = 0;
	float speed = 1f;

	public TrainDoors(boolean isClosing) {
		closing = isClosing;
	}

	@Override
	public boolean finished() {
		return complete;
	}

	@Override
	public void render(Main main) {
		renderUpdate(main);

		Texture tex = AssetRegistry.getTexture("transition_door");

		if (closing) {
			main.batch.draw(tex, -(Gdx.graphics.getWidth() / 2f) * (1f - closePercentage), 0,
					Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());

			main.batch.draw(tex, Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 2f)
					* (closePercentage) + (Gdx.graphics.getWidth() / 2f), 0,
					-Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
		} else {
			main.batch.draw(tex, -(Gdx.graphics.getWidth() / 2f) * closePercentage, 0,
					Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());

			main.batch.draw(tex, Gdx.graphics.getWidth() - (Gdx.graphics.getWidth() / 2f)
					* (1f - closePercentage) + (Gdx.graphics.getWidth() / 2f), 0,
					-Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight());
		}
	}

	@Override
	public void tickUpdate(Main main) {
	}

	private void renderUpdate(Main main) {
		if (complete) return;

		// closing doesn't have a linear close, it closes then slows down at the end
		// opening is linear
		// closing also has a pause at the end
		if (closing) {
			// fast until last part
			if (closePercentage >= 0.9325f) {
				closePercentage += Gdx.graphics.getDeltaTime() / 5f * speed;
			} else {
				closePercentage += Gdx.graphics.getDeltaTime() / 0.9f * speed;
			}
		} else {
			closePercentage += Gdx.graphics.getDeltaTime() / 0.9f * speed;
		}

		// complete check
		if (closePercentage >= 1) {
			closePercentage = 1;
			
			if(!closing){
				complete = true;
			}else{
				// closing has a pause for a short period
				if(pausePercentage >= 1){
					complete = true;
				}else{
					pausePercentage += Gdx.graphics.getDeltaTime() / 0.2f * speed;
				}
			}
		}
	}

}
