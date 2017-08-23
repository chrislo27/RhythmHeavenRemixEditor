package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.version.Version


object RHRE3 {

    val VERSION: Version = Version(3, 0, 0, "DEVELOPMENT")
    const val WIDTH = 1280
    const val HEIGHT = 720
    val DEFAULT_SIZE = WIDTH to HEIGHT
    val MINIMUM_SIZE: Pair<Int, Int> = 640 to 360

    val SUPPORTED_SOUND_TYPES = listOf("ogg", "mp3", "wav")
    val tmpMusic: FileHandle by lazy {
        val fh = Gdx.files.local("tmpMusic/")
        fh.mkdirs()
        fh
    }
    const val REMIX_FILE_EXTENSION = "rhre3"

    const val GITHUB: String = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
    const val DATABASE_URL: String = "https://github.com/chrislo27/RHRE-database.git"
    const val DEV_DATABASE_BRANCH: String = "dev"
    const val DATABASE_BRANCH: String = DEV_DATABASE_BRANCH // FIXME
    const val DATABASE_CURRENT_VERSION: String = "https://raw.githubusercontent.com/chrislo27/RHRE-database/$DATABASE_BRANCH/current.json"

    var skipGitScreen: Boolean = false
    var forceGitFetch: Boolean = false

}