package chrislo27.rhre.editor;

import chrislo27.rhre.EditorScreen;
import chrislo27.rhre.Main;
import com.badlogic.gdx.utils.Align;
import ionium.registry.AssetRegistry;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
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
		final Palette palette = Palettes.getIoniumDefault(main.fontBordered, main.font);
		stage = new Stage();

		{
			ImageButton playRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "play")) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);


				}
			};
			stage.addActor(playRemix);

			playRemix.align(Align.topLeft).setScreenOffset(0.5f, 0)
					.setPixelOffset(-PADDING - BUTTON_HEIGHT, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);

			playRemix.getColor().set(0, 0.5f, 0.055f, 1);
		}
		{
			ImageButton stopRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "stop")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);


				}

			};

			stopRemix.getColor().set(242 / 255f, 0.0525f, 0.0525f, 1);
			stage.addActor(stopRemix).align(Align.topLeft).setScreenOffset(0.5f, 0)
					.setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}
	}

	public Stage getStage() {
		return stage;
	}

}
