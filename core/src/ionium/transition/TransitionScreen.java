package ionium.transition;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import ionium.screen.Updateable;
import ionium.templates.Main;

public class TransitionScreen extends Updateable<Main> {

	public TransitionScreen(Main m) {
		super(m);
	}

	Transition fromTransition = null;
	Transition toTransition = null;

	Screen previousScreen = null;
	Screen nextScreen = null;

	boolean onToTransition = false;

	@Override
	public void render(float delta) {

		// finish to transition
		if (toTransition == null) {
			if (onToTransition) {
				finishToTransition();
			}
		} else {
			if ((toTransition.finished() && onToTransition)) {
				finishToTransition();
			}
		}

		if (!onToTransition) {
			if (fromTransition == null) {
				// first transition doesn't exist, skip right ahead
				finishFromStartToTransition();
			} else {
				if (fromTransition.finished()) {
					finishFromStartToTransition();
				}
			}
		}

		if (onToTransition) {
			if (nextScreen != null) {
				nextScreen.render(delta);
			}

			main.batch.begin();
			if (toTransition != null) {
				toTransition.render(main);
			}
			main.batch.end();
		} else {
			if (previousScreen != null && previousScreen != this) {
				previousScreen.render(delta);
			}

			main.batch.begin();
			fromTransition.render(main);
			main.batch.end();
		}

	}

	/**
	 * Called when the first transition is starting, calls the updateable event onTransitionFromStart
	 */
	private void startFromTransition(){
		if (fromTransition == null) {
			onToTransition = true;
		} else {
			if (previousScreen instanceof Updateable) {
				((Updateable) previousScreen).onTransitionFromStart();
			}
		}
	}

	/**
	 * Called when the first transition ends and the next begins. Calls the previous updateable event onTransitionFromEnd(),
	 * and next updateable event onTransitionToStart()
	 */
	private void finishFromStartToTransition() {
		onToTransition = true;

		if (previousScreen != null) {
			if (previousScreen instanceof Updateable) {
				((Updateable) previousScreen).onTransitionFromEnd();
			}
		}

		if (nextScreen != null) {
			if (nextScreen instanceof Updateable) {
				((Updateable) nextScreen).onTransitionToStart();
			}
		}
	}
	
	/**
	 * Called when the final transition ends, calls updateable event onTransitionToEnd()
	 */
	private void finishToTransition() {
		if (nextScreen instanceof Updateable) {
			((Updateable) nextScreen).onTransitionToEnd();
		}
		main.setScreen(nextScreen);
	}

	@Override
	public void tickUpdate() {
		if (onToTransition) {
			if (toTransition != null) toTransition.tickUpdate(main);
		} else {
			if (fromTransition != null) fromTransition.tickUpdate(main);
		}
	}

	public void prepare(Screen p, Transition f, Transition t, Screen n) {
		fromTransition = f;
		previousScreen = p;
		toTransition = t;
		nextScreen = n;
		onToTransition = false;
		
		startFromTransition();
	}

	@Override
	public void getDebugStrings(Array<String> array) {
		array.add("prevScreen: "
				+ (previousScreen == null ? null : previousScreen.getClass().getSimpleName()));
		array.add("nextScreen: "
				+ (nextScreen == null ? null : nextScreen.getClass().getSimpleName()));
		array.add("[" + (!onToTransition ? "GREEN" : "RED") + "]fromTransition: "
				+ (fromTransition == null ? null : fromTransition.getClass().getSimpleName())
				+ "[]");
		array.add("[" + (onToTransition ? "GREEN" : "RED") + "]toTransition: "
				+ (toTransition == null ? null : toTransition.getClass().getSimpleName()) + "[]");
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

	@Override
	public void renderUpdate() {
	}

}
