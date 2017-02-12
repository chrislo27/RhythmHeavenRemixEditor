package chrislo27.rhre

import chrislo27.rhre.init.DefAssetLoader
import chrislo27.rhre.init.VisualAssetLoader
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.palette.DarkPalette
import chrislo27.rhre.palette.LightPalette
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.version.VersionChecker
import chrislo27.rhre.version.VersionState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import com.mashape.unirest.http.Unirest
import ionium.registry.AssetRegistry
import ionium.registry.GlobalVariables
import ionium.registry.ScreenRegistry
import ionium.util.DebugSetting
import ionium.util.Logger
import ionium.util.i18n.Localization

class Main(l: Logger) : ionium.templates.Main(l) {

	lateinit var biggerFont: BitmapFont
		private set
	lateinit var biggerFontBordered: BitmapFont
		private set
	lateinit var font: BitmapFont
		private set
	lateinit var fontBordered: BitmapFont
		private set

	var palette: AbstractPalette = LightPalette()
	lateinit var preferences: Preferences
		private set
	lateinit var horizontalResize: Cursor
		private set

	var helpTipsEnabled: Boolean = true
	var inspectionsEnabled: Boolean = true

	override fun getScreenToSwitchToAfterLoadingAssets(): Screen {
		return if (VersionChecker.versionState == VersionState.AVAILABLE || DebugSetting.debug)
			ScreenRegistry.get("version")
		else
			ScreenRegistry.get("editor")
	}

	override fun getAssetLoadingScreenToUse(): Screen {
		return super.getAssetLoadingScreenToUse()
	}

	override fun create() {
		ionium.templates.Main.version = "v2.3.3-SNAPSHOT"
		GlobalVariables.versionUrl = null // Deprecated - use new versioning instead
		VersionChecker

		super.create()

		Gdx.graphics.setTitle(ionium.templates.Main.getTitle())

		AssetRegistry.instance().addAssetLoader(DefAssetLoader())
		AssetRegistry.instance().addAssetLoader(VisualAssetLoader())

		DebugSetting.showFPS = false

		horizontalResize = Gdx.graphics
				.newCursor(Pixmap(Gdx.files.internal("images/cursor/horizontalResize.png")), 16, 8)

		preferences = Gdx.app.getPreferences("RHRE2")
		helpTipsEnabled = preferences.getBoolean("helpTipsEnabled", helpTipsEnabled)
		inspectionsEnabled = preferences.getBoolean("inspectionsEnabled", inspectionsEnabled)
	}

	override fun setScreen(scr: Screen?) {
		super.setScreen(scr)
	}

	override fun prepareStates() {
		super.prepareStates()

		GameRegistry.instance()
		AssetRegistry.instance().addAssetLoader(GameRegistry.instance().assetLoader)

		val reg = ScreenRegistry.instance()
		reg.add("editor", EditorScreen(this))
		reg.add("tapalong", TapalongScreen(this))
		reg.add("info", InfoScreen(this))
		reg.add("music", MusicScreen(this))
		reg.add("load", LoadScreen(this))
		reg.add("save", SaveScreen(this))
		reg.add("new", NewScreen(this))
		reg.add("soundboard", SoundboardScreen(this))
		reg.add("version", VersionScreen(this))
	}

	override fun preRender() {
		super.preRender()
	}

	override fun render() {
		super.render()
	}

	override fun postRender() {
		super.postRender()

		batch.begin()
		fontBordered.setColor(1f, 1f, 1f, 1f)
		fontBordered.data.setScale(0.5f)
		val str = if (VersionChecker.versionState == VersionState.AVAILABLE)
			Localization.get("versionAvailable", ionium.templates.Main.githubVersion, ionium.templates.Main.version)
		else
			ionium.templates.Main.version
		fontBordered.draw(batch, str, (Gdx.graphics.width - 4).toFloat(), fontBordered.capHeight + 2, 0f,
						  Align.right, false)
		fontBordered.data.setScale(1f)
		batch.end()
	}

	override fun getDebugStrings(): Array<String> {
		return super.getDebugStrings()
	}

	override fun tickUpdate() {
		super.tickUpdate()
	}

	override fun inputUpdate() {
		super.inputUpdate()

		if (DebugSetting.debug && Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			if (palette is DarkPalette) {
				palette = LightPalette()
			} else {
				palette = DarkPalette()
			}
		}
	}

	override fun loadFont() {
		super.loadFont()

		val ttfGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/PTSans.ttf"))

		val ttfParam = FreeTypeFontGenerator.FreeTypeFontParameter()
		ttfParam.magFilter = Texture.TextureFilter.Nearest
		ttfParam.minFilter = Texture.TextureFilter.Linear
		ttfParam.genMipMaps = true
		ttfParam.size = 24
		//ttfParam.characters += SpecialCharactersList.getJapaneseKana();

		font = ttfGenerator.generateFont(ttfParam)
		font.data.markupEnabled = true
		font.setFixedWidthGlyphs("0123456789")

		ttfParam.size *= 4
		biggerFont = ttfGenerator.generateFont(ttfParam)
		biggerFont.data.markupEnabled = true
		biggerFont.setFixedWidthGlyphs("0123456789")

		ttfParam.borderWidth = 1.5f
		ttfParam.size /= 4

		fontBordered = ttfGenerator.generateFont(ttfParam)
		fontBordered.data.markupEnabled = true
		fontBordered.setFixedWidthGlyphs("0123456789")

		ttfParam.size *= 4
		ttfParam.borderWidth *= 4f
		biggerFontBordered = ttfGenerator.generateFont(ttfParam)
		biggerFontBordered.data.markupEnabled = true
		biggerFontBordered.setFixedWidthGlyphs("0123456789")

		ttfGenerator.dispose()
	}

	override fun resize(width: Int, height: Int) {
		super.resize(width, height)
	}

	override fun dispose() {
		super.dispose()

		biggerFont.dispose()
		biggerFontBordered.dispose()
		font.dispose()
		fontBordered.dispose()
		preferences.flush()
		GameRegistry.instance().dispose()
		Unirest.shutdown()
	}
}
