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
import java.util.concurrent.TimeUnit


object GameRegistry : Disposable, IAssetLoader {

	@Volatile var loadState: LoadState = LoadState.NOT_LOADING
	val games: Map<String, Game> = mutableMapOf()
	val gameList: List<Game> = mutableListOf()
	val gamesBySeries: Map<Series, List<Game>> = mutableMapOf()
	val cueMap: Map<String, SoundCue> = mutableMapOf()
	val patternMap: Map<String, Pattern> = mutableMapOf()
	private val cueMapDeprecations: Map<String, SoundCue> = mutableMapOf()
	private val patternMapDeprecations: Map<String, Pattern> = mutableMapOf()
	val allowedSoundTypes: List<String> = listOf("ogg", "wav", "mp3")

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
		return cueMapDeprecations[id]
	}

	fun getPattern(id: String): Pattern? {
		return patternMapDeprecations[id]
	}

	/**
	 * @return Execution time in milliseconds
	 */
	@SuppressWarnings("unchecked")
	fun load(): Double {
		if (loadState != LoadState.NOT_LOADING)
			return 0.0

		// start time for timing execution
		val startNano: Long = System.nanoTime()
		fun timeTaken(): Double = (System.nanoTime() - startNano) / 1_000_000.0

		// set load state
		loadState = LoadState.LOADING

		// add smart casts
		this.games as MutableMap
		this.gameList as MutableList
		this.gamesBySeries as MutableMap
		this.cueMap as MutableMap
		this.patternMap as MutableMap
		this.cueMapDeprecations as MutableMap
		this.patternMapDeprecations as MutableMap

		// make debug folder idk why this is here actually
		val debugFolder = Gdx.files.local("debug/")
		debugFolder.mkdirs()

		val gamesListHandle: FileHandle = Gdx.files.internal("data/games.json")
		val gameIDs: List<String> = JsonHandler.fromJson<Array<String>>(gamesListHandle.readString("UTF-8")).toList()

		val customFolderHandle: FileHandle = Gdx.files.local("customSounds/")
		if (!customFolderHandle.exists())
			customFolderHandle.mkdirs()
		val customFoldersHandles: List<FileHandle> = customFolderHandle.list(File::isDirectory).toList()

		// readme notice
		launch(CommonPool) {
			val notice = customFolderHandle.child("README_SFX.txt")
			notice.writeString(CustomSoundUtil.getActualCustomSoundNotice(), false, "UTF-8")
		}

		// load
		val coroutines: MutableList<Deferred<GameParseResult?>> = mutableListOf()
		gameIDs.forEach { id ->
			coroutines += async(CommonPool) {
				return@async loadGameDefinition(id, "sounds/cues/", false)
			}
		}
		customFoldersHandles.forEach {
			val id = it.nameWithoutExtension()
			if (id in gameIDs) {
				throw IllegalArgumentException(
						"Custom sound $id shares an ID with an existing game, please rename your custom sound folder")
			}
			coroutines += async(CommonPool) {
				return@async if (it.child("data.json").exists()) {
					loadGameDefinition(id, customFolderHandle.path() + "/", true)
				} else {
					loadCustomSoundFolder(it, customFolderHandle)
				}
			}
		}

		// aggregate
		val errors: MutableList<GameParseError> = mutableListOf()
		runBlocking {
			for (c in coroutines) {
				val result: GameParseResult = c.await() ?: continue

				if (result.errors.isNotEmpty()) {
					errors.addAll(result.errors)
				} else if (result.game == null) {
					// should never happen
					throw IllegalStateException("Result of parsing has both the game and error being null")
				} else {
					gameList += result.game
//					Main.logger.debug("${result.game.id} finished in ${result.timeMs} ms")

					result.game.soundCues.forEach { sc ->
						if (cueMap.containsKey(sc.id)) {
							errors += GameParseError(result.game.id, "Duplicate sound cue ID ${sc.id} (already assigned in ${cueMap[sc.id]?.gameID})")
						} else {
							cueMap[sc.id] = sc
						}
						if (cueMapDeprecations.containsKey(sc.id) && cueMapDeprecations[sc.id]!!.id != sc.id) {
							errors += GameParseError(result.game.id, "Duplicate sound cue ID ${sc.id} (already assigned in ${cueMap[sc.id]?.gameID})")
						} else {
							cueMapDeprecations[sc.id] = sc
						}
						sc.deprecated.forEach {
							if (cueMapDeprecations.containsKey(it)) {
								errors += GameParseError(result.game.id,
														 "Duplicate deprecation of sound cue ID $it (already assigned to ${cueMapDeprecations[it]?.id})")
							} else {
								cueMapDeprecations[it] = sc
							}
						}
					}

					result.game.patterns.forEach { pattern ->
						if (patternMap.containsKey(pattern.id)) {
							errors += GameParseError(result.game.id, "Duplicate pattern ID ${pattern.id} (already assigned in ${patternMap[pattern.id]?.gameID})")
						} else {
							patternMap[pattern.id] = pattern
						}
						if (patternMapDeprecations.containsKey(pattern.id) && patternMapDeprecations[pattern.id]!!.id != pattern.id) {
							errors += GameParseError(result.game.id, "Duplicate pattern ID ${pattern.id} (already assigned in ${patternMap[pattern.id]?.gameID})")
						} else {
							patternMapDeprecations[pattern.id] = pattern
						}
						pattern.deprecated.forEach {
							if (patternMapDeprecations.containsKey(it)) {
								errors += GameParseError(result.game.id,
														 "Duplicate deprecation of pattern ID $it (already assigned to ${patternMapDeprecations[it]?.id})")
							} else {
								patternMapDeprecations[it] = pattern
							}
						}
					}
				}
			}
		}

		// kill if errors found
		val errorCount = errors.size
		if (errorCount > 0) {
			val builder = StringBuilder()
			builder.append("""
+=================+
| REGISTRY ERRORS |
+=================+
$errorCount errors found in ${errors.distinctBy(GameParseError::game).size} games

""")

			errors.distinctBy(GameParseError::game).forEach { error ->
				builder.append(error.game).append("\n")
				errors.filter { it.game == error.game }
						.forEach {
							builder.append(" > ${it.message}\n")
						}
				builder.append("\n")
			}

			Main.logger.error(builder.toString())
			val timeTaken: Double = timeTaken()
			Main.logger.error(
					"Failed to complete game registry loading due to $errorCount errors ($timeTaken ms elapsed)")
			loadState = LoadState.LOADED
			Gdx.app.exit()
			return timeTaken
		}

		// sort
		gameList.sort()
		synchronized(games) {
			gameList.associateByTo(games, Game::id)
		}
		synchronized(gamesBySeries) {
			gamesBySeries.clear()
			gameList.groupByTo((gamesBySeries as MutableMap<Series, MutableList<Game>>), Game::series)
			gamesBySeries.values.forEach { (it as MutableList).sort() }
		}

		// warning check
		launch(CommonPool) {
			val warnings = checkWarnings()
			if (warnings.first.isNotEmpty()) {
				Main.logger.error(
						"Failed to complete game registry loading due to warnings (${warnings.second} ms elapsed)")

				val builder = StringBuilder()
				builder.append("""
+===================+
| REGISTRY WARNINGS |
+===================+
${warnings.first.size} warnings

""")

				warnings.first.forEach {
					builder.append("> $it\n")
				}

				Main.logger.error(builder.toString())

				Gdx.app.exit()
			} else {
				Main.logger.info("Completed post-load warning checks in ${warnings.second} ms")
			}
		}

		// finishing
		val timeTaken: Double = timeTaken()
		val customGames: Int = gameList.size - gameIDs.size

		Main.logger.info(
				"Finished game registry loading (${gameList.size} games, ${gameIDs.size} databased, $customGames custom) in $timeTaken ms")

		loadState = LoadState.LOADED
		return timeTaken
	}

	private fun String.getBaseIDFromCue(): String? {
		if (!this.contains('/'))
			return null
		return this.substringBefore('/')
	}

	private fun String.getBaseIDFromPattern(): String? {
		if (!this.contains('_') || this.contains('/'))
			return null
		return this.substringBefore('_')
	}

	private fun loadGameDefinition(gameID: String, baseDir: String, isCustom: Boolean): GameParseResult {
		val nano = System.nanoTime()
		val timeElapsed: Double by lazy { (System.nanoTime() - nano) / 1_000_000.0 }
		val errorList = mutableListOf<GameParseError>()
		fun err(msg: String) {
			errorList += GameParseError(gameID, msg)
		}

		fun createErrorResult() = GameParseResult(null, errorList, timeElapsed)
		val fh = Gdx.files.local("$baseDir$gameID/data.json")
		if (!fh.exists()) {
			err("The data.json file doesn't exist")
			return createErrorResult()
		}
		val gameObj: GameObject = JsonHandler.fromJson(fh.readString("UTF-8"))
		val game: Game
		val patterns: MutableList<Pattern> = mutableListOf()
		val soundCues: MutableList<SoundCue> = mutableListOf()

		if (gameObj.gameID != gameID) {
			err("The ID specified in the data (${gameObj.gameID}) doesn't match what should've been parsed ($gameID)")
			return createErrorResult()
		}

		if (gameObj.gameName == null) {
			err("Missing gameName field")
			return createErrorResult()
		}

		if (gameObj.usesGeneratorHelper) {
			val gh = GeneratorHelpers.map[gameID]

			if (gh == null) {
				err("GeneratorHelper not found")
				return createErrorResult()
			} else {
				gh.process(fh, gameObj, patterns, soundCues)
			}
		}

		gameObj.cues?.forEach { so ->
			if (so.id == null) {
				err("A cue object has no ID")
				return createErrorResult()
			}
			if (so.id!!.getBaseIDFromCue() != gameID) {
				err("Sound cue ID " + so.id + " doesn't start with game ID")
				return@forEach
			}
			soundCues.add(SoundCue(so.id!!, gameID, so.fileExtension,
								   so.name ?: so.id!!,
								   so.deprecatedIDs?.toMutableList() ?: mutableListOf(), so.duration,
								   so.canAlterPitch, so.pan, so.canAlterDuration, so.introSound, so.baseBpm,
								   so.loops ?: so.canAlterDuration,
								   if (isCustom) baseDir else null))
		} ?: err("Cues not found")

		gameObj.patterns?.forEach { po ->
			if (po.id == null) {
				err("A pattern object has no ID")
				return createErrorResult()
			}
			if (po.id!!.getBaseIDFromPattern() != gameID) {
				err("Pattern definition ID " + po.id + " doesn't start with game ID")
			}
			val p: Pattern
			val patternCues = po.cues!!.map { pc ->
				if (pc.id == null) {
					err("Pattern ${po.id} has a pattern cue without an ID")
					return@map null
				}
				if (pc.id!!.getBaseIDFromCue() != gameID) {
					err("Pattern cue ${pc.id} doesn't start with game ID")
					return@map null
				}
				return@map Pattern.PatternCue(pc.id!!, gameID, pc.beat, pc.track, pc.duration!!, pc.semitone!!)
			}.filterNotNull()

			p = Pattern(po.id!!, gameID, po.name!!, po.isStretchable, patternCues, false,
						po.deprecatedIDs?.toMutableList() ?: mutableListOf())

			patterns.add(p)
		} ?: err("Patterns not found")

		soundCues.filter { sc -> soundCues.none { sc.id == it.introSound } }.forEach { sc ->
			val pattern = Pattern(sc.id + "_AUTO-GENERATED", gameID, "cue: " + sc.name,
								  sc.canAlterDuration,
								  mutableListOf(Pattern.PatternCue(sc.id, gameID, 0f, 0, sc.duration, 0)),
								  true, mutableListOf())
			if (sc.id.endsWith("/silence")) {
				patterns.add(0, pattern)
			} else {
				patterns += pattern
			}
		}

		val iconFh = Gdx.files.internal("$baseDir$gameID/icon.png")

		if (isCustom && gameObj.series == null) {
			gameObj.series = SeriesList.CUSTOM.name
		}

		game = Game(gameID, gameObj.gameName!!, soundCues, patterns,
					if (gameObj.series == null) SeriesList.UNKNOWN else SeriesList.getOrPut(gameObj.series!!),
					if (iconFh.exists()) "$baseDir$gameID/icon.png" else null, true, gameObj.notRealGame,
					gameObj.priority)

		if (!iconFh.exists())
			Main.logger.warn(game.id + " is missing icon.png")

		return if (errorList.isEmpty()) GameParseResult(game, mutableListOf(), timeElapsed) else GameParseResult(null,
																												 errorList,
																												 timeElapsed)
	}

	private fun loadCustomSoundFolder(fh: FileHandle, customFolder: FileHandle): GameParseResult? {
		val nano = System.nanoTime()
		val timeElapsed: Double by lazy { (System.nanoTime() - nano) / 1_000_000.0 }
		val errorList = mutableListOf<GameParseError>()
		val id = fh.nameWithoutExtension()
		fun err(msg: String) {
			errorList += GameParseError(id, msg)
		}

		fun createErrorResult() = GameParseResult(null, errorList, timeElapsed)
		val list = fh.list { f ->
			allowedSoundTypes.contains(f.extension.toLowerCase(Locale.ROOT))
		}

		if (list.isEmpty()) {
			return null
		}

		val game: Game
		val patterns: MutableList<Pattern> = mutableListOf()
		val soundCues: MutableList<SoundCue> = mutableListOf()
		val icon = fh.child("icon.png")

		list.forEach { soundFh ->
			val sc = SoundCue(fh.nameWithoutExtension() + "/" + soundFh.nameWithoutExtension(), fh.name(),
							  soundFh.extension(), "custom:\n" + soundFh.nameWithoutExtension(),
							  listOf(),
							  CustomSoundUtil.DURATION, true, 0f, true, null,
							  0f, false, customFolder.path() + "/")
			soundCues.add(sc)
		}

		soundCues.forEach { sc ->
			val l = mutableListOf<Pattern.PatternCue>()

			l.add(Pattern.PatternCue(sc.id, fh.name(), 0f, 0, sc.duration, 0))

			patterns += Pattern(sc.id + "_AUTO-GENERATED", fh.name(), "cue: " +
					sc.name.replace("custom:\n", ""),
								sc.canAlterDuration, l, true, mutableListOf())
		}

		game = Game(id, id, soundCues, patterns,
					SeriesList.CUSTOM, if (icon.exists()) icon.path() else null, true)

		return if (errorList.isEmpty()) GameParseResult(game, mutableListOf(), timeElapsed) else GameParseResult(null,
																												 errorList,
																												 timeElapsed)
	}

	private fun checkWarnings(): Pair<List<String>, Double> {
		val nano = System.nanoTime()
		val warnings: MutableList<String> = mutableListOf()
		// missing sound cues in patterns
		this.gameList.forEach { game ->
			game.patterns.forEach { pattern ->
				pattern.cues.forEach { patternCue ->
					if (getCue(patternCue.id) == null) {
						warnings +=
								"Pattern " + pattern.id + " has a pattern cue " + patternCue.id + " with no matching sound cue"
					}
				}
			}
		}
		// intro sound cue check
		run {
			this.gameList.forEach { game ->
				game.soundCues.forEach { sc ->
					if (sc.introSound != null) {
						if (getCue(sc.introSound) == null) {
							warnings += "Intro sound cue ${sc.introSound} in sound cue ${sc.id} doesn't exist"
						}
					}
				}
			}
		}

		// deprecations for IDs that still exist
		run {
			val allIDs: List<String> = this.gameList.flatMap { game ->
				listOf(game.soundCues.map(SoundCue::id),
					   game.patterns.map(Pattern::id)).flatten()
			}
			this.gameList.forEach { game ->
				game.soundCues.filter { it.deprecated.isNotEmpty() }.forEach { sc ->
					val any = sc.deprecated.filter { it in allIDs }
					if (any.any()) {
						warnings += "Sound ID ${sc.id}'s deprecations still exist - $any"
					}
				}
				game.patterns.filter { it.deprecated.isNotEmpty() }.forEach { pat ->
					val any = pat.deprecated.filter { it in allIDs }
					if (any.any()) {
						warnings += "Pattern ID ${pat.id}'s deprecations still exist - $any"
					}
				}
			}
		}

		return warnings to ((System.nanoTime() - nano) / 1_000_000.0)
	}

	@Synchronized override fun dispose() {
	}

	override @Synchronized fun addManagedAssets(manager: AssetManager) {
		if (loadState == LoadState.NOT_LOADING) {
			load()
		} else if (loadState == LoadState.LOADING) {
			runBlocking {
				while (loadState != LoadState.LOADED) {
					delay(100L, TimeUnit.MILLISECONDS)
				}
			}
		}

		games.values.forEach { g ->
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

	override fun addUnmanagedTextures(textures: HashMap<String, Texture>) {
	}

	override fun addUnmanagedAnimations(animations: HashMap<String, Animation>) {
	}

	enum class LoadState {
		NOT_LOADING, LOADING, LOADED
	}

	private data class GameParseResult(val game: Game?, val errors: MutableList<GameParseError>, val timeMs: Double) {
		init {
			if (game == null && errors.isEmpty()) {
				throw IllegalArgumentException("Both game and errors are null/empty")
			}
			if (game != null && errors.isNotEmpty()) {
				throw IllegalArgumentException("Both game and errors are not null/empty")
			}
		}
	}

	private data class GameParseError(val game: String, val message: String)

}