package ionium.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.utils.Array;
import ionium.templates.Main;

public abstract class Updateable<T extends Main> implements Screen {

	public T main;

	public Updateable(T m) {
		main = m;
	}

	@Override
	public abstract void render(float delta);

	/**
	 * updates once a render call only if this screen is active
	 */
	public abstract void renderUpdate();

	public abstract void tickUpdate();

	public abstract void getDebugStrings(Array<String> array);

	@Override
	public abstract void resize(int width, int height);

	@Override
	public abstract void show();

	@Override
	public abstract void hide();

	@Override
	public abstract void pause();

	@Override
	public abstract void resume();

	@Override
	public abstract void dispose();

	/**
	 * Called when a transition starts where this screen is the prior one.
	 */
	public void onTransitionFromStart() {

	}

	/**
	 * Called when a transition ends where this screen is the prior one.
	 */
	public void onTransitionFromEnd() {

	}

	/**
	 * Called when a transition starts where this screen is the transitioning-to one.
	 */
	public void onTransitionToStart() {

	}
	
	/**
	 * Called when a transition ends where this screen is the transitioning-to one.
	 */
	public void onTransitionToEnd() {

	}

	public void debug(String message) {
		Main.logger.debug(message);
	}

	public void debug(String message, Exception exception) {
		Main.logger.debug(message, exception);
	}

	public void info(String message) {
		Main.logger.info(message);
	}

	public void info(String message, Exception exception) {
		Main.logger.info(message, exception);
	}

	public void error(String message) {
		Main.logger.error(message);
	}

	public void error(String message, Throwable exception) {
		Main.logger.error(message, exception);
	}

	public void warn(String message) {
		Main.logger.warn(message);
	}

	public void warn(String message, Throwable exception) {
		Main.logger.warn(message, exception);
	}

	/**
	 * Called when scrolled. Negative amount means scrolled up.
	 * @param amount
	 */
	public boolean onScrolled(int amount) {
		return false;
	}

}
