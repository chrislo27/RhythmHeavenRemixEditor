package ionium.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import ionium.registry.GlobalVariables;
import ionium.templates.Main;

public class MiscLoadingScreen extends Updateable<Main> {

	public MiscLoadingScreen(Main m) {
		super(m);
	}

	@Override
	public void render(float delta) {
		Gdx.gl20.glClearColor(0, 0, 0, 1);
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT);

		main.batch.begin();
		Main.gears.render(main, GlobalVariables.defaultWidth - (192), 0);
		main.batch.end();
	}

	@Override
	public void tickUpdate() {
	}

	@Override
	public void getDebugStrings(Array<String> array) {

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
