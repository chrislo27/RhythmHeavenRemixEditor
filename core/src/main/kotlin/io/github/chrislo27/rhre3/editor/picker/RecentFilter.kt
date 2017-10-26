package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Game


class RecentFilter : SimpleFilter(
        { it.games.any(Game::isRecent) },
        gameFilter = Game::isRecent
                                 ) {

    override fun sort() {
        super.sort()
        gameGroups as MutableList

        gameGroups.sortWith(compareBy {
            it.games.minBy { if (it.recency == -1) Int.MAX_VALUE else it.recency }?.recency ?: error(
                    "No games in group $it")
        })
    }
}