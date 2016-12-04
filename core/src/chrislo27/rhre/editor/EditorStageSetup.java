package chrislo27.rhre.editor;

import chrislo27.rhre.EditorScreen;
import chrislo27.rhre.Main;
import chrislo27.rhre.track.PlayingState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import ionium.registry.AssetRegistry;
import ionium.registry.ScreenRegistry;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
import ionium.stage.ui.LocalizationStrategy;
import ionium.stage.ui.TextButton;
import ionium.stage.ui.skin.Palette;
import ionium.stage.ui.skin.Palettes;

public class EditorStageSetup {

	public static final int BUTTON_HEIGHT = 32;
	public static final int PADDING = 4;
	public static final int BAR_HEIGHT = BUTTON_HEIGHT + PADDING * 2;

	private final Main main;
	private final EditorScreen screen;
	private Stage stage;

	public EditorStageSetup(EditorScreen sc) {
		screen = sc;
		main = sc.main;

		create();
	}

	private void create() {
		final Palette palette = Palettes.getIoniumDefault(main.font, main.font);
		stage = new Stage();

		{
			ImageButton playRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "play")) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					screen.editor.remix.setPlayingState(PlayingState.PLAYING);
				}
			};
			stage.addActor(playRemix);

			playRemix.align(Align.topLeft).setScreenOffset(0.5f, 0)
					.setPixelOffset(-BUTTON_HEIGHT / 2, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);

			playRemix.getColor().set(0, 0.5f, 0.055f, 1);
		}
		{
			ImageButton pauseRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "pause")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (screen.editor.remix.getPlayingState() == PlayingState.PLAYING)
						screen.editor.remix.setPlayingState(PlayingState.PAUSED);
				}

			};

			pauseRemix.getColor().set(0.75f, 0.75f, 0.25f, 1);
			stage.addActor(pauseRemix).align(Align.topLeft).setScreenOffset(0.5f, 0)
					.setPixelOffset(-BUTTON_HEIGHT / 2 - PADDING - BUTTON_HEIGHT, PADDING, BUTTON_HEIGHT,
							BUTTON_HEIGHT);
		}
		{
			ImageButton stopRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "stop")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					screen.editor.remix.setPlayingState(PlayingState.STOPPED);
				}

			};

			stopRemix.getColor().set(242 / 255f, 0.0525f, 0.0525f, 1);
			stage.addActor(stopRemix).align(Align.topLeft).setScreenOffset(0.5f, 0)
					.setPixelOffset(BUTTON_HEIGHT / 2 + PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			ImageButton exitGame = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "no")) {

				Runnable exitRun = () -> Gdx.app.exit();

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (screen.editor.remix.getPlayingState() != PlayingState.STOPPED)
						return;
				}

			};

			exitGame.getColor().set(0.85f, 0.25f, 0.25f, 1);
			exitGame.align(Align.topRight)
					.setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
//			stage.addActor(exitGame);
		}

		{
			TextButton info = new TextButton(stage, palette, "i"){
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("info"));
				}
			};

			info.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... objects) {
					if (key == null)
						return "";

					return key;
				}

			});

			stage.addActor(info).align(Align.topRight)
					.setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			TextButton interval = new TextButton(stage, palette, "") {

				private final int[] intervals = {4, 6, 8, 12, 24};
				private int interval = 0;

				{
					updateIntervalText();
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					interval++;

					if (interval >= intervals.length) {
						interval = 0;
					}

					screen.editor.snappingInterval = 1f / intervals[interval];

					updateIntervalText();
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					super.render(batch, alpha);
				}

				private void updateIntervalText() {
					setLocalizationKey("Snap: 1/" + intervals[interval]);
				}

			};

			interval.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... objects) {
					if (key == null)
						return "";

					return key;
				}

			});

			stage.addActor(interval).align(Align.topRight)
					.setPixelOffset(PADDING * 2 + BUTTON_HEIGHT, PADDING, BUTTON_HEIGHT * 4, BUTTON_HEIGHT);

		}
	}

	public Stage getStage() {
		return stage;
	}

}
