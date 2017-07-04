package chrislo27.rhre.registry

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
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


object GameRegistry : Disposable, IAssetLoader {

	@Volatile var loadState: LoadState = LoadState.NOT_LOADING
	val games: Map<String, Game> = mutableMapOf()
	val gameList: List<Game> = mutableListOf()
	val gamesBySeries: Map<Series, List<Game>> = mutableMapOf()
	val allowedSoundTypes: List<String> = listOf("ogg", "wav", "mp3")

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
		val coroutines: MutableList<Deferred<GameParseResult>> = mutableListOf()
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
					loadCustomSoundFolder(it)
				}
			}
		}

		// aggregate
		val errors: MutableList<GameParseError> = mutableListOf()
		runBlocking {
			for (c in coroutines) {
				val result: GameParseResult = c.await()

				if (result.error != null) {
					errors += result.error
				} else if (result.game == null) {
					// should never happen
					throw IllegalStateException("Result of parsing has both the game and error being null")
				} else {
					gameList += result.game
				}
			}
		}

		// kill if errors found
		val errorCount = errors.size
		if (errorCount > 0) {
			val builder = StringBuilder().append('\n')

			Main.logger.error(builder.toString())
			val timeTaken: Double = timeTaken()
			Main.logger.error(
					"Failed to complete game registry loading due to $errorCount errors ($timeTaken ms elapsed)")
			loadState = LoadState.LOADED
			// TODO Gdx.app.exit()
			return timeTaken
		}

		// sort
		gameList.sort()
		synchronized(gamesBySeries) {
			gamesBySeries.clear()
			gameList.groupByTo((gamesBySeries as MutableMap<Series, MutableList<Game>>), Game::series)
			gamesBySeries.values.forEach { (it as MutableList).sort() }
		}

		// warning check
		launch(CommonPool) {
			if (checkWarnings()) {
				Main.logger.error(
						"Failed to complete game registry loading due to warnings - see above (this should only happen in dev)")
				// TODO Gdx.app.exit()
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

	private fun loadGameDefinition(gameID: String, baseDir: String, isCustom: Boolean): GameParseResult {
		// FIXME
		return GameParseResult(null, GameParseError(gameID, "todo"))
	}

	private fun loadCustomSoundFolder(fh: FileHandle): GameParseResult {
		// FIXME
		return GameParseResult(null, GameParseError("thing", "todo"))
	}

	private fun checkWarnings(): Boolean {
		val nano = System.nanoTime()
		var warningCount: Int = 0
		// missing sound cues in patterns
		this.gameList.forEach { game ->
			game.patterns.forEach { pattern ->
				pattern.cues.forEach { patternCue ->
					if (this.gameList.none { g -> g.soundCues.any { sc -> patternCue.id == sc.id } }) {
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
			val checkedDeprecations = mutableMapOf<String, String>()
			this.gameList.forEach { game ->
				game.soundCues.forEach { sc ->
					if (!sc.id.startsWith(game.id + "/")) {
						Main.logger.warn("Sound cue ${sc.id} is in game ${game.id}")
						warningCount++
					}

					if (sc.introSound != null) {
						if (!OldGameRegistry.gameList.any { g -> g.soundCues.any { it.id == sc.introSound } }) {
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

					sc.deprecated.forEach {
						if (checkedDeprecations[it] != null) {
							Main.logger.warn(
									"Duplicate deprecation $it in sound cue ${sc.id} (first existed in ${checkedDeprecations[it]})")
							warningCount++
						} else {
							checkedDeprecations[it] = sc.id
						}
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

					pat.deprecated.forEach {
						if (checkedDeprecations[it] != null) {
							Main.logger.warn(
									"Duplicate deprecation $it in pattern ${pat.id} (first existed in ${checkedDeprecations[it]})")
							warningCount++
						} else {
							checkedDeprecations[it] = pat.id
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
						Main.logger.warn("Sound ID ${sc.id}'s deprecations still exist - $any")
						warningCount++
					}
				}
				game.patterns.filter { it.deprecated.isNotEmpty() }.forEach { pat ->
					val any = pat.deprecated.filter { it in allIDs }
					if (any.any()) {
						Main.logger.warn("Pattern ID ${pat.id}'s deprecations still exist - $any")
						warningCount++
					}
				}
			}
		}

		Main.logger.info("Completed warning checks in ${(System.nanoTime() - nano) / 1_000_000.0} ms")

		return warningCount > 0
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

	private data class GameParseResult(val game: Game?, val error: GameParseError?) {
		init {
			if (game == null && error == null) {
				throw IllegalArgumentException("Both game and error are null")
			}
			if (game != null && error != null) {
				throw IllegalArgumentException("Both game and error are not null")
			}
		}
	}

	private data class GameParseError(val game: String, val message: String)

}