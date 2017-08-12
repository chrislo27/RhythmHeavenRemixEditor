package io.github.chrislo27.rhre3

import io.github.chrislo27.toolboks.version.Version


object RHRE3 {

    val VERSION: Version = Version(3, 0, 0, "DEVELOPMENT")
    const val WIDTH = 1280
    const val HEIGHT = 720
    val DEFAULT_SIZE = WIDTH to HEIGHT
    val MINIMUM_SIZE: Pair<Int, Int> = 640 to 360

    val SUPPORTED_SOUND_TYPES = listOf("ogg", "mp3", "wav")

    const val GITHUB: String = "https://github.com/chrislo27/RhythmHeavenRemixEditor"
    const val DATABASE_URL: String = "https://github.com/chrislo27/RHRE-database.git"
    const val DATABASE_BRANCH: String = "dev" // FIXME
    const val DATABASE_CURRENT_VERSION: String = "https://raw.githubusercontent.com/chrislo27/RHRE-database/$DATABASE_BRANCH/current.json"

    var skipGitScreen: Boolean = false

}