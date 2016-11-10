package chrislo27.rhre.editor;

import chrislo27.rhre.Main;
import chrislo27.rhre.entity.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class Editor extends InputAdapter implements Disposable {

	public static final int MESSAGE_BAR_HEIGHT = 12;
	public static final int PICKER_HEIGHT = 160;
	public static final int GAME_TAB_HEIGHT = 24;
	public static final int GAME_ICON_SIZE = 32;
	public static final int GAME_ICON_PADDING = 8;
	public static final int STAFF_START_Y = MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + GAME_TAB_HEIGHT + 32;
	public static final int TRACK_COUNT = 5;

	private final Main main;
	private final OrthographicCamera camera;

	public Editor(Main m) {
		this.main = m;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 16, 9);
	}

	public void render(SpriteBatch batch) {
		Gdx.gl.glClearColor(main.palette.getEditorBg().r, main.palette.getEditorBg().g, main.palette.getEditorBg().b,
				1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// entities
		batch.begin();

		batch.setColor(main.palette.getStaffLine());
		for (int i = 0; i < TRACK_COUNT + 1; i++) {
			Main.fillRect(batch, 0, STAFF_START_Y + i * Entity.PX_HEIGHT,
					Gdx.graphics.getWidth(), 2);
		}
		batch.setColor(1, 1, 1, 1);

		// message bar on bottom
		batch.setColor(0, 0, 0, 0.5f);
		Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), MESSAGE_BAR_HEIGHT);
		Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), PICKER_HEIGHT + MESSAGE_BAR_HEIGHT);
		batch.setColor(1, 1, 1, 1);
		main.font.setColor(1, 1, 1, 1);
		main.font.getData().setScale(0.5f);
		main.font.draw(batch, "testtesttesttest", 2, 2 + main.font.getCapHeight());
		main.font.getData().setScale(1);

		batch.end();
	}

	public void renderUpdate() {

	}

	public void inputUpdate() {

	}

	@Override
	public void dispose() {

	}
}
