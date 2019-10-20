package io.github.chrislo27.rhre3.sfxdb

import java.util.*


enum class Series(val console: String = "") {

    OTHER,
    TENGOKU("GBA"), DS("DS"), FEVER("Wii"), MEGAMIX("3DS"),
//    SWITCH("Switch"),
    SIDE;

    companion object {
        val VALUES: List<Series> = Series.values().toList()
    }

    val lowerCaseName: String = this.name.toLowerCase(Locale.ROOT)
    val localization: String = "series.$lowerCaseName.name"
    val textureId: String = "series_icon_$lowerCaseName"
    val texturePath: String = "images/series/$lowerCaseName.png"
}