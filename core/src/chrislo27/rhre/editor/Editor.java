package chrislo27.rhre.editor;

import chrislo27.rhre.Main;
import chrislo27.rhre.entity.Entity;
import chrislo27.rhre.track.Remix;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import ionium.util.MathHelper;

public class Editor extends InputAdapter implements Disposable {

	public static final int MESSAGE_BAR_HEIGHT = 12;
	public static final int PICKER_HEIGHT = 160;
	public static final int GAME_TAB_HEIGHT = 24;
	public static final int GAME_ICON_SIZE = 32;
	public static final int GAME_ICON_PADDING = 8;
	public static final int OVERVIEW_HEIGHT = 32;
	public static final int STAFF_START_Y = MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + GAME_TAB_HEIGHT + OVERVIEW_HEIGHT +
			32;
	public static final int TRACK_COUNT = 5;

	private final Main main;
	private final OrthographicCamera camera = new OrthographicCamera();
	private final Vector3 vec3Tmp = new Vector3();
	private final Vector3 vec3Tmp2 = new Vector3();
	private Remix remix;
	/**
	 * null = not selecting
	 */
	private Vector2 selectionOrigin = null;
	private Vector3 cameraPickVec3 = new Vector3();

	public Editor(Main m) {
		this.main = m;
		camera.setToOrtho(false, 1280, 720);
		camera.position.x = 0.333f * camera.viewportWidth;

		remix = new Remix();
	}

	public Entity getEntityAtPoint(float x, float y) {
		camera.unproject(cameraPickVec3.set(x, y, 0));

		return remix.entities.stream()
				.filter(e -> e.bounds.contains(cameraPickVec3.x / Entity.PX_WIDTH, cameraPickVec3.y / Entity
						.PX_HEIGHT))
				.findFirst().orElse(null);
	}

	public Entity getEntityAtMouse() {
		return getEntityAtPoint(Gdx.input.getX(), Gdx.input.getY());
	}

	public void render(SpriteBatch batch) {
		Gdx.gl.glClearColor(main.palette.getEditorBg().r, main.palette.getEditorBg().g, main.palette.getEditorBg().b,
				1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.position.y = (camera.viewportHeight * 0.5f) - STAFF_START_Y;
		camera.update();
		batch.setProjectionMatrix(camera.combined);
		// entities
		batch.begin();

		// don't replace with foreach call b/c of performance
		Rectangle.tmp.set((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH,
				(camera.position.y - camera.viewportHeight * 0.5f) / Entity.PX_HEIGHT,
				(camera.viewportWidth) / Entity.PX_WIDTH, (camera.viewportHeight) / Entity.PX_HEIGHT);
		for (Entity e : remix.entities) {
			if (e.bounds.overlaps(Rectangle.tmp)) {
				e.render(main, main.palette, batch, remix.selection.contains(e));
			}
		}

		// staff lines
		{
			batch.setColor(1, 1, 1, 1);

			final float yOffset = -1;

			batch.setColor(main.palette.getStaffLine());
			for (int i = 0; i < TRACK_COUNT + 1; i++) {
				Main.fillRect(batch, camera.position.x - camera.viewportWidth * 0.5f, yOffset + i * Entity.PX_HEIGHT,
						camera.viewportWidth, 2);
			}

			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				batch.setColor(main.palette.getStaffLine());
				batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
						batch.getColor().a * (x == 0 ? 1f : (x < 0 ? 0.25f : 0.5f)));

				Main.fillRect(batch, x * Entity.PX_WIDTH, yOffset, 2, TRACK_COUNT * Entity.PX_HEIGHT);
			}

			main.font.setColor(main.palette.getStaffLine());
			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				main.font.draw(batch, x + "", x * Entity.PX_WIDTH,
						TRACK_COUNT * Entity.PX_HEIGHT + main.font.getCapHeight() + 4, 0, Align.center, false);
			}

			batch.setColor(1, 1, 1, 1);
		}

		// selection rect
		if (selectionOrigin != null) {
			camera.unproject(vec3Tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			batch.setColor(main.palette.getSelectionFill());
			Main.fillRect(batch, selectionOrigin.x, selectionOrigin.y, vec3Tmp.x - selectionOrigin.x,
					vec3Tmp.y - selectionOrigin.y);
			batch.setColor(main.palette.getSelectionBorder());
			Main.drawRect(batch, selectionOrigin.x, selectionOrigin.y, vec3Tmp.x - selectionOrigin.x,
					vec3Tmp.y - selectionOrigin.y, 4);
			batch.setColor(1, 1, 1, 1);
		}

		batch.end();

		// ------------------------------------------------------------------------------------------------------------

		batch.setProjectionMatrix(main.camera.combined);
		batch.begin();

		// message bar on bottom
		{
			batch.setColor(0, 0, 0, 0.5f);
			Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), MESSAGE_BAR_HEIGHT);
			Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), PICKER_HEIGHT + MESSAGE_BAR_HEIGHT);
			Main.fillRect(batch, 0, Gdx.graphics.getHeight() - EditorStageSetup.BAR_HEIGHT, Gdx.graphics.getWidth(),
					EditorStageSetup.BAR_HEIGHT);
			batch.setColor(1, 1, 1, 1);
			main.font.setColor(1, 1, 1, 1);
			main.font.getData().setScale(0.5f);
			main.font.draw(batch, "Palette: " + main.palette.getClass().getSimpleName(), 2,
					2 + main.font.getCapHeight());
			main.font.getData().setScale(1);
		}

		batch.end();
	}

	public void renderUpdate() {

	}

	public void inputUpdate() {
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			camera.position.x -= Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			camera.position.x += Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime();
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			Entity possible = getEntityAtPoint(screenX, screenY);
			camera.unproject(cameraPickVec3.set(screenX, screenY, 0));

			if (possible == null) {
				// start selection
				selectionOrigin = new Vector2(cameraPickVec3.x, cameraPickVec3.y);
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (selectionOrigin != null) {
				camera.unproject(vec3Tmp.set(screenX, screenY, 0));
				Rectangle selection = new Rectangle(selectionOrigin.x, selectionOrigin.y, vec3Tmp.x, vec3Tmp.y);

				MathHelper.normalizeRectangle(selection);

				// TODO hold shift for OR select
				remix.selection.clear();
				remix.entities.stream().filter(e -> e.bounds.overlaps(Rectangle.tmp
						.set(selection.x / Entity.PX_WIDTH, selection.y / Entity.PX_HEIGHT,
								selection.width / Entity.PX_WIDTH, selection.height / Entity.PX_HEIGHT)))
						.forEachOrdered(remix.selection::add);

				selectionOrigin = null;
			}
		}

		return false;
	}

	@Override
	public void dispose() {

	}
}
