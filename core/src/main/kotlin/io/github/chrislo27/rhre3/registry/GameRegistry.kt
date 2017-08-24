package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.git.ChangelogObject
import io.github.chrislo27.rhre3.git.CurrentObject
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.EndRemix
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.version.Version
import java.util.*


object GameRegistry : Disposable {

    const val DATA_JSON_FILENAME: String = "data.json"
    const val ICON_FILENAME: String = "icon.png"
    const val SPECIAL_GAME_ID: String = "special"

    val SFX_FOLDER: FileHandle by lazy {
        GitHelper.SOUNDS_DIR.child("games/")
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
        val changelog: ChangelogObject
        private val currentObj: CurrentObject
        val version: Int
            get() = currentObj.version
        val editorVersion: Version

        private val folders: List<FileHandle> by lazy {
            val list = SFX_FOLDER.list { fh ->
                val datajson = fh.resolve(DATA_JSON_FILENAME)
                fh.isDirectory && datajson.exists() && datajson.isFile
            }.toList()

            if (list.isEmpty()) {
                error("No valid sfx folders with $DATA_JSON_FILENAME inside found")
            }

            list
        }
        private val currentObjFh: FileHandle by lazy {
            GitHelper.SOUNDS_DIR.child("current.json")
        }

        private var index: Int = 0
        var lastLoadedID: String? = null

        init {
            JsonHandler.setFailOnUnknown(false)
            currentObj = JsonHandler.fromJson(currentObjFh.readString())
            changelog = JsonHandler.fromJson(GitHelper.SOUNDS_DIR.child("changelogs/$version.json").readString())

            editorVersion = Version.fromString(currentObj.requiresVersion)
            JsonHandler.setFailOnUnknown(true)
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
            }

            errors.forEach(Toolboks.LOGGER::error)
            if (errors.isNotEmpty()) {
                error("Check above for database errors")
            }
        }

        fun loadOne(): Float {
            if (ready)
                return 1f

            objectMap as MutableMap

            val folder: FileHandle = folders[index]
            val datajsonFile: FileHandle = folder.child(DATA_JSON_FILENAME)
            val dataObject: DataObject = JsonHandler.fromJson(datajsonFile.readString("UTF-8"))

            val game: Game = Game(dataObject.id,
                                  dataObject.name,
                                  Series.valueOf(
                                          dataObject.series?.toUpperCase(
                                                  Locale.ROOT) ?: Series.OTHER.name),
                                  mutableListOf(),
                                  Texture(folder.child(
                                          ICON_FILENAME)),
                                  dataObject.group ?: dataObject.name,
                                  dataObject.groupDefault,
                                  dataObject.priority, false)

            dataObject.objects.mapTo(game.objects as MutableList) { obj ->
                when (obj) {
                    is CueObject ->
                        Cue(game, obj.id, obj.deprecatedIDs, obj.name,
                            obj.duration,
                            obj.stretchable, obj.repitchable,
                            SFX_FOLDER.child(
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
