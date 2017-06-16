package chrislo27.rhre.editor;

import chrislo27.rhre.Main;
import chrislo27.rhre.PreferenceKeys;
import chrislo27.rhre.SaveScreen;
import chrislo27.rhre.entity.Entity;
import chrislo27.rhre.entity.HasGame;
import chrislo27.rhre.entity.PatternEntity;
import chrislo27.rhre.entity.SoundEntity;
import chrislo27.rhre.json.GameObject;
import chrislo27.rhre.registry.*;
import chrislo27.rhre.track.*;
import chrislo27.rhre.util.JsonHandler;
import chrislo27.rhre.util.message.IconMessage;
import chrislo27.rhre.util.message.MessageHandler;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import ionium.registry.AssetRegistry;
import ionium.util.DebugSetting;
import ionium.util.MathHelper;
import ionium.util.Utils;
import ionium.util.i18n.Localization;
import ionium.util.render.StencilMaskUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static chrislo27.rhre.PreferenceKeys.CAMERA_TRACK;

public class Editor extends InputAdapter implements Disposable {

	public static final int GAME_ICON_SIZE = 32;
	public static final int GAME_ICON_PADDING = 8;
	public static final int ICON_COUNT_X = 15;
	public static final int ICON_COUNT_Y = 4;
	public static final int TRACK_COUNT = 5;
	public static final int MAX_SEMITONE = Semitones.SEMITONES_IN_OCTAVE * 2;
	private static final int MESSAGE_BAR_HEIGHT = 12;
	private static final int GAME_TAB_HEIGHT = 24;
	private static final int PICKER_HEIGHT = ICON_COUNT_Y * (GAME_ICON_PADDING + GAME_ICON_SIZE) + GAME_ICON_PADDING;
	private static final int OVERVIEW_HEIGHT = 32;
	private static final int STAFF_START_Y =
			MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + GAME_TAB_HEIGHT + OVERVIEW_HEIGHT + 32;
	private static final int ICON_START_Y = PICKER_HEIGHT + MESSAGE_BAR_HEIGHT - GAME_ICON_PADDING - GAME_ICON_SIZE;
	private static final int PATTERNS_ABOVE_BELOW = 2;
	private static final float STRETCHABLE_AREA = 16f / Entity.PX_WIDTH;
	private static final float AUTOSAVE_PERIOD = 60f;
	public final OrthographicCamera camera = new OrthographicCamera();
	public final MessageHandler messageHandler = new MessageHandler();
	private final Main main;
	private final Vector3 vec3Tmp = new Vector3();
	private final Vector3 vec3Tmp2 = new Vector3();
	private final Map<Series, ScrollValue> scrolls = new HashMap<>();
	private final Vector3 cameraPickVec3 = new Vector3();
	public Remix remix;
	public FileHandle file = null;
	public boolean isNormalSave = false;
	float snappingInterval;
	private String status;
	private Tool currentTool = Tool.NORMAL;
	private Series currentSeries = Series.TENGOKU;
	/**
	 * null = not selecting
	 */
	private Vector2 selectionOrigin = null;
	/**
	 * null = not dragging
	 */
	private SelectionGroup selectionGroup = null;
	private boolean isCursorStretching = false;
	private int isStretching = 0;
	private TempoChange selectedTempoChange;
	private float timeUntilAutosave = AUTOSAVE_PERIOD;
	private boolean didMoveCamera = false;
	private int trackerMoving = 0; // 0 - none, 1 - playback, 2 - music
	private float lastTrackerPos = 0f;

	public Editor(Main m) {
		this.main = m;
		camera.setToOrtho(false, 1280, 720);
		camera.position.x = 0.333f * camera.viewportWidth;

		remix = new Remix();

		for (Series s : Series.values)
			scrolls.put(s, new ScrollValue(0, 0, 0));
		snappingInterval = 0.25f;
	}

	private Entity getEntityAtPoint(float x, float y) {
		camera.unproject(cameraPickVec3.set(x, y, 0));
		cameraPickVec3.x /= Entity.PX_WIDTH;
		cameraPickVec3.y /= Entity.PX_HEIGHT;

		return remix.getEntities().stream().filter(e -> e.getBounds().contains(cameraPickVec3.x, cameraPickVec3.y))
				.findFirst().orElse(null);
	}

	private Entity getEntityAtMouse() {
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
				if (e.getBounds().overlaps(Rectangle.tmp)) {
					e.render(main, main.getPalette(), batch, remix.getSelection().contains(e));
				}
			}
			if (selectionGroup != null) {
				for (Entity e : selectionGroup.getList()) {
					if (e.getBounds().overlaps(Rectangle.tmp)) {
						e.render(main, main.getPalette(), batch, remix.getSelection().contains(e));
					}
				}
			}
		}

		// vertical beat line
		{
			// vertical
			final int beatInside = ((int) Math
					.floor(camera.unproject(vec3Tmp2.set(Gdx.input.getX(), Gdx.input.getY(), 0)).x / Entity.PX_WIDTH));
			for (int x = (int) ((camera.position.x - camera.viewportWidth * 0.5f) / Entity.PX_WIDTH);
				 x * Entity.PX_WIDTH < camera.position.x + camera.viewportWidth * 0.5f; x++) {
				batch.setColor(main.getPalette().getStaffLine());
				batch.setColor(batch.getColor().r, batch.getColor().g, batch.getColor().b,
						batch.getColor().a * (x == 0 ? 1f : (x < 0 ? 0.25f : 0.5f)));

				Main.fillRect(batch, x * Entity.PX_WIDTH, yOffset, 2, TRACK_COUNT * Entity.PX_HEIGHT);

				if (((selectionGroup != null || trackerMoving > 0 || currentTool == Tool.BPM) &&
						remix.getPlayingState() == PlayingState.STOPPED) && beatInside == x) {
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
			main.getFontBordered().setUseIntegerPositions(false);

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

				float start = remix.getTempoChanges().beatsToSeconds(remix.getPlaybackStart());
				main.getFontBordered().draw(batch, Localization.get("editor.beatTrackerSec",
						String.format("%1$02d:%2$02.3f", (int) (Math.abs(start) / 60), Math.abs(start) % 60)),
						remix.getPlaybackStart() * Entity.PX_WIDTH + 4,
						Entity.PX_HEIGHT * (TRACK_COUNT + 2) - (main.getFontBordered().getCapHeight() * 6));
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
					if (isSelected)
						continue;

					batch.setColor(main.getPalette().getBpmTracker());
					Main.fillRect(batch, tc.getBeat() * Entity.PX_WIDTH, -Entity.PX_HEIGHT, 2,
							Entity.PX_HEIGHT * (TRACK_COUNT + 1));

					main.getFontBordered().setColor(batch.getColor());
					main.getFontBordered()
							.draw(batch, Localization.get("editor.bpmTracker", String.format("%.1f", tc.getTempo())),
									tc.getBeat() * Entity.PX_WIDTH + 4,
									-Entity.PX_HEIGHT + main.getFontBordered().getCapHeight());
				}
				if (selectedTempoChange != null) {
					final TempoChange tc = selectedTempoChange;
					batch.setColor(main.getPalette().getBpmTrackerSelected());

					Main.fillRect(batch, tc.getBeat() * Entity.PX_WIDTH, -Entity.PX_HEIGHT, 2,
							Entity.PX_HEIGHT * (TRACK_COUNT + 1));

					main.getFontBordered().setColor(batch.getColor());
					main.getFontBordered()
							.draw(batch, Localization.get("editor.bpmTracker", String.format("%.1f", tc.getTempo())),
									tc.getBeat() * Entity.PX_WIDTH + 4,
									-Entity.PX_HEIGHT + main.getFontBordered().getCapHeight());

					main.getFontBordered().setColor(main.getPalette().getBpmTrackerSelected());
					main.getFontBordered().getData().setScale(0.5f);
					main.getFontBordered()
							.draw(batch, Localization.get("editor.bpmTrackerHint"), tc.getBeat() * Entity.PX_WIDTH - 4,
									-Entity.PX_HEIGHT + main.getFontBordered().getLineHeight() * 2, 0, Align.right,
									false);
					main.getFontBordered().getData().setScale(1);
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
				main.getFontBordered()
						.draw(batch, Localization.get("editor.beatTrackerBpm", String.format("%.1f", currentBpm)),
								remix.getBeat() * Entity.PX_WIDTH + 4,
								Entity.PX_HEIGHT * (TRACK_COUNT + 2) - main.getFontBordered().getLineHeight() * 3);
				main.getFontBordered().getData().setScale(1);
				main.getFontBordered().setColor(1, 1, 1, 1);
			}

			main.getFontBordered().setUseIntegerPositions(true);
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

		messageHandler.render(batch, camera.viewportWidth, camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - 64,
				camera.viewportWidth - 360, camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - 64, 360, 64);

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
			Main.fillRect(batch, 0, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, Series.values.length * GAME_ICON_SIZE,
					OVERVIEW_HEIGHT);
			for (int i = 0; i < Series.values.length; i++) {
				batch.setColor(0.65f, 0.65f, 0.65f, 1);
				Main.drawRect(batch, i * GAME_ICON_SIZE, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE,
						OVERVIEW_HEIGHT, 1);
			}
			batch.setColor(1, 1, 1, 1);
			main.getFont().setColor(1, 1, 1, 1);
			main.getFont().getData().setScale(0.5f);
			Main.drawCompressed(main.getFont(), batch, status == null ? "" : status, 2,
					2 + main.getFont().getCapHeight(), main.camera.viewportWidth - 16 - main.getVersionStringLength(),
					Align.left);

			// series buttons
			for (int i = 0; i < Series.values.length; i++) {
//				main.font.draw(batch, Series.values[i].getShorthand(), i * GAME_ICON_SIZE + GAME_ICON_SIZE * 0.5f,
//						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT * 0.5f + main.font.getCapHeight() * 0.5f,
//						0, Align.center, false);

				batch.draw(AssetRegistry.getTexture("series_icon_" + Series.values[i].name()), i * GAME_ICON_SIZE,
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);

				if (Series.values[i] == currentSeries) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(AssetRegistry.getTexture("icon_selector_tengoku"), i * GAME_ICON_SIZE,
							PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);
				}
			}
			main.getFont().getData().setScale(1);
		}

		if (remix.getCurrentGame() != null &&
				main.getPreferences().getBoolean(PreferenceKeys.SHOW_CURRENT_GAME, true)) {
			final float x = 4, y = main.camera.viewportHeight - EditorStageSetup.BAR_HEIGHT - 4;
			final Texture icon = remix.getCurrentGame().getIconTexture();
			main.batch.draw(icon, x, y - 32, 32, 32);
			main.getFontBordered().getData().setScale(0.75f);
//			main.getFontBordered().setColor(1f, 0.25f, 0.25f, 1);
			main.getFontBordered().draw(batch, remix.getCurrentGame().getName(), x * 2 + 32,
					y - (16 - main.getFontBordered().getCapHeight() / 2));
			main.getFontBordered().setColor(1, 1, 1, 1);
			main.getFontBordered().getData().setScale(1f);
		}

		// tool icons
		{
			batch.setColor(0, 0, 0, 0.5f);
			Main.fillRect(batch, main.camera.viewportWidth, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT,
					-Tool.values.length * GAME_ICON_SIZE, OVERVIEW_HEIGHT);
			final float start = main.camera.viewportWidth - Tool.values.length * GAME_ICON_SIZE;
			for (int i = 0; i < Tool.values.length; i++) {
				batch.setColor(0.65f, 0.65f, 0.65f, 1);
				Main.drawRect(batch, start + i * GAME_ICON_SIZE, PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE,
						OVERVIEW_HEIGHT, 1);
				batch.setColor(1, 1, 1, 1);

				batch.draw(AssetRegistry.getTexture("tool_icon_" + Tool.values[i].name()), start + i * GAME_ICON_SIZE,
						PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);

				if (Tool.values[i] == currentTool) {
					batch.setColor(1, 1, 1, 1);
					batch.draw(AssetRegistry.getTexture("icon_selector_fever"), start + i * GAME_ICON_SIZE,
							PICKER_HEIGHT + MESSAGE_BAR_HEIGHT, GAME_ICON_SIZE, OVERVIEW_HEIGHT);
				}
			}
			batch.setColor(1, 1, 1, 1);
		}

		// picker icons
		{
			for (int i = 0, count = 0; i < GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries).size(); i++) {
				Game game = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries).get(i);

				batch.draw(game.getIconTexture(), getIconX(count), getIconY(count), GAME_ICON_SIZE, GAME_ICON_SIZE);

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
			Game game = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
					.get(scrolls.get(currentSeries).getGame());

			float middle = MESSAGE_BAR_HEIGHT + PICKER_HEIGHT * 0.5f + main.getFontBordered().getCapHeight() * 0.5f;

			scrolls.values().forEach(ScrollValue::update);

			for (int i = Math
					.max(0, scrolls.get(currentSeries).getPattern() - PATTERNS_ABOVE_BELOW - 1), first = scrolls
						 .get(currentSeries).getPattern();
				 i < Math.min(game.getPatterns().size(), first + PATTERNS_ABOVE_BELOW + 900); i++) {
				Pattern p = game.getPatterns().get(i);

				main.getFontBordered().setColor(1, 1, 1, 1);
				if (p.getAutoGenerated()) {
					main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
				}
				if (i == first) {
					main.getFontBordered().setColor(0.65f, 1, 1, 1);

					main.getFontBordered().draw(batch, GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
									.get(scrolls.get(currentSeries).getGame()).getPointerString(),
							main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING, middle);

					List<Pattern> list = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
							.get(scrolls.get(currentSeries).getGame()).getPatterns();

					String arrow = "▲";

					if (scrolls.get(currentSeries).getPattern() == 0) {
						arrow = "△";
						main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
					}
					main.getFontBordered().draw(batch, arrow, main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING,
							middle + PICKER_HEIGHT * 0.5f - main.getFontBordered().getCapHeight());

					main.getFontBordered().setColor(0.65f, 1, 1, 1);

					arrow = "▼";
					if (scrolls.get(currentSeries).getPattern() == list.size() - 1) {
						arrow = "▽";
						main.getFontBordered().setColor(0.75f, 0.75f, 0.75f, 1);
					}
					main.getFontBordered().draw(batch, arrow, main.camera.viewportWidth * 0.5f + GAME_ICON_PADDING,
							middle - PICKER_HEIGHT * 0.5f + 12);

					main.getFontBordered().setColor(0.65f, 1, 1, 1);
				}

				float lerp = scrolls.get(currentSeries).getPatternLerp();
				Main.drawCompressed(main.getFontBordered(), batch, p.getName() + (DebugSetting.debug ? (" [GRAY](" +
								(!p.getAutoGenerated() ? p.getId() : p.getCues().get(0).getId()) + ")[]") : ""),
						main.camera.viewportWidth * 0.525f + GAME_ICON_PADDING,
						middle + (lerp - i) * PICKER_HEIGHT / (PATTERNS_ABOVE_BELOW * 2 + 1),
						main.camera.viewportWidth * 0.45f - GAME_ICON_PADDING * 2, Align.left);
			}

			main.getFontBordered().setColor(1, 1, 1, 1);

			batch.flush();
			StencilMaskUtil.resetMask();
		}

		// minimap
		{
			final float startX = Series.values.length * GAME_ICON_SIZE;
			final float startY = PICKER_HEIGHT + MESSAGE_BAR_HEIGHT;
			final float mapWidth = main.camera.viewportWidth - (startX + Tool.values.length * GAME_ICON_SIZE);
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

				float x = (remix.getStartTime() < 0 ? (e.getBounds().x - remix.getStartTime()) : e.getBounds().x) *
						ENTITY_WIDTH;

				e.setBatchColorFromState(batch, c, main.getPalette().getSelectionTint(),
						remix.getSelection().contains(e));
				Main.fillRect(batch, startX + x, startY + e.getBounds().y * ENTITY_HEIGHT,
						e.getBounds().width * ENTITY_WIDTH, ENTITY_HEIGHT);
			}

			batch.setColor(1, 0, 0, 1);

			batch.setColor(1, 1, 1, 1);

			float camX = (remix.getStartTime() < 0
					? camera.position.x - remix.getStartTime() * Entity.PX_WIDTH
					: camera.position.x) / Entity.PX_WIDTH * ENTITY_WIDTH;
			float camW = camera.viewportWidth / Entity.PX_WIDTH * ENTITY_WIDTH;

			if (duration > 0)
				Main.drawRect(batch, startX + camX - camW / 2, startY, camW, OVERVIEW_HEIGHT, 2);

			if (remix.getSweepLoadProgress() > 0 && remix.getSweepLoadProgress() < 1) {
				batch.setColor(0.75f, 1, 0.75f, 0.25f);
				Main.fillRect(batch, startX, startY, mapWidth * remix.getSweepLoadProgress(), OVERVIEW_HEIGHT);
				batch.setColor(1, 1, 1, 1);
			}

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

		if (file != null && (remix.getPlayingState() == PlayingState.STOPPED ||
				(remix.getPlayingState() != PlayingState.STOPPED && timeUntilAutosave > 10))) {
			timeUntilAutosave -= Gdx.graphics.getDeltaTime();
		}

		if (remix.getPlayingState() == PlayingState.PLAYING) {
			if (remix.getBeat() * Entity.PX_WIDTH < camera.position.x - camera.viewportWidth * 0.5f ||
					main.getPreferences().getBoolean(CAMERA_TRACK)) {
				camera.position.x = remix.getBeat() * Entity.PX_WIDTH + camera.viewportWidth * 0.25f;
			} else if (camera.position.x + camera.viewportWidth * 0.5f < remix.getBeat() * Entity.PX_WIDTH) {
				camera.position.x = remix.getBeat() * Entity.PX_WIDTH + camera.viewportWidth * 0.5f;
			}
		} else if (remix.getPlayingState() == PlayingState.STOPPED && file != null) {
			if (timeUntilAutosave <= 0) {
				timeUntilAutosave = AUTOSAVE_PERIOD;
				if (main.getPreferences().getBoolean(PreferenceKeys.AUTOSAVE, true)) {
					autosave();
				}
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
					sibling.writeString(JsonHandler.toJson(Remix.Companion.writeToJsonObject(remix)), false, "UTF-8");
				} else {
					final File siblingFile = sibling.file();
					for (int i = 1; i <= SaveScreen.MAX_ZIP_ATTEMPTS; i++) {
						ZipFile zipFile = null;
						try {
							ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(siblingFile));

							Remix.Companion.writeToZipStream(remix, zipStream);

							zipStream.close();

							zipFile = new ZipFile(siblingFile);

							break;
						} catch (IOException e) {
							if (i == SaveScreen.MAX_ZIP_ATTEMPTS) {
								throw new RuntimeException(e);
							} else {
								e.printStackTrace();
							}
						} finally {
							if (zipFile != null) {
								zipFile.close();
							}
						}
					}
				}

				isNormalSave = false;
				messageHandler.getList().add(0,
						new IconMessage(3f, AssetRegistry.getTexture("ui_save"), Localization.get("editor.autosaved"),
								main, 0.5f, 4f));
			} catch (Exception e) {
				e.printStackTrace();
				messageHandler.getList().add(0,
						new IconMessage(5f, AssetRegistry.getTexture("ui_save"), Localization.get("saveScreen.failed"),
								main, 0.5f, 4f));
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
					if (!remix.getSelection().stream().allMatch(e -> e instanceof SoundEntity)) {
						Main.logger.debug("Cannot export pattern - must all be sound entities");
					} else {
						List<Entity> selection = remix.getSelection();
						selection.sort((e1, e2) -> {
							if (e1.getBounds().x < e2.getBounds().x)
								return -1;
							if (e1.getBounds().x > e2.getBounds().x)
								return 1;

							if (e1.getBounds().y < e2.getBounds().y)
								return -1;
							if (e1.getBounds().y > e2.getBounds().y)
								return 1;

							return 0;
						});

						Main.logger.debug("Exporting pattern:");

						GameObject.PatternObject pattern = new GameObject.PatternObject();
						final Entity first = selection.get(0);
						pattern.setId(((HasGame) first).getGame().getId() + "_NEW-PATTERN");
						pattern.setName("PATTERN NAME");
						List<GameObject.PatternObject.CueObject> cues = new ArrayList<>();

						selection.forEach(e -> {
							GameObject.PatternObject.CueObject cue = new GameObject.PatternObject.CueObject();

							cue.setBeat(e.getBounds().x - first.getBounds().x);
							cue.setTrack(Math.round(first.getBounds().y - e.getBounds().y));
							cue.setId(e.getId());
							if (e.isRepitchable())
								cue.setSemitone(e.getSemitone());
							else
								cue.setSemitone(null);
							if (e.isStretchable())
								cue.setDuration(e.getBounds().width);
							else
								cue.setDuration(null);

							cues.add(cue);
						});

						pattern.setCues(cues.toArray(new GameObject.PatternObject.CueObject[cues.size()]));
						System.out.println(JsonHandler.toJson(pattern) + "\n\n");
					}
				} else {
					Main.logger.debug("Cannot export pattern - nothing is selected");
				}
			}
		}

		if (currentTool != Tool.BPM && selectedTempoChange != null)
			selectedTempoChange = null;

		if (remix.getPlayingState() != PlayingState.STOPPED) {
			if (isCursorStretching) {
				isCursorStretching = false;
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
			}

			if (selectedTempoChange != null) {
				selectedTempoChange = null;
			}
			return;
		}

		// camera
		{
			boolean accelerate = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
					Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ||
					(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT));
			boolean left = Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT);
			boolean right = Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT);
			if (left) {
				camera.position.x -= Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime() * (accelerate ? 5 : 1);
				didMoveCamera = true;
			}
			if (right) {
				camera.position.x += Entity.PX_WIDTH * 5 * Gdx.graphics.getDeltaTime() * (accelerate ? 5 : 1);
				didMoveCamera = true;
			}

			if (!left && !right && !accelerate) {
				didMoveCamera = false;
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.HOME)) {
				camera.position.x = 0;
				didMoveCamera = true;
			} else if (Gdx.input.isKeyJustPressed(Input.Keys.END)) {
				remix.updateDurationAndCurrentGame();
				camera.position.x = remix.getEndTime() * Entity.PX_WIDTH;
				didMoveCamera = true;
			}
		}

		// scroll imitation
		if (Gdx.input.isKeyJustPressed(Input.Keys.S) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
			scrolled(1);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.W) || Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			scrolled(-1);
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

				if ((trackerMoving == 0 || trackerMoving == 1) && Gdx.input.isButtonPressed(Input.Buttons.RIGHT) &&
						!(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
								Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT))) {
					if (trackerMoving != 1) {
						trackerMoving = 1;
						lastTrackerPos = remix.getPlaybackStart();
					}
					remix.setPlaybackStart(vec3Tmp2.x);
					if (!shift) {
						remix.setPlaybackStart(MathHelper.snapToNearest(remix.getPlaybackStart(), snappingInterval));
					}
				} else if ((trackerMoving == 0 || trackerMoving == 2) &&
						(Gdx.input.isButtonPressed(Input.Buttons.MIDDLE) ||
								(Gdx.input.isButtonPressed(Input.Buttons.RIGHT) &&
										(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
												Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) && !didMoveCamera)
						)) {
					if (trackerMoving != 2) {
						trackerMoving = 2;
						lastTrackerPos = remix.getMusicStartTime();
					}
					remix.setMusicStartTime(remix.getTempoChanges().beatsToSeconds(vec3Tmp2.x));
					if (!shift) {
						remix.setMusicStartTime(remix.getTempoChanges()
								.beatsToSeconds(MathHelper.snapToNearest(vec3Tmp2.x, snappingInterval)));
					}
				} else {
					if (trackerMoving != 0) {
						if (trackerMoving == 1) {
							remix.addActionWithoutMutating(
									new ActionMovePlaybackTracker(lastTrackerPos, remix.getPlaybackStart()));
						} else if (trackerMoving == 2) {
							remix.addActionWithoutMutating(
									new ActionMoveMusicTracker(lastTrackerPos, remix.getMusicStartTime()));
						}

						trackerMoving = 0;
					}
				}
			}

			// cursor only
			Entity possible = getEntityAtMouse();

			boolean isAbleToStretch = false;

			if (possible != null && remix.getSelection().size() == 1 && possible.isStretchable() &&
					remix.getSelection().contains(possible)) {
				if ((cameraPickVec3.x >= possible.getBounds().x &&
						cameraPickVec3.x <= possible.getBounds().x + STRETCHABLE_AREA) ||
						(cameraPickVec3.x >= possible.getBounds().x + possible.getBounds().width - STRETCHABLE_AREA &&
								cameraPickVec3.x <= possible.getBounds().x + possible.getBounds().width)) {
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

						e.getBounds().x = vec3Tmp2.x;
						e.getBounds().x = Math.min(MathHelper.snapToNearest(e.getBounds().x, snappingInterval),
								rightSideX - snappingInterval);
						e.getBounds().width = (selectionGroup.getOldPositions().get(0).x +
								selectionGroup.getOldPositions().get(0).width) - e.getBounds().x;
					} else if (isStretching == 2) {
						e.getBounds().width = vec3Tmp2.x - selectionGroup.getOldPositions().get(0).x;
						e.getBounds().width = Math
								.max(MathHelper.snapToNearest(e.getBounds().width, snappingInterval),
										snappingInterval);
					}
				} else {
					// change "origin" entity
					Rectangle rect = selectionGroup.getEntityClickedOn().getBounds();
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

							e.getBounds().setPosition(rect.x + selectionGroup.getRelativePositions().get(i).x,
									rect.y + selectionGroup.getRelativePositions().get(i).y);
						}
					}
				}


			} else {
				Game game = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
						.get(scrolls.get(currentSeries).getGame());
				status = Localization.get("editor.currentGame") + " " + game.getName() +
						(DebugSetting.debug ? (" [GRAY](" + game.getId() + ")[]") : "");

				if (main.camera.viewportHeight - main.getInputY() <=
						MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT) {
					if (main.camera.viewportHeight - main.getInputY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT) {
						if (main.getInputX() <=
								GAME_ICON_PADDING + ICON_COUNT_X * (GAME_ICON_PADDING + GAME_ICON_SIZE)) {
							List<Game> list = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries);
							int icon = getIconIndex(main.getInputX(),
									(int) main.camera.viewportHeight - main.getInputY());

							if (icon < list.size() && icon >= 0)
								status += " - " + Localization.get("editor.lookingAt", list.get(icon).getName());
						} else if (main.getInputX() >= main.camera.viewportWidth * 0.5f) {
							status += " - " + Localization.get("editor.scrollPatterns");
						}
					} else {
						int i = main.getInputX() / GAME_ICON_SIZE;
						if (i < Series.values.length && i >= 0) {
							status += " - " + Localization.get("editor.lookingAt", Series.values[i].getLocalizedName
									());
						}
					}
				} else {

				}

				if (remix.getSelection().size() > 0) {
					if (Gdx.input.isKeyJustPressed(Input.Keys.FORWARD_DEL) ||
							Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
//						remix.getEntities().removeAll(remix.getSelection());
						remix.mutate(new ActionDeleteEntities(remix.getSelection()));
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
							selectionGroup.getList().get(0).getBounds().width;
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

					remix.mutate(new ActionAddTempoChange(tc));
				}
			} else if (selectedTempoChange != null) {
				if (Utils.isButtonJustPressed(Input.Buttons.LEFT)) {

				}

				if (Utils.isButtonJustPressed(Input.Buttons.RIGHT)) {
					remix.mutate(new ActionRemoveTempoChange(selectedTempoChange));
				}
			}
		} else if (currentTool == Tool.SPLIT_PATTERN) {
			status = Localization.get("editor.splitPatternToolStatus");

			if (Utils.isButtonJustPressed(Input.Buttons.LEFT)) {
				Entity e = getEntityAtMouse();

				if (e instanceof PatternEntity) {
					PatternEntity pe = (PatternEntity) e;

					remix.mutate(new ActionSplitPattern(pe));
					selectionGroup = null;
					remix.getSelection().clear();
				}
			}
		}

		if (remix.getPlayingState() == PlayingState.STOPPED && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
				Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
			if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
				remix.undo();
			}

			if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
				remix.redo();
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

							Game game = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
									.get(scrolls.get(currentSeries).getGame());
							Pattern p = game.getPatterns().get(scrolls.get(currentSeries).getPattern());

							Entity en = p.getCues().size() == 1 ? new SoundEntity(this.remix,
									game.getSoundCues().stream()
											.filter(it -> it.getId().equals(p.getCues().get(0).getId())).findFirst()
											.orElse(null), 0, 0, 0) : new PatternEntity(this.remix, p);

							en.attemptLoadSounds();

							if (DebugSetting.debug && en instanceof HasGame) {
								// load entire game sounds
								HasGame hasGame = (HasGame) en;
								Main.logger.debug("Loading every sound for " + hasGame.getGame().getId());
								final long nano = System.nanoTime();
								hasGame.getGame().getSoundCues().forEach(SoundCue::attemptLoadSounds);
								Main.logger.debug("Loaded all of " + hasGame.getGame().getId() + ", took " +
										((System.nanoTime() - nano) / 1_000_000f) + " ms");
							}

							remix.getEntities().add(en);
							remix.getSelection().add(en);

							final List<Rectangle> oldPos = new ArrayList<>();
							remix.getSelection().stream()
									.map(e -> new Rectangle(e.getBounds().x, e.getBounds().y, e.getBounds().width,
											e.getBounds().height)).forEachOrdered(oldPos::add);
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

							List<Pattern> list = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
									.get(scrolls.get(currentSeries).getGame()).getPatterns();

							scrolls.get(currentSeries).setPattern(
									MathUtils.clamp(scrolls.get(currentSeries).getPattern() + dir, 0, list.size() -
											1));
						}
					}
				} else {
					// game picker
					List<Game> list = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries);
					int icon = getIconIndex(main.getInputX(), (int) main.camera.viewportHeight - main.getInputY());

					if (icon < list.size() && icon >= 0) {
						scrolls.get(currentSeries).setGame(icon);
						scrolls.get(currentSeries).setPattern(0);
					}
				}
			} else if (main.getInputY() >=
					main.camera.viewportHeight - (MESSAGE_BAR_HEIGHT + PICKER_HEIGHT + OVERVIEW_HEIGHT)) {
				// series
				if (main.getInputX() <= Series.values.length * GAME_ICON_SIZE) {
					int i = main.getInputX() / GAME_ICON_SIZE;
					if (i < Series.values.length) {
						if (GameRegistry.INSTANCE.getGamesBySeries().get(Series.values[i]) != null &&
								GameRegistry.INSTANCE.getGamesBySeries().get(Series.values[i]).size() > 0)
							currentSeries = Series.values[i];
					}
				}

				// tools
				if (main.getInputX() >= main.camera.viewportWidth - Tool.values.length * GAME_ICON_SIZE) {
					Tool[] tools = Tool.values;
					int icon = (int) (tools.length - ((main.camera.viewportWidth - main.getInputX()) /
							GAME_ICON_SIZE));

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
								.map(e -> new Rectangle(e.getBounds().x, e.getBounds().y, e.getBounds().width,
										e.getBounds().height)).forEachOrdered(oldPos::add);

						selectionGroup = new SelectionGroup(remix.getSelection(), oldPos, possible,
								new Vector2(cameraPickVec3.x / Entity.PX_WIDTH - possible.getBounds().x,
										cameraPickVec3.y / Entity.PX_HEIGHT - possible.getBounds().y), isCopying);

						// stretch code
						if (remix.getSelection().size() <= 1 && possible.isStretchable() && !isCopying) {
							cameraPickVec3.x /= Entity.PX_WIDTH;
							cameraPickVec3.y /= Entity.PX_HEIGHT;
							if ((cameraPickVec3.x >= possible.getBounds().x &&
									cameraPickVec3.x <= possible.getBounds().x + STRETCHABLE_AREA) ||
									(cameraPickVec3.x >=
											possible.getBounds().x + possible.getBounds().width - STRETCHABLE_AREA &&
											cameraPickVec3.x <= possible.getBounds().x + possible.getBounds().width)) {
								this.isStretching = (cameraPickVec3.x >= possible.getBounds().x &&
										cameraPickVec3.x <= possible.getBounds().x + STRETCHABLE_AREA) ? 1 : 2;
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
					remix.getEntities().stream().filter(e -> e.getBounds().overlaps(Rectangle.tmp
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
									.noneMatch(e2 -> e2.getBounds().overlaps(e.getBounds())) &&
									(e.getBounds().y >= 0 && e.getBounds().y + e.getBounds().height <= TRACK_COUNT));

					if (!collisionFree) {
						boolean delete = selectionGroup.getDeleteInstead() ||
								selectionGroup.getList().stream().anyMatch(e -> e.getBounds().y < 0);

						if (delete) {
							if (selectionGroup.getDeleteInstead()) {
								remix.getEntities().removeAll(selectionGroup.getList());
							} else {
								// reset positions so undo works correctly
								for (int i = 0; i < selectionGroup.getList().size(); i++) {
									Entity e = selectionGroup.getList().get(i);
									e.getBounds().set(selectionGroup.getOldPositions().get(i));
								}
								remix.mutate(new ActionDeleteEntities(selectionGroup.getList()));
							}
							remix.getSelection().clear();
						} else {
							for (int i = 0; i < selectionGroup.getList().size(); i++) {
								Entity e = selectionGroup.getList().get(i);

								e.getBounds().set(selectionGroup.getOldPositions().get(i));
							}
						}
					} else {
						if (isStretching > 0) {
							selectionGroup.getList().get(0)
									.onLengthChange(selectionGroup.getOldPositions().get(0).width);
						}

						if (selectionGroup.getDeleteInstead()) {
							// new entities, send action
							remix.addActionWithoutMutating(new ActionAddEntities(selectionGroup.getList()));
						} else {
							// move action
							remix.addActionWithoutMutating(new ActionEditEntityBounds(selectionGroup));
						}

						remix.updateDurationAndCurrentGame();
					}

					selectionGroup = null;
					selectionOrigin = null;
				}

				isStretching = 0;
			} else if (currentTool == Tool.BPM) {

			}

			remix.updateDurationAndCurrentGame();

			return true;
		}

		return false;
	}

	@Override
	public boolean scrolled(int amount) {

		if (main.camera.viewportHeight - main.getInputY() <= MESSAGE_BAR_HEIGHT + PICKER_HEIGHT) {
			if (main.getInputX() >= main.camera.viewportWidth * 0.5f) {
				List<Pattern> list = GameRegistry.INSTANCE.getGamesBySeries().get(currentSeries)
						.get(scrolls.get(currentSeries).getGame()).getPatterns();

				scrolls.get(currentSeries).setPattern(
						MathUtils.clamp(scrolls.get(currentSeries).getPattern() + amount, 0, list.size() - 1));
				return true;
			}
		} else if (remix.getPlayingState() == PlayingState.STOPPED) {
			if (currentTool == Tool.NORMAL) {
				if (remix.getSelection().size() > 0 && remix.getSelection().stream().anyMatch(Entity::isRepitchable)) {
					int[] old = remix.getSelection().stream().mapToInt(Entity::getSemitone).toArray();
					boolean anyChanged = remix.getSelection().stream()
							.map(e -> e.adjustPitch(-amount, -MAX_SEMITONE, MAX_SEMITONE)).distinct().findAny()
							.orElse(false); // CANNOT SHORT CIRCUIT

					if (anyChanged) {
						remix.addActionWithoutMutating(new ActionPitchChange(old, remix.getSelection()));
					}
					return true;
				}
			} else if (currentTool == Tool.BPM) {
				if (selectedTempoChange != null) {
					float old = selectedTempoChange.getTempo();
					float newTempo = MathUtils.clamp(selectedTempoChange.getTempo() + -amount *
							(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) ||
									Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT) ? 5 : 1) *
							(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ||
									Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT) ? 0.1f : 1), 30f, 480f);

					if (selectedTempoChange.getTempo() != newTempo) {
						TempoChange tc = new TempoChange(selectedTempoChange.getBeat(), newTempo,
								remix.getTempoChanges());

						remix.mutate(new ActionChangeTempoChange(tc, old, newTempo));

						selectedTempoChange = null;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean keyDown(int keycode) {

		if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9 &&
				remix.getPlayingState() == PlayingState.STOPPED) {
			int index = keycode - Input.Keys.NUM_1;

			if (index < Tool.values.length)
				currentTool = Tool.values[index];
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

	public void getDebugStrings(Array<String> array) {
		array.add("autosaveTimer: " + timeUntilAutosave);
		array.add("selTempoChange: " + selectedTempoChange);
	}

	public enum Tool {
		NORMAL, BPM, SPLIT_PATTERN;

		public static final Tool[] values = values();
	}

}
