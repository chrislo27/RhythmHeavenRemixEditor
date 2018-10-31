package io.github.chrislo27.rhre3


object PreferenceKeys {

    private val DATABASE_VERSION = "databaseVersion"
    val DATABASE_VERSION_BRANCH = "${DATABASE_VERSION}_${RHRE3.DATABASE_BRANCH}"
    val THEME_INDEX = "themeIndex"
    val THEME_USES_MENU = "themeUsesMenu"
    val LANG_INDEX = "languageIndex"
    val LANGUAGE = "language"
    val WINDOW_STATE = "windowState"
    val MIDI_NOTE = "midiNote"

    // settings
    val SETTINGS_MINIMAP = "settings_minimap"
    val SETTINGS_MINIMAP_PREVIEW = "settings_minimapPreview"
    val SETTINGS_AUTOSAVE = "settings_autosave"
    val SETTINGS_CHASE_CAMERA = "settings_chaseCamera"
    val SETTINGS_SUBTITLE_ORDER = "settings_subtitleOrder"
    val SETTINGS_REMIX_ENDS_AT_LAST = "settings_remixEndsAtLast"
    val SETTINGS_SMOOTH_DRAGGING = "settings_smoothDragging"
    val SETTINGS_DISCORD_RPC_ENABLED = "settings_discordRPCEnabled"
    val SETTINGS_ADVANCED_OPTIONS = "settings_advancedOptions"

    val allSettingsKeys: List<String> by lazy {
        listOf(SETTINGS_MINIMAP, SETTINGS_MINIMAP_PREVIEW, SETTINGS_AUTOSAVE, SETTINGS_CHASE_CAMERA,
               SETTINGS_SUBTITLE_ORDER, SETTINGS_REMIX_ENDS_AT_LAST, SETTINGS_SMOOTH_DRAGGING,
               SETTINGS_DISCORD_RPC_ENABLED, SETTINGS_ADVANCED_OPTIONS)
    }

    val FILE_CHOOSER_MUSIC = "fileChooser_musicSelect"
    val FILE_CHOOSER_SAVE = "fileChooser_save"
    val FILE_CHOOSER_LOAD = "fileChooser_load"
    val FILE_CHOOSER_EXPORT = "fileChooser_export"
    val FILE_CHOOSER_TEXENT = "fileChooser_texEnt"

    val FAVOURITES = "favourites"
    val RECENT_GAMES = "recentGames"
    val LAST_VERSION = "lastVersion"
    val TIMES_SKIPPED_UPDATE = "timesSkippedUpdate"
    val BACKGROUND = "background"
    val LAST_NEWS = "lastNewsArticles"
    val READ_NEWS = "readNewsArticles"
    val PADDLER_LOADING_ICON = "paddlerLoadingIcon"

    val EVENT_PREFIX = "event_"
    val SEEN_EXPANSION_SPLASH = "seenExpansionSplash"

}