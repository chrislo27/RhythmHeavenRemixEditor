package chrislo27.rhre.editor;

import chrislo27.rhre.EditorScreen;
import chrislo27.rhre.Main;
import chrislo27.rhre.SoundboardScreen;
import chrislo27.rhre.json.PaletteObject;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.palette.DarkPalette;
import chrislo27.rhre.palette.LightPalette;
import chrislo27.rhre.palette.PaletteUtils;
import chrislo27.rhre.track.PlayingState;
import chrislo27.rhre.util.FileChooser;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Align;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ionium.registry.AssetRegistry;
import ionium.registry.ScreenRegistry;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
import ionium.stage.ui.LocalizationStrategy;
import ionium.stage.ui.TextButton;
import ionium.stage.ui.skin.Palette;
import ionium.stage.ui.skin.Palettes;
import ionium.util.Utils;
import ionium.util.i18n.Localization;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
		final Palette palette = Palettes.getIoniumDefault(main.getFont(), main.getFont());
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
			exitGame.align(Align.topRight).setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
//			stage.addActor(exitGame);
		}

		{
			ImageButton info = new ImageButton(stage, palette,
					new TextureRegion(AssetRegistry.getTexture("ui_infobutton"))) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("info"));
				}
			};

			stage.addActor(info).align(Align.topRight).setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			ImageButton lang = new ImageButton(stage, palette,
					new TextureRegion(AssetRegistry.getTexture("ui_language"))) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Localization.instance().nextLanguage(1);
					Localization.instance().saveToSettings(main.getPreferences());
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					super.render(batch, alpha);

					if (this.stage.isMouseOver(this)) {
						main.getFont().getData().setScale(0.5f);

						String text = Localization.instance().getCurrentBundle().getLocale().getName();
						float width = Utils.getWidth(main.getFont(), text);
						float height = Utils.getHeight(main.getFont(), text);

						batch.setColor(0, 0, 0, 0.5f);
						Main.fillRect(batch, this.getX() + this.getWidth() + PADDING,
								this.getY() - (PADDING * 3) - height, -(width + PADDING * 2), height + PADDING * 2);
						main.getFont().draw(batch, text, this.getX() + this.getWidth(), this.getY() - PADDING * 2, 0,
								Align.right, false);
						main.getFont().getData().setScale(1);
						batch.setColor(1, 1, 1, 1);
					}
				}
			};

			stage.addActor(lang).align(Align.topRight)
					.setPixelOffset(PADDING * 2 + BUTTON_HEIGHT, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			TextButton fs = new TextButton(stage, palette, "FS") {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (Gdx.graphics.isFullscreen()) {
						Gdx.graphics.setWindowedMode(main.getPreferences().getInteger("width", 1280),
								main.getPreferences().getInteger("height", 720));
					} else {
						Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
					}

					main.persistWindowSettings();
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			fs.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... objects) {
					if (key == null)
						return "";

					return key;
				}

			});

			stage.addActor(fs).align(Align.topRight)
					.setPixelOffset(PADDING * 3 + BUTTON_HEIGHT * 2, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			TextButton reset = new TextButton(stage, palette, "R") {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Gdx.graphics.setWindowedMode(1280, 720);
					main.persistWindowSettings();
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			reset.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... objects) {
					if (key == null)
						return "";

					return key;
				}

			});

			stage.addActor(reset).align(Align.topRight)
					.setPixelOffset(PADDING * 4 + BUTTON_HEIGHT * 3, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			TextButton interval = new TextButton(stage, palette, "editor.button.snap") {

				private final int[] intervals = {4, 6, 8, 12, 24};
				private int interval = 0;

				{
					setI10NStrategy(new LocalizationStrategy() {

						@Override
						public String get(String key, Object... objects) {
							if (key == null)
								return "";

							return Localization.get(key, "1/" + intervals[interval]);
						}

					});
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					interval++;

					if (interval >= intervals.length) {
						interval = 0;
					}

					screen.editor.snappingInterval = 1f / intervals[interval];
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}

			};

			stage.addActor(interval).align(Align.topRight)
					.setPixelOffset(PADDING * 5 + BUTTON_HEIGHT * 4, PADDING, BUTTON_HEIGHT * 3, BUTTON_HEIGHT);

		}

		{
			TextButton metronome = new TextButton(stage, palette, "editor.button.metronome.off") {


				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					screen.editor.remix.setTickEachBeat(!screen.editor.remix.getTickEachBeat());
					setLocalizationKey(
							"editor.button.metronome." + (screen.editor.remix.getTickEachBeat() ? "on" : "off"));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			stage.addActor(metronome).align(Align.topRight)
					.setPixelOffset(PADDING * 6 + BUTTON_HEIGHT + BUTTON_HEIGHT * 6, PADDING, BUTTON_HEIGHT * 4,
							BUTTON_HEIGHT);

		}

		{
			TextButton music = new TextButton(stage, palette, "editor.button.music") {


				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("music"));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			stage.addActor(music).align(Align.topRight)
					.setPixelOffset(PADDING * 7 + BUTTON_HEIGHT + BUTTON_HEIGHT * 10, PADDING, BUTTON_HEIGHT * 4,
							BUTTON_HEIGHT);

		}

		{
			TextButton soundboard = new TextButton(stage, palette, "editor.button.soundboard") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

//					main.setScreen(ScreenRegistry.get("soundboard"));
					main.setScreen(new SoundboardScreen(main));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

//			stage.addActor(soundboard).align(Align.topRight)
//					.setPixelOffset(PADDING * 5 + BUTTON_HEIGHT + BUTTON_HEIGHT * 11, PADDING, BUTTON_HEIGHT * 4,
//							BUTTON_HEIGHT);
		}

		{
			ImageButton newButton = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "newFile")) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("new"));
				}
			};

			newButton.getColor().set(0.25f, 0.25f, 0.25f, 1);
			stage.addActor(newButton).align(Align.topLeft)
					.setPixelOffset(PADDING, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			ImageButton openButton = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "openFile")) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("load"));
				}
			};

			openButton.getColor().set(0.25f, 0.25f, 0.25f, 1);
			stage.addActor(openButton).align(Align.topLeft)
					.setPixelOffset(PADDING * 2 + BUTTON_HEIGHT, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			ImageButton saveButton = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "saveFile")) {
				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("save"));
				}
			};

			saveButton.getColor().set(0.25f, 0.25f, 0.25f, 1);
			stage.addActor(saveButton).align(Align.topLeft)
					.setPixelOffset(PADDING * 3 + BUTTON_HEIGHT * 2, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			final boolean isMac = System.getProperty("os.name").startsWith("Mac");
			final AtomicReference<String> pathToApp = new AtomicReference<>(main.getPreferences()
					.getString("audacityLocation", System.getProperty("os.name").startsWith("Windows")
							? "C:\\Program Files (x86)\\Audacity\\audacity.exe"
							: null));
			final AtomicReference<File> file = new AtomicReference<>(
					pathToApp.get() == null ? null : new File(pathToApp.get()));

			ImageButton audacity = new ImageButton(stage, palette,
					new TextureRegion(AssetRegistry.getTexture("ui_audacity"))) {
				AtomicBoolean isRunning = new AtomicBoolean(false);
				AtomicBoolean shouldCheckVisibility = new AtomicBoolean(true);

				FileChooser chooser = new FileChooser() {
					{
						this.setFileSelectionMode(JFileChooser.FILES_ONLY);
						this.setDialogTitle("Select your Audacity application");
					}
				};

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (!isRunning.get() && file.get() != null && file.get().exists() && file.get().isFile()) {
						isRunning.set(true);

						Thread thread = new Thread(() -> {
							Process process = null;
							try {
								process = Runtime.getRuntime().exec((isMac ? "open " : "") + pathToApp);

								process.waitFor();
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								if (process != null)
									process.destroy();
								isRunning.set(false);
							}
						});
						thread.setDaemon(true);
						thread.start();
					}
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					super.render(batch, alpha);

					if (shouldCheckVisibility.get()) {
						shouldCheckVisibility.set(false);
						setEnabled(file.get() != null && file.get().exists());
					}

					if (this.stage.isMouseOver(this)) {
						main.getFont().getData().setScale(0.5f);

						String text = Localization.get("editor.button.audacity");
						float width = Utils.getWidth(main.getFont(), text);
						float height = Utils.getHeight(main.getFont(), text);

						batch.setColor(0, 0, 0, 0.5f);
						Main.fillRect(batch, this.getX() - PADDING, this.getY() - (PADDING * 3) - height,
								width + PADDING * 2, height + PADDING * 2);
						main.getFont().draw(batch, text, this.getX(), this.getY() - PADDING * 2);
						main.getFont().getData().setScale(1);
						batch.setColor(1, 1, 1, 1);

						if (!isRunning.get() && Gdx.input.isButtonPressed(Input.Buttons.RIGHT)) {
							isRunning.set(true);

							Thread thread = new Thread(() -> {
								try {
									chooser.setCurrentDirectory(file.get() == null || !file.get().exists() ? new File(
											System.getProperty("user.home"), "Desktop") : file.get());
									chooser.setVisible(true);
									int result = chooser.showDialog(null, "Select");

									if (result == JFileChooser.APPROVE_OPTION) {
										file.set(chooser.getSelectedFile());
										pathToApp.set(file.get().getAbsolutePath());
									}
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									main.getPreferences().putString("audacityLocation",
											file.get() == null ? null : file.get().getAbsolutePath());
									main.getPreferences().flush();

									shouldCheckVisibility.set(true);
									isRunning.set(false);
								}
							});
							thread.setDaemon(true);
							thread.start();
						}
					}
				}
			};

			stage.addActor(audacity);

			audacity.align(Align.topLeft)
					.setPixelOffset(PADDING * 4 + BUTTON_HEIGHT * 3, PADDING, BUTTON_HEIGHT, BUTTON_HEIGHT);
		}

		{
			TextButton paletteSwap = new TextButton(stage, palette, "editor.button.paletteSwap") {
				private final List<AbstractPalette> palettes = new ArrayList<>();

				private int num = 1;

				{
					palettes.add(new LightPalette());
					palettes.add(new DarkPalette());
					palettes.add(PaletteUtils.getRHRE0Palette());

					final FileHandle folder = Gdx.files.local("palettes/");
					if (!folder.exists()) {
						folder.mkdirs();

						FileHandle example = folder.child("example.json");

						PaletteObject po = new PaletteObject() {
							{
								LightPalette lp = new LightPalette();

								editorBg = PaletteUtils.toHex(lp.getEditorBg());
								staffLine = PaletteUtils.toHex(lp.getStaffLine());

								soundCue = PaletteUtils.toHex(lp.getSoundCue().getBg());
								stretchableSoundCue = PaletteUtils.toHex(lp.getStretchableSoundCue().getBg());
								patternCue = PaletteUtils.toHex(lp.getPattern().getBg());
								stretchablePatternCue = PaletteUtils.toHex(lp.getStretchablePattern().getBg());

								selectionCueTint = PaletteUtils.toHex(lp.getSelectionTint());

								selectionBg = PaletteUtils.toHex(lp.getSelectionFill());
								selectionBorder = PaletteUtils.toHex(lp.getSelectionBorder());

								beatTracker = PaletteUtils.toHex(lp.getBeatTracker());
								bpmTracker = PaletteUtils.toHex(lp.getBpmTracker());
								bpmTrackerSelected = PaletteUtils.toHex(lp.getBpmTrackerSelected());
								musicStartTracker = PaletteUtils.toHex(lp.getMusicStartTracker());

								cueText = PaletteUtils.toHex(lp.getCueText());
							}
						};

						example.writeString(
								new GsonBuilder().setPrettyPrinting().create().toJson(po, PaletteObject.class), false,
								"UTF-8");
					}

					if (folder.exists() && folder.isDirectory()) {
						final FileHandle[] list = folder.list((dir, name) -> !name.equals("example.json") &&
								name.toLowerCase(Locale.ROOT).endsWith(".json"));
						Main.logger.info("Found " + list.length + " palette files");

						final Gson gson = new GsonBuilder().create();
						Arrays.stream(list).forEach(fh -> {
							Main.logger.info("Loading palette " + fh.name());

							palettes.add(PaletteUtils
									.getPaletteFromObject(gson.fromJson(fh.readString("UTF-8"), PaletteObject.class)));

							Main.logger.info("Loaded palette " + fh.name());
						});
					}

					num = main.getPreferences().getInteger("palette", 0);
					if (num >= palettes.size())
						num = 0;

					cycle();
				}

				private void cycle() {
					main.switchPalette(palettes.get(num), 0.35f);

					main.getPreferences().putInteger("palette", num);
					main.getPreferences().flush();

					num++;
					if (num >= palettes.size())
						num = 0;
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					cycle();
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			stage.addActor(paletteSwap).align(Align.topLeft)
					.setPixelOffset(PADDING * 5 + BUTTON_HEIGHT * 4, PADDING, BUTTON_HEIGHT * 3, BUTTON_HEIGHT);
		}

		{
			TextButton tapalong = new TextButton(stage, palette, "editor.button.tapalong") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setScreen(ScreenRegistry.get("tapalong"));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			stage.addActor(tapalong).align(Align.topLeft)
					.setPixelOffset(PADDING * 6 + BUTTON_HEIGHT * 7, PADDING, BUTTON_HEIGHT * 3, BUTTON_HEIGHT);
		}

		{
			TextButton inspections = new TextButton(stage, palette, "editor.button.inspections.on") {

				{
					setLocalizationKey("editor.button.inspections." + (main.getInspectionsEnabled() ? "on" : "off"));
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setInspectionsEnabled(!main.getInspectionsEnabled());
					main.getPreferences().flush();
					setLocalizationKey("editor.button.inspections." + (main.getInspectionsEnabled() ? "on" : "off"));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

			stage.addActor(inspections).align(Align.topLeft)
					.setPixelOffset(PADDING * 7 + BUTTON_HEIGHT * 10, PADDING, BUTTON_HEIGHT * 4, BUTTON_HEIGHT);
		}

		{
			TextButton stats = new TextButton(stage, palette, "editor.button.stats") {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

//			stage.addActor(stats).align(Align.topLeft)
//					.setPixelOffset(PADDING * 8 + BUTTON_HEIGHT * 14, PADDING, BUTTON_HEIGHT * 3 + PADDING * 2,
//							BUTTON_HEIGHT);
		}

		{
			TextButton helpTips = new TextButton(stage, palette, "editor.button.helpTips.on") {

				{
					setLocalizationKey("editor.button.helpTips." + (main.getHelpTipsEnabled() ? "on" : "off"));
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					main.setHelpTipsEnabled(!main.getHelpTipsEnabled());
					main.getPreferences().flush();
					setLocalizationKey("editor.button.helpTips." + (main.getHelpTipsEnabled() ? "on" : "off"));
				}

				@Override
				public void render(SpriteBatch batch, float alpha) {
					getPalette().labelFont.getData().setScale(0.5f);
					super.render(batch, alpha);
					getPalette().labelFont.getData().setScale(1);
				}
			};

//			stage.addActor(helpTips).align(Align.topLeft)
//					.setPixelOffset(PADDING * 7 + BUTTON_HEIGHT * 13, PADDING, BUTTON_HEIGHT * 4, BUTTON_HEIGHT);
		}
	}

	public Stage getStage() {
		return stage;
	}

}
