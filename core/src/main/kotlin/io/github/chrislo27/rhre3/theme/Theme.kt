package io.github.chrislo27.rhre3.theme


abstract class Theme {

    companion object Themes {
        val defaultThemes: List<Theme> =
                listOf(
                        LightTheme(),
                        DarkTheme(),
                        RHRE0Theme()
                      )
    }

}

open class LightTheme : Theme() {

}

open class DarkTheme : Theme() {

}

open class RHRE0Theme : Theme() {

}

