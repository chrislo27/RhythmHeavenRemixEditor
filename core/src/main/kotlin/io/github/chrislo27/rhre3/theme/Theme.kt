package io.github.chrislo27.rhre3.theme

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Base64Coder
import com.badlogic.gdx.utils.Disposable
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.util.getUninitializedLateinits
import kotlin.reflect.KProperty


object Themes : Disposable {
    val defaultThemes: List<Theme> =
            listOf(
                    LightTheme(),
                    DarkTheme(),
                    RHRE0Theme(),
                    BackgroundTheme("theme.pastel.red", Color(1f, 0.55f, 0.55f, 1f)),
                    BackgroundTheme("theme.pastel.orange", Color(1f, 0.73f, 0.55f, 1f)),
                    BackgroundTheme("theme.pastel.yellow", Color.valueOf("FFDA7D")),
                    BackgroundTheme("theme.pastel.green", Color(0.81f, 1f, 0.81f, 1f)),
                    BackgroundTheme("theme.pastel.blue", Color(0.70f, 0.86f, 1f, 1f)),
                    BackgroundTheme("theme.pastel.indigo", Color(0.70f, 0.70f, 1f, 1f)),
                    BackgroundTheme("theme.pastel.violet", Color(0.89f, 0.86f, 1f, 1f)),
                    DarkPastelTheme("theme.darkPastel.red", Color(1f, 0.55f, 0.55f, 1f)),
                    DarkPastelTheme("theme.darkPastel.orange", Color(1f, 0.73f, 0.55f, 1f)),
                    DarkPastelTheme("theme.darkPastel.yellow", Color.valueOf("FFDA7D")),
                    DarkPastelTheme("theme.darkPastel.green", Color(0.81f, 1f, 0.81f, 1f)),
                    DarkPastelTheme("theme.darkPastel.blue", Color(0.70f, 0.86f, 1f, 1f)),
                    DarkPastelTheme("theme.darkPastel.indigo", Color(0.70f, 0.70f, 1f, 1f)),
                    DarkPastelTheme("theme.darkPastel.violet", Color(0.89f, 0.86f, 1f, 1f))
                  )

    override fun dispose() {
        defaultThemes.forEach(Theme::dispose)
    }
}

open class Theme : Disposable {

    companion object {

        const val DEFAULT_NAME = "<no name>"

    }

    fun getUninitialized(): List<KProperty<*>> {
        return this.getUninitializedLateinits() + trackers.getUninitializedLateinits() + entities.getUninitializedLateinits() + selection.getUninitializedLateinits()
    }

    fun isAllInitialized(): Boolean {
        return getUninitialized().isEmpty()
    }

    var name: String = Theme.DEFAULT_NAME
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

    @field:JsonIgnore
    private var waveformBacking: Color? = null
    var waveform: Color
        @HexColor get() = waveformBacking ?: trackLine
        @HexColor set(value) {
            waveformBacking = value
        }

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    var texture: String? = "<insert optional Base64 encoded RGBA8888 PNG here>"

    @delegate:JsonIgnore
    val textureObj: Texture? by lazy {
        if (texture.isNullOrBlank() || texture!!.matches("<.*>".toRegex())) {
            null
        } else {
            try {
                val array = Base64Coder.decode(texture)
                Texture(Pixmap(array, 0, array.size))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

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
        @HexColor lateinit var special: Color
        @HexColor lateinit var equidistant: Color
        @HexColor lateinit var keepTheBeat: Color

        @JsonSetter("subtitle")
        @HexColor
        private fun setSubtitleColor(color: Color) {
            special = color
        }

        fun applyDefaults() {
            selectionTint = Color(0f, 0.75f, 0.75f, 1f)
            nameColor = Color(0f, 0f, 0f, 1f)
            special = Color(1f, 212f / 255f, 186f / 255f, 1f)

            keepTheBeat = Color(1f, 226f / 255f, 124f / 255f, 1f)
            equidistant = Color(1f, 178f / 255f, 191f / 255f, 1f)
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

    override fun dispose() {
        textureObj?.dispose()
    }
}

open class LightTheme : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        name = "theme.light"

        background = Color(0.925f, 0.925f, 0.925f, 1f)
        trackLine = Color(0.1f, 0.1f, 0.1f, 1f)
        waveform = Color(trackLine)

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

open class DarkTheme : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        name = "theme.dark"

        background = Color(0.15f, 0.15f, 0.15f, 1f)
        trackLine = Color(0.95f, 0.95f, 0.95f, 1f)
        waveform = Color(trackLine)

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
        waveform = Color(trackLine)

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

open class BackgroundTheme(name: String, background: Color) : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        this.name = name

        this.background = background
        trackLine = Color(0.1f, 0.1f, 0.1f, 1f)
        waveform = Color(trackLine)

        trackers = TrackersGroup().apply {
            applyDefaults()
        }

        entities = EntitiesGroup().apply {
            applyDefaults()

            // Based on LightTheme
            cue = Color(0.85f, 0.85f, 0.85f, 1f)
            pattern = Color(0.85f, 0.85f, 1f, 1f)
        }

        selection = SelectionGroup().apply {
            applyDefaults()
        }
    }

}

open class DarkPastelTheme(name: String, trackLine: Color) : Theme() {

    @field:JsonIgnore override val nameIsLocalization: Boolean = true

    init {
        this.name = name

        this.background = Color(0.15f, 0.15f, 0.15f, 1f)
        this.trackLine = trackLine
        waveform = Color(trackLine)

        trackers = TrackersGroup().apply {
            applyDefaults()
        }

        entities = EntitiesGroup().apply {
            applyDefaults()

            // Based on DarkTheme
            cue = Color(0.65f, 0.65f, 0.65f, 1f)
            pattern = Color(0.75f, 0.75f, 0.9f, 1f)
        }

        selection = SelectionGroup().apply {
            applyDefaults()
        }
    }

}

