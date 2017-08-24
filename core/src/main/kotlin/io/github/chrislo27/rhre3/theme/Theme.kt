package io.github.chrislo27.rhre3.theme

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.annotation.JsonIgnore
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.anyUninitializedLateinits


abstract class Theme {

    companion object Themes {
        val defaultThemes: List<Theme> =
                listOf(
                        LightTheme(),
                        DarkTheme(),
                        RHRE0Theme()
                      )

        val DEFAULT_NAME = "<no name>"
    }

    fun isAllInitialized(): Boolean {
        return !(this.anyUninitializedLateinits() ||
                trackers.anyUninitializedLateinits() ||
                entities.anyUninitializedLateinits() ||
                selection.anyUninitializedLateinits())
    }

    var name: String = DEFAULT_NAME
    @field:JsonIgnore open val nameIsLocalization: Boolean = false

    fun getRealName(): String =
            if (nameIsLocalization)
                Localization[name]
            else
                name

    // main elements
    @HexColor lateinit var background: Color
        protected set
    @HexColor lateinit var trackLine: Color
        protected set

    // trackers
    open class TrackersGroup {
        @HexColor lateinit var playback: Color
        @HexColor lateinit var musicStart: Color
        @HexColor lateinit var musicVolume: Color
        @HexColor lateinit var tempoChange: Color
        @HexColor lateinit var timeSignature: Color

        fun applyDefaults() {
            playback = Color(0f, 1f, 0f, 1f)
            musicStart = Color(1f, 0f, 0f, 1f)
            tempoChange = Color(0.4f, 0.4f, 0.9f, 1f)
            musicVolume = Color(1f, 0.4f, 0f, 1f)
            timeSignature = Color(1f, 2016f / 255f, 0f, 1f)
        }
    }

    lateinit var trackers: TrackersGroup
        protected set

    open class EntitiesGroup {
        @HexColor lateinit var selectionTint: Color
        @HexColor lateinit var nameColor: Color

        @HexColor lateinit var cue: Color
        @HexColor lateinit var pattern: Color
        @HexColor lateinit var subtitle: Color

        fun applyDefaults() {
            selectionTint = Color(0f, 0.75f, 0.75f, 1f)
            nameColor = Color(0f, 0f, 0f, 1f)
            subtitle = Color(1f, 212f / 255f, 186f / 255f, 1f)
        }
    }

    lateinit var entities: EntitiesGroup
        protected set

    open class SelectionGroup {
        @HexColor lateinit var selectionFill: Color
        @HexColor lateinit var selectionBorder: Color

        fun applyDefaults() {
            selectionFill = Color(0.1f, 0.75f, 0.75f, 0.333f)
            selectionBorder = Color(0.1f, 0.85f, 0.85f, 1f)
        }
    }

    lateinit var selection: SelectionGroup
        protected set

}

open class LightTheme : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        name = "theme.light"

        background = Color(0.925f, 0.925f, 0.925f, 1f)
        trackLine = Color(0.1f, 0.1f, 0.1f, 1f)

        trackers = TrackersGroup().apply {
            applyDefaults()
        }

        entities = EntitiesGroup().apply {
            applyDefaults()

            cue = Color(0.85f, 0.85f, 0.85f, 1f)
            pattern = Color(0.85f, 0.85f, 1f, 1f)
        }

        selection = SelectionGroup().apply {
            applyDefaults()

        }
    }

}

object ExampleTheme : LightTheme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = false

    init {
        name = "Example Theme"
    }

}

open class DarkTheme : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        name = "theme.dark"

        background = Color(0.15f, 0.15f, 0.15f, 1f)
        trackLine = Color(0.95f, 0.95f, 0.95f, 1f)

        trackers = TrackersGroup().apply {
            applyDefaults()
        }

        entities = EntitiesGroup().apply {
            applyDefaults()

            cue = Color(0.65f, 0.65f, 0.65f, 1f)
            pattern = Color(0.75f, 0.75f, 0.9f, 1f)
        }

        selection = SelectionGroup().apply {
            applyDefaults()

        }
    }

}

open class RHRE0Theme : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        name = "theme.rhre0"

        background = Color(1f, 165f / 255f, 0.5f, 1f)
        trackLine = Color(0f, 0f, 0f, 1f)

        trackers = TrackersGroup().apply {
            applyDefaults()
        }

        entities = EntitiesGroup().apply {
            applyDefaults()

            cue = Color(207f / 255f, 184f / 255f, 175f / 255f, 1f)
            pattern = Color(187f / 255f, 164f / 255f, 155f / 255f, 1f)
        }

        selection = SelectionGroup().apply {
            applyDefaults()

        }
    }

}

