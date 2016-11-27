package chrislo27.rhre.editor;

import chrislo27.rhre.Main;
import chrislo27.rhre.entity.Entity;
import chrislo27.rhre.registry.Game;
import chrislo27.rhre.registry.GameRegistry;
import chrislo27.rhre.registry.Series;
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
import ionium.registry.AssetRegistry;
import ionium.util.MathHelper;
import ionium.util.i18n.Localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Editor extends InputAdapter implements Disposable {

	public static final int MESSAGE_BAR_HEIGHT = 12;
	public static final int GAME_TAB_HEIGHT = 24;
	public static final int GAME_ICON_SIZE = 32;
	public static final int GAME_ICON_PADDING = 8;
	public static final int ICON_COUNT_X = 15;
	public static final int ICON_COUNT_Y = 4;
	public static final int PICKER_HEIGHT = ICON_COUNT_Y * (GAME_ICON_PADDING + GAME_ICON_SIZE) + GAME_ICON_PADDING;
	public static final int OVERVIEW_HEIGHT = 32;
	public static final int STAFF_START_Y = MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + GAME_TAB_HEIGHT + OVERVIEW_HEIGHT +
			32;
	public static final int TRACK_COUNT = 5;
	private static final int ICON_START_Y = PICKER_HEIGHT + MESSAGE_BAR_HEIGHT - GAME_ICON_PADDING - GAME_ICON_SIZE;

	private final Main main;
	private final OrthographicCamera camera = new OrthographicCamera();
	private final Vector3 vec3Tmp = new Vector3();
	private final Vector3 vec3Tmp2 = new Vector3();
	// TODO add button for this - 1/4, 1/6, 1/8
	public float snappingInterval = 0.25f;
	public String status;
	private Map<Series, Scroll> scrolls = new HashMap<>();
	private Series currentSeries = Series.TENGOKU;
	private Remix remix;
	/**
	 * null = not selecting
	 */
	private Vector2 selectionOrigin = null;
	/**
	 * null = not dragging
	 */
	private SelectionGroup selectionGroup = null;
	private Vector3 cameraPickVec3 = new Vector3();

	public Editor(Main m) {
		this.main = m;
		camera.setToOrtho(false, 1280, 720);
		camera.position.x = 0.333f * camera.viewportWidth;

		remix = new Remix();

		for (Series s : Series.values())
			scrolls.put(s, new Scroll(0, 0));
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
		{
			Rectangle.tmp.set((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH,
					(camera.position.y - camera.viewportHeight * 0.5f) / Entity.PX_HEIGHT,
					(camera.viewportWidth) / Entity.PX_WIDTH, (camera.viewportHeight) / Entity.PX_HEIGHT);
			for (Entity e : remix.entities) {
				if (selectionGroup != null && selectionGroup.getList().contains(e))
					continue;
				if (e.bounds.overlaps(Rectangle.tmp)) {
					e.render(main, main.palette, batch, remix.selection.contains(e));
				}
			}
			if (selectionGroup != null) {
				for (Entity e : selectionGroup.getList()) {
					if (e.bounds.overlaps(Rectangle.tmp)) {
						e.render(main, main.palette, batch, remix.selection.contains(e));
					}
				}
			}
		}

		// staff lines
		{
			batch.setColor(1, 1, 1, 1);

			final float yOffset = -1;

			// horizontal
			batch.setColor(main.palette.getStaffLine());
			for (int i = 0; i < TRACK_COUNT + 1; i++) {
				Main.fillRect(batch, camera.position.x - camera.viewportWidth * 0.5f, yOffset + i * Entity.PX_HEIGHT,
						camera.viewportWidth, 2);
			}

			// vertical
			final int beatInside = ((int) (camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0)).x /
					Entity.PX_WIDTH));
			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				batch.setColor(main.palette.getStaffLine());
				batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
						batch.getColor().a * (x == 0 ? 1f : (x < 0 ? 0.25f : 0.5f)));

				Main.fillRect(batch, x * Entity.PX_WIDTH, yOffset, 2, TRACK_COUNT * Entity.PX_HEIGHT);

				if (selectionGroup != null && beatInside == x) {
					final int numOfLines = ((int) (1 / snappingInterval));
					for (int i = 0; i < numOfLines; i++) {
						float a = 0.75f;

						batch.setColor(main.palette.getStaffLine());
						batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
								batch.getColor().a * a);

						Main.fillRect(batch, (x + (snappingInterval * i)) * Entity.PX_WIDTH, yOffset, 1,
								TRACK_COUNT * Entity.PX_HEIGHT);
					}
				}
			}

			// beat numbers
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
			// message bar
			Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), MESSAGE_BAR_HEIGHT);
			// picker
			Main.fillRect(batch, 0, 0, Gdx.graphics.getWidth(), PICKER_HEIGHT + MESSAGE_BAR_HEIGHT);
			// button bar on top
			Main.fillRect(batch, 0, Gdx.graphics.getHeight() - EditorStageSetup.BAR_HEIGHT, Gdx.graphics.getWidth(),
					EditorStageSetup.BAR_HEIGHT);
			// series buttons
			Main.fillRect(batch, 0, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, Series.values().length * GAME_ICON_SIZE,
					OVERVIEW_HEIGHT);
			for (int i = 0; i < Series.values().length; i++) {
				batch.setColor(0.65f, 0.65f, 0.65f, 1);
				Main.drawRect(batch, i * GAME_ICON_SIZE, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE,
						OVERVIEW_HEIGHT, 1);

				if (Series.values()[i] == currentSeries) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(AssetRegistry.getTexture("icon_selector_tengoku"), i * GAME_ICON_SIZE,
							PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);
				}
			}
			batch.setColor(1, 1, 1, 1);
			main.font.setColor(1, 1, 1, 1);
			main.font.getData().setScale(0.5f);
			main.font.draw(batch, status == null ? "" : status, 2, 2 + main.font.getCapHeight());

			// series buttons
			for (int i = 0; i < Series.values().length; i++) {
				main.font.draw(batch, Series.values()[i].getShorthand(), i * GAME_ICON_SIZE + GAME_ICON_SIZE * 0.5f,
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT * 0.5f + main.font.getCapHeight() * 0.5f,
						0, Align.center, false);
			}
			main.font.getData().setScale(1);
		}

		// picker icons
		{
			for (int i = 0, count = 0; i < GameRegistry.instance().gamesBySeries.get(currentSeries).size(); i++) {
				Game game = GameRegistry.instance().gamesBySeries.get(currentSeries).get(i);

				batch.draw(AssetRegistry.getTexture("gameIcon_" + game.getId()), getIconX(count), getIconY(count),
						GAME_ICON_SIZE, GAME_ICON_SIZE);

				if (count == scrolls.get(currentSeries).getGame()) {
					final int offset = 3;
					batch.draw(AssetRegistry.getTexture("icon_selector_fever"), getIconX(count) - offset,
							getIconY(count) - offset, GAME_ICON_SIZE + offset * 2, GAME_ICON_SIZE + offset * 2);
				}

				count++;
			}
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

		if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
			camera.position.x = 0;
		}

		if (selectionGroup != null) {
			camera.update();

			camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			vec3Tmp2.x /= Entity.PX_WIDTH;
			vec3Tmp2.y /= Entity.PX_HEIGHT;

			// change "origin" entity
			Rectangle rect = selectionGroup.getEntityClickedOn().bounds;
			rect.x = vec3Tmp2.x - selectionGroup.getOffset().x;
			rect.y = vec3Tmp2.y - selectionGroup.getOffset().y;

			// TODO snap on X levels
			rect.x = MathHelper.snapToNearest(rect.x, snappingInterval);

			// snap on Y
//			rect.y = MathUtils.clamp(Math.round(rect.y), 0, TRACK_COUNT - 1);
			rect.y = Math.round(rect.y);

			// change others relative to the origin, using the others' positions as a guideline
			{
				for (int i = 0; i < selectionGroup.getList().size(); i++) {
					Entity e = selectionGroup.getList().get(i);

					if (e == selectionGroup.getEntityClickedOn())
						continue;

					e.bounds.setPosition(rect.x + selectionGroup.getRelativePositions().get(i).x,
							rect.y + selectionGroup.getRelativePositions().get(i).y);
				}
			}
		} else {
			status = Localization.get("editor.currentGame") + " " +
					GameRegistry.instance().gamesBySeries.get(currentSeries).get(scrolls.get(currentSeries).getGame())
							.getName();

			if (Gdx.graphics.getHeight() - Gdx.input.getY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT) {
				if (Gdx.graphics.getHeight() - Gdx.input.getY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT &&
						Gdx.input.getX() <= GAME_ICON_PADDING + ICON_COUNT_X * (GAME_ICON_PADDING + GAME_ICON_SIZE)) {
					List<Game> list = GameRegistry.instance().gamesBySeries.get(currentSeries);
					int icon = getIconIndex(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

					if (icon < list.size() && icon >= 0)
						status += " - " + Localization.get("editor.lookingAt", list.get(icon).getName());
				} else {
					int i = Gdx.input.getX() / GAME_ICON_SIZE;
					if (i < Series.values().length) {
						status += " - " + Localization.get("editor.lookingAt", Series.values()[i].getProperName());
					}
				}
			}
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (screenY >= Gdx.graphics.getHeight() - (MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT)) {
				if (screenY >= Gdx.graphics.getHeight() - (MESSAGE_BAR_HEIGHT + PICKER_HEIGHT)) {
					List<Game> list = GameRegistry.instance().gamesBySeries.get(currentSeries);
					int icon = getIconIndex(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());

					if (icon < list.size() && icon >= 0) {
						scrolls.get(currentSeries).setGame(icon);
					}
				} else {
					int i = Gdx.input.getX() / GAME_ICON_SIZE;
					if (i < Series.values().length) {
						currentSeries = Series.values()[i];
					}
				}
			} else {

				Entity possible = getEntityAtPoint(screenX, screenY);
				camera.unproject(cameraPickVec3.set(screenX, screenY, 0));

				if (possible == null || !remix.selection.contains(possible)) {
					// start selection
					selectionOrigin = new Vector2(cameraPickVec3.x, cameraPickVec3.y);
				} else if (remix.selection.contains(possible)) {
					// begin move
					final List<Vector2> oldPos = new ArrayList<>();

					remix.selection.stream().map(e -> new Vector2(e.bounds.x, e.bounds.y)).forEachOrdered(oldPos::add);

					selectionGroup = new SelectionGroup(remix.selection, oldPos, possible,
							new Vector2(cameraPickVec3.x / Entity.PX_WIDTH - possible.bounds.x,
									cameraPickVec3.y / Entity.PX_HEIGHT - possible.bounds.y));
				}
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (selectionOrigin != null) {
				// put selected entities into the selection list
				camera.unproject(vec3Tmp.set(screenX, screenY, 0));
				Rectangle selection = new Rectangle(selectionOrigin.x, selectionOrigin.y, vec3Tmp.x -
						selectionOrigin.x,
						vec3Tmp.y - selectionOrigin.y);

				MathHelper.normalizeRectangle(selection);

				boolean shouldAdd =
						Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys
								.SHIFT_RIGHT);
				if (!shouldAdd)
					remix.selection.clear();
				remix.entities.stream().filter(e -> e.bounds.overlaps(Rectangle.tmp
						.set(selection.x / Entity.PX_WIDTH, selection.y / Entity.PX_HEIGHT,
								selection.width / Entity.PX_WIDTH, selection.height / Entity.PX_HEIGHT)) &&
						(!shouldAdd || !remix.selection.contains(e))).forEachOrdered(remix.selection::add);

				selectionOrigin = null;
				selectionGroup = null;
			} else if (selectionGroup != null) {
				// move the selection group to the new place, or snap back

				boolean collisionFree = selectionGroup.getList().stream().allMatch(
						e -> remix.entities.stream().filter(e2 -> !selectionGroup.getList().contains(e2))
								.noneMatch(e2 -> e2.bounds.overlaps(e.bounds)) &&
								(e.bounds.y >= 0 && e.bounds.y + e.bounds.height <= TRACK_COUNT));

				if (!collisionFree) {
					boolean delete = selectionGroup.getList().stream().anyMatch(e -> e.bounds.y < 0);

					if (delete) {
						remix.selection.clear();
						remix.entities.removeIf(e -> e.bounds.y < 0);
					} else {
						for (int i = 0; i < selectionGroup.getList().size(); i++) {
							Entity e = selectionGroup.getList().get(i);

							e.bounds.setPosition(selectionGroup.getOldPositions().get(i));
						}
					}
				}

				selectionGroup = null;
				selectionOrigin = null;
			}
		}

		return false;
	}

	@Override
	public void dispose() {

	}

	private int getIconX(int i) {
		return GAME_ICON_PADDING + (i % ICON_COUNT_X) * (GAME_ICON_PADDING + GAME_ICON_SIZE);
	}

	private int getIconY(int i) {
		return ICON_START_Y - ((i / ICON_COUNT_X) * (GAME_ICON_PADDING + GAME_ICON_SIZE));
	}

	private int getIconIndex(int x, int y) {
		return (x - GAME_ICON_PADDING) / (GAME_ICON_PADDING + GAME_ICON_SIZE) +
				(-(y - (ICON_START_Y + GAME_ICON_SIZE + GAME_ICON_PADDING)) / (GAME_ICON_PADDING + GAME_ICON_SIZE)) *
						ICON_COUNT_X;
	}
}
