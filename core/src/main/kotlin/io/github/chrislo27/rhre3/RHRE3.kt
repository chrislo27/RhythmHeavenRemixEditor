package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.util.ExportOptions
import io.github.chrislo27.toolboks.version.Version
import java.time.LocalDate
import java.time.Month


object RHRE3 {

    const val TITLE = "Rhythm Heaven Remix Editor"
    const val TITLE_3 = "Rhythm Heaven Remix Editor 3"
    val VERSION: Version = Version(3, 19, 0, "DEVELOPMENT")
    val EXPERIMENTAL: Boolean = VERSION.suffix.matches("DEVELOPMENT|SNAPSHOT(?:.)*|RC\\d+".toRegex())
    const val WIDTH = 1280
    const val HEIGHT = 720
    val DEFAULT_SIZE = WIDTH to HEIGHT
    val MINIMUM_SIZE: Pair<Int, Int> = 640 to 360
    val RHRE3_FOLDER: FileHandle by lazy { (if (portableMode) Gdx.files.local(".rhre3/") else Gdx.files.external(".rhre3/")).apply(FileHandle::mkdirs) }
    val SOUNDSTRETCH_FOLDER: FileHandle by lazy { RHRE3_FOLDER.child("soundstretch/") }

    val SUPPORTED_DECODING_SOUND_TYPES = listOf("ogg", "mp3", "wav")
    val tmpMusic: FileHandle by lazy {
        RHRE3_FOLDER.child("tmpMusic/").apply {
            mkdirs()
        }
    }
    const val REMIX_FILE_EXTENSION = "rhre3"

    const val GITHUB: String = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
    const val GITHUB_RELEASES = "$GITHUB/releases"
    const val DATABASE_URL: String = "https://github.com/chrislo27/RHRE-database.git"
    const val DONATION_URL: String = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=VA45DPLCC4958"
    val DEV_DATABASE_BRANCH: String = "prototype"
    val MASTER_DATABASE_BRANCH: String = "master"
    val DATABASE_BRANCH: String = if (VERSION.suffix.startsWith("DEV")/* || VERSION.suffix.startsWith("RC")*/) {
        DEV_DATABASE_BRANCH
    } else {
        MASTER_DATABASE_BRANCH
    }
    val DATABASE_CURRENT_VERSION: String = "https://raw.githubusercontent.com/chrislo27/RHRE-database/$DATABASE_BRANCH/current.json"
    const val DATABASE_RELEASES = "https://github.com/chrislo27/RHRE-database/releases"
    const val RELEASE_API_URL = "https://api.github.com/repos/chrislo27/RhythmHeavenRemixEditor/releases/latest"
    const val OUT_OF_MEMORY_DOC_LINK: String = "https://rhre.readthedocs.io/en/latest/Out-of-memory-on-music/"
    const val DOCS_URL: String = "https://rhre.readthedocs.io/en/latest/"

    val RHRE_ANNIVERSARY: LocalDate = LocalDate.of(2016, Month.MAY, 29)
    private val RHRE3_ANNIVERSARY: LocalDate = LocalDate.of(2017, Month.AUGUST, 30)
    private val RHRE2_ANNIVERSARY: LocalDate = LocalDate.of(2016, Month.DECEMBER, 6)

    var portableMode: Boolean = false
    var skipGitScreen: Boolean = false
    var forceGitFetch: Boolean = false
    var forceGitCheck: Boolean = false
    var verifySfxDb: Boolean = false
    var immediateEvent: Int = 0
    var noAnalytics: Boolean = false
    var noOnlineCounter: Boolean = false
    var outputGeneratedDatamodels: Boolean = false
    var outputCustomSfx: Boolean = false
    var showTapalongMarkers: Boolean = false
    var exportOptions: ExportOptions = ExportOptions.DEFAULT
    var midiRecording: Boolean = false
    var logMissingLocalizations: Boolean = false

    lateinit var launchArguments: List<String>

}