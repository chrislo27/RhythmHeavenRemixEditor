package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.registry.json.*
import io.github.chrislo27.rhre3.util.JsonHandler


object GameRegistry {

    const val DATA_JSON_FILENAME: String = "data.json"

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

        backingData = RegistryData()
        return backingData
    }

    class RegistryData {

        @Volatile var ready: Boolean = false
            private set

        private val folders: List<FileHandle> by lazy {
            val list = SFX_FOLDER.list { fh ->
                val datajson = fh.resolve(DATA_JSON_FILENAME)
                fh.isDirectory && datajson.exists() && datajson.isFile
            }.toList()

            if (list.isEmpty()) {
                error("No valid sfx folders with data.json inside found")
            }

            list
        }

        private var index: Int = 0

        fun loadOne(): Float {
            val folder: FileHandle = folders[index]
            val datajsonFile: FileHandle = folder.child(DATA_JSON_FILENAME)
            val dataObject: DataObject = JsonHandler.fromJson(datajsonFile.readString("UTF-8"))

            dataObject.objects.map { obj ->
                return@map when (obj) {
                    is CueObject -> TODO()
                    is DataObject -> TODO()
                    is EquidistantObject -> TODO()
                    is KeepTheBeatObject -> TODO()
                    is PatternObject -> TODO()
                    is RandomCueObject -> TODO()
                }
            }

            index++
            return getProgress()
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

    }


}
