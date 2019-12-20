package io.github.chrislo27.rhre3

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_ADVANCED_OPTIONS
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_DISABLE_TIME_STRETCHING
import io.github.chrislo27.rhre3.PreferenceKeys.SETTINGS_GLASS_ENTITIES
import io.github.chrislo27.rhre3.PreferenceKeys.THEME_USES_MENU


class Settings(private val main: RHRE3Application) {
    
    private val preferences: Preferences get() = main.preferences
    
    var advancedOptions: Boolean = false
    var disableTimeStretching: Boolean = false
    var themeUsesMenu: Boolean = false
    var glassEntities: Boolean = false
    
    fun load() {
        advancedOptions = preferences.getBoolean(SETTINGS_ADVANCED_OPTIONS, false)
        disableTimeStretching = preferences.getBoolean(SETTINGS_DISABLE_TIME_STRETCHING, false)
        themeUsesMenu = preferences.getBoolean(THEME_USES_MENU, false)
        glassEntities = preferences.getBoolean(SETTINGS_GLASS_ENTITIES, false)
    }
    
    fun persist() {
        preferences
                .putBoolean(SETTINGS_ADVANCED_OPTIONS, advancedOptions)
                .putBoolean(SETTINGS_DISABLE_TIME_STRETCHING, disableTimeStretching)
                .putBoolean(THEME_USES_MENU, themeUsesMenu)
                .putBoolean(SETTINGS_GLASS_ENTITIES, glassEntities)
                .flush()
    }
}