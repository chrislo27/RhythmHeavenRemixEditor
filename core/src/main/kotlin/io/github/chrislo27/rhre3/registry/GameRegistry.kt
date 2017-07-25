package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.registry.datamodel.*
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.version.Version


object GameRegistry : Disposable {

    const val DATA_JSON_FILENAME: String = "data.json"
    const val ICON_FILENAME: String = "icon.png"

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

    fun initialize(): RegistryData {
        if (!backingData.ready)
            throw IllegalStateException("Cannot call load when already loading")

        dispose()

        backingData = RegistryData()
        return backingData
    }

    class RegistryData : Disposable {

        @Volatile var ready: Boolean = false
            private set
        val gameMap: Map<String, Game> = mutableMapOf()
        val gameList: List<Game> by lazy {
            if (!ready)
                error("Attempt to map game list when not ready")

            gameMap.values.toList()
        }
        val objectMap: Map<String, Datamodel> = mutableMapOf()
        val objectList: List<Datamodel> by lazy {
            if (!ready)
                error("Attempt to map datamodels when not ready")

            objectMap.values.toList()
        }

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

        private var index: Int = 0

        private fun whenDone() {
            ready = true

            // create
            gameList
            objectList
        }

        fun loadOne(): Float {
            if (ready)
                error("Attempt to load when already ready")

            val folder: FileHandle = folders[index]
            val datajsonFile: FileHandle = folder.child(DATA_JSON_FILENAME)
            val dataObject: DataObject = JsonHandler.fromJson(datajsonFile.readString("UTF-8"))

            val game: Game = Game(dataObject.id, dataObject.name, Version.fromString(dataObject.requiresVersion),
                                  mutableListOf(),
                                  Texture(folder.child(ICON_FILENAME)))

            dataObject.objects.mapTo(game.objects as MutableList) { obj ->
                when (obj) {
                    is TempoBasedCueObject ->
                        TempoBasedCue(game, obj.id, obj.deprecatedIDs, obj.name, obj.duration,
                                      obj.stretchable, obj.repitchable,
                                      SFX_FOLDER.child("${obj.id}.${obj.fileExtension}"),
                                      obj.introSound, obj.endingSound,
                                      obj.responseIDs)
                    is FillbotsFillCueObject ->
                        FillbotsFillCue(game, obj.id, obj.deprecatedIDs, obj.name, obj.duration,
                                        obj.stretchable, obj.repitchable,
                                        SFX_FOLDER.child("${obj.id}.${obj.fileExtension}"),
                                        obj.introSound, obj.endingSound,
                                        obj.responseIDs)
                    is LoopingCueObject ->
                        LoopingCue(game, obj.id, obj.deprecatedIDs, obj.name, obj.duration,
                                   obj.stretchable, obj.repitchable,
                                   SFX_FOLDER.child("${obj.id}.${obj.fileExtension}"),
                                   obj.introSound, obj.endingSound,
                                   obj.responseIDs)
                    is CueObject ->
                        Cue(game, obj.id, obj.deprecatedIDs, obj.name, obj.duration,
                            obj.stretchable, obj.repitchable,
                            SFX_FOLDER.child("${obj.id}.${obj.fileExtension}"),
                            obj.introSound, obj.endingSound,
                            obj.responseIDs)
                    is EquidistantObject ->
                        Equidistant(game, obj.id, obj.deprecatedIDs, obj.name, obj.distance, obj.stretchable, obj.cues)
                    is KeepTheBeatObject ->
                        KeepTheBeat(game, obj.id, obj.deprecatedIDs, obj.name, obj.duration, obj.cues)
                    is PatternObject ->
                        Pattern(game, obj.id, obj.deprecatedIDs, obj.name, obj.cues)
                    is RandomCueObject ->
                        RandomCue(game, obj.id, obj.deprecatedIDs, obj.name, obj.cues)
                }
            }

            (gameMap as MutableMap)[game.id] = game

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
            val msToLoad = (delta * 1000f)
            val startNano = System.nanoTime()

            do {
                loadOne()
            } while (getProgress() < 1 && (System.nanoTime() - startNano) / 1_000_000f < msToLoad)

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
