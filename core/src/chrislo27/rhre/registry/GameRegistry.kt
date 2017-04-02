package chrislo27.rhre.registry

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.inspections.InspectionPostfix
import chrislo27.rhre.json.GameObject
import chrislo27.rhre.lazysound.LazySound
import chrislo27.rhre.util.CustomSoundUtil
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import ionium.animation.Animation
import ionium.registry.handler.IAssetLoader
import ionium.templates.Main
import ionium.util.AssetMap
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.io.File
import java.util.*

object GameRegistry : Disposable {

//	/**
//	 * 0 for normal loading
//	 */
//	@Volatile var coroutinesToUse: Int = 0

	val games: Map<String, Game> = linkedMapOf()
	val gameList: List<Game> = mutableListOf()
	val gamesBySeries: Map<Series, List<Game>> = mutableMapOf()
//	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

	@Volatile private var alreadyLoaded = false

	val luaValue: LuaValue by lazy {
		val l = LuaValue.tableOf()

		l.set("games", LuaValue.tableOf(
				games.flatMap { listOf(CoerceJavaToLua.coerce(it.key), it.value.luaValue) }.toTypedArray()))
		l.set("cues", LuaValue.tableOf(
				games.values.flatMap(Game::soundCues).flatMap {
					listOf(CoerceJavaToLua.coerce(it.id), it.luaValue)
				}.toTypedArray()))
		l.set("patterns", LuaValue.tableOf(
				games.values.flatMap(Game::patterns).flatMap {
					listOf(CoerceJavaToLua.coerce(it.id), it.luaValue)
				}.toTypedArray()))

		return@lazy l
	}

	operator fun get(id: String): Game? {
		return games[id]
	}

	fun getCue(id: String): SoundCue? {
		return gameList.map {
			it.soundCues.filter {
				it.id == id || it.deprecated.contains(id)
			}.firstOrNull()
		}.filterNotNull().firstOrNull()
	}

	fun getPattern(id: String): Pattern? {
		return gameList.map {
			it.patterns.filter {
				it.id == id || it.deprecated.contains(id)
			}.firstOrNull()
		}.filterNotNull().firstOrNull()
	}

	fun newAssetLoader(): CueAssetLoader {
		return CueAssetLoader()
	}

	override fun dispose() {
		games.values.forEach {
			it.soundCues.forEach {
				// dispose AL music
			}
		}
	}

	// are you ready
	@Synchronized fun load() {
		if (alreadyLoaded) {
			throw IllegalStateException("Already loaded!")
		}

		alreadyLoaded = true

		this.games as MutableMap
		this.gameList as MutableList
		this.gamesBySeries as MutableMap

//		val coroutinesToUse = coroutinesToUse

		val startTime: Long = System.nanoTime()

		val gamesList: FileHandle = Gdx.files.internal("data/games.json")
		val games: List<String> = JsonHandler.fromJson<Array<String>>(gamesList.readString("UTF-8")).toList()
		val numberPerSeries: MutableMap<Series, Int> = mutableMapOf()

		val customFolder = Gdx.files.local("customSounds/")
		if (!customFolder.exists())
			customFolder.mkdirs()
		val customFolders = customFolder.list(File::isDirectory)

		// readme notice
		run {
			val notice = customFolder.child("README_SFX.txt")
			notice.writeString(CustomSoundUtil.getActualCustomSoundNotice(), false, "UTF-8")
		}

		val allowedSoundTypes = listOf("ogg", "wav", "mp3")

		fun loadGameDefinition(gameDef: String, baseDir: String, isCustom: Boolean) {
			val nano = System.nanoTime()
			Main.logger.info("Loading $gameDef")
			val gameFh: FileHandle = Gdx.files.internal("$baseDir$gameDef/data.json")
			val gameObj: GameObject = JsonHandler.fromJson<GameObject>(gameFh.readString("UTF-8"))

			val game: Game
			val patterns: MutableList<Pattern> = mutableListOf()
			val soundCues: MutableList<SoundCue> = mutableListOf()

			if (gameObj.usesGeneratorHelper) {
				val gh = GeneratorHelpers.map[gameObj.gameID]

				if (gh == null) {
					Main.logger.warn("GeneratorHelper not found for " + gameObj.gameID)
				} else {
					Main.logger.info("GeneratorHelper found for " + gameObj.gameID + ", invoking...")
					gh.process(gameFh, gameObj, patterns, soundCues)
				}
			}

			gameObj.cues!!.forEach { so ->
				soundCues.add(SoundCue(so.id!!, gameObj.gameID!!, so.fileExtension,
									   if (so.name == null) so.id!! else so.name!!,
									   if (so.deprecatedIDs == null)
										   mutableListOf()
									   else
										   so.deprecatedIDs!!.toMutableList(), so.duration,
									   so.canAlterPitch, so.canAlterDuration, so.introSound, so.baseBpm,
									   if (so.loops == null) so.canAlterDuration else (so.loops ?: false), null))
			}

			gameObj.patterns!!.forEach { po ->
				val p: Pattern
				val patternCues = po.cues!!.map { pc ->
					Pattern.PatternCue(pc.id!!, gameObj.gameID!!, pc.beat, pc.track, pc.duration!!, pc.semitone!!)
				}

				p = Pattern(po.id!!, gameObj.gameID!!, po.name!!, po.isStretchable, patternCues, false,
							if (po.deprecatedIDs == null)
								mutableListOf()
							else
								po.deprecatedIDs!!.toMutableList())

				patterns.add(p)
			}

			soundCues.filter { sc -> soundCues.none { sc.id == it.introSound } }.forEach { sc ->
				patterns.add(
						Pattern(sc.id + "_AUTO-GENERATED", gameObj.gameID!!, "cue: " + sc.name, sc.canAlterDuration,
								mutableListOf(Pattern.PatternCue(sc.id, gameObj.gameID!!, 0f, 0, sc.duration, 0)),
								true, mutableListOf()))
			}

			val iconFh = Gdx.files.internal("$baseDir$gameDef/icon.png")

			if (isCustom) {
				gameObj.series = Series.CUSTOM.name
			}

			game = Game(gameObj.gameID!!, gameObj.gameName!!, soundCues, patterns,
						if (gameObj.series == null) Series.UNKNOWN else Series.valueOf(
								gameObj.series!!.toUpperCase(Locale.ROOT)),
						if (iconFh.exists()) "$baseDir$gameDef/icon.png" else null, true)

			if (!iconFh.exists())
				Main.logger.warn(game.id + " is missing icon.png")

			this.games[game.id] = game
			if (this.gamesBySeries[game.series] == null) {
				this.gamesBySeries[game.series] = mutableListOf()
			}
			(this.gamesBySeries[game.series] as MutableList).add(game)
			this.gameList.add(game)
			numberPerSeries[game.series] = numberPerSeries.getOrDefault(game.series, 0) + 1

			Main.logger.info("Loaded ${if (isCustom) "CUSTOM" else ""}" + game.id + " with " + game.soundCues.size + " cues and " +
									 game.patterns.filter { p -> !p.autoGenerated }.count() + " patterns," +
									 " took ${(System.nanoTime() - nano) / 1000000.0} ms")
		}

		fun loadCustomSoundFolder(fh: FileHandle) {
			Main.logger.info("Loading custom sound folder " + fh.name())

			val list = fh.list { f ->
				allowedSoundTypes.contains(f.extension.toLowerCase(Locale.ROOT))
			}

			if (this.games.contains(fh.nameWithoutExtension()))
				throw IllegalArgumentException("Duplicate game ${fh.nameWithoutExtension()}")

			if (list.isEmpty()) {
				Main.logger.info(
						"No valid sounds found (" + allowedSoundTypes.toString() + "), skipping this folder")
				return
			}

			val game: Game
			val patterns: MutableList<Pattern> = mutableListOf()
			val soundCues: MutableList<SoundCue> = mutableListOf()
			val icon = fh.child("icon.png")

			list.forEach { soundFh ->
				val sc = SoundCue(fh.nameWithoutExtension() + "/" + soundFh.nameWithoutExtension(), fh.name(),
								  soundFh.extension(), "custom:\n" + soundFh.nameWithoutExtension(),
								  ArrayList<String>(),
								  CustomSoundUtil.DURATION, true, true, null, 0f, false, customFolder.path() + "/")
				soundCues.add(sc)
			}

			soundCues.forEach { sc ->
				val l = mutableListOf<Pattern.PatternCue>()

				l.add(Pattern.PatternCue(sc.id, fh.name(), 0f, 0, sc.duration, 0))

				patterns.add(
						Pattern(sc.id + "_AUTO-GENERATED", fh.name(), "cue: " +
								sc.name.replace("custom:\n", ""),
								sc.canAlterDuration, l, true, mutableListOf()))
			}

			game = Game(fh.nameWithoutExtension(), fh.nameWithoutExtension(), soundCues, patterns,
						Series.CUSTOM, if (icon.exists()) icon.path() else null, true)

			Main.logger.info("Finished loading custom folder " + fh.name() + " with " + soundCues.size + " " +
									 "cues")

			this.games[game.id] = game
			if (this.gamesBySeries[game.series] == null) {
				this.gamesBySeries[game.series] = mutableListOf()
			}
			(this.gamesBySeries[game.series] as MutableList).add(game)
			this.gameList.add(game)
			numberPerSeries[game.series] = numberPerSeries.getOrDefault(game.series, 0) + 1
		}

		// games from games.json
		games.forEach { loadGameDefinition(it, "sounds/cues/", false) }

		// custom sounds
		customFolders.forEach {
			if (it.child("data.json").exists()) {
				loadGameDefinition(it.nameWithoutExtension(), customFolder.path() + "/", true)
			} else {
				loadCustomSoundFolder(it)
			}
		}

		InspectionPostfix.applyInspectionFunctions()

		// warnings
		var warningCount: Int = 0
		// more than what's visible
		numberPerSeries.filter { (_, v) -> v > Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y }.forEach { k, v ->
			Main.logger.warn("")
			Main.logger.warn("Series $k has $v games, but the maximum is ${Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y}")
			Main.logger.warn("")
			warningCount++
		}
		// missing sound cues in patterns
		gameList.forEach { game ->
			game.patterns.forEach { pattern ->
				pattern.cues.forEach { patternCue ->
					if (gameList.none { g -> g.soundCues.any { sc -> patternCue.id == sc.id } }) {
						Main.logger.warn(
								"Pattern " + pattern.id + " has a pattern cue " + patternCue.id + " with no matching sound cue")
						warningCount++
					}
				}
			}
		}
		// duplicate check
		run {
			val checked = mutableMapOf<String, Boolean>()
			gameList.forEach { game ->
				game.soundCues.forEach { sc ->
					if (checked[sc.id] != null) {
						Main.logger.warn("Duplicate sound cue " + sc.id)
						warningCount++
					} else {
						checked[sc.id] = true
					}
				}
				game.patterns.forEach { pat ->
					if (checked[pat.id] != null) {
						Main.logger.warn("Duplicate pattern " + pat.id)
						warningCount++
					} else {
						checked[pat.id] = true
					}
				}
			}
		}

		val numAreCustom = this.gameList.filter { it.series == Series.CUSTOM }.count()
		Main.logger.info(
				"Loaded " + this.gameList.size + " games (" +
						(this.gameList.size - numAreCustom) + " databased, $numAreCustom custom game(s)) with " +
						warningCount + " warning(s), done in " + ((System.nanoTime() - startTime) / 1_000_000.0))
	}

}

class CueAssetLoader : IAssetLoader {
	override fun addManagedAssets(manager: AssetManager) {
		GameRegistry.games.values.forEach { g ->
			g.soundCues.forEach { sc ->
				val path = (sc.soundFolder ?: "sounds/cues/") + sc.id + "." +
						sc.fileExtension
				manager.load(AssetMap.add("soundCue_" + sc.id, path), LazySound::class.java)
			}
			manager.load(AssetMap.add("gameIcon_" + g.id,
									  if (g.icon == null)
										  "images/missing_game_icon.png"
									  else
										  if (g.iconIsRawPath) g.icon else "sounds/cues/" + g.id + "/icon.png"),
						 Texture::class.java)
		}

	}

	override fun addUnmanagedTextures(textures: HashMap<String, Texture>?) {
	}

	override fun addUnmanagedAnimations(animations: HashMap<String, Animation>?) {
	}

}
