package chrislo27.rhre.editor;

import chrislo27.rhre.EditorScreen;
import chrislo27.rhre.Main;
import ionium.stage.Stage;

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
		stage = new Stage();
	}

	public Stage getStage() {
		return stage;
	}

}
