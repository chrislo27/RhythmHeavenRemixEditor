package io.github.chrislo27.rhre3

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.PreferenceKeys.ADVOPT_EXPLODING_ENTITIES
import io.github.chrislo27.rhre3.PreferenceKeys.ADVOPT_IGNORE_PITCH_RESTRICTIONS
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_ADVANCED_OPTIONS
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_DISABLE_MINIMAP
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_DISABLE_TIME_STRETCHING
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_GLASS_ENTITIES
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_MINIMAP_PREVIEW
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_REMIX_ENDS_AT_LAST
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_SMOOTH_DRAGGING
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_SUBTITLE_ORDER
import io.github.chrislo27.rhre3.PreferenceKeys.THEME_USES_MENU
import io.github.chrislo27.rhre3.editor.CameraBehaviour
import io.github.chrislo27.rhre3.editor.Editor


class Settings(private val main: RHRE3Application) {
    
    private val preferences: Preferences get() = main.preferences
    
    var advancedOptions: Boolean = false
    var disableTimeStretching: Boolean = false
    var themeUsesMenu: Boolean = false
    var glassEntities: Boolean = false
    var disableMinimap: Boolean = false
    var minimapPreview: Boolean = true
    var subtitlesBelow: Boolean = false
    var remixEndsAtLast: Boolean = false
    var smoothDragging: Boolean = true
    var cameraBehaviour: CameraBehaviour = Editor.DEFAULT_CAMERA_BEHAVIOUR
    
    var advExplodingEntities: Boolean = false
    var advIgnorePitchRestrictions: Boolean = false
    
    fun load() {
        advancedOptions = preferences.getBoolean(SETTINGS_ADVANCED_OPTIONS, advancedOptions)
        disableTimeStretching = preferences.getBoolean(SETTINGS_DISABLE_TIME_STRETCHING, disableTimeStretching)
        themeUsesMenu = preferences.getBoolean(THEME_USES_MENU, themeUsesMenu)
        glassEntities = preferences.getBoolean(SETTINGS_GLASS_ENTITIES, glassEntities)
        disableMinimap = preferences.getBoolean(SETTINGS_DISABLE_MINIMAP, disableMinimap)
        minimapPreview = preferences.getBoolean(SETTINGS_MINIMAP_PREVIEW, minimapPreview)
        subtitlesBelow = preferences.getBoolean(SETTINGS_SUBTITLE_ORDER, subtitlesBelow)
        remixEndsAtLast = preferences.getBoolean(SETTINGS_REMIX_ENDS_AT_LAST, remixEndsAtLast)
        smoothDragging = preferences.getBoolean(SETTINGS_SMOOTH_DRAGGING, smoothDragging)
        val oldChaseCamera = "settings_chaseCamera"
        if (oldChaseCamera in preferences) {
            // Retroactively apply settings
            val oldSetting = preferences.getBoolean(oldChaseCamera, true)
            cameraBehaviour = if (oldSetting) CameraBehaviour.FOLLOW_PLAYBACK else CameraBehaviour.PAN_OVER_INSTANT
            // Delete
            preferences.remove(oldChaseCamera)
            preferences.flush()
        } else {
            cameraBehaviour = CameraBehaviour.MAP.getOrDefault(preferences.getString(PreferenceKeys.SETTINGS_CAMERA_BEHAVIOUR), Editor.DEFAULT_CAMERA_BEHAVIOUR)
        }

        advExplodingEntities = preferences.getBoolean(ADVOPT_EXPLODING_ENTITIES, advExplodingEntities)
        advIgnorePitchRestrictions = preferences.getBoolean(ADVOPT_IGNORE_PITCH_RESTRICTIONS, advIgnorePitchRestrictions)
    }
    
    fun persist() {
        preferences
                .putBoolean(SETTINGS_ADVANCED_OPTIONS, advancedOptions)
                .putBoolean(SETTINGS_DISABLE_TIME_STRETCHING, disableTimeStretching)
                .putBoolean(THEME_USES_MENU, themeUsesMenu)
                .putBoolean(SETTINGS_GLASS_ENTITIES, glassEntities)
                .putBoolean(SETTINGS_DISABLE_MINIMAP, disableTimeStretching)
                .putBoolean(SETTINGS_MINIMAP_PREVIEW, minimapPreview)
                .putBoolean(SETTINGS_SUBTITLE_ORDER, subtitlesBelow)
                .putBoolean(SETTINGS_REMIX_ENDS_AT_LAST, remixEndsAtLast)
                .putBoolean(SETTINGS_SMOOTH_DRAGGING, smoothDragging)
                
                .putBoolean(ADVOPT_EXPLODING_ENTITIES, advExplodingEntities)
                .putBoolean(ADVOPT_IGNORE_PITCH_RESTRICTIONS, advIgnorePitchRestrictions)
                .flush()
    }
}