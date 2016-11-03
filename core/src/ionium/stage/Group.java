package ionium.stage;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import ionium.templates.Main;

public class Group extends Actor {

	private Array<Actor> children = new Array<>();
	private Array<Actor> pressedActors = new Array<>();

	public Group(Stage s) {
		super(s);
		setScreenOffsetSize(1, 1);
	}

	@Override
	public Group setEnabled(boolean enabled) {
		super.setEnabled(enabled);

		for (int i = 0; i < children.size; i++) {
			Actor act = children.get(i);
			act.setEnabled(enabled);
		}

		return this;
	}

	@Override
	public Group setVisible(boolean visible) {
		super.setVisible(visible);

		for (int i = 0; i < children.size; i++) {
			Actor act = children.get(i);
			act.setVisible(visible);
		}

		return this;
	}

	@Override
	public void onClicked(float x, float y) {
		super.onClicked(x, y);

		stage.setVectorToMouse(Gdx.input.getX(), Gdx.input.getY(), Stage.tmpVec3);

		pressedActors.clear();

		for (int i = 0; i < children.size; i++) {
			Actor act = children.get(i);
			if (act.isEnabled() && stage.isMouseOver(Stage.tmpVec3.x, Stage.tmpVec3.y, act)) {
				pressedActors.add(act);
				act.onClicked((Stage.tmpVec3.x - act.getX()) / act.getWidth(),
						(Stage.tmpVec3.y - act.getY()) / act.getHeight());
			}
		}

	}

	@Override
	public void onClickRelease(float x, float y) {
		super.onClickRelease(x, y);

		stage.setVectorToMouse(Gdx.input.getX(), Gdx.input.getY(), Stage.tmpVec3);

		for (int i = pressedActors.size - 1; i >= 0; i--) {
			Actor act = pressedActors.get(i);

			act.onClickRelease((Stage.tmpVec3.x - act.getX()) / act.getWidth(),
					(Stage.tmpVec3.y - act.getY()) / act.getHeight());

			pressedActors.removeValue(act, true);
		}
	}

	@Override
	public void onMouseDrag(float x, float y) {
		super.onMouseDrag(x, y);

		checkMouseStillOnActors(true);
	}

	@Override
	public void onMouseMove(float x, float y) {
		super.onMouseMove(x, y);

		stage.setVectorToMouse(Gdx.input.getX(), Gdx.input.getY(), Stage.tmpVec3);

		for (int i = children.size - 1; i >= 0; i--) {
			Actor act = children.get(i);

			if (!act.isVisible()) continue;

			float actorLocalX = (Stage.tmpVec3.x - act.getX()) / act.getWidth();
			float actorLocalY = (Stage.tmpVec3.y - act.getY()) / act.getHeight();

			act.onMouseMove(actorLocalX, actorLocalY);
		}
	}

	private void checkMouseStillOnActors(boolean checkDrag) {
		stage.setVectorToMouse(Gdx.input.getX(), Gdx.input.getY(), Stage.tmpVec3);

		for (int i = pressedActors.size - 1; i >= 0; i--) {
			Actor act = pressedActors.get(i);

			float actorLocalX = (Stage.tmpVec3.x - act.getX()) / act.getWidth();
			float actorLocalY = (Stage.tmpVec3.y - act.getY()) / act.getHeight();

			if (checkDrag) act.onMouseDrag(actorLocalX, actorLocalY);

			if (!act.isEnabled() || !stage.isMouseOver(Stage.tmpVec3.x, Stage.tmpVec3.y, act)) {

				act.onClickRelease(actorLocalX, actorLocalY);

				pressedActors.removeIndex(i);
			}
		}
	}

	public <T extends Actor> T addActor(T actor) {
		if (actor == null) return actor;
		if (actor == this) return null;

		children.add(actor);

		return actor;
	}

	public <T extends Actor> T removeActor(T actor) {
		if (actor == null) return null;
		if (actor == this) return null;

		pressedActors.removeValue(actor, true);
		if (children.removeValue(actor, true)) return actor;

		return null;
	}

	@Override
	public void updateActualPosition() {
		super.updateActualPosition();

		for (int i = 0; i < children.size; i++) {
			children.get(i).getViewport().set(getX(), getY(), getWidth(), getHeight());
			children.get(i).updateActualPosition();
		}

		checkMouseStillOnActors(false);
	}

	@Override
	public void render(SpriteBatch batch, float alpha) {
		for (int i = 0; i < children.size; i++) {
			if (children.get(i).isVisible())
				children.get(i).render(batch, children.get(i).getAlpha() * alpha);
		}
	}

	@Override
	public void renderDebug(SpriteBatch batch) {
		for (int i = 0; i < children.size; i++) {
			children.get(i).renderDebug(batch);
		}

		batch.setColor(0, 1, 0, 1);
		Main.drawRect(batch, getX(), getY(), getWidth(), getHeight(), 1);
		batch.setColor(1, 1, 1, 1);
	}

	@Override
	public void onResize(int width, int height) {
		super.onResize(width, height);

		for (int i = 0; i < children.size; i++) {
			children.get(i).onResize(width, height);
		}

		updateActualPosition();
	}

	@Override
	public boolean onKeyAction(int key) {
		boolean worked = false;

		for (int i = 0; i < children.size; i++) {
			Actor act = children.get(i);
			if (act.isEnabled() && act.isVisible()) {
				worked |= act.onKeyAction(key);
			}
		}

		return worked;
	}

}
