package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.GameGroupListComparator
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
    val currentGroup: GameGroup?
        get() = gameGroups.getOrNull(currentGroupIndex)
    val currentGame: Game?
        get() = currentGameList?.current
    val currentDatamodel: Datamodel?
        get() = currentDatamodelList?.current
    val currentGameList: GameList?
        get() = gamesPerGroup[currentGroup]
    open val currentDatamodelList: DatamodelList?
        get() = datamodelsPerGame[currentGame]
    val areGroupsEmpty: Boolean
        get() = gameGroups.isEmpty()
    val areGamesEmpty: Boolean
        get() = areGroupsEmpty || gamesPerGroup[currentGroup]?.isEmpty != false
    open val areDatamodelsEmpty: Boolean
        get() = areGamesEmpty || datamodelsPerGame[currentGame]?.isEmpty != false
    val maxGroupIndex: Int
        get() = (gameGroups.size - 1).coerceAtLeast(0)
    var groupScroll: Int = 0
        set(value) {
            field = value.coerceIn(0, maxGroupScroll)
        }
    val maxGroupScroll: Int
        get() = (((gameGroups.size - 1) / Editor.ICON_COUNT_X + 1) - Editor.ICON_COUNT_Y).coerceAtLeast(0)

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

            list.sortWith(GameGroupListComparator)
        }

        gameGroups.sortWith(compareBy {
            it.games.first()
        })
    }

}