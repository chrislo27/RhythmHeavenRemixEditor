package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.VersionHistory
import io.github.chrislo27.rhre3.git.CurrentObject
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.io.File
import java.util.*


object GameRegistry : Disposable {

    const val DATA_JSON_FILENAME: String = "data.json"
    const val ICON_FILENAME: String = "icon.png"
    const val SPECIAL_GAME_ID: String = "special"
    const val CUSTOM_PREFIX: String = "custom_"

    val SFX_FOLDER: FileHandle by lazy {
        GitHelper.SOUNDS_DIR.child("games/")
    }
    val CUSTOM_FOLDER: FileHandle by lazy {
        Gdx.files.local("customSounds/")
    }

    private var backingData: RegistryData = RegistryData()

    val data: RegistryData
        get() {
            if (!backingData.ready)
                throw IllegalStateException("Cannot get data when loading")

            return backingData
        }

    fun isDataLoading(): Boolean =
            !backingData.ready

    fun initialize(): RegistryData {
        dispose()

        backingData = RegistryData()
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
//        val changelog: ChangelogObject
        private val currentObj: CurrentObject
        val version: Int
            get() = currentObj.version
        val editorVersion: Version

        private val objectMapper: ObjectMapper
        private val shouldCustomsGetPrefixes by lazy {
            RHRE3.VERSION >= VersionHistory.CUSTOM_SOUNDS_GET_PREFIXES
        }

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
            CUSTOM_FOLDER.child("README_SFX.txt").writeString(CustomSoundNotice.getActualCustomSoundNotice(), false, "UTF-8")
            val custom = CUSTOM_FOLDER.list { fh ->
                fh.isDirectory
            }.mapNotNull {
                if (it.child("data.json").exists()) {
                    return@mapNotNull it
                }
                val sfx = it.list { file: File ->
                    file.extension in RHRE3.SUPPORTED_SOUND_TYPES
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
            objectMapper = JsonHandler.createObjectMapper(true)
            currentObj = JsonHandler.fromJson(currentObjFh.readString())
//            changelog = JsonHandler.fromJson(GitHelper.SOUNDS_DIR.child("changelogs/$version.json").readString())

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

            gameMap["special"] ?: error("Missing special game")

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
                        }
                    }.forEach {
                        it.join()
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
                if (directive.isCustom && shouldCustomsGetPrefixes) {
                    dataObject.id = CUSTOM_PREFIX + dataObject.id
                }

                game = Game(dataObject.id,
                            dataObject.name,
                            if (directive.isCustom) Series.CUSTOM
                            else Series.valueOf(
                                    dataObject.series?.toUpperCase(
                                            Locale.ROOT) ?: Series.OTHER.name),
                            mutableListOf(),
                            if (directive.textureFh.exists()) Texture(directive.textureFh)
                            else Texture("images/missing_game_icon.png"),
//                            (if (directive.isCustom) "(Custom) " else "") + (dataObject.group ?: dataObject.name),
                            dataObject.group ?: dataObject.name,
                            dataObject.groupDefault,
                            dataObject.priority, directive.isCustom, dataObject.noDisplay)
                val baseFileHandle = directive.folder.parent()

                dataObject.objects.mapTo(game.objects as MutableList) { obj ->
                    when (obj) {
                        is CueObject ->
                            Cue(game, obj.id, obj.deprecatedIDs, obj.name,
                                obj.duration,
                                obj.stretchable, obj.repitchable,
                                baseFileHandle.child(
                                        "${obj.id}.${obj.fileExtension}"),
                                obj.introSound, obj.endingSound,
                                obj.responseIDs,
                                obj.baseBpm, obj.loops)
                        is EquidistantObject ->
                            Equidistant(game, obj.id, obj.deprecatedIDs,
                                        obj.name, obj.distance,
                                        obj.stretchable,
                                        obj.cues.mapToDatamodel())
                        is KeepTheBeatObject ->
                            KeepTheBeat(game, obj.id, obj.deprecatedIDs,
                                        obj.name, obj.defaultDuration,
                                        obj.cues.mapToDatamodel())
                        is PatternObject ->
                            Pattern(game, obj.id, obj.deprecatedIDs,
                                    obj.name, obj.cues.mapToDatamodel(), obj.stretchable)
                        is RandomCueObject ->
                            RandomCue(game, obj.id, obj.deprecatedIDs,
                                      obj.name, obj.cues.mapToDatamodel(), obj.responseIDs)
                        is EndRemixObject ->
                            EndRemix(game, obj.id, obj.deprecatedIDs, obj.name)
                        is SubtitleEntityObject ->
                            Subtitle(game, obj.id, obj.deprecatedIDs, obj.name)
                    }
                }

                DatamodelGenerator.generators[game.id]?.process(folder, dataObject, game)
            } else {
                val id = if (shouldCustomsGetPrefixes) CUSTOM_PREFIX + folder.nameWithoutExtension() else folder.nameWithoutExtension()
                if (gameMap.containsKey(id)) {
                    throw UnsupportedOperationException("Cannot load custom sound folder $id/ - already exists in registry")
                }
                game = Game(id,
                            id,
                            Series.CUSTOM,
                            mutableListOf(),
                            if (directive.textureFh.exists()) Texture(directive.textureFh)
                            else Texture("images/missing_game_icon.png"),
                            id,
                            true,
                            0, true, false)

                val sfxList = directive.folder.list { fh ->
                    fh.isFile && fh.extension in RHRE3.SUPPORTED_SOUND_TYPES
                }.toList()

                game.objects as MutableList
                sfxList.forEach { fh ->
                    val name = fh.nameWithoutExtension()
                    game.objects += Cue(game, "${game.id}/$name", listOf(), name, CustomSoundNotice.DURATION,
                                        true, true, fh, null, null,
                                        listOf(), 0f, false)
                }
            }

            (gameMap as MutableMap)[game.id] = game
            game.objects.forEach {
                objectMap[it.id] = it
                it.deprecatedIDs.forEach { dep ->
                    objectMap[dep] = it
                }
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

    }

    override fun dispose() {
        backingData.dispose()
    }
}
