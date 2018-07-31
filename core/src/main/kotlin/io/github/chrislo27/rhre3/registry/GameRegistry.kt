package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.git.CurrentObject
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.registry.datamodel.ContainerModel
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.DurationModel
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.ShakeScreen
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.TextureModel
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.experimental.*
import java.io.File
import java.util.*


object GameRegistry : Disposable {

    const val DATA_JSON_FILENAME: String = "data.json"
    const val ICON_FILENAME: String = "icon.png"
    const val SPECIAL_GAME_ID: String = "special"
    const val CUSTOM_PREFIX: String = "custom_"
    val ID_REGEX: Regex = "(?:[A-Za-z0-9_/\\-])+".toRegex()

    val SFX_FOLDER: FileHandle by lazy {
        GitHelper.SOUNDS_DIR.child("games/")
    }
    val CUSTOM_FOLDER: FileHandle by lazy {
        RHRE3.RHRE3_FOLDER.child("customSounds/")
    }

    private val backingData: RegistryData = RegistryData()

    val data: RegistryData
        get() {
            if (!backingData.ready)
                throw IllegalStateException("Cannot get data when loading")

            return backingData
        }

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
        val gameGroupsMap: Map<String, GameGroup> = mutableMapOf()
        val gameGroupsList: List<GameGroup> by lazy {
            if (!ready)
                error("Attempt to map game groups when not ready")

            gameGroupsMap.values.toList().sortedBy(GameGroup::name)
        }
        val seriesCount: Map<Series, Int> = mutableMapOf()
        //        val changelog: ChangelogObject
        private val currentObj: CurrentObject
        val version: Int
            get() = currentObj.version
        val editorVersion: Version
        lateinit var specialGame: Game
            private set

        private val objectMapper: ObjectMapper = JsonHandler.createObjectMapper(true)

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

            CUSTOM_FOLDER.mkdirs()
            CUSTOM_FOLDER.child("README_SFX.txt").writeString(CustomSoundNotice.getActualCustomSoundNotice(), false,
                                                              "UTF-8")
            val custom = CUSTOM_FOLDER.list { fh ->
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

        private var index: Int = 0
        var lastLoadedID: String? = null

        init {
            currentObj = JsonHandler.fromJson(currentObjFh.readString())

            editorVersion = Version.fromString(currentObj.requiresVersion)
        }

        private fun whenDone() {
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

            specialGame = gameMap["special"] ?: error("Missing special game")

            if (!LazySound.loadLazilyWithAssetManager) {
                runBlocking {
                    objectList.filterIsInstance<Cue>().map {
                        launch(CommonPool) {
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

            GameMetadata.initialize()

            if (RHRE3.verifyRegistry) {
                Toolboks.LOGGER.info("Checking registry for errors")
                val nanoStart = System.nanoTime()
                launch(CommonPool) {
                    val coroutines = LinkedList<Deferred<VerificationResult>>()

                    gameList.forEach { game ->
                        coroutines += async(CommonPool) {
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

                    Thread.sleep(250L)

                    Toolboks.LOGGER.info(
                            "Registry checked in ${(System.nanoTime() - nanoStart) / 1_000_000.0} ms, $failures error(s)")

                    if (failures > 0) {
                        Thread.sleep(500L)

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

            val directive = folders[index]
            val folder: FileHandle = directive.folder
            val datajsonFile: FileHandle = directive.datajson
            val game: Game

            if (datajsonFile.exists()) {
                val dataObject: DataObject = objectMapper.readValue(datajsonFile.readString("UTF-8"), DataObject::class.java)
                val gameID = dataObject.id
                if (!gameID.matches(ID_REGEX))
                    error("Game ID ($gameID) doesn't match allowed characters: must only contain alphanumerics, -, /, _, or spaces")

                game = Game(dataObject.id,
                            dataObject.name,
                            Series.valueOf(dataObject.series?.toUpperCase(Locale.ROOT) ?: Series.OTHER.name),
                            mutableListOf(),
                            if (directive.textureFh.exists()) Texture(directive.textureFh)
                            else Texture("images/missing_game_icon.png"),
                        //                            (if (directive.isCustom) "(Custom) " else "") + (dataObject.group ?: dataObject.name),
                            dataObject.group ?: dataObject.name,
                            dataObject.groupDefault,
                            dataObject.priority, directive.isCustom, dataObject.noDisplay, false)
                val baseFileHandle = directive.folder.parent()

                fun String.starSubstitution(): String = replace("*", gameID)
                fun List<String>.starSubstitution(): List<String> = map(String::starSubstitution)

                dataObject.objects.mapTo(game.objects as MutableList) { obj ->
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
                                obj.baseBpm, obj.loops)
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
                    }
                }

                DatamodelGenerator.generators[game.id]?.let {
                    it.process(folder, dataObject, game)
                    if (RHRE3.outputGeneratedDatamodels) {
                        Toolboks.LOGGER.info("JSON output for ${game.id}:\n${JsonHandler.toJson(game.toJsonObject(true))}\n")
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
                            else Texture("images/missing_game_icon.png"),
                            nameWithoutExt,
                            true,
                            0, true, false, true)

                val sfxList = directive.folder.list { fh ->
                    fh.isFile && fh.extension in RHRE3.SUPPORTED_DECODING_SOUND_TYPES
                }.toList()

                game.objects as MutableList
                sfxList.forEach { fh ->
                    val loops = fh.nameWithoutExtension().endsWith(".loop")
                    val name = if (!loops) fh.nameWithoutExtension() else (fh.nameWithoutExtension().substringBeforeLast(".loop") + " - loop")
                    game.objects += Cue(game, "${game.id}/$name", listOf(), name, CustomSoundNotice.DURATION,
                                        true, true, fh, null, null,
                                        listOf(), 0f, loops)
                }

                if (RHRE3.outputCustomSfx) {
                    Toolboks.LOGGER.info("JSON output for custom SFX in folder ${folder.name()} (${game.id}):\n${JsonHandler.toJson(game.toJsonObject(true))}\n")
                }
            }

            val existingGame: Game? = gameMap[game.id]
            val isOverwriting: Boolean = game.isCustom && gameMap[game.id]?.isCustom == false
            if (existingGame != null) {
                if (isOverwriting) {
                    Toolboks.LOGGER.info("Overwrote existing non-custom game with custom game ${game.id}")
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
                it.deprecatedIDs.forEach { dep ->
                    objectMap[dep] = it
                }
            }

            if (!isOverwriting && duplicateObjs.isNotEmpty()) {
                error("Duplicate objects in game ${game.id}: $duplicateObjs")
            }

            lastLoadedID = game.id
            index++
            val progress = getProgress()

            if (progress >= 1f) {
                whenDone()
            }

            return progress
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

        fun loadBlocking() {
            while (!ready) {
                loadOne()
            }
        }

        override fun dispose() {
            gameMap.values.forEach(Disposable::dispose)
        }

        private fun verify(game: Game): VerificationResult {
            val builder = StringBuilder()

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
                    builder.append("Model ID (${model.id}) should start with \"${game.id}$separator\"\n")
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
                            builder.append("Model ${model.id} has an invalid pointer: ${pointer.id}\n")
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

                if (model is ResponseModel) {
                    model.responseIDs.forEach { id ->
                        if (objectMap[id] == null) {
                            builder.append("Model ${model.id} has a non-existent response ID: $id\n")
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
