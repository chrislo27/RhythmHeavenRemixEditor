package ionium.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import ionium.templates.Main;
import ionium.util.MathHelper;

public class Stage implements InputProcessor {

	protected static final Vector3 tmpVec3 = new Vector3();

	private Array<Actor> actors = new Array<>();
	private Array<Actor> pressedActors = new Array<>();
	private OrthographicCamera camera;

	public boolean debugMode = false;

	public Stage() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	}

	public <T extends Actor> T addActor(T actor) {
		if (actor == null) return actor;

		actors.add(actor);

		return actor;
	}

	public <T extends Actor> T removeActor(T actor) {
		if (actor == null) return null;

		pressedActors.removeValue(actor, true);
		if (actors.removeValue(actor, true)) return actor;

		return null;
	}

	public void removeAllActors() {
		actors.clear();
		pressedActors.clear();
	}

	public void setAllVisible(boolean visible) {
		for (int i = 0; i < actors.size; i++) {
			actors.get(i).setVisible(visible);
		}
	}

	public void setAllEnabled(boolean enabled) {
		for (int i = 0; i < actors.size; i++) {
			actors.get(i).setEnabled(enabled);
		}
	}

	/**
	 * Sets the batch projection matrix to the stage's camera matrix and renders
	 * @param batch
	 */
	public void render(SpriteBatch batch) {
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		for (int i = 0; i < actors.size; i++) {
			if (!actors.get(i).isVisible()) continue;

			actors.get(i).render(batch, actors.get(i).getAlpha());
		}

		if (!debugMode) {
			batch.end();
			return;
		}

		batch.setColor(1, 0, 0, 1);
		Main.drawRect(batch, 0, 0, camera.viewportWidth, camera.viewportHeight, 1);

		for (int i = 0; i < actors.size; i++) {
			Actor act = actors.get(i);

			act.renderDebug(batch);
		}

		batch.setColor(1, 1, 1, 1);
		batch.end();

	}

	public void onResize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();

		for (int i = 0; i < actors.size; i++) {
			actors.get(i).onResize(width, height);
		}
	}

	public OrthographicCamera getCamera() {
		return camera;
	}

	public boolean isMouseOver(Actor act) {
		setVectorToMouse(Gdx.input.getX(), Gdx.input.getY(), tmpVec3);

		return isMouseOver(tmpVec3.x, tmpVec3.y, act);
	}

	public boolean isMouseOver(float x, float y, Actor act) {
		if (!act.isVisible()) return false;

		return MathHelper.intersects(x, y, 1, 1, act.getX(), act.getY(), act.getWidth(),
				act.getHeight(), false);
	}

	// input processor stuff

	public Vector3 setVectorToMouse(float x, float y, Vector3 vec3) {
		return vec3.set(camera.unproject(vec3.set(x, y, 0)));
	}

	public void addSelfToInputMultiplexer(InputMultiplexer plex) {
		plex.addProcessor(0, this);
	}

	public void removeSelfFromInputMultiplexer(InputMultiplexer plex) {
		plex.removeProcessor(this);
	}

	@Override
	public boolean keyDown(int keycode) {
		boolean worked = false;

		for (int i = 0; i < actors.size; i++) {
			Actor act = actors.get(i);
			if (act.isEnabled() && act.isVisible()) {
				worked |= act.onKeyAction(keycode);
			}
		}

		return worked;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		setVectorToMouse(screenX, screenY, tmpVec3);

		if (button == Buttons.LEFT) {
			pressedActors.clear();

			boolean worked = false;

			for (int i = 0; i < actors.size; i++) {
				Actor act = actors.get(i);
				if (act.isEnabled() && isMouseOver(tmpVec3.x, tmpVec3.y, act)) {
					pressedActors.add(act);
					act.onClicked((tmpVec3.x - act.getX()) / act.getWidth(),
							(tmpVec3.y - act.getY()) / act.getHeight());

					worked = true;
				}
			}

			return worked;
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		setVectorToMouse(screenX, screenY, tmpVec3);

		if (button == Buttons.LEFT) {
			for (int i = pressedActors.size - 1; i >= 0; i--) {
				Actor act = pressedActors.get(i);

				act.onClickRelease((tmpVec3.x - act.getX()) / act.getWidth(),
						(tmpVec3.y - act.getY()) / act.getHeight());

				pressedActors.removeIndex(i);
			}
		}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		setVectorToMouse(screenX, screenY, tmpVec3);

		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
			for (int i = pressedActors.size - 1; i >= 0; i--) {
				Actor act = pressedActors.get(i);

				float actorLocalX = (tmpVec3.x - act.getX()) / act.getWidth();
				float actorLocalY = (tmpVec3.y - act.getY()) / act.getHeight();

				act.onMouseDrag(actorLocalX, actorLocalY);

				if (!act.isEnabled() || !isMouseOver(tmpVec3.x, tmpVec3.y, act)) {

					act.onClickRelease(actorLocalX, actorLocalY);
					pressedActors.removeIndex(i);
				}
			}
		}

		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		setVectorToMouse(screenX, screenY, tmpVec3);

		for (int i = actors.size - 1; i >= 0; i--) {
			Actor act = actors.get(i);

			if (!act.isVisible()) continue;

			float actorLocalX = (tmpVec3.x - act.getX()) / act.getWidth();
			float actorLocalY = (tmpVec3.y - act.getY()) / act.getHeight();

			act.onMouseMove(actorLocalX, actorLocalY);
		}

		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

}
