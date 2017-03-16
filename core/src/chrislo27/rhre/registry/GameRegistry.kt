package chrislo27.rhre.registry

import chrislo27.rhre.editor.Editor
import chrislo27.rhre.inspections.InspectionPostfix
import chrislo27.rhre.json.GameObject
import chrislo27.rhre.lazysound.LazySound
import chrislo27.rhre.util.CustomSoundUtil
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ionium.animation.Animation
import ionium.registry.handler.IAssetLoader
import ionium.templates.Main
import ionium.util.AssetMap
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import java.util.*

object GameRegistry : Disposable {

	val games: Map<String, Game> = linkedMapOf()
	val gameList: List<Game> = mutableListOf()
	val gamesBySeries: Map<Series, List<Game>> = mutableMapOf()
	private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

	val luaValue: LuaValue by lazy {
		val l = LuaValue.tableOf()

		l.set("games", LuaValue.tableOf(games.keys.map { CoerceJavaToLua.coerce(it) }.toTypedArray(),
										games.values.map(Game::luaValue).toTypedArray()))
		l.set("cues", LuaValue.tableOf(
				games.values.flatMap(Game::soundCues).map { CoerceJavaToLua.coerce(it.id) }.toTypedArray(),
				games.values.flatMap(Game::soundCues).map { it.luaValue }.toTypedArray()))
		l.set("patterns", LuaValue.tableOf(
				games.values.flatMap(Game::patterns).map { CoerceJavaToLua.coerce(it.id) }.toTypedArray(),
				games.values.flatMap(Game::patterns).map { it.luaValue }.toTypedArray()))

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
	init {
		this.games as MutableMap
		this.gameList as MutableList
		this.gamesBySeries as MutableMap

		val startTime: Long = System.nanoTime()

		val gamesList: FileHandle = Gdx.files.internal("data/games.json")
		val games: List<String> = gson.fromJson(gamesList.readString("UTF-8"), Array<String>::class.java).toList()
		val numberPerSeries: MutableMap<Series, Int> = mutableMapOf()

		// games from games.json
		games.forEach { gameDef ->
			Main.logger.info("Loading $gameDef")
			val gameFh: FileHandle = Gdx.files.internal("sounds/cues/$gameDef/data.json")
			val gameObj: GameObject = gson.fromJson(gameFh.readString("UTF-8"), GameObject::class.java)

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
				soundCues.add(SoundCue(so.id!!, so.fileExtension, if (so.name == null) so.id!! else so.name!!,
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
					Pattern.PatternCue(pc.id!!, pc.beat, pc.track, pc.duration!!, pc.semitone!!)
				}

				p = Pattern(po.id!!, po.name!!, po.isStretchable, patternCues, false, if (po.deprecatedIDs == null)
					mutableListOf()
				else
					po.deprecatedIDs!!.toMutableList())

				patterns.add(p)
			}

			soundCues.filter { sc -> soundCues.none { sc.id == it.introSound } }.forEach { sc ->
				patterns.add(Pattern(sc.id + "_AUTO-GENERATED", "cue: " + sc.name, sc.canAlterDuration,
									 mutableListOf(Pattern.PatternCue(sc.id, 0f, 0, sc.duration, 0)),
									 true, mutableListOf()))
			}

			val iconFh = Gdx.files.internal("sounds/cues/$gameDef/icon.png")

			game = Game(gameObj.gameID!!, gameObj.gameName!!, soundCues, patterns,
						if (gameObj.series == null) Series.UNKNOWN else Series.valueOf(
								gameObj.series!!.toUpperCase(Locale.ROOT)),
						if (iconFh.exists()) "sounds/cues/$gameDef/icon.png" else null, false)

			if (!iconFh.exists())
				Main.logger.warn(game.id + " is missing icon.png")

			Main.logger.info("Loaded " + game.id + " with " + game.soundCues.size + " cues and " +
									 game.patterns.filter { (_, _, _, _, autoGenerated) -> !autoGenerated }.count() + " patterns")

			this.games[game.id] = game
			if (this.gamesBySeries[game.series] == null) {
				this.gamesBySeries[game.series] = mutableListOf()
			}
			(this.gamesBySeries[game.series] as MutableList).add(game)
			this.gameList.add(game)
			numberPerSeries[game.series] = numberPerSeries.getOrDefault(game.series, 0) + 1
		}

		// custom sounds
		run {
			this.games as MutableMap
			this.gameList as MutableList
			this.gamesBySeries as MutableMap

			val customFolder = Gdx.files.local("customSounds/")
			if (!customFolder.exists())
				customFolder.mkdirs()

			// readme notice
			run {
				val notice = customFolder.child("README_SFX.txt")
				notice.writeString(CustomSoundUtil.getActualCustomSoundNotice(), false, "UTF-8")
			}

			val allowedSoundTypes = listOf("ogg", "wav", "mp3")
			customFolder.list { f -> f.isDirectory }.forEach { fh ->
				Main.logger.info("Loading custom sound folder " + fh.name())

				val list = fh.list { f ->
					allowedSoundTypes.contains(f.extension.toLowerCase(Locale.ROOT))
				}

				if (list.isEmpty()) {
					Main.logger.info(
							"No valid sounds found (" + allowedSoundTypes.toString() + "), skipping this folder")
					return@forEach
				}

				val game: Game
				val patterns: MutableList<Pattern> = mutableListOf()
				val soundCues: MutableList<SoundCue> = mutableListOf()
				val icon = fh.child("icon.png")

				list.forEach { soundFh ->
					val sc = SoundCue(fh.nameWithoutExtension() + "/" + soundFh.nameWithoutExtension(),
									  soundFh.extension(), "custom:\n" + soundFh.nameWithoutExtension(),
									  ArrayList<String>(),
									  CustomSoundUtil.DURATION, true, true, null, 0f, false, customFolder.path() + "/")
					soundCues.add(sc)
				}

				soundCues.forEach { (id, _, name, _, duration, _, canAlterDuration) ->
					val l = mutableListOf<Pattern.PatternCue>()

					l.add(Pattern.PatternCue(id, 0f, 0, duration, 0))

					patterns.add(
							Pattern(id + "_AUTO-GENERATED", "cue: " +
									name.replace("custom:\n", ""),
									canAlterDuration, l, true, mutableListOf()))
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
						warningCount + " warning(s), done in " + ((System.nanoTime() - startTime) / 1_000_000.0) + " ms")
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
