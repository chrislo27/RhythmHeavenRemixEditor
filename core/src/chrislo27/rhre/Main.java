package chrislo27.rhre;

import chrislo27.rhre.init.DefAssetLoader;
import chrislo27.rhre.palette.AbstractPalette;
import chrislo27.rhre.palette.DarkPalette;
import chrislo27.rhre.palette.LightPalette;
import chrislo27.rhre.registry.GameRegistry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import ionium.registry.AssetRegistry;
import ionium.registry.GlobalVariables;
import ionium.registry.ScreenRegistry;
import ionium.util.DebugSetting;
import ionium.util.Logger;
import ionium.util.i18n.Localization;

public class Main extends ionium.templates.Main {

	public BitmapFont biggerFont;
	public BitmapFont biggerFontBordered;
	public BitmapFont font;
	public BitmapFont fontBordered;

	public AbstractPalette palette = new LightPalette();

	public Cursor horizontalResize;

	private volatile int newVersionAvailable = -1;

	public Main(Logger l) {
		super(l);
	}

	@Override
	public Screen getScreenToSwitchToAfterLoadingAssets() {
		return ScreenRegistry.get("editor");
	}

	@Override
	public Screen getAssetLoadingScreenToUse() {
		return super.getAssetLoadingScreenToUse();
	}

	@Override
	public void create() {
		Main.version = "v2.0.0";
		GlobalVariables.versionUrl = "https://raw.githubusercontent.com/chrislo27/VersionPlace/master/RHRE-version" +
				".txt";

		super.create();

		Gdx.graphics.setTitle(getTitle());

		AssetRegistry.instance().addAssetLoader(new DefAssetLoader());

		DebugSetting.showFPS = false;

		horizontalResize = Gdx.graphics
				.newCursor(new Pixmap(Gdx.files.internal("images/cursor/horizontalResize.png")), 16, 8);
	}

	@Override
	public void prepareStates() {
		super.prepareStates();

		GameRegistry.instance();
		AssetRegistry.instance().addAssetLoader(GameRegistry.instance().getAssetLoader());

		ScreenRegistry reg = ScreenRegistry.instance();
		reg.add("editor", new EditorScreen(this));
		reg.add("tapalong", new TapalongScreen(this));
		reg.add("info", new InfoScreen(this));
		reg.add("music", new MusicScreen(this));
		reg.add("load", new LoadScreen(this));
		reg.add("save", new SaveScreen(this));
		reg.add("new", new NewScreen(this));
	}

	@Override
	protected void preRender() {
		super.preRender();
	}

	@Override
	public void render() {
		super.render();
	}

	@Override
	protected void postRender() {
		super.postRender();

		if (newVersionAvailable == -1 && Main.githubVersion != null) {
			if (!Main.githubVersion.equals(Main.version)) {
				newVersionAvailable = 1;
			} else {
				newVersionAvailable = 0;
			}
		}

		batch.begin();
		fontBordered.setColor(1, 1, 1, 1);
		fontBordered.getData().setScale(0.5f);
		fontBordered.draw(batch, newVersionAvailable == 1
				? Localization.get("versionAvailable", Main.githubVersion, Main.version)
				: version, Gdx.graphics.getWidth() - 4, fontBordered.getCapHeight() + 2, 0, Align.right, false);
		fontBordered.getData().setScale(1);
		batch.end();
	}

	@Override
	protected Array<String> getDebugStrings() {
		return super.getDebugStrings();
	}

	@Override
	public void tickUpdate() {
		super.tickUpdate();
	}

	@Override
	public void inputUpdate() {
		super.inputUpdate();

		if (DebugSetting.debug && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			if (palette instanceof DarkPalette) {
				palette = new LightPalette();
			} else {
				palette = new DarkPalette();
			}
		}
	}

	@Override
	public void loadFont() {
		super.loadFont();

		FreeTypeFontGenerator ttfGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/PTSans.ttf"));

		FreeTypeFontGenerator.FreeTypeFontParameter ttfParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
		ttfParam.magFilter = Texture.TextureFilter.Nearest;
		ttfParam.minFilter = Texture.TextureFilter.Linear;
		ttfParam.genMipMaps = true;
		ttfParam.size = 24;
		//ttfParam.characters += SpecialCharactersList.getJapaneseKana();

		font = ttfGenerator.generateFont(ttfParam);
		font.getData().markupEnabled = true;
		font.setFixedWidthGlyphs("0123456789");

		ttfParam.size *= 4;
		biggerFont = ttfGenerator.generateFont(ttfParam);
		biggerFont.getData().markupEnabled = true;
		biggerFont.setFixedWidthGlyphs("0123456789");

		ttfParam.borderWidth = 1.5f;
		ttfParam.size /= 4;

		fontBordered = ttfGenerator.generateFont(ttfParam);
		fontBordered.getData().markupEnabled = true;
		fontBordered.setFixedWidthGlyphs("0123456789");

		ttfParam.size *= 4;
		ttfParam.borderWidth *= 4;
		biggerFontBordered = ttfGenerator.generateFont(ttfParam);
		biggerFontBordered.getData().markupEnabled = true;
		biggerFontBordered.setFixedWidthGlyphs("0123456789");

		ttfGenerator.dispose();
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
	}

	@Override
	public void dispose() {
		super.dispose();

		biggerFont.dispose();
		biggerFontBordered.dispose();
		font.dispose();
		fontBordered.dispose();
	}
}
