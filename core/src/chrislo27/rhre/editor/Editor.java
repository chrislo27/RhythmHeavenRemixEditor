package chrislo27.rhre.editor;

import chrislo27.rhre.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

public class Editor extends InputAdapter implements Disposable {

	private final Main main;

	public Editor(Main m) {
		this.main = m;
	}

	public void render(SpriteBatch batch) {
		Gdx.gl.glClearColor(main.palette.getEditorBg().r, main.palette.getEditorBg().g, main.palette.getEditorBg().b,
				1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// entities
		batch.begin();

		batch.setColor(main.palette.getStaffLine());
		for (int i = 0; i < 7; i++) {
			Main.fillRect(batch, 0, 256 + i * 48, Gdx.graphics.getWidth(), 2);
		}
		batch.setColor(1, 1, 1, 1);

		batch.end();

		// picker
		batch.begin();

		// message bar on bottom
		batch.setColor(0, 0, 0, 0.5f);
		Main.fillRect(batch, 0, 0, 1280, 12);
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
