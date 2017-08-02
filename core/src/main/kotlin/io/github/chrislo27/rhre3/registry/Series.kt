package io.github.chrislo27.rhre3.registry

import java.util.*


enum class Series {

    OTHER, TENGOKU, DS, FEVER, MEGAMIX, SIDE, CUSTOM;

    companion object {
        val VALUES: List<Series> by lazy { Series.values().toList() }
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