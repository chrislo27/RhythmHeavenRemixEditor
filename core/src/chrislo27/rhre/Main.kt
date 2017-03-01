package chrislo27.rhre

import chrislo27.rhre.init.DefAssetLoader
import chrislo27.rhre.init.VisualAssetLoader
import chrislo27.rhre.lazysound.LazySound
import chrislo27.rhre.lazysound.LazySoundLoader
import chrislo27.rhre.logging.SysOutPiper
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
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
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

	private var lastPalette: AbstractPalette = LightPalette()
	private var toSwitchPalette: AbstractPalette = lastPalette
	private val currentPalette: AbstractPalette = LightPalette()
	private var paletteLerp: Vector2 = Vector2()

	fun getInputX(): Int {
		return camera.unproject(inputProj.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f)).x.toInt()
	}

	fun getInputY(): Int {
		return (camera.unproject(
				inputProj.set(Gdx.input.x.toFloat(), (Gdx.graphics.height - Gdx.input.y).toFloat(), 0f)).y).toInt()
	}

	fun getPalette(): AbstractPalette {
		return currentPalette
	}

	fun switchPalette(newPalette: AbstractPalette, interpolation: Float) {
		paletteLerp.x = interpolation
		paletteLerp.y = 0f

		if (interpolation <= 0)
			paletteLerp.y = 1f

		lastPalette = toSwitchPalette
		toSwitchPalette = newPalette

		updatePalette()
	}

	private fun updatePalette() {
		currentPalette.lerp(lastPalette, toSwitchPalette, paletteLerp.y)
	}

	lateinit var preferences: Preferences
		private set
	lateinit var horizontalResize: Cursor
		private set
	lateinit var oldSize: Triple<Int, Int, Boolean>
		private set

	var helpTipsEnabled: Boolean = true
	var inspectionsEnabled: Boolean = true

	private val inputProj: Vector3 = Vector3()

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
		ionium.templates.Main.version = "v2.6.1-SNAPSHOT"

		SysOutPiper.pipe()

		GlobalVariables.versionUrl = null // Deprecated - use new versioning instead
		VersionChecker
		AssetRegistry.instance().assetManager.setLoader(LazySound::class.java, LazySoundLoader(
				InternalFileHandleResolver()))

		preferences = Gdx.app.getPreferences("RHRE2")
		helpTipsEnabled = preferences.getBoolean("helpTipsEnabled", helpTipsEnabled)
		inspectionsEnabled = preferences.getBoolean("inspectionsEnabled", inspectionsEnabled)

		oldSize = Triple(preferences.getInteger("width", 1280), preferences.getInteger("height", 720),
						 preferences.getBoolean("fullscreen", false))

		super.create()

		Gdx.graphics.setTitle(ionium.templates.Main.getTitle())

		AssetRegistry.instance().addAssetLoader(DefAssetLoader())
		AssetRegistry.instance().addAssetLoader(VisualAssetLoader())

		DebugSetting.showFPS = false

		horizontalResize = Gdx.graphics
				.newCursor(Pixmap(Gdx.files.internal("images/cursor/horizontalResize.png")), 16, 8)

		val tmpMusic = Gdx.files.local("tmpMusic/").file()
		if (tmpMusic.exists() && tmpMusic.isDirectory) {
			tmpMusic.deleteRecursively()
		}
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
		if (paletteLerp.x > 0 && paletteLerp.y < 1) {
			paletteLerp.y += Gdx.graphics.deltaTime / paletteLerp.x
			if (paletteLerp.y > 1) {
				paletteLerp.y = 1f
			}
			updatePalette()
		} else {
			if (paletteLerp.y != 1f) {
				paletteLerp.y = 1f
				updatePalette()
			}
		}

		super.preRender()
	}

	override fun render() {
		super.render()
	}

	override fun postRender() {
		super.postRender()

		batch.projectionMatrix = camera.combined
		batch.begin()
		fontBordered.setColor(1f, 1f, 1f, 1f)
		fontBordered.data.setScale(0.5f)
		val str = if (VersionChecker.versionState == VersionState.AVAILABLE)
			Localization.get("versionAvailable", ionium.templates.Main.githubVersion, ionium.templates.Main.version)
		else
			ionium.templates.Main.version
		fontBordered.draw(batch, str, (camera.viewportWidth - 4), fontBordered.capHeight + 2, 0f,
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
			if (getPalette() is DarkPalette) {
				switchPalette(LightPalette(), 0.75f)
			} else {
				switchPalette(DarkPalette(), 0.75f)
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
//		ttfParam.characters += "éàèùâêîôûç"
		ttfParam.characters += "éàèùâêîôûçëïüáéíóú¿¡ñ"

		font = ttfGenerator.generateFont(ttfParam)
		font.data.markupEnabled = true
//		font.setUseIntegerPositions(false)
		font.setFixedWidthGlyphs("0123456789")

		ttfParam.size *= 4
		biggerFont = ttfGenerator.generateFont(ttfParam)
		biggerFont.data.markupEnabled = true
//		biggerFont.setUseIntegerPositions(false)
		biggerFont.setFixedWidthGlyphs("0123456789")

		ttfParam.borderWidth = 1.5f
		ttfParam.size /= 4

		fontBordered = ttfGenerator.generateFont(ttfParam)
		fontBordered.data.markupEnabled = true
//		fontBordered.setUseIntegerPositions(false)
		fontBordered.setFixedWidthGlyphs("0123456789")

		ttfParam.size *= 4
		ttfParam.borderWidth *= 4f
		biggerFontBordered = ttfGenerator.generateFont(ttfParam)
		biggerFontBordered.data.markupEnabled = true
//		biggerFontBordered.setUseIntegerPositions(false)
		biggerFontBordered.setFixedWidthGlyphs("0123456789")

		ttfGenerator.dispose()
	}

	override fun resize(width: Int, height: Int) {
		super.resize(1280, 720)
	}

	fun persistWindowSettings() {
		preferences.putInteger("width", Gdx.graphics.width)
		preferences.putInteger("height", Gdx.graphics.height)
		preferences.putBoolean("fullscreen", Gdx.graphics.isFullscreen)
		preferences.flush()
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
