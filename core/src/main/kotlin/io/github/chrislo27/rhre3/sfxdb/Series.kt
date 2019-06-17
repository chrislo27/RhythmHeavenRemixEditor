package io.github.chrislo27.rhre3.sfxdb

import java.util.*


enum class Series {

    OTHER,
    TENGOKU, DS, FEVER, MEGAMIX,
//    SWITCH,
    SIDE;

    companion object {
        val VALUES: List<Series> = Series.values().toList()
    }

    val lowerCaseName: String by lazy { this.name.toLowerCase(Locale.ROOT) }
    val localization: String by lazy {
        "series.$lowerCaseName.name"
    }
    val textureId: String by lazy {
        "series_icon_$lowerCaseName"
    }
    val texturePath: String by lazy {
        "images/series/$lowerCaseName.png"
    }

}