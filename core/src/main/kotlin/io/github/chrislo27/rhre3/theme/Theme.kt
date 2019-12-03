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


object Themes : Disposable {
    val defaultThemes: List<Theme> =
            listOf(
                    LightTheme(),
                    DarkTheme(),
                    RHRE0Theme(),
                    LightPastelTheme("theme.pastel.red", Color(1f, 0.55f, 0.55f, 1f)),
                    LightPastelTheme("theme.pastel.orange", Color(1f, 0.73f, 0.55f, 1f)),
                    LightPastelTheme("theme.pastel.yellow", Color.valueOf("FFDA7D")),
                    LightPastelTheme("theme.pastel.green", Color(0.81f, 1f, 0.81f, 1f)),
                    LightPastelTheme("theme.pastel.blue", Color(0.70f, 0.86f, 1f, 1f)),
                    LightPastelTheme("theme.pastel.indigo", Color(0.70f, 0.70f, 1f, 1f)),
                    LightPastelTheme("theme.pastel.violet", Color.valueOf("C59FFF")),
                    DarkPastelTheme("theme.darkPastel.red", Color(1f, 0.55f, 0.55f, 1f)),
                    DarkPastelTheme("theme.darkPastel.orange", Color(1f, 0.73f, 0.55f, 1f)),
                    DarkPastelTheme("theme.darkPastel.yellow", Color.valueOf("FFDA7D")),
                    DarkPastelTheme("theme.darkPastel.green", Color(0.81f, 1f, 0.81f, 1f)),
                    DarkPastelTheme("theme.darkPastel.blue", Color(0.70f, 0.86f, 1f, 1f)),
                    DarkPastelTheme("theme.darkPastel.indigo", Color(0.70f, 0.70f, 1f, 1f)),
                    DarkPastelTheme("theme.darkPastel.violet", Color.valueOf("C59FFF"))
                  )
    
    override fun dispose() {
        defaultThemes.forEach(Theme::dispose)
    }
}

open class Theme : Disposable {
    
    companion object {
        const val DEFAULT_NAME: String = "<no name>"
    }
    
    var name: String = DEFAULT_NAME
    @field:JsonIgnore
    open val nameIsLocalization: Boolean = false
    
    fun getRealName(): String =
            if (nameIsLocalization)
                Localization[name]
            else
                name
    
    // main elements
    @HexColor
    var background: Color = Color(0.925f, 0.925f, 0.925f, 1f)
    @HexColor
    var trackLine: Color = Color(0.1f, 0.1f, 0.1f, 1f)
    @HexColor
    var playalongFlicking: Color = Color.valueOf("00BC67CC")
    
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
        @HexColor
        var playback: Color = Color(0f, 1f, 0f, 1f)
        @HexColor
        var musicStart: Color = Color(1f, 0f, 0f, 1f)
        @HexColor
        var tempoChange: Color = Color(0.4f, 0.4f, 0.9f, 1f)
        @HexColor
        var musicVolume: Color = Color(1f, 0.4f, 0f, 1f)
    }
    
    var trackers: TrackersGroup = TrackersGroup()
    
    open class EntitiesGroup {
        @HexColor
        var selectionTint: Color = Color(0f, 0.75f, 0.75f, 1f)
        @HexColor
        var nameColor: Color = Color(0f, 0f, 0f, 1f)
        
        @HexColor
        var cue: Color = Color(0.85f, 0.85f, 0.85f, 1f)
        @HexColor
        var pattern: Color = Color(0.85f, 0.85f, 1f, 1f)
        @HexColor
        var special: Color = Color(1f, 212f / 255f, 186f / 255f, 1f)
        @HexColor
        var keepTheBeat: Color = Color(1f, 226f / 255f, 124f / 255f, 1f)
        @HexColor
        var equidistant: Color = Color(1f, 178f / 255f, 191f / 255f, 1f)
        
        @JsonSetter("subtitle")
        @HexColor
        private fun setSubtitleColor(color: Color) {
            special = color
        }
    }
    
    var entities: EntitiesGroup = EntitiesGroup()
    
    open class SelectionGroup {
        @HexColor
        var selectionFill: Color = Color(0.1f, 0.75f, 0.75f, 0.333f)
        @HexColor
        var selectionBorder: Color = Color(0.1f, 0.85f, 0.85f, 1f)
    }
    
    var selection: SelectionGroup = SelectionGroup()
        protected set
    
    override fun dispose() {
        textureObj?.dispose()
    }
}

open class LightTheme : Theme() {
    
    @field:JsonIgnore
    override val nameIsLocalization: Boolean = true
    
    init {
        name = "theme.light"
    }
    
}

open class DarkTheme : Theme() {
    
    @field:JsonIgnore
    override val nameIsLocalization: Boolean = true
    
    init {
        name = "theme.dark"
        background = Color(0.15f, 0.15f, 0.15f, 1f)
        trackLine = Color(0.95f, 0.95f, 0.95f, 1f)
        
        entities = EntitiesGroup().apply {
            cue = Color(0.65f, 0.65f, 0.65f, 1f)
            pattern = Color(0.75f, 0.75f, 0.9f, 1f)
        }
    }
    
}

open class RHRE0Theme : Theme() {
    
    @field:JsonIgnore
    override val nameIsLocalization: Boolean = true
    
    init {
        name = "theme.rhre0"
        
        background = Color(1f, 165f / 255f, 0.5f, 1f)
        trackLine = Color(0f, 0f, 0f, 1f)
        
        entities = EntitiesGroup().apply {
            cue = Color(207f / 255f, 184f / 255f, 175f / 255f, 1f)
            pattern = Color(187f / 255f, 164f / 255f, 155f / 255f, 1f)
        }
    }
    
}

open class LightPastelTheme(name: String, background: Color) : LightTheme() {
    
    @field:JsonIgnore
    override val nameIsLocalization: Boolean = true
    
    init {
        this.name = name
        
        this.background = background
    }
    
}

open class DarkPastelTheme(name: String, trackLine: Color) : DarkTheme() {
    
    @field:JsonIgnore
    override val nameIsLocalization: Boolean = true
    
    init {
        this.name = name
        
        this.trackLine = trackLine
    }
    
}

