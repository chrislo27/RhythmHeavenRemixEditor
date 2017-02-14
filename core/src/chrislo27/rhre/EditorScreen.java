package chrislo27.rhre;

import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.editor.EditorStageSetup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.utils.Array;
import ionium.screen.Updateable;
import ionium.stage.Stage;

public class EditorScreen extends Updateable<Main> {

	public Editor editor;
	public Stage stage;
	private EditorStageSetup stageSetup;

	public EditorScreen(Main m) {
		super(m);
	}

	@Override
	public void render(float delta) {
		editor.render(main.batch);

		if (stage != null) {
			stage.render(main.batch);
		}
	}

	@Override
	public void renderUpdate() {
		editor.inputUpdate();
		editor.renderUpdate();
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
		if (editor == null)
			editor = new Editor(main);

		if (stageSetup == null) {
			stageSetup = new EditorStageSetup(this);
			stage = stageSetup.getStage();
		}

		stage.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
			InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

			stage.addSelfToInputMultiplexer(plex);
			plex.addProcessor(editor);
		}
	}

	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer && stage != null) {
			Gdx.app.postRunnable(() -> {
				InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

				stage.removeSelfFromInputMultiplexer(plex);
				plex.removeProcessor(editor);
			});
		}
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		if (editor != null)
			editor.dispose();
	}
}
