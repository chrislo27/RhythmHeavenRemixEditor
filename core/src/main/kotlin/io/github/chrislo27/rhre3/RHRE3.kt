package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.version.Version


object RHRE3 {

    val VERSION: Version = Version(3, 1, 3, "")
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
    const val GITHUB_RELEASES = "$GITHUB/releases"
    const val DATABASE_URL: String = "https://github.com/chrislo27/RHRE-database.git"
    const val DEV_DATABASE_BRANCH: String = "dev"
    const val MASTER_DATABASE_BRANCH: String = "master"
    const val DATABASE_BRANCH: String = MASTER_DATABASE_BRANCH
    const val DATABASE_CURRENT_VERSION: String = "https://raw.githubusercontent.com/chrislo27/RHRE-database/$DATABASE_BRANCH/current.json"
    const val DATABASE_RELEASES = "https://github.com/chrislo27/RHRE-database/releases"
    const val RELEASE_API_URL = "https://api.github.com/repos/chrislo27/RhythmHeavenRemixEditor/releases/latest"

    var skipGitScreen: Boolean = false
    var forceGitFetch: Boolean = false
    var forceGitCheck: Boolean = false
    var verifyRegistry: Boolean = false

}