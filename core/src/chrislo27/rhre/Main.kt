package chrislo27.rhre

import chrislo27.rhre.init.DefAssetLoader
import chrislo27.rhre.logging.SysOutPiper
import chrislo27.rhre.palette.AbstractPalette
import chrislo27.rhre.palette.DarkPalette
import chrislo27.rhre.palette.LightPalette
import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.util.HideVersionText
import chrislo27.rhre.version.VersionChecker
import chrislo27.rhre.version.VersionState
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.Screen
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Cursor
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Array
import ionium.registry.AssetRegistry
import ionium.registry.GlobalVariables
import ionium.registry.ScreenRegistry
import ionium.registry.lazysound.LazySound
import ionium.registry.lazysound.LazySoundLoader
import ionium.templates.Main
import ionium.util.DebugSetting
import ionium.util.Logger
import ionium.util.SpecialCharactersList
import ionium.util.Utils
import ionium.util.i18n.Localization
import ionium.util.i18n.NamedLocale
import java.util.*

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

	private val inputProj: Vector3 = Vector3()

	private lateinit var ttfGenerator: FreeTypeFontGenerator
	private var fontCharsToLoad: String = FreeTypeFontGenerator.DEFAULT_CHARS +
			"éàèùâêîôûçëïüáéíóú¿¡ñ" +
			SpecialCharactersList.getJapaneseKana() +
			"レ力、己寸心" + // These characters are for Power Calligraphy
			"◉☂"

	var versionStringLength: Float = 0f
		private set

	override fun getScreenToSwitchToAfterLoadingAssets(): Screen {
		return if ((VersionChecker.versionState == VersionState.AVAILABLE && VersionChecker.shouldShowOnInit) || DebugSetting.debug)
			ScreenRegistry.get("version")
		else
			ScreenRegistry.get("editor")
	}

	override fun getAssetLoadingScreenToUse(): Screen {
		return ScreenRegistry.get("assetloading")
	}

	companion object {

		val languagesList: List<NamedLocale> = mutableListOf(
				NamedLocale("English", Locale("")),
				NamedLocale("Français (French)", Locale("fr")),
				NamedLocale("Español (Spanish)", Locale("es"))
															)
		val languagesMap: Map<String, NamedLocale> = languagesList.associate { it.locale.toString() to it }

		init {
			Localization.DEFAULT_LOCALE = languagesMap[""]
		}

		fun drawCompressed(font: BitmapFont, batch: SpriteBatch, text: String, x: Float, y: Float, width: Float,
						   align: Int) {
			val textWidth = Utils.getWidth(font, text)
			val oldScaleX = font.data.scaleX

			if (textWidth > width) {
				font.data.scaleX = (width / textWidth) * oldScaleX
			}

			font.draw(batch, text, x, y, width, align, false)

			font.data.scaleX = oldScaleX
		}
	}

	override fun create() {
		ionium.templates.Main.version = "v2.10.5"

		SysOutPiper.pipe()

		GlobalVariables.versionUrl = null // Deprecated - use new versioning instead
		VersionChecker
		AssetRegistry.instance().assetManager.setLoader(LazySound::class.java, LazySoundLoader(
				InternalFileHandleResolver()))

		preferences = Gdx.app.getPreferences("RHRE2")

		oldSize = Triple(preferences.getInteger("width", 1280).takeUnless { it <= 0 } ?: 1280,
						 preferences.getInteger("height", 720).takeUnless { it <= 0 } ?: 720,
						 preferences.getBoolean("fullscreen", false))

		fun addBundle(namedLocale: NamedLocale, onlyLoadGlyphs: Boolean = false) {
			if (!onlyLoadGlyphs) Localization.instance().addBundle(namedLocale)

			val base = Localization.instance().baseFileHandle
			val locale = namedLocale.locale
			val language = locale.language
			val country = locale.country
			val variant = locale.variant
			val emptyLanguage = "" == language
			val emptyCountry = "" == country
			val emptyVariant = "" == variant

			val sb: StringBuilder = StringBuilder(base.name())

			if (!(emptyLanguage && emptyCountry && emptyVariant)) {
				sb.append('_')
				if (!emptyVariant) {
					sb.append(language).append('_').append(country).append('_').append(variant)
				} else if (!emptyCountry) {
					sb.append(language).append('_').append(country)
				} else {
					sb.append(language)
				}
			}

			val handle: FileHandle = base.sibling(sb.append(".properties").toString())

			if (handle.exists()) {
				val content: String = handle.readString("UTF-8")
				content.forEach {
					if (!fontCharsToLoad.contains(it, ignoreCase = false) && (it != ' ' && it != '\n')) {
						fontCharsToLoad += it
					}
				}
			} else {
				ionium.templates.Main.logger.warn("Lang file not found: " + handle.toString())
			}
		}

		languagesList.forEachIndexed { index, l ->
			addBundle(l, onlyLoadGlyphs = index == 0)
		}

		Localization.instance().loadFromSettings(preferences)

		super.create()

		Gdx.graphics.setTitle(ionium.templates.Main.getTitle())

		AssetRegistry.instance().addAssetLoader(DefAssetLoader())

		DebugSetting.showFPS = false

		horizontalResize = Gdx.graphics
				.newCursor(Pixmap(Gdx.files.internal("images/cursor/horizontalResize.png")), 16, 8)

		val scripts = listOf("stats", "debug", "dump")
		val scriptsDir = Gdx.files.local("scripts/examples/")
		scriptsDir.mkdirs()
		scripts.forEach {
			val master = Gdx.files.internal("scripts/$it.lua")
			val child = scriptsDir.child("$it.lua")
			if (!child.exists() || child.readString("UTF-8") != master.readString("UTF-8"))
				master.copyTo(child)
		}

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

//		GameRegistry.coroutinesToUse = 0
		GameRegistry.load()
		AssetRegistry.instance().addAssetLoader(GameRegistry.newAssetLoader())

		val reg = ScreenRegistry.instance()

		reg.add("assetloading", LoadingScreen(this))
		reg.add("editor", EditorScreen(this))
		reg.add("tapalong", TapalongScreen(this))
		reg.add("info", InfoScreen(this))
		reg.add("music", MusicScreen(this))
		reg.add("load", LoadScreen(this))
		reg.add("save", SaveScreen(this))
		reg.add("new", NewScreen(this))
		reg.add("version", VersionScreen(this))
		reg.add("script", ScriptLoadingScreen(this))

		reg.all.forEach { it.resize(1280, 720) }
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

		Colors.put("RAINBOW", Main.getRainbow(System.currentTimeMillis(), 2f, 0.8f))

		super.preRender()
	}

	override fun render() {
		super.render()
	}

	override fun postRender() {
		super.postRender()

		if (screen !is HideVersionText) {
			batch.projectionMatrix = camera.combined
			batch.begin()
			fontBordered.setColor(1f, 1f, 1f, 1f)
			fontBordered.data.setScale(0.5f)
			val str = (if (VersionChecker.versionState == VersionState.AVAILABLE && VersionChecker.shouldShowOnInit)
				Localization.get("versionAvailable", ionium.templates.Main.githubVersion, ionium.templates.Main.version)
			else
				ionium.templates.Main.version) +
					if (LazySound.forceLoadNow)
						" (force load)"
					else
						""
			versionStringLength = Utils.getWidth(fontBordered, str)
			fontBordered.draw(batch, str, (camera.viewportWidth - 4), fontBordered.capHeight + 2, 0f,
							  Align.right, false)
			fontBordered.data.setScale(1f)

			batch.end()
		}
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

		ttfGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/rodin.otf"))

		var ttfParam: FreeTypeFontGenerator.FreeTypeFontParameter

		fun getParam(): FreeTypeFontGenerator.FreeTypeFontParameter {
			val param = FreeTypeFontGenerator.FreeTypeFontParameter()
			param.magFilter = Texture.TextureFilter.Nearest
			param.minFilter = Texture.TextureFilter.Linear
			param.genMipMaps = true
			param.incremental = true
			param.size = 24
			param.characters = fontCharsToLoad
			return param
		}

		val downScale: Float = 0.6f

		ttfParam = getParam()
		font = ttfGenerator.generateFont(ttfParam)
		font.data.markupEnabled = true
//		font.setUseIntegerPositions(false)
		font.setFixedWidthGlyphs("0123456789")
		font.data.setLineHeight(font.data.lineHeight * downScale)

		ttfParam = getParam()
		ttfParam.size *= 4
		biggerFont = ttfGenerator.generateFont(ttfParam)
		biggerFont.data.markupEnabled = true
//		biggerFont.setUseIntegerPositions(false)
		biggerFont.setFixedWidthGlyphs("0123456789")
		biggerFont.data.setLineHeight(biggerFont.data.lineHeight * downScale)

		ttfParam = getParam()
		ttfParam.borderWidth = 1.5f
		fontBordered = ttfGenerator.generateFont(ttfParam)
		fontBordered.data.markupEnabled = true
//		fontBordered.setUseIntegerPositions(false)
		fontBordered.setFixedWidthGlyphs("0123456789")
		fontBordered.data.setLineHeight(fontBordered.data.lineHeight * downScale)

		ttfParam = getParam()
		ttfParam.borderWidth = 1.5f * 4f
		ttfParam.size *= 4
		biggerFontBordered = ttfGenerator.generateFont(ttfParam)
		biggerFontBordered.data.markupEnabled = true
//		biggerFontBordered.setUseIntegerPositions(false)
		biggerFontBordered.setFixedWidthGlyphs("0123456789")
		biggerFontBordered.data.setLineHeight(biggerFontBordered.data.lineHeight * downScale)
	}

	override fun resize(width: Int, height: Int) {
		super.resize(1280, 720)

		persistWindowSettings()
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
		ttfGenerator.dispose()

		preferences.flush()
		GameRegistry.dispose()
	}
}
