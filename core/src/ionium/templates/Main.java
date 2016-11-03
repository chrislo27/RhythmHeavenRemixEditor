package ionium.templates;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import ionium.audio.transition.MusicTransitioner;
import ionium.benchmarking.TickBenchmark;
import ionium.registry.AssetRegistry;
import ionium.registry.ErrorLogRegistry;
import ionium.registry.GlobalVariables;
import ionium.registry.ScreenRegistry;
import ionium.screen.AssetLoadingScreen;
import ionium.screen.Updateable;
import ionium.transition.Transition;
import ionium.transition.TransitionScreen;
import ionium.util.*;
import ionium.util.CaptureStream.Consumer;
import ionium.util.i18n.Localization;
import ionium.util.render.Gears;
import ionium.util.render.Shaders;
import ionium.util.version.VersionGetter;

import javax.swing.*;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * 
 * Main class, think of it like slick's Main class
 *
 */
public abstract class Main extends Game implements Consumer {

	public OrthographicCamera camera;

	public SpriteBatch batch;

	public ShapeRenderer shapes;

	public ImmediateModeRenderer20 verticesRenderer;

	public BitmapFont defaultFont;
	public BitmapFont arial;
	public BitmapFont debugFont;

	private static Color rainbow = new Color();
	private static Color inverseRainbow = new Color();

	public static String version = "v0.1.0-alpha";
	public static String githubVersion = null;

	public static Texture filltex;
	public static TextureRegion filltexRegion;

	/**
	 * Pixmap used to "hide" the cursor.
	 */
	public static Pixmap clearPixmapCursor;

	public ShaderProgram maskshader;
	public ShaderProgram greyshader;
	public ShaderProgram warpshader;
	public ShaderProgram blurshader;
	public static ShaderProgram defaultShader;
	public ShaderProgram invertshader;
	public static ShaderProgram meshShader;
	public ShaderProgram maskNoiseShader;

	private CaptureStream output;
	private PrintStream printstrm;
	private JFrame consolewindow;
	private JTextArea consoletext;
	private JScrollPane conscrollPane;

	private long lastKnownNano = System.nanoTime();
	public static float totalSeconds = 0f;
	public static long totalFrames = 0;
	public static long totalTicks = 0;
	public static float tickDeltaTime = 0;
	private long totalTicksElapsed = 0;
	private long lastTickDurationNano = 0;
	private long nanoUntilTick = 1;
	private String lastSetNullScreen = "<no data>";

	private Array<String> debugStrings = new Array<>();
	private Array<String> persistentDebugStrings = new Array<>();
	private Array<String> tickBenchmarkColors = new Array<>();
	private int lineToRenderDebugTickGraph = 0;
	private static final float TICK_BENCHMARK_GRAPH_WIDTH = 256;

	public static Gears gears;

	/**
	 * use this rather than Gdx.app.log
	 */
	public static Logger logger;

	public Main(Logger l) {
		super();
		logger = l;
	}

	@Override
	public void create() {
		redirectSysOut();

		ShaderProgram.pedantic = false;
		camera = new OrthographicCamera();
		camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		batch = new SpriteBatch();
		batch.enableBlending();

		verticesRenderer = new ImmediateModeRenderer20(false, true, 0);

		defaultShader = SpriteBatch.createDefaultShader();
		AssetRegistry.createMissingTexture();

		loadFont();

		arial = new BitmapFont();
		arial.getRegion().getTexture().setFilter(TextureFilter.Linear, TextureFilter.Linear);

		Pixmap pix = new Pixmap(1, 1, Format.RGBA8888);
		pix.setColor(Color.WHITE);
		pix.fill();
		filltex = new Texture(pix);
		pix.dispose();
		filltexRegion = new TextureRegion(filltex);
		clearPixmapCursor = new Pixmap(16, 16, Format.RGBA8888);
		clearPixmapCursor.setColor(0, 0, 0, 0);
		clearPixmapCursor.fill();

		shapes = new ShapeRenderer();

		maskshader = new ShaderProgram(Shaders.VERTDEFAULT, Shaders.FRAGBAKE);
		maskshader.begin();
		maskshader.setUniformi("u_mask", 1);
		maskshader.end();

		greyshader = new ShaderProgram(Shaders.VERTGREY, Shaders.FRAGGREY);

		warpshader = new ShaderProgram(Shaders.VERTDEFAULT, Shaders.FRAGWARP);
		warpshader.begin();
		warpshader.setUniformf(warpshader.getUniformLocation("time"), totalSeconds);
		warpshader.setUniformf(warpshader.getUniformLocation("amplitude"), 1.0f, 1.0f);
		warpshader.setUniformf(warpshader.getUniformLocation("frequency"), 1.0f, 1.0f);
		warpshader.setUniformf(warpshader.getUniformLocation("speed"), 1f);
		warpshader.end();

		blurshader = new ShaderProgram(Shaders.VERTBLUR, Shaders.FRAGBLUR);
		blurshader.begin();
		blurshader.setUniformf("dir", 1f, 0f);
		blurshader.setUniformf("resolution", GlobalVariables.defaultWidth);
		blurshader.setUniformf("radius", 2f);
		blurshader.end();

		maskNoiseShader = new ShaderProgram(Shaders.VERTDEFAULT, Shaders.FRAGBAKENOISE);

		invertshader = new ShaderProgram(Shaders.VERTINVERT, Shaders.FRAGINVERT);
		meshShader = new ShaderProgram(Shaders.VERTMESH, Shaders.FRAGMESH);

		loadUnmanagedAssets();
		loadAssets();

		Gdx.input.setInputProcessor(getDefaultInput());

		prepareStates();

		this.setScreen(getAssetLoadingScreenToUse());

		new Thread("version checker") {

			@Override
			public void run() {
				VersionGetter.instance().getVersionFromServer();
				persistentDebugStrings.clear();
			}
		}.start();
	}

	public void prepareStates() {
		ScreenRegistry reg = ScreenRegistry.instance();
		reg.add("ionium_assetloading", new AssetLoadingScreen(this));
		reg.add("ionium_transition", new TransitionScreen(this));
	}

	public Screen getAssetLoadingScreenToUse() {
		return ScreenRegistry.get("ionium_assetloading");
	}

	@Override
	public void dispose() {
		batch.dispose();
		verticesRenderer.dispose();
		AssetRegistry.instance().dispose();
		defaultFont.dispose();
		arial.dispose();
		maskshader.dispose();
		warpshader.dispose();
		blurshader.dispose();
		invertshader.dispose();
		meshShader.dispose();
		maskNoiseShader.dispose();
		shapes.dispose();
		clearPixmapCursor.dispose();
		debugFont.dispose();

		// dispose screens
		ScreenRegistry.instance().dispose();
	}

	protected void preRender() {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glClearDepthf(1f);
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

		camera.update();
		batch.setProjectionMatrix(camera.combined);
		gears.update(1);
	}

	@Override
	public void render() {
		totalSeconds += Gdx.graphics.getDeltaTime();
		nanoUntilTick += (System.nanoTime() - lastKnownNano);
		lastKnownNano = System.nanoTime();
		tickDeltaTime += Gdx.graphics.getDeltaTime();

		try {
			// ticks
			while (nanoUntilTick >= (1_000_000_000 / GlobalVariables.ticks)) {
				long nano = System.nanoTime();

				TickBenchmark.instance().startBenchmarking();

				if (getScreen() != null) {
					((Updateable) getScreen()).tickUpdate();
				}

				TickBenchmark.instance().start("ionium");
				tickUpdate();
				TickBenchmark.instance().stop("ionium");

				TickBenchmark.instance().stopBenchmarking();

				totalTicks++;
				tickDeltaTime = 0;

				lastTickDurationNano = System.nanoTime() - nano;

				nanoUntilTick -= (1_000_000_000 / GlobalVariables.ticks);
			}

			MusicTransitioner.instance().update();

			// render updates
			if (getScreen() != null) {
				((Updateable) getScreen()).renderUpdate();
			}

			preRender();
			super.render();
			postRender();

			totalFrames += 1;

		} catch (Exception e) {
			e.printStackTrace();

			Gdx.files.local("crash/").file().mkdir();
			FileHandle handle = Gdx.files.local("crash/crash-log_"
					+ new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date()).trim()
					+ ".txt");

			handle.writeString(ErrorLogRegistry.instance().createErrorLog(output.toString()),
					false);

			resetSystemOut();
			System.out.println(
					"\n\nThe game crashed. There is an error log at " + handle.path() + "\n");

			Gdx.app.exit();
		}

	}

	protected void postRender() {
		batch.begin();

		debugFont.setColor(1, 1, 1, 1);

		if (DebugSetting.showFPS || DebugSetting.debug) {
			debugFont.draw(
					batch, "FPS: "
							+ (Gdx.graphics.getFramesPerSecond() <= (GlobalVariables.maxFps / 4f)
									? "[RED]"
									: (Gdx.graphics
											.getFramesPerSecond() <= (GlobalVariables.maxFps / 2f)
													? "[YELLOW]" : ""))
							+ Gdx.graphics.getFramesPerSecond() + "[]",
					5, Gdx.graphics.getHeight() - (debugFont.getCapHeight() * 1.25f));
		}

		if (DebugSetting.debug) {
			// update array
			this.getDebugStrings();
			if (getScreen() != null) ((Updateable) getScreen()).getDebugStrings(debugStrings);

			float baseHeight = Gdx.graphics.getHeight() - (debugFont.getCapHeight() * 1.25f * 3f);

			debugFont.setColor(1, 1, 1, 1);

			for (int i = 0; i < debugStrings.size; i++) {
				float height = (baseHeight - ((debugFont.getCapHeight() * 1.5f) * (i)));

				debugFont.draw(batch, debugStrings.get(i), 5, height);

				if (DebugSetting.showTickBenchmarks && i == lineToRenderDebugTickGraph) {
					int colorNum = 0;
					float currentPercentage = 0;
					for (Entry<String, Long> entry : TickBenchmark.instance().getAllEntries()) {
						float percentage = entry.value * 1f
								/ TickBenchmark.instance().getTotalTime();

						batch.setColor(Colors.get(tickBenchmarkColors.get(colorNum)));
						Main.fillRect(batch, 5 + currentPercentage * TICK_BENCHMARK_GRAPH_WIDTH,
								height, percentage * TICK_BENCHMARK_GRAPH_WIDTH,
								-debugFont.getLineHeight());
						batch.setColor(1, 1, 1, 1);

						colorNum++;
						currentPercentage += percentage;
					}
				}
			}

		}

		if (this.getScreen() == null) {
			debugFont.draw(batch, "null screen: " + lastSetNullScreen, Gdx.graphics.getWidth() / 2,
					Gdx.graphics.getHeight() * 0.4f, 0, Align.center, false);
		}

		batch.end();

		warpshader.begin();
		warpshader.setUniformf(warpshader.getUniformLocation("time"), totalSeconds);
		warpshader.setUniformf(warpshader.getUniformLocation("amplitude"), 0.25f, 0.1f);
		warpshader.setUniformf(warpshader.getUniformLocation("frequency"), 10f, 5f);
		warpshader.setUniformf(warpshader.getUniformLocation("speed"), 2.5f);
		warpshader.end();

		inputUpdate();
	}

	protected Array<String> getDebugStrings() {
		if (MemoryUtils.getUsedMemory() > getMostMemory)
			getMostMemory = MemoryUtils.getUsedMemory();

		debugStrings.clear();

		if (persistentDebugStrings.size == 0) {
			StringBuffer keysInfo = new StringBuffer();

			keysInfo.append("Detached console: " + Keys.toString(DebugSetting.CONSOLE_KEY));
			keysInfo.append(
					" | Re-init I18N: " + Keys.toString(DebugSetting.REINIT_LOCALIZATION_KEY));
			keysInfo.append(
					" | Tick percentages: " + Keys.toString(DebugSetting.TICK_PERCENTAGE_KEY));

			persistentDebugStrings.add("Debug info: " + Keys.toString(DebugSetting.DEBUG_KEY));
			persistentDebugStrings.add(keysInfo.toString());
			persistentDebugStrings.add("");
			persistentDebugStrings.add("version: " + Main.version
					+ (githubVersion == null ? "" : "; latestV: " + Main.githubVersion));
			persistentDebugStrings.add("OS: " + System.getProperty("os.name") + ", "
					+ MemoryUtils.getCores() + " cores");
		}

		for (String s : persistentDebugStrings) {
			debugStrings.add(s);
		}

		debugStrings.add("memory: " + NumberFormat.getInstance().format(MemoryUtils.getUsedMemory())
				+ " KB / " + NumberFormat.getInstance().format(MemoryUtils.getMaxMemory())
				+ " KB (max " + NumberFormat.getInstance().format(getMostMemory) + " KB) ");
		debugStrings.add("tickDuration: " + (lastTickDurationNano / 1000000f) + " ms");
		debugStrings.add("tick delta: " + tickDeltaTime);
		debugStrings.add("frame delta: " + Gdx.graphics.getDeltaTime());
		debugStrings.add("state: "
				+ (getScreen() == null ? "null" : getScreen().getClass().getSimpleName()));

		if (DebugSetting.showTickBenchmarks) {
			debugStrings.add("");
			debugStrings.add("Tick benchmarks:");

			if (tickBenchmarkColors.size == 0) {
				Colors.getColors().keys().toArray(tickBenchmarkColors);
			}

			int colorNum = 0;
			for (Entry<String, Long> entry : TickBenchmark.instance().getAllEntries()) {
				float msTime = entry.value / 1_000_000f;

				debugStrings.add("[" + tickBenchmarkColors.get(colorNum) + "]" + entry.key + ": "
						+ String.format("%.5f", msTime) + " ms ("
						+ String.format("%.1f",
								(entry.value * 100f / TickBenchmark.instance().getTotalTime()))
						+ "%)");

				colorNum++;
			}

			debugStrings.add("Total time: "
					+ String.format("%.5f", TickBenchmark.instance().getTotalTime() / 1_000_000f)
					+ " ms");

			// this is where the bar graph goes
			debugStrings.add("");
			lineToRenderDebugTickGraph = debugStrings.size - 1;

			debugStrings.add("");
		}

		// newline before screen debug
		debugStrings.add("");

		return debugStrings;
	}

	public void inputUpdate() {
		if (Gdx.input.isKeyJustPressed(Keys.F1)) {
			ScreenshotFactory.saveScreenshot();
		}

		if (Gdx.input.isKeyPressed(DebugSetting.DEBUG_KEY)) {
			if (Gdx.input.isKeyJustPressed(DebugSetting.CONSOLE_KEY)) {
				if (consolewindow.isVisible()) {
					consolewindow.setVisible(false);
				} else {
					consolewindow.setVisible(true);
					conscrollPane.getVerticalScrollBar()
							.setValue(conscrollPane.getVerticalScrollBar().getMaximum());
				}
			} else if (Gdx.input.isKeyJustPressed(DebugSetting.REINIT_LOCALIZATION_KEY)) {
				Localization.instance().reloadFromFile();
				Main.logger.debug("Reloaded I18N from files");
			} else if (Gdx.input.isKeyJustPressed(DebugSetting.TICK_PERCENTAGE_KEY)) {
				DebugSetting.showTickBenchmarks = !DebugSetting.showTickBenchmarks;
			} else if (Gdx.input.isKeyJustPressed(DebugSetting.DEBUG_KEY)) {
				DebugSetting.debug = !DebugSetting.debug;
			}
		}

	}

	public void tickUpdate() {

	}

	public void loadFont() {
		FreeTypeFontGenerator ttfGenerator = new FreeTypeFontGenerator(
				Gdx.files.internal("fonts/courbd.ttf"));
		FreeTypeFontParameter ttfParam = new FreeTypeFontParameter();
		ttfParam.magFilter = TextureFilter.Nearest;
		ttfParam.minFilter = TextureFilter.Nearest;
		ttfParam.genMipMaps = true;
		ttfParam.size = 20;
		ttfParam.characters += SpecialCharactersList.getJapaneseKana();
		defaultFont = ttfGenerator.generateFont(ttfParam);
		defaultFont.getData().markupEnabled = true;

		ttfParam.borderColor = new Color(0, 0, 0, 1);
		ttfParam.borderWidth = 1.5f;
		debugFont = ttfGenerator.generateFont(ttfParam);
		debugFont.getData().markupEnabled = true;

		ttfGenerator.dispose();
	}

	private void loadAssets() {
		AssetMap.instance(); // load asset map namer thing
		Localization.instance();

		// the default assets are already added in StandardAssetLoader
	}

	protected void loadUnmanagedAssets() {
		long timeTaken = System.currentTimeMillis();

		AssetRegistry.instance().loadUnmanagedTextures();

		// load gears instance (used in loading screen)
		gears = new Gears(this);

		Main.logger.info("Finished loading all unmanaged assets, took "
				+ (System.currentTimeMillis() - timeTaken) + " ms");
	}

	public static String getTitle() {
		return (Localization.get("gamename") + " " + version);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);

		camera.setToOrtho(false, width, height);
		shapes.setProjectionMatrix(camera.combined);

		for (Updateable up : ScreenRegistry.instance().getAll()) {
			if (getScreen() != null && getScreen() == up) continue;

			up.resize(width, height);
		}
	}

	public void redirectSysOut() {
		PrintStream ps = System.out;
		output = new CaptureStream(this, ps);
		printstrm = new PrintStream(output);
		resetConsole();
		System.setOut(printstrm);
	}

	public void resetConsole() {
		consolewindow = new JFrame();
		consolewindow.setTitle("Console for " + Localization.get("gamename") + " " + Main.version);
		consolewindow.setVisible(false);
		consoletext = new JTextArea(40, 60);
		consoletext.setEditable(false);
		conscrollPane = new JScrollPane(consoletext);
		consolewindow.add(conscrollPane, null);
		consolewindow.pack();
	}

	public void resetSystemOut() {
		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
	}

	@Override
	public void appendText(final String text) {
		consoletext.append(text);
		consoletext.setCaretPosition(consoletext.getText().length());
	}

	@Override
	public void setScreen(Screen scr) {
		super.setScreen(scr);

		if (getScreen() == null) {
			lastSetNullScreen = ScreenRegistry.instance().lastNullScreen;
		}
	}

	public void transition(Transition from, Transition to, Screen next) {
		TransitionScreen transition = ScreenRegistry.get("ionium_transition",
				TransitionScreen.class);

		transition.prepare(this.getScreen(), from, to, next);
		setScreen(transition);
	}

	public static Color getRainbow() {
		return getRainbow(System.currentTimeMillis(), 1, 1);
	}

	public static Color getRainbow(float s) {
		return getRainbow(System.currentTimeMillis(), s, 1);
	}

	public static Color getRainbow(long ms, float s, float saturation) {
		return rainbow.set(Utils.HSBtoRGBA8888(
				(s < 0 ? 1.0f : 0) - MathHelper.getSawtoothWave(ms, Math.abs(s)), saturation,
				0.75f)).clamp();
	}

	public InputMultiplexer getDefaultInput() {
		InputMultiplexer plexer = new InputMultiplexer();
		plexer.addProcessor(new MainInputProcessor(this));
		return plexer;
	}

	private static Random random = new Random();

	public static Random getRandom() {
		return random;
	}

	public static void fillRect(Batch batch, float x, float y, float width, float height) {
		batch.draw(filltex, x, y, width, height);
	}

	public static void drawRect(Batch batch, float x, float y, float width, float height,
			float thickness) {
		if (width < 0) {
			width = Math.abs(width);
			x -= width;
		}

		if (height < 0) {
			height = Math.abs(height);
			y -= height;
		}

		// bottom
		batch.draw(filltex, x, y, width, thickness);

		// top
		batch.draw(filltex, x, y + height - thickness, width, thickness);

		// left
		batch.draw(filltex, x, y + thickness, thickness, height - (thickness * 2));

		// right
		batch.draw(filltex, x + width - thickness, y + thickness, thickness,
				height - (thickness * 2));
	}

	private static float[] gradientverts = new float[20];
	private static Color tempGradientColor = new Color();

	public static void drawGradient(SpriteBatch batch, float x, float y, float width, float height,
			Color bl, Color br, Color tr, Color tl) {

		tempGradientColor.set((bl.r + br.r + tr.r + tl.r) / 4f, (bl.g + br.g + tr.g + tl.g) / 4f,
				(bl.b + br.b + tr.b + tl.b) / 4f, (bl.a + br.a + tr.a + tl.a) / 4f);

		int idx = 0;

		// draw bottom face
		idx = 0;
		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		gradientverts[idx++] = x;
		gradientverts[idx++] = y;
		gradientverts[idx++] = bl.toFloatBits(); // bottom left
		gradientverts[idx++] = filltexRegion.getU(); //NOTE: texture coords origin is top left
		gradientverts[idx++] = filltexRegion.getV2();

		gradientverts[idx++] = x + width;
		gradientverts[idx++] = y;
		gradientverts[idx++] = br.toFloatBits(); // bottom right
		gradientverts[idx++] = filltexRegion.getU2();
		gradientverts[idx++] = filltexRegion.getV2();

		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		batch.draw(filltexRegion.getTexture(), gradientverts, 0, gradientverts.length);

		// draw top face
		idx = 0;
		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		gradientverts[idx++] = x;
		gradientverts[idx++] = y + height;
		gradientverts[idx++] = tl.toFloatBits(); // top left
		gradientverts[idx++] = filltexRegion.getU();
		gradientverts[idx++] = filltexRegion.getV();

		gradientverts[idx++] = x + width;
		gradientverts[idx++] = y + height;
		gradientverts[idx++] = tr.toFloatBits(); // top right
		gradientverts[idx++] = filltexRegion.getU2();
		gradientverts[idx++] = filltexRegion.getV();

		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		batch.draw(filltexRegion.getTexture(), gradientverts, 0, gradientverts.length);

		// draw left face
		idx = 0;
		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		gradientverts[idx++] = x;
		gradientverts[idx++] = y + height;
		gradientverts[idx++] = tl.toFloatBits(); // top left
		gradientverts[idx++] = filltexRegion.getU();
		gradientverts[idx++] = filltexRegion.getV();

		gradientverts[idx++] = x;
		gradientverts[idx++] = y;
		gradientverts[idx++] = bl.toFloatBits(); // bottom left
		gradientverts[idx++] = filltexRegion.getU(); //NOTE: texture coords origin is top left
		gradientverts[idx++] = filltexRegion.getV2();

		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		batch.draw(filltexRegion.getTexture(), gradientverts, 0, gradientverts.length);

		// draw right face
		idx = 0;
		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		gradientverts[idx++] = x + width;
		gradientverts[idx++] = y + height;
		gradientverts[idx++] = tr.toFloatBits(); // top right
		gradientverts[idx++] = filltexRegion.getU2();
		gradientverts[idx++] = filltexRegion.getV();

		gradientverts[idx++] = x + width;
		gradientverts[idx++] = y;
		gradientverts[idx++] = br.toFloatBits(); // bottom right
		gradientverts[idx++] = filltexRegion.getU2();
		gradientverts[idx++] = filltexRegion.getV2();

		gradientverts[idx++] = x + (width / 2);
		gradientverts[idx++] = y + (height / 2);
		gradientverts[idx++] = tempGradientColor.toFloatBits(); // middle
		gradientverts[idx++] = 0.5f;
		gradientverts[idx++] = 0.5f;

		batch.draw(filltexRegion.getTexture(), gradientverts, 0, gradientverts.length);

	}

	/**
	 * Call after the masking shader is set to mask a texture onto another stencil texture. Use with maskShader.
	 * 
	 * @param mask mask itself (generally base tex as well)
	 */
	public static void useMask(Texture mask) {
		mask.bind(1);
		Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
	}

	/**
	 * converts y-down to y-up
	 * 
	 * @param f
	 *            the num. of px down from the top of the screen
	 * @return the y-down conversion of input
	 */
	public static int convertY(float f) {
		return Math.round(Gdx.graphics.getHeight() - f);
	}

	public void drawTextBg(BitmapFont font, String text, float x, float y, float wrapWidth,
			int align) {
		batch.setColor(0, 0, 0, batch.getColor().a * 0.6f);
		fillRect(batch, x, y, Utils.getWidth(font, text) + 2, (Utils.getHeight(font, text)) + 2);
		font.draw(batch, text, x + 1, y + font.getCapHeight(), wrapWidth, align, true);
		batch.setColor(1, 1, 1, 1);
	}

	public void drawTextBg(BitmapFont font, String text, float x, float y) {
		drawTextBg(font, text, x, y, Utils.getWidth(font, text), Align.left);
	}

	public int getMostMemory = MemoryUtils.getUsedMemory();

	public void setClearColor(int r, int g, int b) {
		Gdx.gl20.glClearColor(r / 255f, g / 255f, b / 255f, 1f);
	}

	public long getNanoUntilTick() {
		return nanoUntilTick;
	}

	public abstract Screen getScreenToSwitchToAfterLoadingAssets();

}
