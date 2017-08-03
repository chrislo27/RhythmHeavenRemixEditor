package io.github.chrislo27.rhre3.theme

import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.toolboks.util.anyUninitializedLateinits


abstract class Theme {

    companion object Themes {
        val defaultThemes: List<Theme> =
                listOf(
                        LightTheme(),
                        DarkTheme(),
                        RHRE0Theme()
                      )
    }

    fun isAllInitialized(): Boolean {
        return !(this.anyUninitializedLateinits() || trackers.anyUninitializedLateinits())
    }

    // main elements
    @HexColor open lateinit var background: Color
        protected set
    @HexColor open lateinit var trackLine: Color
        protected set

    // trackers
    open class Trackers {
        @HexColor open lateinit var playback: Color
            protected set
        @HexColor open lateinit var musicStart: Color
            protected set
        @HexColor open lateinit var musicVolume: Color
            protected set
        @HexColor open lateinit var tempoChange: Color
            protected set
        @HexColor open lateinit var timeSignature: Color
            protected set
    }
    lateinit open var trackers: Trackers
        protected set

}

open class LightTheme : Theme() {

}

open class DarkTheme : Theme() {

}

open class RHRE0Theme : Theme() {

}

