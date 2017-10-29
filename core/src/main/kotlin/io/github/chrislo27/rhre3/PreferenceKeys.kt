package io.github.chrislo27.rhre3


object PreferenceKeys {

    private const val DATABASE_VERSION = "databaseVersion"
    val DATABASE_VERSION_BRANCH = "${DATABASE_VERSION}_${RHRE3.DATABASE_BRANCH}"
    const val THEME_INDEX = "themeIndex"
    const val LANG_INDEX = "languageIndex"
    const val WINDOW_STATE = "windowState"

    // settings
    const val SETTINGS_MINIMAP = "settings_minimap"
    const val SETTINGS_AUTOSAVE = "settings_autosave"
    const val SETTINGS_CHASE_CAMERA = "settings_chaseCamera"
    const val SETTINGS_SUBTITLE_ORDER = "settings_subtitleOrder"
    const val SETTINGS_REMIX_ENDS_AT_LAST = "settings_remixEndsAtLast"
    const val SETTINGS_SMOOTH_DRAGGING = "settings_smoothDragging"
    const val SETTINGS_SOUND_SYSTEM = "settings_soundSystem"

    const val FILE_CHOOSER_MUSIC = "fileChooser_musicSelect"
    const val FILE_CHOOSER_SAVE = "fileChooser_save"
    const val FILE_CHOOSER_LOAD = "fileChooser_load"
    const val FILE_CHOOSER_EXPORT = "fileChooser_export"

    const val FAVOURITES = "favourites"
    const val RECENT_GAMES = "recentGames"

}