package chrislo27.rhre.editor;

import chrislo27.rhre.LoadScreen;
import chrislo27.rhre.Main;
import chrislo27.rhre.WhenFilesDropped;
import chrislo27.rhre.entity.Entity;
import chrislo27.rhre.entity.HasGame;
import chrislo27.rhre.entity.PatternEntity;
import chrislo27.rhre.entity.SoundEntity;
import chrislo27.rhre.inspections.InspectionType;
import chrislo27.rhre.json.GameObject;
import chrislo27.rhre.registry.Game;
import chrislo27.rhre.registry.GameRegistry;
import chrislo27.rhre.registry.Pattern;
import chrislo27.rhre.registry.Series;
import chrislo27.rhre.track.PlayingState;
import chrislo27.rhre.track.Remix;
import chrislo27.rhre.track.Semitones;
import chrislo27.rhre.track.TempoChange;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ionium.registry.AssetRegistry;
import ionium.registry.ScreenRegistry;
import ionium.util.DebugSetting;
import ionium.util.MathHelper;
import ionium.util.Utils;
import ionium.util.i18n.Localization;
import ionium.util.render.StencilMaskUtil;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;

public class Editor extends InputAdapter implements Disposable, WhenFilesDropped {

	public static final int GAME_ICON_SIZE = 32;
	public static final int GAME_ICON_PADDING = 8;
	public static final int ICON_COUNT_X = 15;
	public static final int ICON_COUNT_Y = 4;
	private static final int MESSAGE_BAR_HEIGHT = 12;
	private static final int GAME_TAB_HEIGHT = 24;
	private static final int PICKER_HEIGHT = ICON_COUNT_Y * (GAME_ICON_PADDING + GAME_ICON_SIZE) + GAME_ICON_PADDING;
	private static final int OVERVIEW_HEIGHT = 32;
	private static final int STAFF_START_Y =
			MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + GAME_TAB_HEIGHT + OVERVIEW_HEIGHT + 32;
	private static final int TRACK_COUNT = 5;
	private static final int ICON_START_Y = PICKER_HEIGHT + MESSAGE_BAR_HEIGHT - GAME_ICON_PADDING - GAME_ICON_SIZE;
	private static final int PATTERNS_ABOVE_BELOW = 2;
	private static final float STRETCHABLE_AREA = 16f / Entity.PX_WIDTH;
	private static final int MAX_SEMITONE = Semitones.SEMITONES_IN_OCTAVE * 2;
	private static final float AUTOSAVE_PERIOD = 60f;

	private final Main main;
	private final OrthographicCamera camera = new OrthographicCamera();
	private final Vector3 vec3Tmp = new Vector3();
	private final Vector3 vec3Tmp2 = new Vector3();
	private final List<InspectionType> highlightedInspections = new ArrayList<>();
	private final GlyphLayout glyphLayout = new GlyphLayout();
	public String status;
	public Tool currentTool = Tool.NORMAL;
	public Remix remix;
	public FileHandle file = null;
	float snappingInterval;
	private Map<Series, Scroll> scrolls = new HashMap<>();
	private Series currentSeries = Series.TENGOKU;
	/**
	 * null = not selecting
	 */
	private Vector2 selectionOrigin = null;
	/**
	 * null = not dragging
	 */
	private SelectionGroup selectionGroup = null;
	private Vector3 cameraPickVec3 = new Vector3();
	private boolean isCursorStretching = false;
	private int isStretching = 0;
	private TempoChange selectedTempoChange;
	private float timeUntilAutosave = AUTOSAVE_PERIOD;
	private float autosaveMessageShow = 0f;

	public Editor(Main m) {
		this.main = m;
		camera.setToOrtho(false, 1280, 720);
		camera.position.x = 0.333f * camera.viewportWidth;

		remix = new Remix();

		for (Series s : Series.values())
			scrolls.put(s, new Scroll(0, 0));
		snappingInterval = 0.25f;
	}

	public Entity getEntityAtPoint(float x, float y) {
		camera.unproject(cameraPickVec3.set(x, y, 0));
		cameraPickVec3.x /= Entity.PX_WIDTH;
		cameraPickVec3.y /= Entity.PX_HEIGHT;

		return remix.getEntities().stream().filter(e -> e.bounds.contains(cameraPickVec3.x, cameraPickVec3.y))
				.findFirst().orElse(null);
	}

	public Entity getEntityAtMouse() {
		return getEntityAtPoint(Gdx.input.getX(), Gdx.input.getY());
	}

	public void render(SpriteBatch batch) {
		Gdx.gl.glClearColor(main.getPalette().getEditorBg().r, main.getPalette().getEditorBg().g,
				main.getPalette().getEditorBg().b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		camera.position.y = (camera.viewportHeight * 0.5f) - STAFF_START_Y;
		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		final float beatInSeconds = remix.getTempoChanges().beatsToSeconds(remix.getBeat());
		final float yOffset = -1;

		// beat lines
		{
			// horizontal
			batch.setColor(main.getPalette().getStaffLine());
			for (int i = 0; i < TRACK_COUNT + 1; i++) {
				Main.fillRect(batch, camera.position.x - camera.viewportWidth * 0.5f, yOffset + i * Entity.PX_HEIGHT,
						camera.viewportWidth, 2);
			}

			batch.setColor(1, 1, 1, 1);
		}

		// entities
		// don't replace with foreach call b/c of performance
		{
			Rectangle.tmp.set((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH,
					(camera.position.y - camera.viewportHeight * 0.5f) / Entity.PX_HEIGHT,
					(camera.viewportWidth) / Entity.PX_WIDTH, (camera.viewportHeight) / Entity.PX_HEIGHT);
			for (Entity e : remix.getEntities()) {
				if (selectionGroup != null && selectionGroup.getList().contains(e))
					continue;
				if (e.bounds.overlaps(Rectangle.tmp)) {
					e.render(main, main.getPalette(), batch, remix.getSelection().contains(e));
				}
			}
			if (selectionGroup != null) {
				for (Entity e : selectionGroup.getList()) {
					if (e.bounds.overlaps(Rectangle.tmp)) {
						e.render(main, main.getPalette(), batch, remix.getSelection().contains(e));
					}
				}
			}
		}

		// vertical beat line
		{
			// vertical
			final int beatInside = ((int) (camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0)).x /
					Entity.PX_WIDTH));
			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				batch.setColor(main.getPalette().getStaffLine());
				batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
						batch.getColor().a * (x == 0 ? 1f : (x < 0 ? 0.25f : 0.5f)));

				Main.fillRect(batch, x * Entity.PX_WIDTH, yOffset, 2, TRACK_COUNT * Entity.PX_HEIGHT);

				if (selectionGroup != null && beatInside == x) {
					final int numOfLines = ((int) (1 / snappingInterval));
					for (int i = 0; i < numOfLines; i++) {
						float a = 0.75f;

						batch.setColor(main.getPalette().getStaffLine());
						batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
								batch.getColor().a * a);

						Main.fillRect(batch, (x + (snappingInterval * i)) * Entity.PX_WIDTH, yOffset, 1,
								TRACK_COUNT * Entity.PX_HEIGHT);
					}
				}
			}
		}

		// trackers
		{
			// music start
			{
				float musicToBeats = remix.getTempoChanges().secondsToBeats(remix.getMusicStartTime());

				batch.setColor(main.getPalette().getMusicStartTracker());
				Main.fillRect(batch, musicToBeats * Entity.PX_WIDTH, 0, 2, Entity.PX_HEIGHT * (TRACK_COUNT + 3));
				batch.setColor(1, 1, 1, 1);

				main.getFontBordered().setColor(main.getPalette().getMusicStartTracker());
				main.getFontBordered()
						.draw(batch, Localization.get("editor.musicStartTracker", String.format("%.3f", musicToBeats)),
								musicToBeats * Entity.PX_WIDTH + 4, Entity.PX_HEIGHT * (TRACK_COUNT + 3));
				main.getFontBordered().getData().setScale(0.5f);
				main.getFontBordered().draw(batch, Localization.get("editor.beatTrackerSec",
						String.format("%1$02d:%2$02.3f", (int) (Math.abs(remix.getMusicStartTime()) / 60),
								Math.abs(remix.getMusicStartTime()) % 60)), musicToBeats * Entity.PX_WIDTH + 4,
						Entity.PX_HEIGHT * (TRACK_COUNT + 3) + main.getFontBordered().getLineHeight());
				main.getFontBordered()
						.draw(batch, Localization.get("editor.musicTrackerHint"), musicToBeats * Entity.PX_WIDTH - 4,
								Entity.PX_HEIGHT * (TRACK_COUNT + 3) - main.getFontBordered().getCapHeight(), 0,
								Align.right, false);
				main.getFontBordered().getData().setScale(1);
				main.getFontBordered().setColor(1, 1, 1, 1);
			}

			// playback start
			{
				batch.setColor(main.getPalette().getBeatTracker());
				Main.fillRect(batch, remix.getPlaybackStart() * Entity.PX_WIDTH, 0, 2,
						Entity.PX_HEIGHT * (TRACK_COUNT + 2));
				batch.setColor(1, 1, 1, 1);

				main.getFontBordered().setColor(main.getPalette().getBeatTracker());
				main.getFontBordered().draw(batch, Localization
								.get("editor.playbackStartTracker", String.format("%.3f", remix.getPlaybackStart())),
						remix.getPlaybackStart() * Entity.PX_WIDTH + 4, Entity.PX_HEIGHT * (TRACK_COUNT + 2));

				main.getFontBordered().getData().setScale(0.5f);
				main.getFontBordered().draw(batch, Localization.get("editor.playbackTrackerHint"),
						remix.getPlaybackStart() * Entity.PX_WIDTH - 4,
						Entity.PX_HEIGHT * (TRACK_COUNT + 2) - main.getFontBordered().getLineHeight() * 1.25f, 0,
						Align.right, false);
				main.getFontBordered().getData().setScale(1);
				main.getFontBordered().setColor(1, 1, 1, 1);
			}

			// tempo changes
			{
				for (TempoChange tc : remix.getTempoChanges().getBeatMap().values()) {
					if (tc.getBeat() * Entity.PX_WIDTH < camera.position.x - camera.viewportWidth * 0.75f ||
							tc.getBeat() * Entity.PX_WIDTH > camera.position.x + camera.viewportWidth * 0.75f)
						continue;

					boolean isSelected = tc == selectedTempoChange;

					batch.setColor(main.getPalette().getBpmTracker());
					if (isSelected) {
						batch.setColor(main.getPalette().getBpmTrackerSelected());
					}

					Main.fillRect(batch, tc.getBeat() * Entity.PX_WIDTH, -Entity.PX_HEIGHT, 2,
							Entity.PX_HEIGHT * (TRACK_COUNT + 1));

					main.getFontBordered().setColor(batch.getColor());
					main.getFontBordered().draw(batch, Localization.get("editor.bpmTracker", String.format("%.1f", tc.getTempo())),
							tc.getBeat() * Entity.PX_WIDTH + 4,
							-Entity.PX_HEIGHT + main.getFontBordered().getCapHeight());

					if (isSelected) {
						main.getFontBordered().setColor(main.getPalette().getBpmTracker());
						main.getFontBordered().getData().setScale(0.5f);
						main.getFontBordered().draw(batch, Localization.get("editor.bpmTrackerHint"),
								tc.getBeat() * Entity.PX_WIDTH - 4,
								-Entity.PX_HEIGHT + main.getFontBordered().getLineHeight() * 2, 0, Align.right, false);
						main.getFontBordered().getData().setScale(1);
					}
				}
				main.getFontBordered().setColor(1, 1, 1, 1);
				batch.setColor(1, 1, 1, 1);
			}

			// playing
			if (remix.getPlayingState() != PlayingState.STOPPED) {
				batch.setColor(main.getPalette().getBeatTracker());
				Main.fillRect(batch, remix.getBeat() * Entity.PX_WIDTH, 0, 2, Entity.PX_HEIGHT * (TRACK_COUNT + 2));
				batch.setColor(1, 1, 1, 1);

				TempoChange tc = remix.getTempoChanges().getTempoChangeFromBeat(remix.getBeat());
				float currentBpm = tc == null ? remix.getTempoChanges().getDefTempo() : tc.getTempo();

				main.getFontBordered().setColor(main.getPalette().getBeatTracker());
				main.getFontBordered()
						.draw(batch, Localization.get("editor.beatTrackerBeat", String.format("%.3f", remix.getBeat
										())),
								remix.getBeat() * Entity.PX_WIDTH + 4, Entity.PX_HEIGHT * (TRACK_COUNT + 2));
				main.getFontBordered().getData().setScale(0.5f);
				main.getFontBordered().draw(batch, Localization.get("editor.beatTrackerSec",
						String.format("%1$02d:%2$02.3f", (int) (Math.abs(beatInSeconds) / 60),
								Math.abs(beatInSeconds) % 60)), remix.getBeat() * Entity.PX_WIDTH + 4,
						Entity.PX_HEIGHT * (TRACK_COUNT + 2) - main.getFontBordered().getLineHeight() * 2);
				main.getFontBordered().draw(batch, Localization.get("editor.beatTrackerBpm", String.format("%.1f", currentBpm)),
						remix.getBeat() * Entity.PX_WIDTH + 4,
						Entity.PX_HEIGHT * (TRACK_COUNT + 2) - main.getFontBordered().getLineHeight() * 3);
				main.getFontBordered().getData().setScale(1);
				main.getFontBordered().setColor(1, 1, 1, 1);
			}
		}

		// inspections
		if (main.getInspectionsEnabled()) {
			highlightedInspections.clear();
			camera.unproject(cameraPickVec3.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			for (InspectionType inspection : remix.getInspections().getInspections()) {
				if (inspection.getBeat() * Entity.PX_WIDTH < camera.position.x - camera.viewportWidth * 0.75f ||
						inspection.getBeat() * Entity.PX_WIDTH > camera.position.x + camera.viewportWidth * 0.75f)
					continue;

				batch.setColor(1, 0, 0, 1);
				Main.fillRect(batch, inspection.getBeat() * Entity.PX_WIDTH, 0, 2,
						Entity.PX_HEIGHT * (TRACK_COUNT + 0.5f));
				batch.setColor(1, 1, 1, 1);
				batch.draw(AssetRegistry.getTexture("inspectionIcon"), inspection.getBeat() * Entity.PX_WIDTH - 7,
						Entity.PX_HEIGHT * (TRACK_COUNT + 0.5f), 16, 16);

				if (MathHelper.intersects(cameraPickVec3.x, cameraPickVec3.y, 0, 0,
						inspection.getBeat() * Entity.PX_WIDTH - 10, Entity.PX_HEIGHT * (TRACK_COUNT + 0.5f) - 2, 20,
						20)) {
					highlightedInspections.add(inspection);
				}
			}
		}

		// beat numbers
		{
			main.getFont().setColor(main.getPalette().getStaffLine());
			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				main.getFont().draw(batch, x + "", x * Entity.PX_WIDTH,
						TRACK_COUNT * Entity.PX_HEIGHT + main.getFont().getCapHeight() + 4, 0, Align.center, false);
			}
		}

		// delete area
		{
			batch.setColor(1, 1, 1, 1);

			if (selectionGroup != null) {
				camera.unproject(vec3Tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
				float a = MathUtils.clamp(1.0f - (vec3Tmp.y / Entity.PX_HEIGHT), 0.5f, 1f);

				batch.setColor(1, 0, 0, 0.25f * a);
				Main.fillRect(batch, (camera.position.x - camera.viewportWidth * 0.5f), 0, camera.viewportWidth,
						-camera.viewportHeight);
				batch.setColor(1, 1, 1, 1);

				main.getBiggerFont().getData().setScale(0.5f);
				main.getBiggerFont().setColor(0.75f, 0.5f, 0.5f, 0.6f * a);
				main.getBiggerFont().draw(batch, Localization.get("editor.delete"), camera.position.x,
						-main.getBiggerFont().getCapHeight(), 0, Align.center, false);
				main.getBiggerFont().getData().setScale(1);
				main.getBiggerFont().setColor(1, 1, 1, 1);
			}
		}

		// selection rect
		if (selectionOrigin != null) {
			camera.unproject(vec3Tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));

			batch.setColor(main.getPalette().getSelectionFill());
			Main.fillRect(batch, selectionOrigin.x, selectionOrigin.y, vec3Tmp.x - selectionOrigin.x,
					vec3Tmp.y - selectionOrigin.y);
			batch.setColor(main.getPalette().getSelectionBorder());
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
			Main.fillRect(batch, 0, 0, main.camera.viewportWidth, MESSAGE_BAR_HEIGHT);
			// picker
			Main.fillRect(batch, 0, 0, main.camera.viewportWidth, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT);
			// button bar on top
			Main.fillRect(batch, 0, main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT, main.camera
							.viewportWidth,
					EditorStageSetup.BAR_HEIGHT);
			// series buttons
			Main.fillRect(batch, 0, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, Series.values().length * GAME_ICON_SIZE,
					OVERVIEW_HEIGHT);
			for (int i = 0; i < Series.values().length; i++) {
				batch.setColor(0.65f, 0.65f, 0.65f, 1);
				Main.drawRect(batch, i * GAME_ICON_SIZE, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE,
						OVERVIEW_HEIGHT, 1);
			}
			batch.setColor(1, 1, 1, 1);
			main.getFont().setColor(1, 1, 1, 1);
			main.getFont().getData().setScale(0.5f);
			main.getFont().draw(batch, status == null ? "" : status, 2, 2 + main.getFont().getCapHeight());

			// series buttons
			for (int i = 0; i < Series.values().length; i++) {
//				main.font.draw(batch, Series.values()[i].getShorthand(), i * GAME_ICON_SIZE + GAME_ICON_SIZE * 0.5f,
//						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT * 0.5f + main.font.getCapHeight() * 0.5f,
//						0, Align.center, false);

				batch.draw(AssetRegistry.getTexture("series_icon_" + Series.values()[i].name()), i * GAME_ICON_SIZE,
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);

				if (Series.values()[i] == currentSeries) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(AssetRegistry.getTexture("icon_selector_tengoku"), i * GAME_ICON_SIZE,
							PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);
				}
			}
			main.getFont().getData().setScale(1);
		}

		// inspections
		if (main.getInspectionsEnabled()) {
			main.getFontBordered().getData().setScale(0.75f);
//			main.getFontBordered().setColor(1f, 0.25f, 0.25f, 1);
			main.getFontBordered().draw(batch, Localization
							.get("editor.inspectionStatus", "" + remix.getInspections().getInspections().size(),
									"" + remix.getInspections().getLastRefreshDuration()), main.camera.viewportWidth
							- 4,
					main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - main.getFontBordered().getCapHeight()
					, 0,
					Align.right, false);
			main.getFontBordered().setColor(1, 1, 1, 1);
			main.getFontBordered().getData().setScale(1f);
		}

		main.getFontBordered().getData().setScale(0.75f);
//			main.getFontBordered().setColor(1f, 0.25f, 0.25f, 1);
		main.getFontBordered().draw(batch, (remix.getCurrentGame() == null ? "" : remix.getCurrentGame().getName()), 4,
				main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - main.getFontBordered().getCapHeight());
		main.getFontBordered().setColor(1, 1, 1, 1);
		main.getFontBordered().getData().setScale(1f);

		if (main.getInspectionsEnabled() && highlightedInspections.size() > 0) {
			main.getFont().getData().setScale(0.5f);
			float offsetY = 0;
			for (int i = 0; i < highlightedInspections.size(); i++) {
				InspectionType inspection = highlightedInspections.get(i);

				main.getFont().setColor(1, 1, 1, 1);
				glyphLayout
						.setText(main.getFont(), inspection.getProperInfo(), main.getFont().getColor(), 256, Align
										.left,
								true);

				batch.setColor(0, 0, 0, 0.5f);
				float bgHeight = glyphLayout.height + main.getFont().getLineHeight() + main.getFont().getCapHeight();
				Main.fillRect(batch, main.camera.viewportWidth,
						main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - offsetY, -glyphLayout.width - 12,
						-bgHeight);
//				Main.drawRect(batch, main.camera.viewportWidth,
//						main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - offsetY, -glyphLayout.width - 12,
//						-bgHeight, 1);

				main.getFont()
						.draw(batch, inspection.getProperName(), main.camera.viewportWidth - glyphLayout.width - 8,
								main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - offsetY - 4, 256, Align
										.left,
								true);
				main.getFont()
						.draw(batch, inspection.getProperInfo(), main.camera.viewportWidth - glyphLayout.width - 8,
								main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - offsetY - 4 -
										main.getFont().getLineHeight(), 256, Align.left, true);

				offsetY += bgHeight;
			}

			main.getFont().getData().setScale(0.5f);
		}

		if (remix.getPlayingState() == PlayingState.STOPPED) {
			if (autosaveMessageShow > 0) {
				Color c = Main.getRainbow(1.0f);
				main.getFontBordered().setColor(c.r, c.g, c.b, Math.min(autosaveMessageShow, 1f));
				main.getFontBordered()
						.draw(batch, Localization.get("editor.autosaved"), main.camera.viewportWidth * 0.5f,
								main.camera.viewportHeight * 0.5f - main.getFontBordered().getCapHeight() * 0.5f, 0,
								Align.center, false);
				main.getFontBordered().setColor(1, 1, 1, 1);
			}
		}

		// tool icons
		{
			batch.setColor(0, 0, 0, 0.5f);
			Main.fillRect(batch, main.camera.viewportWidth, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT,
					-Tool.values().length * GAME_ICON_SIZE, OVERVIEW_HEIGHT);
			final float start = main.camera.viewportWidth - Tool.values().length * GAME_ICON_SIZE;
			for (int i = 0; i < Tool.values().length; i++) {
				batch.setColor(0.65f, 0.65f, 0.65f, 1);
				Main.drawRect(batch, start + i * GAME_ICON_SIZE, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE,
						OVERVIEW_HEIGHT, 1);
				batch.setColor(1, 1, 1, 1);

				batch.draw(AssetRegistry.getTexture("tool_icon_" + Tool.values()[i].name()), start + i *
								GAME_ICON_SIZE,
						PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);

				if (Tool.values()[i] == currentTool) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(AssetRegistry.getTexture("icon_selector_fever"), start + i * GAME_ICON_SIZE,
							PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);
				}
			}
			batch.setColor(1, 1, 1, 1);
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

		// pattern list
		{
			batch.setColor(1, 1, 1, 1);
			Main.fillRect(batch, main.camera.viewportWidth * 0.5f, MESSAGE_BAR_HEIGHT, 1, PICKER_HEIGHT);

			batch.end();
			StencilMaskUtil.prepareMask();
			main.shapes.setProjectionMatrix(main.camera.combined);
			main.shapes.begin(ShapeRenderer.ShapeType.Filled);
			main.shapes.rect(main.camera.viewportWidth * 0.5f, MESSAGE_BAR_HEIGHT, main.camera.viewportWidth,
					PICKER_HEIGHT);
			main.shapes.end();

			batch.begin();
			StencilMaskUtil.useMask();

			main.getFontBordered().setColor(1, 1, 1, 1);
			Game game = GameRegistry.instance().gamesBySeries.get(currentSeries)
					.get(scrolls.get(currentSeries).getGame());

			float middle = MESSAGE_BAR_HEIGHT + PICKER_HEIGHT * 0.5f + main.getFontBordered().getCapHeight() * 0.5f;

			for (int i = Math.max(0, scrolls.get(currentSeries).getPattern() - PATTERNS_ABOVE_BELOW), first = scrolls
					.get(currentSeries).getPattern();
				 i < Math.min(game.getPatterns().size(), first + PATTERNS_ABOVE_BELOW + 900); i++) {
				Pattern p = game.getPatterns().get(i);

				main.getFontBordered().setColor(1, 1, 1, 1);
				if (p.getAutoGenerated()) {
					main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
				}
				if (i == first) {
					main.getFontBordered().setColor(0.65f, 1, 1, 1);

					main.getFontBordered()
							.draw(batch, ">", main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING, middle);

					List<Pattern> list = GameRegistry.instance().gamesBySeries.get(currentSeries)
							.get(scrolls.get(currentSeries).getGame()).getPatterns();

					if (scrolls.get(currentSeries).getPattern() == 0)
						main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
					main.getFontBordered().draw(batch, "^", main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING,
							middle + PICKER_HEIGHT * 0.5f - main.getFontBordered().getCapHeight());

					main.getFontBordered().setColor(0.65f, 1, 1, 1);

					if (scrolls.get(currentSeries).getPattern() == list.size() - 1)
						main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
					main.getFontBordered().draw(batch, "v", main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING,
							middle - PICKER_HEIGHT * 0.5f + 12);

					main.getFontBordered().setColor(0.65f, 1, 1, 1);
				}

				main.getFontBordered().draw(batch, p.getName(), main.camera.viewportWidth * 0.525f,
						middle + (first - i) * PICKER_HEIGHT / (PATTERNS_ABOVE_BELOW * 2 + 1), 0, Align.left, false);
			}

			main.getFontBordered().setColor(1, 1, 1, 1);

			batch.flush();
			StencilMaskUtil.resetMask();
		}

		// minimap
		{
			final float startX = Series.values().length * GAME_ICON_SIZE;
			final float startY = PICKER_HEIGHT + MESSAGE_BAR_HEIGHT;
			final float mapWidth = main.camera.viewportWidth - (startX + Tool.values().length * GAME_ICON_SIZE);
			final float duration = Math.max(remix.getDuration(), remix.getEndTime());
			final float ENTITY_WIDTH = duration == 0 ? mapWidth : mapWidth / duration;
			final float ENTITY_HEIGHT = (OVERVIEW_HEIGHT / TRACK_COUNT);

			batch.setColor(0, 0, 0, 0.5f);
			Main.fillRect(batch, startX, startY, mapWidth, OVERVIEW_HEIGHT);

			batch.end();
			StencilMaskUtil.prepareMask();
			main.shapes.setProjectionMatrix(main.camera.combined);
			main.shapes.begin(ShapeRenderer.ShapeType.Filled);
			main.shapes.rect(startX, startY, mapWidth, OVERVIEW_HEIGHT);
			main.shapes.end();

			batch.begin();
			StencilMaskUtil.useMask();

			batch.setColor(main.getPalette().getStaffLine());
			for (int i = 0; i < TRACK_COUNT + 1; i++) {
				Main.fillRect(batch, startX, startY + i * ENTITY_HEIGHT, mapWidth, 1);

			}

			batch.setColor(1, 1, 1, 1);

			for (Entity e : remix.getEntities()) {
				Color c = main.getPalette().getSoundCue().getBg();
				if (e.isStretchable())
					main.getPalette().getStretchableSoundCue().getBg();
				if (e instanceof PatternEntity) {
					c = main.getPalette().getPattern().getBg();

					if (e.isStretchable())
						c = main.getPalette().getStretchablePattern().getBg();
				}

				float x = (remix.getStartTime() < 0 ? (e.bounds.x - remix.getStartTime()) : e.bounds.x) * ENTITY_WIDTH;

				e.setBatchColorFromState(batch, c, main.getPalette().getSelectionTint(),
						remix.getSelection().contains(e));
				Main.fillRect(batch, startX + x, startY + e.bounds.y * ENTITY_HEIGHT, e.bounds.width * ENTITY_WIDTH,
						ENTITY_HEIGHT);
			}

			batch.setColor(1, 0, 0, 1);

			// inspections
			if (main.getInspectionsEnabled()) {
				for (InspectionType inspect : remix.getInspections().getInspections()) {
					Main.fillRect(batch, startX + inspect.getBeat() * ENTITY_WIDTH, startY, 1, OVERVIEW_HEIGHT);
				}
			}

			batch.setColor(1, 1, 1, 1);

			float camX = (remix.getStartTime() < 0
					? camera.position.x - remix.getStartTime() * Entity.PX_WIDTH
					: camera.position.x) / Entity.PX_WIDTH * ENTITY_WIDTH;
			float camW = camera.viewportWidth / Entity.PX_WIDTH * ENTITY_WIDTH;

			if (duration > 0)
				Main.drawRect(batch, startX + camX - camW / 2, startY, camW, OVERVIEW_HEIGHT, 2);

			batch.flush();
			StencilMaskUtil.resetMask();

			if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && duration > 0 && selectionOrigin == null &&
					selectionGroup == null && remix.getPlayingState() != PlayingState.PLAYING) {
				if (main.getInputX() > startX && main.getInputX() < startX + mapWidth) {
					if (main.camera.viewportHeight - main.getInputY() > startY &&
							main.camera.viewportHeight - main.getInputY() < startY + OVERVIEW_HEIGHT) {
						float percent = (main.getInputX() - startX) / mapWidth;
						percent *= duration;
						camera.position.x = (percent + Math.min(remix.getStartTime(), 0)) * Entity.PX_WIDTH;
					}
				}
			}
		}

		batch.end();
	}

	public void renderUpdate() {
		remix.update(Gdx.graphics.getDeltaTime());

		if (autosaveMessageShow > 0)
			autosaveMessageShow = Math.max(0f, autosaveMessageShow - Gdx.graphics.getDeltaTime());

		if (remix.getPlayingState() == PlayingState.PLAYING) {
			if (camera.position.x + camera.viewportWidth * 0.5f < remix.getBeat() * Entity.PX_WIDTH) {
				camera.position.x = remix.getBeat() * Entity.PX_WIDTH + camera.viewportWidth * 0.5f;
			} else if (remix.getBeat() * Entity.PX_WIDTH < camera.position.x - camera.viewportWidth * 0.5f) {
				camera.position.x = remix.getBeat() * Entity.PX_WIDTH + camera.viewportWidth * 0.25f;
			}
		} else if (remix.getPlayingState() == PlayingState.STOPPED && file != null) {
			timeUntilAutosave -= Gdx.graphics.getDeltaTime();

			if (timeUntilAutosave <= 0) {
				timeUntilAutosave = AUTOSAVE_PERIOD;
				autosave();
			}
		}
	}

	private void autosave() {
		if (file != null) {
			String extension = "brhre2";
			if (file.extension().equalsIgnoreCase("rhre2"))
				extension = "rhre2";
			FileHandle sibling = file.sibling(file.nameWithoutExtension() + ".autosave." + extension);

			try {
				sibling.file().createNewFile();

				if (extension.equals("rhre2")) {
					sibling.writeString(new Gson().toJson(Remix.Companion.writeToJsonObject(remix)), false, "UTF-8");
				} else {
					ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(sibling.file()));

					Remix.Companion.writeToZipStream(remix, zipStream);

					zipStream.close();
				}

				autosaveMessageShow = 3f;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.gc();
			}

		}
	}

	public void inputUpdate() {
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
				remix.setPlayingState(
						remix.getPlayingState() == PlayingState.PLAYING ? PlayingState.PAUSED : PlayingState.PLAYING);
			} else {
				remix.setPlayingState(
						remix.getPlayingState() == PlayingState.PLAYING ? PlayingState.STOPPED : PlayingState.PLAYING);
			}
		}

		if (DebugSetting.debug) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
				if (remix.getSelection().size() > 0) {
					if (remix.getSelection().stream().anyMatch(e -> e instanceof PatternEntity)) {
						Main.logger.debug("Cannot export pattern - contains a pattern");
					} else {
						List<Entity> selection = remix.getSelection();
						selection.sort((e1, e2) -> {
							if (e1.bounds.x < e2.bounds.x)
								return -1;
							if (e1.bounds.x > e2.bounds.x)
								return 1;

							if (e1.bounds.y < e2.bounds.y)
								return -1;
							if (e1.bounds.y > e2.bounds.y)
								return 1;

							return 0;
						});

						Main.logger.debug("Exporting pattern:");

						GameObject.PatternObject pattern = new GameObject.PatternObject();
						final Entity first = selection.get(0);
						pattern.id = ((HasGame) first).getGame().getId() + "_NEW-PATTERN";
						pattern.name = "PATTERN NAME";
						List<GameObject.PatternObject.CueObject> cues = new ArrayList<>();

						selection.forEach(e -> {
							GameObject.PatternObject.CueObject cue = new GameObject.PatternObject.CueObject();

							cue.beat = e.bounds.x - first.bounds.x;
							cue.track = Math.round(first.bounds.y - e.bounds.y);
							cue.id = e.getID();
							if (e.isRepitchable())
								cue.semitone = e.getSemitone();
							else
								cue.semitone = null;
							if (e.isStretchable())
								cue.duration = e.bounds.width;
							else
								cue.duration = null;

							cues.add(cue);
						});

						pattern.cues = cues.toArray(new GameObject.PatternObject.CueObject[cues.size()]);
						System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(pattern) + "\n\n");
					}
				} else {
					Main.logger.debug("Cannot export pattern - nothing is selected");
				}
			}
		}

		if (remix.getPlayingState() != PlayingState.STOPPED) {
			if (isCursorStretching) {
				isCursorStretching = false;
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
			}
			return;
		}

		// camera
		{
			boolean accelerate = (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
					Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) || (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
					Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
			if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
				camera.position.x -= Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime() *
						(accelerate ? 5 : 1);
			}
			if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
				camera.position.x += Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime() *
						(accelerate ? 5 : 1);
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
				camera.position.x = 0;
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
				remix.updateDurationAndCurrentGame();
				camera.position.x = remix.getEndTime() * Entity.PX_WIDTH;
			}
		}

		if (currentTool == Tool.NORMAL && main.getInputY() > EditorStageSetup.BAR_HEIGHT) {
			// trackers
			{
				camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0));
				vec3Tmp2.x /= Entity.PX_WIDTH;
				vec3Tmp2.y /= Entity.PX_HEIGHT;

				final boolean shift =
						Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys
								.SHIFT_RIGHT);

				if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) &&
						!(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
								Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
					remix.setPlaybackStart(vec3Tmp2.x);
					if (!shift) {
						remix.setPlaybackStart(MathHelper.snapToNearest(remix.getPlaybackStart(), snappingInterval));
					}
				}

				if (Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) || Gdx.input.isButtonPressed(Input.Buttons
						.RIGHT) &&
						(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
								Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
					remix.setMusicStartTime(remix.getTempoChanges().beatsToSeconds(vec3Tmp2.x));
					if (!shift) {
						remix.setMusicStartTime(remix.getTempoChanges()
								.beatsToSeconds(MathHelper.snapToNearest(vec3Tmp2.x, snappingInterval)));
					}
				}
			}

			// cursor only
			Entity possible = getEntityAtMouse();

			boolean isAbleToStretch = false;

			if (possible != null && remix.getSelection().size() == 1 && possible.isStretchable() &&
					remix.getSelection().contains(possible)) {
				if ((cameraPickVec3.x >= possible.bounds.x &&
						cameraPickVec3.x <= possible.bounds.x + STRETCHABLE_AREA) ||
						(cameraPickVec3.x >= possible.bounds.x + possible.bounds.width - STRETCHABLE_AREA &&
								cameraPickVec3.x <= possible.bounds.x + possible.bounds.width)) {
					isAbleToStretch = true;
				}
			}

			if ((this.isStretching > 0 || isAbleToStretch) && !isCursorStretching) {
				isCursorStretching = true;
				Gdx.graphics.setCursor(main.getHorizontalResize());
			} else if ((isStretching <= 0 && !isAbleToStretch) && isCursorStretching) {
				isCursorStretching = false;
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
			}

			if (selectionGroup != null) {
				camera.update();

				camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0));
				vec3Tmp2.x /= Entity.PX_WIDTH;
				vec3Tmp2.y /= Entity.PX_HEIGHT;

				if (isStretching > 0) {
					Entity e = selectionGroup.getList().get(0);
					// stretch
					if (isStretching == 1) {
						float rightSideX = (selectionGroup.getOldPositions().get(0).x +
								selectionGroup.getOldPositions().get(0).width);

						e.bounds.x = vec3Tmp2.x;
						e.bounds.x = Math.min(MathHelper.snapToNearest(e.bounds.x, snappingInterval),
								rightSideX - snappingInterval);
						e.bounds.width = (selectionGroup.getOldPositions().get(0).x +
								selectionGroup.getOldPositions().get(0).width) - e.bounds.x;
					} else if (isStretching == 2) {
						e.bounds.width = vec3Tmp2.x - selectionGroup.getOldPositions().get(0).x;
						e.bounds.width = Math
								.max(MathHelper.snapToNearest(e.bounds.width, snappingInterval), snappingInterval);
					}
				} else {
					// change "origin" entity
					Rectangle rect = selectionGroup.getEntityClickedOn().bounds;
					rect.x = vec3Tmp2.x - selectionGroup.getOffset().x;
					rect.y = vec3Tmp2.y - selectionGroup.getOffset().y;

					rect.x = MathHelper.snapToNearest(rect.x, snappingInterval);

					// snap on Y
//				rect.y = MathUtils.clamp(Math.round(rect.y), 0, TRACK_COUNT - 1);
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
				}


			} else {
				status = Localization.get("editor.currentGame") + " " +
						GameRegistry.instance().gamesBySeries.get(currentSeries)
								.get(scrolls.get(currentSeries).getGame()).getName();

				if (main.camera.viewportHeight - main.getInputY() <=
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT) {
					if (main.camera.viewportHeight - main.getInputY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT) {
						if (main.getInputX() <=
								GAME_ICON_PADDING + ICON_COUNT_X * (GAME_ICON_PADDING + GAME_ICON_SIZE)) {
							List<Game> list = GameRegistry.instance().gamesBySeries.get(currentSeries);
							int icon = getIconIndex(main.getInputX(),
									(int) main.camera.viewportHeight - main.getInputY());

							if (icon < list.size() && icon >= 0)
								status += " - " + Localization.get("editor.lookingAt", list.get(icon).getName());
						} else if (main.getInputX() >= main.camera.viewportWidth * 0.5f) {
							status += " - " + Localization.get("editor.scrollPatterns");
						}
					} else {
						int i = main.getInputX() / GAME_ICON_SIZE;
						if (i < Series.values().length && i >= 0) {
							status += " - " + Localization.get("editor.lookingAt", Series.values()[i].getProperName());
						}
					}
				} else {

				}

				if (remix.getSelection().size() > 0) {
					if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) ||
							Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
						remix.getEntities().removeAll(remix.getSelection());
						remix.getSelection().clear();
					}
				}
			}

			if (selectionGroup != null && selectionGroup.getList().size() == 1 && isStretching > 0) {
				if (selectionGroup.getList().get(0).isStretchable()) {
					status = Localization.get("editor.stretchStatus2") + " " +
							selectionGroup.getOldPositions().get(0).width;
					status += " | ";
					status += Localization.get("editor.stretchStatus1") + " " +
							selectionGroup.getList().get(0).bounds.width;
				}
			}
		} else if (currentTool == Tool.BPM) {
			status = Localization.get("editor.bpmToolStatus");

			camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0));
			vec3Tmp2.x /= Entity.PX_WIDTH;
			vec3Tmp2.y /= Entity.PX_HEIGHT;

			selectedTempoChange = null;
			for (TempoChange tc : remix.getTempoChanges().getBeatMap().values()) {
				if (MathUtils.isEqual(tc.getBeat(), vec3Tmp2.x, snappingInterval / 2)) {
					selectedTempoChange = tc;
					break;
				}
			}

			float beatPos = MathHelper.snapToNearest(vec3Tmp2.x, snappingInterval);

			if (selectedTempoChange == null) {
				if (Utils.isButtonJustPressed(Input.Buttons.LEFT) && main.camera.viewportHeight - main.getInputY() >
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT &&
						main.getInputY() > EditorStageSetup.BAR_HEIGHT) {
					TempoChange tc = new TempoChange(beatPos, remix.getTempoChanges().getTempoAt(beatPos),
							remix.getTempoChanges());

					remix.getTempoChanges().add(tc);
				}
			} else if (selectedTempoChange != null) {
				if (Utils.isButtonJustPressed(Input.Buttons.LEFT)) {

				}

				if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)) {
					remix.getTempoChanges().remove(selectedTempoChange);
				}
			}
		} else if (currentTool == Tool.SPLIT_PATTERN) {
			status = Localization.get("editor.splitPatternToolStatus");

			if (Utils.isButtonJustPressed(Input.Buttons.LEFT)) {
				Entity e = getEntityAtMouse();

				if (e instanceof PatternEntity) {
					PatternEntity pe = (PatternEntity) e;

					pe.internal.forEach(se -> {
						se.bounds.x += pe.bounds.x;
						se.bounds.y += pe.bounds.y;

						remix.getEntities().add(se);
					});

					remix.getEntities().remove(pe);
					selectionGroup = null;
					remix.getSelection().clear();
				}
			}
		}
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0 && remix.getPlayingState() == PlayingState.STOPPED) {
			if (main.getInputY() >= main.camera.viewportHeight - (MESSAGE_BAR_HEIGHT + PICKER_HEIGHT)) {
				if (main.getInputX() >= main.camera.viewportWidth * 0.5f) {
					if (currentTool == Tool.NORMAL) {
						if (main.getInputX() >= main.camera.viewportWidth * 0.525f) {
							// drag new pattern
							remix.getSelection().clear();
							selectionGroup = null;

							Game game = GameRegistry.instance().gamesBySeries.get(currentSeries)
									.get(scrolls.get(currentSeries).getGame());
							Pattern p = game.getPatterns().get(scrolls.get(currentSeries).getPattern());

							Entity en = p.getCues().size() == 1 ? new SoundEntity(this.remix,
									game.getSoundCues().stream()
											.filter(it -> it.getId().equals(p.getCues().get(0).getId())).findFirst()
											.orElse(null), 0, 0, 0) : new PatternEntity(this.remix, p);

							en.attemptLoadSounds();

							remix.getEntities().add(en);
							remix.getSelection().add(en);

							final List<Rectangle> oldPos = new ArrayList<>();
							remix.getSelection().stream()
									.map(e -> new Rectangle(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height))
									.forEachOrdered(oldPos::add);
							selectionGroup = new SelectionGroup(remix.getSelection(), oldPos,
									remix.getSelection().get(0), new Vector2(0, 0), true);
						} else {
							int dir;
							if (main.camera.viewportHeight - main.getInputY() >
									MESSAGE_BAR_HEIGHT + PICKER_HEIGHT * 0.5f) {
								dir = -1;
							} else {
								dir = 1;
							}

							List<Pattern> list = GameRegistry.instance().gamesBySeries.get(currentSeries)
									.get(scrolls.get(currentSeries).getGame()).getPatterns();

							scrolls.get(currentSeries).setPattern(
									MathUtils.clamp(scrolls.get(currentSeries).getPattern() + dir, 0, list.size() -
											1));
						}
					}
				} else {
					// game picker
					List<Game> list = GameRegistry.instance().gamesBySeries.get(currentSeries);
					int icon = getIconIndex(main.getInputX(), (int) main.camera.viewportHeight - main.getInputY());

					if (icon < list.size() && icon >= 0) {
						scrolls.get(currentSeries).setGame(icon);
						scrolls.get(currentSeries).setPattern(0);
					}
				}
			} else if (main.getInputY() >=
					main.camera.viewportHeight - (MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT)) {
				// series
				if (main.getInputX() <= Series.values().length * GAME_ICON_SIZE) {
					int i = main.getInputX() / GAME_ICON_SIZE;
					if (i < Series.values().length) {
						if (GameRegistry.instance().gamesBySeries.get(Series.values()[i]) != null &&
								GameRegistry.instance().gamesBySeries.get(Series.values()[i]).size() > 0)
							currentSeries = Series.values()[i];
					}
				}

				// tools
				if (main.getInputX() >= main.camera.viewportWidth - Tool.values().length * GAME_ICON_SIZE) {
					Tool[] tools = Tool.values();
					int icon = (int) (tools.length -
							((main.camera.viewportWidth - main.getInputX()) / GAME_ICON_SIZE));

					if (icon < tools.length && icon >= 0) {
						currentTool = tools[icon];
					}
				}
			} else {
				if (currentTool == Tool.NORMAL) {
					Entity possible = getEntityAtPoint(Gdx.input.getX(), Gdx.input.getY());
					camera.unproject(cameraPickVec3.set(Gdx.input.getX(), Gdx.input.getY(), 0));

					if (possible == null || !remix.getSelection().contains(possible)) {
						// start selection
						selectionOrigin = new Vector2(cameraPickVec3.x, cameraPickVec3.y);
					} else if (remix.getSelection().contains(possible)) {
						// begin move
						final boolean isCopying = Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) ||
								Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);

						if (isCopying) {
							final List<Entity> newSel = new ArrayList<>();
							final Entity finalPos = possible;
							remix.getSelection().stream().filter(it -> it != finalPos).map(Entity::copy)
									.forEach(newSel::add);

							possible = possible.copy();
							newSel.add(possible);

							remix.getSelection().clear();
							remix.getEntities().addAll(newSel);
							remix.getSelection().addAll(newSel);
						}

						final List<Rectangle> oldPos = new ArrayList<>();

						remix.getSelection().stream()
								.map(e -> new Rectangle(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height))
								.forEachOrdered(oldPos::add);

						selectionGroup = new SelectionGroup(remix.getSelection(), oldPos, possible,
								new Vector2(cameraPickVec3.x / Entity.PX_WIDTH - possible.bounds.x,
										cameraPickVec3.y / Entity.PX_HEIGHT - possible.bounds.y), isCopying);

						// stretch code
						if (remix.getSelection().size() <= 1 && possible.isStretchable() && !isCopying) {
							cameraPickVec3.x /= Entity.PX_WIDTH;
							cameraPickVec3.y /= Entity.PX_HEIGHT;
							if ((cameraPickVec3.x >= possible.bounds.x &&
									cameraPickVec3.x <= possible.bounds.x + STRETCHABLE_AREA) ||
									(cameraPickVec3.x >= possible.bounds.x + possible.bounds.width -
											STRETCHABLE_AREA &&
											cameraPickVec3.x <= possible.bounds.x + possible.bounds.width)) {
								this.isStretching = (cameraPickVec3.x >= possible.bounds.x &&
										cameraPickVec3.x <= possible.bounds.x + STRETCHABLE_AREA) ? 1 : 2;
							}
						}
					}
				} else if (currentTool == Tool.BPM) {

				}
			}
		}

		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (button == Input.Buttons.LEFT && pointer == 0) {
			if (currentTool == Tool.NORMAL) {
				if (selectionOrigin != null) {
					// put selected entities into the selection list
					camera.unproject(vec3Tmp.set(Gdx.input.getX(), Gdx.input.getY(), 0));
					Rectangle selection = new Rectangle(selectionOrigin.x, selectionOrigin.y,
							vec3Tmp.x - selectionOrigin.x, vec3Tmp.y - selectionOrigin.y);

					MathHelper.normalizeRectangle(selection);

					boolean shouldAdd = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
							Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
					if (!shouldAdd)
						remix.getSelection().clear();
					remix.getEntities().stream().filter(e -> e.bounds.overlaps(Rectangle.tmp
							.set(selection.x / Entity.PX_WIDTH, selection.y / Entity.PX_HEIGHT,
									selection.width / Entity.PX_WIDTH, selection.height / Entity.PX_HEIGHT)) &&
							(!shouldAdd || !remix.getSelection().contains(e)))
							.forEachOrdered(remix.getSelection()::add);

					selectionOrigin = null;
					selectionGroup = null;
				} else if (selectionGroup != null) {
					// move the selection group to the new place, or snap back

					boolean collisionFree = selectionGroup.getList().stream().allMatch(
							e -> remix.getEntities().stream().filter(e2 -> !selectionGroup.getList().contains(e2))
									.noneMatch(e2 -> e2.bounds.overlaps(e.bounds)) &&
									(e.bounds.y >= 0 && e.bounds.y + e.bounds.height <= TRACK_COUNT));

					if (!collisionFree) {
						boolean delete = selectionGroup.getDeleteInstead() ||
								selectionGroup.getList().stream().anyMatch(e -> e.bounds.y < 0);

						if (delete) {
							remix.getEntities().removeAll(selectionGroup.getList());
							remix.getSelection().clear();
						} else {
							for (int i = 0; i < selectionGroup.getList().size(); i++) {
								Entity e = selectionGroup.getList().get(i);

								e.bounds.set(selectionGroup.getOldPositions().get(i));
							}
						}
					} else {
						if (isStretching > 0) {
							selectionGroup.getList().get(0)
									.onLengthChange(selectionGroup.getOldPositions().get(0).width);
						}
					}

					selectionGroup = null;
					selectionOrigin = null;
				}

				isStretching = 0;
			} else if (currentTool == Tool.BPM) {

			}

			remix.updateDurationAndCurrentGame();
			remix.getInspections().refresh();

			return true;
		}

		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		if (main.camera.viewportHeight - main.getInputY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT) {
			if (main.getInputX() >= main.camera.viewportWidth * 0.5f) {
				List<Pattern> list = GameRegistry.instance().gamesBySeries.get(currentSeries)
						.get(scrolls.get(currentSeries).getGame()).getPatterns();

				scrolls.get(currentSeries).setPattern(
						MathUtils.clamp(scrolls.get(currentSeries).getPattern() + amount, 0, list.size() - 1));
				return true;
			}
		} else if (remix.getPlayingState() == PlayingState.STOPPED) {
			if (currentTool == Tool.NORMAL) {
				if (remix.getSelection().size() > 0 && remix.getSelection().stream().anyMatch(Entity::isRepitchable)) {
					remix.getSelection().forEach(e -> e.adjustPitch(-amount, -MAX_SEMITONE, MAX_SEMITONE));
					return true;
				}
			} else if (currentTool == Tool.BPM) {
				if (selectedTempoChange != null) {
					float newTempo = MathUtils.clamp(selectedTempoChange.getTempo() + -amount *
							(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
									Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ? 5 : 1) *
							(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
									Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ? 0.1f : 1), 30f, 480f);

					if (selectedTempoChange.getTempo() != newTempo) {
						TempoChange tc = new TempoChange(selectedTempoChange.getBeat(), newTempo,
								remix.getTempoChanges());

						remix.getTempoChanges().remove(selectedTempoChange);
						selectedTempoChange = null;

						remix.getTempoChanges().add(tc);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {

		if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
			int index = keycode - Input.Keys.NUM_1;

			if (index < Tool.values().length)
				currentTool = Tool.values()[index];
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

	@Override
	public void onFilesDropped(@NotNull List<? extends FileHandle> list) {
		if (list.size() != 1 || remix.getPlayingState() != PlayingState.STOPPED)
			return;

		ScreenRegistry.get("load", LoadScreen.class).setShouldShowPicker(false);

		main.setScreen(ScreenRegistry.get("load"));
		ScreenRegistry.get("load", LoadScreen.class).onFilesDropped(list);
		ScreenRegistry.get("load", LoadScreen.class).hidePicker();
	}

	public enum Tool {
		NORMAL, BPM, SPLIT_PATTERN
	}

}
