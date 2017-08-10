package io.github.chrislo27.rhre3.registry

import io.github.chrislo27.rhre3.registry.Game

data class GameGroup(val name: String, val games: List<Game>) {

    val series: Series

    init {
        if (games.isEmpty())
            error("Game list in game group $name cannot be empty")

        series = games.first().series

        val mismatchingSeries = games.filter { it.series != series }
        if (mismatchingSeries.isNotEmpty())
            error("The following games in game group $name do not have the series $series: $mismatchingSeries")
    }

}
