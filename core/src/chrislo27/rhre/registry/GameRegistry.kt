package chrislo27.rhre.registry

import chrislo27.rhre.json.GameObject
import chrislo27.rhre.util.CustomSoundUtil
import chrislo27.rhre.util.JsonHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import ionium.animation.Animation
import ionium.registry.handler.IAssetLoader
import ionium.registry.lazysound.LazySound
import ionium.templates.Main
import ionium.util.AssetMap
import kotlinx.coroutines.experimental.*
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object GameRegistry : Disposable {

//	/**
//	 * 0 for normal loading
//	 */
//	@Volatile var coroutinesToUse: Int = 0

	val games: Map<String, Game> = ConcurrentHashMap()
	val gameList: List<Game> = CopyOnWriteArrayList()
	val gamesBySeries: Map<Series, List<Game>> = ConcurrentHashMap()
//	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

	@Volatile private var alreadyLoaded = false

	/**
	 * Throw exception on any warnings
	 */
	var pedantic: Boolean = true
	@Volatile internal var loadingState = false

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
		loadingState = true

		this.games as MutableMap
		this.gameList as MutableList
		this.gamesBySeries as MutableMap

		val debugFolder = Gdx.files.local("debug/")
		debugFolder.mkdirs()

//		val coroutinesToUse = coroutinesToUse

		val startTime: Long = System.nanoTime()

		val gamesList: FileHandle = Gdx.files.internal("data/games.json")
		val games: List<String> = JsonHandler.fromJson<Array<String>>(gamesList.readString("UTF-8")).toList()
		val numberPerSeries: MutableMap<Series, Int> = ConcurrentHashMap()

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

		fun loadGameDefinition(gameDef: String, baseDir: String, isCustom: Boolean): Game {
			val nano = System.nanoTime()
			Main.logger.info("Loading $gameDef")
			val gameFh: FileHandle = Gdx.files.internal("$baseDir$gameDef/data.json")
			val jsonString: String = gameFh.readString("UTF-8")
			val timeToRead: Long = System.nanoTime() - nano
			val nano2 = System.nanoTime()
			val gameObj: GameObject = JsonHandler.fromJson<GameObject>(jsonString)

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
				if (!so.id!!.startsWith(gameObj.gameID!!)) {
					throw RuntimeException(
							"Sound cue ID " + so.id + " doesn't start with game definition ID (${gameObj.gameID})")
				}
				soundCues.add(SoundCue(so.id!!, gameObj.gameID!!, so.fileExtension,
									   so.name ?: so.id!!,
									   so.deprecatedIDs?.toMutableList() ?: mutableListOf(), so.duration,
									   so.canAlterPitch, so.pan, so.canAlterDuration, so.introSound, so.baseBpm,
									   so.loops ?: so.canAlterDuration,
									   if (isCustom) baseDir else null))
			}

			gameObj.patterns!!.forEach { po ->
				if (po.id == null) {
					throw RuntimeException("Pattern object inside ${gameObj.gameID!!} has no ID")
				}
				if (!po.id!!.startsWith(gameObj.gameID!!)) {
					throw RuntimeException(
							"Pattern definition ID " + po.id + " doesn't start with game definition ID (${gameObj.gameID})")
				}
				val p: Pattern
				val patternCues = po.cues!!.map { pc ->
					Pattern.PatternCue(pc.id!!, gameObj.gameID!!, pc.beat, pc.track, pc.duration!!, pc.semitone!!)
				}

				p = Pattern(po.id!!, gameObj.gameID!!, po.name!!, po.isStretchable, patternCues, false,
							po.deprecatedIDs?.toMutableList() ?: mutableListOf())

				patterns.add(p)
			}

			soundCues.filter { sc -> soundCues.none { sc.id == it.introSound } }.forEach { sc ->
				val pattern = Pattern(sc.id + "_AUTO-GENERATED", gameObj.gameID!!, "cue: " + sc.name,
									  sc.canAlterDuration,
									  mutableListOf(Pattern.PatternCue(sc.id, gameObj.gameID!!, 0f, 0, sc.duration, 0)),
									  true, mutableListOf())
				if (sc.id.endsWith("/silence")) {
					patterns.add(0, pattern)
				} else {
					patterns += pattern
				}
			}

			val iconFh = Gdx.files.internal("$baseDir$gameDef/icon.png")

			if (isCustom && gameObj.series == null) {
				gameObj.series = SeriesList.CUSTOM.name
			}

			game = Game(gameObj.gameID!!, gameObj.gameName!!, soundCues, patterns,
						if (gameObj.series == null) SeriesList.UNKNOWN else SeriesList.getOrPut(gameObj.series!!),
						if (iconFh.exists()) "$baseDir$gameDef/icon.png" else null, true, gameObj.notRealGame)

			if (!iconFh.exists())
				Main.logger.warn(game.id + " is missing icon.png")

			Main.logger.info(
					"Loaded ${if (isCustom) "CUSTOM " else ""}" + game.id + " with " + game.soundCues.size + " cues and " +
							game.patterns.filter { p -> !p.autoGenerated }.count() + " patterns," +
							" took ${(System.nanoTime() - nano) / 1000000.0} ms (str: ${timeToRead / 1000000f}, parse: ${(System.nanoTime() - nano2) / 1000000f})")
			return game
		}

		fun loadCustomSoundFolder(fh: FileHandle): Game? {
			val nano = System.nanoTime()
			Main.logger.info("Loading custom sound folder " + fh.name())

			val list = fh.list { f ->
				allowedSoundTypes.contains(f.extension.toLowerCase(Locale.ROOT))
			}

			if (this.games.contains(fh.nameWithoutExtension()))
				throw IllegalArgumentException("Duplicate game ${fh.nameWithoutExtension()}")

			if (list.isEmpty()) {
				Main.logger.info(
						"No valid sounds found (" + allowedSoundTypes.toString() + "), skipping this folder")
				return null
			}

			val game: Game
			val patterns: MutableList<Pattern> = mutableListOf()
			val soundCues: MutableList<SoundCue> = mutableListOf()
			val icon = fh.child("icon.png")

			list.forEach { soundFh ->
				val sc = SoundCue(fh.nameWithoutExtension() + "/" + soundFh.nameWithoutExtension(), fh.name(),
								  soundFh.extension(), "custom:\n" + soundFh.nameWithoutExtension(),
								  ArrayList<String>(),
								  CustomSoundUtil.DURATION, true, 0f, true, null, 0f, false, customFolder.path() + "/")
				soundCues.add(sc)
			}

			soundCues.forEach { sc ->
				val l = mutableListOf<Pattern.PatternCue>()

				l.add(Pattern.PatternCue(sc.id, fh.name(), 0f, 0, sc.duration, 0))

				patterns += Pattern(sc.id + "_AUTO-GENERATED", fh.name(), "cue: " +
						sc.name.replace("custom:\n", ""),
									sc.canAlterDuration, l, true, mutableListOf())
			}

			game = Game(fh.nameWithoutExtension(), fh.nameWithoutExtension(), soundCues, patterns,
						SeriesList.CUSTOM, if (icon.exists()) icon.path() else null, true)

			Main.logger.info("Finished loading custom folder " + fh.name() + " with " + soundCues.size + " " +
									 "cues, took ${(System.nanoTime() - nano) / 1000000.0} ms")

			return game
		}

		// games from games.json
		val coroutines = mutableListOf<Deferred<Game?>>()
		games.forEach {
			coroutines += async(CommonPool) {
				return@async loadGameDefinition(it, "sounds/cues/", false)
			}
		}

		// custom sounds
		customFolders.forEach {
			coroutines += async(CommonPool) {
				return@async if (it.child("data.json").exists()) {
					loadGameDefinition(it.nameWithoutExtension(), customFolder.path() + "/", true)
				} else {
					loadCustomSoundFolder(it)
				}
			}
		}

		runBlocking(CommonPool) {
			val nano = System.nanoTime()
			for (c in coroutines) {
				val game: Game = c.await() ?: continue

				GameRegistry.games as MutableMap
				GameRegistry.games[game.id] = game
				if (gamesBySeries[game.series] == null) {
					gamesBySeries[game.series] = mutableListOf()
				}
				(gamesBySeries[game.series] as MutableList).add(game)
				gameList.add(game)
				numberPerSeries[game.series] = numberPerSeries.getOrDefault(game.series, 0) + 1
			}
			Main.logger.info(
					"Joined all ${coroutines.size} coroutines in ${(System.nanoTime() - nano) / 1_000_000.0} ms")
		}

		synchronized(gameList) {
			gameList.sortBy(Game::id)
		}

		synchronized(gamesBySeries) {
			gamesBySeries.values.forEach { list ->
				list as MutableList
				list.sortBy(Game::id)
			}
		}

		loadingState = false
		val numAreCustom = this.gameList.filter { it.series == SeriesList.CUSTOM }.count()
		Main.logger.info(
				"Loaded " + this.gameList.size + " games (" +
						(this.gameList.size - numAreCustom) + " databased, $numAreCustom custom game(s)), done in " + ((System.nanoTime() - startTime) / 1_000_000.0) + " ms" +
						" using ${coroutines.size} coroutines")

		launch(CommonPool) warningCheck@ {
			val nano = System.nanoTime()
			var warningCount: Int = 0
//			numberPerSeries.filter { (_, v) -> v > Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y }.forEach { k, v ->
//				Main.logger.warn("")
//				Main.logger.warn(
//						"Series $k has $v games, but the maximum is ${Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y}")
//				Main.logger.warn("")
//				warningCount++
//			}
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
						if (!sc.id.startsWith(game.id + "/")) {
							Main.logger.warn("Sound cue ${sc.id} is in game ${game.id}")
							warningCount++
						}

						if (sc.introSound != null) {
							if (!gameList.any { g -> g.soundCues.any { it.id == sc.introSound } }) {
								Main.logger.warn("Intro sound cue ${sc.introSound} in sound cue ${sc.id} doesn't exist")
								warningCount++
							}
						}

						if (checked[sc.id] != null) {
							Main.logger.warn("Duplicate sound cue " + sc.id + " in game " + game.id)
							warningCount++
						} else {
							checked[sc.id] = true
						}
					}
					game.patterns.forEach { pat ->
						if (!pat.autoGenerated && !pat.id.startsWith(game.id + "_")) {
							Main.logger.warn("Pattern cue ${pat.id} is in game ${game.id}")
							warningCount++
						}

						pat.cues.forEach { pc ->
							if (!pc.id.startsWith(game.id + "/")) {
								Main.logger.warn("Pattern cue ${pc.id} in pattern ${pat.id} is in game ${game.id}")
								warningCount++
							}
						}

						if (checked[pat.id] != null) {
							Main.logger.warn("Duplicate pattern " + pat.id + " in game " + game.id)
							warningCount++
						} else {
							checked[pat.id] = true
						}
					}
				}
			}

			Main.logger.info("Completed warning checks in ${(System.nanoTime() - nano) / 1_000_000.0} ms")

			delay(50) // allow logger to catch up

			if (pedantic && warningCount > 0) {
				IllegalStateException("Warnings exist in the game registry, please see above").printStackTrace()
				Gdx.app.exit()
			}
		}
	}

}

class CueAssetLoader : IAssetLoader {
	override fun addManagedAssets(manager: AssetManager) {
		runBlocking {
			while (GameRegistry.loadingState) {
				delay(50L)
			}

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

			SeriesList.list.forEach {
				val path = "series/${it.name}.png"

				manager.load(AssetMap.add("series_icon_${it.name}",
										  if (Gdx.files.internal(path).exists()) path else "series/unknown.png"),
							 Texture::class.java)
			}
		}
	}

	override fun addUnmanagedTextures(textures: HashMap<String, Texture>?) {
	}

	override fun addUnmanagedAnimations(animations: HashMap<String, Animation>?) {
	}

}
