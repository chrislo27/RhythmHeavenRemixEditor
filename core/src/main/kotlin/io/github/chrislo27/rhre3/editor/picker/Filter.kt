package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameByNameComparator
import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


abstract class Filter {

    companion object {
        val everythingFilter: Filter = SimpleFilter({ true })
    }

    val gameGroups: List<GameGroup> = mutableListOf()
    val gamesPerGroup: Map<GameGroup, GameList> = mutableMapOf()
    val datamodelsPerGame: Map<Game, DatamodelList> = mutableMapOf()

    var currentGroupIndex: Int = 0
        set(value) {
            field = value.coerceIn(0, maxGroupIndex)
        }
    val currentGroup: GameGroup get() = gameGroups[currentGroupIndex]
    val currentGame: Game get() = currentGameList.current
    val currentDatamodel: Datamodel get() = currentDatamodelList.current
    val currentGameList: GameList get() = gamesPerGroup[currentGroup]!!
    val currentDatamodelList: DatamodelList get() = datamodelsPerGame[currentGame]!!
    val areGroupsEmpty: Boolean
        get() = gameGroups.isEmpty()
    val areGamesEmpty: Boolean
        get() = areGroupsEmpty || gamesPerGroup[currentGroup]?.isEmpty ?: true
    val areDatamodelsEmpty: Boolean
        get() = areGamesEmpty || datamodelsPerGame[currentGame]?.isEmpty ?: true
    val maxGroupIndex: Int get() = (gameGroups.size - 1).coerceAtLeast(0)
    var groupScroll: Int = 0
        set(value) {
            field = value.coerceIn(0, maxGroupScroll)
        }
    val maxGroupScroll: Int
        get() = (gameGroups.size / Editor.ICON_COUNT_X - Editor.ICON_COUNT_Y).coerceAtLeast(0)

    protected fun clearAll() {
        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        gameGroups.clear()
        gamesPerGroup.clear()
        datamodelsPerGame.clear()
    }

    abstract fun update()

    open fun sort() {
        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        currentGroupIndex = currentGroupIndex
        groupScroll = groupScroll

        gamesPerGroup.values.forEach {
            val list = it.list

            list.sortWith(GameByNameComparator)
        }

        gameGroups.sortWith(compareBy {
            it.games.first()
        })
    }

}