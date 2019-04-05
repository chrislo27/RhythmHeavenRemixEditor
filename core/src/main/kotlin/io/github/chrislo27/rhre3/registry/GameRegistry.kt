package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.git.SfxDbInfoObject
import io.github.chrislo27.rhre3.modding.ModdingMetadata
import io.github.chrislo27.rhre3.playalong.PlayalongChars
import io.github.chrislo27.rhre3.playalong.PlayalongInput
import io.github.chrislo27.rhre3.playalong.PlayalongMethod
import io.github.chrislo27.rhre3.registry.datamodel.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.*
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.*
import java.io.File
import java.util.*


object GameRegistry : Disposable {

    const val DATA_JSON_FILENAME: String = "data.json"
    const val ICON_FILENAME: String = "icon.png"
    const val SPECIAL_ENTITIES_GAME_ID: String = "special"
    const val END_REMIX_ENTITY_ID: String = "special_endEntity"
    const val PLAYALONG_GAME_ID: String = "specialPlayalong"
    const val SKILL_STAR_ID: String = "extraSFX/skillStar"
    const val CUSTOM_PREFIX: String = "custom_"
    private const val MISSING_GAME_ICON_PATH = "images/gameicon/missing.png"
    val ID_REGEX: Regex = "(?:[A-Za-z0-9_/\\-])+".toRegex()

    val SFX_FOLDER: FileHandle by lazy {
        GitHelper.SOUNDS_DIR.child("games/")
    }
    val MODDING_METADATA_FOLDER: FileHandle by lazy {
        GitHelper.SOUNDS_DIR.child("moddingMetadata/")
    }
    val CUSTOM_SFX_FOLDER: FileHandle by lazy {
        RHRE3.RHRE3_FOLDER.child("customSounds/")
    }
    val CUSTOM_MODDING_METADATA_FOLDER: FileHandle by lazy {
        RHRE3.RHRE3_FOLDER.child("customModdingMetadata/")
    }

    private val backingData: RegistryData = RegistryData()

    val data: RegistryData
        get() {
            if (!backingData.ready)
                throw IllegalStateException("Cannot get data when loading")

            return backingData
        }
    val moddingMetadata: ModdingMetadata get() = data.moddingMetadata

    fun isDataLoading(): Boolean =
            !backingData.ready

    fun initialize(): RegistryData {
        if (!isDataLoading())
            throw IllegalStateException("Cannot initialize registry when already loaded")
        return backingData
    }

    class RegistryData : Disposable {

        @Volatile
        var ready: Boolean = false
            private set

        var hasCustom: Boolean = false
            get() {
                if (!ready)
                    error("Attempt to get hasCustom field when not ready")

                return field
            }
            private set
        val gameMap: Map<String, Game> = mutableMapOf()
        val gameList: List<Game> by lazy {
            if (!ready)
                error("Attempt to map game list when not ready")

            gameMap.values.toList().sortedByName()
        }
        val objectMap: Map<String, Datamodel> = mutableMapOf()
        val objectList: List<Datamodel> by lazy {
            if (!ready)
                error("Attempt to map datamodels when not ready")

            objectMap.values.toList()
        }
        val noDeprecationsObjectMap: Map<String, Datamodel> = mutableMapOf()
        val gameGroupsMap: Map<String, GameGroup> = mutableMapOf()
        val gameGroupsList: List<GameGroup> by lazy {
            if (!ready)
                error("Attempt to map game groups when not ready")

            gameGroupsMap.values.toList().sortedBy(GameGroup::name)
        }
        val seriesCount: Map<Series, Int> = mutableMapOf()

        lateinit var moddingMetadata: ModdingMetadata
            private set

        private val dbInfoObj: SfxDbInfoObject
        val sfxCredits: List<String>
        val version: Int
            get() = dbInfoObj.version
        val editorVersion: Version

        lateinit var specialGame: Game
            private set
        lateinit var playalongGame: Game
            private set

        private val objectMapper: ObjectMapper = JsonHandler.createObjectMapper(false)

        class SfxDirectory(val folder: FileHandle, val isCustom: Boolean, val datajson: FileHandle) {
            val textureFh: FileHandle = folder.child(ICON_FILENAME)
        }

        private val folders: List<SfxDirectory> by lazy {
            val list = SFX_FOLDER.list { fh ->
                val datajson = fh.resolve(DATA_JSON_FILENAME)
                fh.isDirectory && datajson.exists() && datajson.isFile
            }.toList()

            if (list.isEmpty()) {
                error("No valid sfx folders with $DATA_JSON_FILENAME inside found")
            }

            CUSTOM_SFX_FOLDER.mkdirs()
            CUSTOM_SFX_FOLDER.child("README_SFX.txt").writeString(CustomSoundNotice.getActualCustomSoundNotice(), false,
                                                                  "UTF-8")
            val custom = CUSTOM_SFX_FOLDER.list { fh ->
                fh.isDirectory
            }.mapNotNull {
                if (it.child("data.json").exists()) {
                    return@mapNotNull it
                }
                val sfx = it.list { file: File ->
                    file.extension in RHRE3.SUPPORTED_DECODING_SOUND_TYPES
                }

                if (sfx.isEmpty()) {
                    return@mapNotNull null
                } else {
                    return@mapNotNull it
                }
            }.toList()

            list.map { SfxDirectory(it, false, it.child(DATA_JSON_FILENAME)) } +
                    custom.map { SfxDirectory(it, true, it.child(DATA_JSON_FILENAME)) }
        }
        private val currentObjFh: FileHandle by lazy {
            GitHelper.SOUNDS_DIR.child("current.json")
        }
        private val sfxCreditsFh: FileHandle by lazy {
            GitHelper.SOUNDS_DIR.child("credits.json")
        }

        private var index: Int = 0
        var lastLoadedID: String? = null

        init {
            dbInfoObj = JsonHandler.fromJson(currentObjFh.readString("UTF-8"))
            sfxCredits = sfxCreditsFh.takeIf(FileHandle::exists)?.readString("UTF-8")?.let {
                try {
                    JsonHandler.fromJson(it, Array<String>::class.java).toList()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            } ?: listOf()

            editorVersion = Version.fromString(dbInfoObj.requiresVersion)

            if (editorVersion > RHRE3.VERSION)
                error("Registry version ($editorVersion) is higher than this RHRE version (${RHRE3.VERSION})")
        }

        private fun whenDone() {
            specialGame = gameMap[SPECIAL_ENTITIES_GAME_ID] ?: error("Missing special game")
            addSpecialGeneratedGames()

            ready = true

            // create
            gameList
            objectList

            gameList.groupBy(Game::group).map {
                it.key to GameGroup(it.key, it.value.sortedWith(
                        GameGroupListComparator))
            }.associateTo(gameGroupsMap as MutableMap) { it }
            gameGroupsList

            seriesCount as MutableMap
            Series.VALUES.associateTo(seriesCount) { series ->
                series to gameList.count { it.series == series }
            }

            hasCustom = gameList.any(Game::isCustom)

            val cues = objectList.filterIsInstance<Cue>()
            val errors = mutableListOf<String>()
            cues.forEach {
                if (it.introSoundCue != null) {
                    if (it.introSound == it.id) {
                        errors += "Recursive intro sound ID for ${it.id}"
                    }
                    it.introSoundCue!!.hidden = true
                }
                if (it.endingSoundCue != null) {
                    if (it.endingSound == it.id) {
                        errors += "Recursive ending sound ID for ${it.id}"
                    }
                    it.endingSoundCue!!.hidden = true
                }

                if (it.soundHandle.extension() != "ogg") {
                    Toolboks.LOGGER.warn("Non-ogg file extension: ${it.soundHandle} for ${it.id}")
                }

                if (!it.soundHandle.exists()) {
                    errors += "Handle does not exist for ${it.id}: ${it.soundHandle}"
                } else if (it.game.isCustom && !it.game.jsonless) {
                    val parent = it.soundHandle.file().resolve("../")
                    val ext = it.soundHandle.extension()
                    val list = parent.list()?.map { n -> n.substringAfterLast('/').substringBeforeLast(".$ext") }
                    if (list != null) {
                        if (parent.isDirectory && it.id.substringAfterLast('/') !in list) {
                            errors += "Handle has wrong casing for ${it.id}: ${it.soundHandle}"
                        }
                    } else {
                        Toolboks.LOGGER.warn("Failed to list files in parent ${parent.absolutePath} during case-sensitivity check")
                    }
                }
            }
            gameList.forEach {
                if (!it.isCustom && it.id.startsWith(CUSTOM_PREFIX)) {
                    errors += "Game ${it.id} starts with custom prefix $CUSTOM_PREFIX"
                }
            }

            errors.forEach(Toolboks.LOGGER::error)
            if (errors.isNotEmpty()) {
                error("Check above for database errors")
            }

            Toolboks.LOGGER.info("Finished loading game registry: ${gameList.size} games, ${objectList.size} datamodels, ${objectList.count { it is Cue }} cues, ${objectList.count { it !is Cue }} patterns")

            // Load modding metadata
            loadModdingMetadata(false)

            if (!LazySound.loadLazilyWithAssetManager) {
                runBlocking {
                    objectList.filterIsInstance<Cue>().map {
                        launch {
                            try {
                                it.sound.load()
                            } catch (e: Exception) {
                                Toolboks.LOGGER.warn("Failed to load ${it.id} in game ${it.game.id}")
                                e.printStackTrace()
                            }
                            try {
                                it.sound.unload()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }.forEach {
                        it.join()
                    }
                }
            }

            // Load favourites, recents, etc
            GameMetadata.initialize()

            if (RHRE3.verifyRegistry) {
                Toolboks.LOGGER.info("Checking registry for errors")
                val nanoStart = System.nanoTime()
                GlobalScope.launch {
                    val coroutines = LinkedList<Deferred<VerificationResult>>()

                    gameList.forEach { game ->
                        coroutines += GlobalScope.async {
                            verify(game)
                        }
                    }

                    val results = coroutines.map {
                        it.await()
                    }.sortedBy { it.game.id }
                    val failures = results.count { !it.success && !it.game.jsonless }

                    results.filter { it.message.isNotBlank() }.forEach {
                        Toolboks.LOGGER.warn("Verification message for ${it.game.id}:\n${it.message}")
                    }
                    val endTime = System.nanoTime()

                    delay(250L)

                    Toolboks.LOGGER.info(
                            "Registry checked in ${(endTime - nanoStart) / 1_000_000.0} ms, $failures error(s)")

                    if (failures > 0) {
                        delay(500L)

                        class RegistryVerificationError : RuntimeException()

                        RegistryVerificationError().printStackTrace()
                        Gdx.app.exit()
                    }
                }
            }

            System.gc()
        }

        fun loadOne(): Float {
            if (ready)
                return 1f

            objectMap as MutableMap
            noDeprecationsObjectMap as MutableMap

            val directive = folders[index]
            val folder: FileHandle = directive.folder
            val datajsonFile: FileHandle = directive.datajson
            val game: Game

            if (datajsonFile.exists()) {
                val gameObject: GameObject = objectMapper.readValue(datajsonFile.readString("UTF-8"), GameObject::class.java)
                val gameID = gameObject.id
                if (!gameID.matches(ID_REGEX))
                    error("Game ID ($gameID) doesn't match allowed characters: must only contain alphanumerics, -, /, _, or spaces")
                if (folder.name() != gameID)
                    error("Game ID ($gameID) does not match folder name ${folder.name()}")

                game = Game(gameObject.id,
                            gameObject.name,
                            Series.valueOf(gameObject.series?.toUpperCase(Locale.ROOT) ?: Series.OTHER.name),
                            mutableListOf(),
                            if (directive.textureFh.exists()) Texture(directive.textureFh) else Texture(MISSING_GAME_ICON_PATH),
                            gameObject.group ?: gameObject.name,
                            gameObject.groupDefault,
                            gameObject.priority, directive.isCustom, gameObject.noDisplay, gameObject.searchHints ?: listOf(),
                            jsonless = false, isSpecial = gameObject.id == SPECIAL_ENTITIES_GAME_ID)
                val baseFileHandle = directive.folder.parent()

                fun String.starSubstitution(): String = replace("*", gameID)
                fun List<String>.starSubstitution(): List<String> = map(String::starSubstitution)

                gameObject.objects.mapTo(game.objects as MutableList) { obj ->
                    val objID = obj.id.starSubstitution()
                    if (!objID.matches(ID_REGEX))
                        error("Model ID ($objID) doesn't match allowed characters: must only contain alphanumerics, -, /, _, or spaces")

                    when (obj) {
                        // Note: if this is updated, remember to update GameToJson
                        is CueObject ->
                            Cue(game, objID, obj.deprecatedIDs, obj.name,
                                obj.duration,
                                obj.stretchable, obj.repitchable,
                                baseFileHandle.child("$objID.${obj.fileExtension}"),
                                obj.introSound?.starSubstitution(), obj.endingSound?.starSubstitution(),
                                obj.responseIDs.starSubstitution(),
                                obj.baseBpm, obj.loops, obj.earliness, obj.loopStart, obj.loopEnd)
                        is EquidistantObject ->
                            Equidistant(game, objID, obj.deprecatedIDs,
                                        obj.name, obj.distance,
                                        obj.stretchable,
                                        obj.cues.mapToDatamodel(gameID))
                        is KeepTheBeatObject ->
                            KeepTheBeat(game, objID, obj.deprecatedIDs,
                                        obj.name, obj.defaultDuration,
                                        obj.cues.mapToDatamodel(gameID))
                        is PatternObject ->
                            Pattern(game, objID, obj.deprecatedIDs,
                                    obj.name, obj.cues.mapToDatamodel(gameID), obj.stretchable)
                        is RandomCueObject ->
                            RandomCue(game, objID, obj.deprecatedIDs,
                                      obj.name, obj.cues.mapToDatamodel(gameID), obj.responseIDs.starSubstitution())
                        is EndRemixObject ->
                            EndRemix(game, objID, obj.deprecatedIDs, obj.name)
                        is SubtitleEntityObject ->
                            Subtitle(game, objID, obj.deprecatedIDs, obj.name, obj.subtitleType)
                        is ShakeEntityObject ->
                            ShakeScreen(game, objID, obj.deprecatedIDs, obj.name)
                        is TextureEntityObject ->
                            TextureModel(game, objID, obj.deprecatedIDs, obj.name)
                        is TapeMeasureObject ->
                            TapeMeasure(game, objID, obj.deprecatedIDs, obj.name)
                        is PlayalongEntityObject ->
                            PlayalongModel(game, objID, obj.deprecatedIDs, obj.name, obj.stretchable,
                                           PlayalongInput[obj.input ?: ""] ?: PlayalongInput.BUTTON_A, PlayalongMethod[obj.method ?: ""] ?: PlayalongMethod.PRESS)
                    }
                }
            } else {
                val nameWithoutExt = folder.nameWithoutExtension()
                val id = CUSTOM_PREFIX + nameWithoutExt
                if (gameMap.containsKey(id)) {
                    throw UnsupportedOperationException(
                            "Cannot load custom sound folder $id/ - already exists in registry")
                }
                game = Game(id,
                            nameWithoutExt,
                            Series.OTHER,
                            mutableListOf(),
                            if (directive.textureFh.exists()) Texture(directive.textureFh)
                            else Texture(MISSING_GAME_ICON_PATH),
                            nameWithoutExt,
                            true,
                            0, true, false, listOf(), jsonless = true, isSpecial = id == SPECIAL_ENTITIES_GAME_ID)

                val sfxList = directive.folder.list { fh ->
                    fh.isFile && fh.extension in RHRE3.SUPPORTED_DECODING_SOUND_TYPES
                }.toList()

                game.objects as MutableList
                sfxList.forEach { fh ->
                    val loops = fh.nameWithoutExtension().endsWith(".loop")
                    val name = if (!loops) fh.nameWithoutExtension() else (fh.nameWithoutExtension().substringBeforeLast(".loop") + " - loop")
                    game.objects += Cue(game, "${game.id}/$name", listOf(), name, CustomSoundNotice.DURATION,
                                        true, true, fh, null, null,
                                        listOf(), 0f, loops, 0f, 0f, 0f)
                }

                if (RHRE3.outputCustomSfx) {
                    Toolboks.LOGGER.info("JSON output for custom SFX in folder ${folder.name()} (${game.id}):\n${JsonHandler.toJson(game.toJsonObject(true))}\n")
                }
            }

            addGameAndObjects(game)

            lastLoadedID = game.id
            index++
            val progress = getProgress()

            if (progress >= 1f) {
                whenDone()
            }

            return progress
        }

        private fun addGameAndObjects(game: Game) {
            objectMap as MutableMap
            noDeprecationsObjectMap as MutableMap
            val existingGame: Game? = gameMap[game.id]
            val isOverwriting: Boolean = game.isCustom && gameMap[game.id]?.isCustom == false
            if (existingGame != null) {
                if (isOverwriting) {
                    Toolboks.LOGGER.info("Overwrote existing non-custom game with custom game ${game.id}")
                    if (game.isSpecial && !RHRE3.EXPERIMENTAL)
                        error("You cannot overwrite the ${game.id} game because it is special")
                    // Deprecation check
                    val missingDeps = existingGame.objects.filter { exObj -> !game.objectsMap.containsKey(exObj.id) }
                    if (missingDeps.isNotEmpty()) {
                        Toolboks.LOGGER.warn("These objects were REMOVED from ${game.id} when it was overwritten: ${missingDeps.map { it.id }}")
                    }
                } else if (existingGame.isCustom && !game.isCustom) {
                    Toolboks.LOGGER.info(
                            "Ignoring non-custom game ${game.id} because a custom game already exists with the same ID")
                } else {
                    error("Duplicate game: ${game.id}")
                }
            }

            (gameMap as MutableMap)[game.id] = game

            if (isOverwriting && existingGame != null) {
                existingGame.objects.forEach {
                    objectMap.remove(it.id, it)
                }
            }

            val duplicateObjs = mutableListOf<String>()
            game.objects.forEach {
                if (objectMap[it.id] != null) {
                    duplicateObjs += it.id
                }
                objectMap[it.id] = it
                noDeprecationsObjectMap[it.id] = it
                it.deprecatedIDs.forEach { dep ->
                    objectMap[dep] = it
                }
            }

            if (duplicateObjs.isNotEmpty()) {
                error("Duplicate objects in game ${game.id}: $duplicateObjs")
            }
        }

        private fun addSpecialGeneratedGames() {
            val playalongObjs = mutableListOf<Datamodel>()
            val playalongGame = Game(PLAYALONG_GAME_ID, "Playalong Input Entities", specialGame.series,
                                     playalongObjs, Texture("images/gameicon/playableEntities.png"), "Special Entities", false, specialGame.priority,
                                     false, specialGame.noDisplay, listOf("playable"), false, true)
            // Press
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_A", listOf(),
                                            "Press ${PlayalongChars.FILLED_A}", false,
                                            PlayalongInput.BUTTON_A, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_A}", "[LIGHT_GRAY](ex: Fruit Basket)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_B", listOf(),
                                            "Press ${PlayalongChars.FILLED_B}", false,
                                            PlayalongInput.BUTTON_B, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_B}", "[LIGHT_GRAY](ex: Ringside \"pose for the fans\")[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_A_or_Dpad", listOf(),
                                            "Press ${PlayalongChars.FILLED_A}/${PlayalongChars.FILLED_DPAD}", false,
                                            PlayalongInput.BUTTON_A_OR_DPAD, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_A}/${PlayalongChars.FILLED_DPAD}", "[LIGHT_GRAY](ex: Shoot-'em-Up, First Contact)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_Dpad", listOf(),
                                            "Press ${PlayalongChars.FILLED_DPAD}", false,
                                            PlayalongInput.BUTTON_DPAD, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_DPAD}", "[LIGHT_GRAY](ex: Blue Bear, Catchy Tune, Sick Beats)[]"))

            // Hold
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_hold_A", listOf(),
                                            "Hold ${PlayalongChars.FILLED_A}", true,
                                            PlayalongInput.BUTTON_A, PlayalongMethod.PRESS_AND_HOLD,
                                            pickerName = PickerName("Hold ${PlayalongChars.FILLED_A}", "[LIGHT_GRAY](ex: Fillbots, Screwbot Factory)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_hold_B", listOf(),
                                            "Hold ${PlayalongChars.FILLED_B}", true,
                                            PlayalongInput.BUTTON_B, PlayalongMethod.PRESS_AND_HOLD,
                                            pickerName = PickerName("Hold ${PlayalongChars.FILLED_B}", "[LIGHT_GRAY](ex: Flock Step, Super Samurai Slice)[]"))

            // Release then hold
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_releaseAndHold_A", listOf(),
                                            "Release and Hold ${PlayalongChars.FILLED_A}", true,
                                            PlayalongInput.BUTTON_A, PlayalongMethod.RELEASE_AND_HOLD,
                                            pickerName = PickerName("Release and Hold ${PlayalongChars.FILLED_A}", "[LIGHT_GRAY](ex: Glee Club)[]"))

            // Long press
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_longPress_A_or_Dpad", listOf(),
                                            "Long Press ${PlayalongChars.FILLED_A}/${PlayalongChars.FILLED_DPAD}", true,
                                            PlayalongInput.BUTTON_A_OR_DPAD, PlayalongMethod.LONG_PRESS,
                                            pickerName = PickerName("Long Press ${PlayalongChars.FILLED_A}/${PlayalongChars.FILLED_DPAD}", "[LIGHT_GRAY](ex: Rhythm Tweezers long pull)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_longPress_B", listOf(),
                                            "Long Press ${PlayalongChars.FILLED_B}", true,
                                            PlayalongInput.BUTTON_B, PlayalongMethod.LONG_PRESS,
                                            pickerName = PickerName("Long Press ${PlayalongChars.FILLED_B}", "[LIGHT_GRAY](ex: Samurai Slice (Fever) demon horde)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_longPress_A", listOf(),
                                            "Long Press ${PlayalongChars.FILLED_A}", true,
                                            PlayalongInput.BUTTON_A, PlayalongMethod.LONG_PRESS,
                                            pickerName = PickerName("Long Press ${PlayalongChars.FILLED_A}", "[LIGHT_GRAY](ex: Glee Club transitions)[]"))

            // D-pad
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_Dpad_right", listOf(),
                                            "Press ${PlayalongChars.FILLED_DPAD_R}", false,
                                            PlayalongInput.BUTTON_DPAD_RIGHT, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_DPAD_R}", "[LIGHT_GRAY](ex: Space Dance \"and pose\")[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_Dpad_down", listOf(),
                                            "Press ${PlayalongChars.FILLED_DPAD_D}", false,
                                            PlayalongInput.BUTTON_DPAD_DOWN, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_DPAD_D}", "[LIGHT_GRAY](ex: Space Dance \"let's sit down\")[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_Dpad_left", listOf(),
                                            "Press ${PlayalongChars.FILLED_DPAD_L}", false,
                                            PlayalongInput.BUTTON_DPAD_LEFT, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_DPAD_L}", "[LIGHT_GRAY](ex: Marching Orders, Sick Beats)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_press_Dpad_up", listOf(),
                                            "Press ${PlayalongChars.FILLED_DPAD_U}", false,
                                            PlayalongInput.BUTTON_DPAD_UP, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Press ${PlayalongChars.FILLED_DPAD_U}", "[LIGHT_GRAY](ex: Sick Beats)[]"))

            // RHDS
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_tap", listOf(),
                                            "Tap", false,
                                            PlayalongInput.TOUCH_TAP, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Tap", "[LIGHT_GRAY](ex: Karate Man)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_flick", listOf(),
                                            "Flick", false,
                                            PlayalongInput.TOUCH_FLICK, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Flick", "[LIGHT_GRAY](ex: Rhythm Rally)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_tap_and_hold", listOf(),
                                            "Tap and Hold", true,
                                            PlayalongInput.TOUCH_TAP, PlayalongMethod.PRESS_AND_HOLD,
                                            pickerName = PickerName("Tap and Hold", "[LIGHT_GRAY](ex: Fillbots)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_release_and_tap", listOf(),
                                            "Release and Tap", true,
                                            PlayalongInput.TOUCH_RELEASE, PlayalongMethod.RELEASE_AND_HOLD,
                                            pickerName = PickerName("Release and Tap", "[LIGHT_GRAY](ex: Glee Club)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_slide", listOf(),
                                            "Slide", false,
                                            PlayalongInput.TOUCH_SLIDE, PlayalongMethod.PRESS,
                                            pickerName = PickerName("Slide", "[LIGHT_GRAY](ex: Love Lizards, Love Lab)[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_touch_quick_tap", listOf(),
                                            "Quick Tap", false,
                                            PlayalongInput.TOUCH_QUICK_TAP, PlayalongMethod.RELEASE,
                                            pickerName = PickerName("Quick Tap", "[LIGHT_GRAY](ex: Moai Doo-Wop \"pah\")[]"))
            playalongObjs += PlayalongModel(playalongGame, "${playalongGame.id}_longTap", listOf(),
                                            "Long Tap and Hold", true,
                                            PlayalongInput.TOUCH_TAP, PlayalongMethod.LONG_PRESS,
                                            pickerName = PickerName("Long Tap and Hold", "[LIGHT_GRAY](ex: Glee Club transitions)[]"))

            addGameAndObjects(playalongGame)
            this.playalongGame = playalongGame
        }

        fun getProgress(): Float {
            return index.toFloat() / folders.size
        }

        fun loadFor(delta: Float): Float {
            if (ready)
                return 1f

            val msToLoad = (delta * 1000f)
            val startNano = System.nanoTime()

            while (getProgress() < 1) {
                loadOne()
                val time = (System.nanoTime() - startNano) / 1_000_000f

                if (time >= msToLoad) {
                    break
                }
            }

            return getProgress()
        }

        fun loadModdingMetadata(ignoreIfFailure: Boolean): Boolean {
            CUSTOM_MODDING_METADATA_FOLDER.mkdirs()
            val newData = ModdingMetadata(this, MODDING_METADATA_FOLDER, CUSTOM_MODDING_METADATA_FOLDER)
            val success = newData.loadAll()
            if (!ignoreIfFailure || success) {
                moddingMetadata = newData
            }
            return success
        }

        override fun dispose() {
            gameMap.values.forEach(Disposable::dispose)
        }

        private fun verify(game: Game): VerificationResult {
            val builder = StringBuilder()

            fun String.starSubstitute(): String = if (this.startsWith(game.id)) (this.replaceFirst(game.id, "*")) else this

            /*
            Game verification:
            * Non-custom games have an icon
            * Non-custom games that are series specific have the right series
             */
            if (game.icon === AssetRegistry.missingTexture) {
                builder.append("Game ${game.id} has a missing texture\n")
            }
            if (game.series != Series.SIDE) {
                if ((game.name.contains("(Fever)") && game.series != Series.FEVER) ||
                        (game.name.contains("(DS)") && game.series != Series.DS) ||
                        (game.name.contains("(Megamix)") && game.series != Series.MEGAMIX) ||
                        (game.name.contains("(GBA)") && game.series != Series.TENGOKU)) {
                    builder.append("Game ${game.id} has the name \"${game.name}\" but is in series ${game.series}\n")
                }

                if (game.name.contains("(Wii)")) {
                    builder.append("Game ${game.id} has (Wii) in its name, should be (Fever): ${game.name}\n")
                }
            }

            game.objects.forEach { model ->
                val separator = if (model is Cue) "/" else "_"
                if (!model.id.startsWith(game.id + separator)) {
                    builder.append("Model ID (${model.id}) should start with \"*$separator\"\n")
                }

                /*
                Model verification:
                * Duration > 0
                 */
                if (model is DurationModel) {
                    if (model.duration <= 0) {
                        builder.append("Model ${model.id} has a negative duration: ${model.duration}\n")
                    }
                }

                /*
                Cue pointer verification:
                * All pointers point to real objects
                * Track does not exceed TRACK_COUNT
                * Duration is not <= 0
                */
                if (model is ContainerModel) {
                    model.cues.forEach { pointer ->
                        if (objectMap[pointer.id] == null) {
                            builder.append("Model ${model.id} has an invalid cue pointer ID: ${pointer.id}\n")
                        } else if (objectMap[pointer.id] != null && noDeprecationsObjectMap[pointer.id] == null) {
                            builder.append("Model ${model.id} refers to a deprecated cue pointer ID: ${pointer.id}, replace with ${objectMap[pointer.id]?.id?.starSubstitute()}\n")
                        }
                        if (pointer.track >= Editor.MIN_TRACK_COUNT) {
                            builder.append(
                                    "Model ${model.id} has a pointer with a track that is too tall: ${pointer.id}, ${pointer.track} / min ${Editor.MIN_TRACK_COUNT}\n")
                        }
                        if (pointer.duration <= 0) {
                            builder.append(
                                    "Model ${model.id} has a pointer with a negative duration: ${pointer.id}, ${pointer.duration}\n")
                        }
                    }
                }

                if (model is Cue) {
                    if (model.introSound != null) {
                        if (objectMap[model.introSound] == null) {
                            builder.append("Cue ${model.id} has an invalid introSound ID: ${model.introSound}\n")
                        } else if (objectMap[model.introSound] != null && noDeprecationsObjectMap[model.introSound] == null) {
                            builder.append("Cue ${model.id} refers to a deprecated introSound ID: ${model.introSound}, replace with ${objectMap[model.introSound]?.id?.starSubstitute()}\n")
                        }
                    }
                    if (model.endingSound != null) {
                        if (objectMap[model.endingSound] == null) {
                            builder.append("Cue ${model.id} has an invalid endingSound ID: ${model.endingSound}\n")
                        } else if (objectMap[model.endingSound] != null && noDeprecationsObjectMap[model.endingSound] == null) {
                            builder.append("Cue ${model.id} refers to a deprecated endingSound ID: ${model.endingSound}, replace with ${objectMap[model.endingSound]?.id?.starSubstitute()}\n")
                        }
                    }
                    if (model.loops) {
                        if (model.loopStart > model.loopEnd && (model.loopStart >= 0 && model.loopEnd > 0)) {
                            builder.append("Cue ${model.id} has invalid loop endpoints: start=${model.loopStart}, end=${model.loopEnd}\n")
                        }
                    }
                }

                if (model is ResponseModel) {
                    model.responseIDs.forEach { id ->
                        if (objectMap[id] == null) {
                            builder.append("Model ${model.id} has a non-existent response ID: $id\n")
                        } else if (objectMap[id] != null && noDeprecationsObjectMap[id] == null) {
                            builder.append("Model ${model.id} refers to a deprecated response ID: $id, replace with ${objectMap[id]?.id?.starSubstitute()}\n")
                        }
                    }
                }
            }

            val msg = builder.toString()
            return VerificationResult(game, msg.isBlank(), msg)
        }

        private data class VerificationResult(val game: Game, val success: Boolean, val message: String)

    }

    override fun dispose() {
        if (backingData.ready) {
            backingData.dispose()
        }
    }
}
