package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.toolboks.version.Version


object RHRE3 {

    const val TITLE = "Rhythm Heaven Remix Editor 3"
    val VERSION: Version = Version(3, 5, 1, "DEVELOPMENT")
    const val WIDTH = 1280
    const val HEIGHT = 720
    val DEFAULT_SIZE = WIDTH to HEIGHT
    val MINIMUM_SIZE: Pair<Int, Int> = 640 to 360

    val SUPPORTED_SOUND_TYPES = listOf("ogg", "mp3", "wav")
    val tmpMusic: FileHandle by lazy {
        Gdx.files.local("tmpMusic/").apply {
            mkdirs()
        }
    }
    const val REMIX_FILE_EXTENSION = "rhre3"

    const val GITHUB: String = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
    const val GITHUB_RELEASES = "$GITHUB/releases"
    const val DATABASE_URL: String = "https://github.com/chrislo27/RHRE-database.git"
    private const val DEV_DATABASE_BRANCH: String = "dev"
    private const val MASTER_DATABASE_BRANCH: String = "master"
    val DATABASE_BRANCH: String = DEV_DATABASE_BRANCH // CANNOT be const
    val DATABASE_CURRENT_VERSION: String = "https://raw.githubusercontent.com/chrislo27/RHRE-database/$DATABASE_BRANCH/current.json"
    const val DATABASE_RELEASES = "https://github.com/chrislo27/RHRE-database/releases"
    const val RELEASE_API_URL = "https://api.github.com/repos/chrislo27/RhythmHeavenRemixEditor/releases/latest"
    const val OUT_OF_MEMORY_DOC_LINK: String = "http://rhre.readthedocs.io/en/latest/Out-of-memory-on-music/"

    var skipGitScreen: Boolean = false
    var forceGitFetch: Boolean = false
    var forceGitCheck: Boolean = false
    var verifyRegistry: Boolean = false
    var defaultSoundSystem: String = "beads"
    lateinit var launchArguments: List<String>

}