package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.GameMetadata


class RecentFilter : Filter() {

    var shouldUpdate: Boolean = true
    private val lastList: MutableList<Game> = mutableListOf()

    override fun update() {
        if (!shouldUpdate)
            return
        shouldUpdate = false

        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        val recents: List<Game> = GameMetadata.recents
        val removedGames = lastList.filter { it !in recents }
        val newGames = recents.filter { it !in lastList }

        removedGames.forEach {
            gameGroups.remove(it.gameGroup)
            gamesPerGroup.remove(it.gameGroup)
            datamodelsPerGame.remove(it)
        }

        newGames.forEach {
            val group = it.gameGroup
            if (group !in gameGroups) {
                gameGroups.add(group)
                gamesPerGroup[group] = GameList()
            }
            if (it !in datamodelsPerGame) {
                datamodelsPerGame[it] = DatamodelList().apply {
                    this.list.addAll(it.placeableObjects)
                }
            }

            gamesPerGroup[group]!!.list += it
        }

        lastList.clear()
        lastList.addAll(recents)
    }

    override fun sort() {
        val currentGroup = if (areGroupsEmpty) null else currentGroup

        super.sort()
        gameGroups as MutableList

        gameGroups.sortWith(compareBy {
            it.games.minBy { if (it.recency == -1) Int.MAX_VALUE else it.recency }?.recency ?: error(
                    "No games in group $it")
        })

        if (currentGroup != null) {
            this.currentGroupIndex = this.gameGroups.indexOf(currentGroup)
        }
        this.currentGroupIndex = this.currentGroupIndex
    }
}