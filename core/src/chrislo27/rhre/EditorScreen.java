package chrislo27.rhre;

import chrislo27.rhre.editor.Editor;
import chrislo27.rhre.editor.EditorStageSetup;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import ionium.screen.Updateable;
import ionium.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EditorScreen extends Updateable<Main> implements WhenFilesDropped {

	public Editor editor;
	public Stage stage;
	private EditorStageSetup stageSetup;
	private boolean first = true;

	public EditorScreen(Main m) {
		super(m);
	}

	@Override
	public void render(float delta) {
		attemptMakeEditor();

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
		if (stage != null)
			stage.onResize((int) main.camera.viewportWidth, (int) main.camera.viewportHeight);
	}

	private void attemptMakeEditor() {
		if (editor == null)
			editor = new Editor(main);

		if (stageSetup == null) {
			stageSetup = new EditorStageSetup(this);
			stage = stageSetup.getStage();
		}
	}

	@Override
	public void show() {
		attemptMakeEditor();

		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
			InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

			stage.addSelfToInputMultiplexer(plex);
			plex.addProcessor(editor);
		}

		stage.onResize((int) main.camera.viewportWidth, (int) main.camera.viewportHeight);

		if (first) {
			first = false;
			if (main.getOldSize().getThird()) {
				Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
			} else {
				System.out.println("resizing: " + main.getOldSize());
				Gdx.graphics.setWindowedMode(main.getOldSize().getFirst(), main.getOldSize().getSecond());
			}
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

	@Override
	public void onFilesDropped(@NotNull List<? extends FileHandle> list) {
		if (editor != null)
			editor.onFilesDropped(list);
	}
}
