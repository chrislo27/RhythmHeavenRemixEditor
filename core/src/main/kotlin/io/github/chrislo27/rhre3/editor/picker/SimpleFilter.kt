package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


open class SimpleFilter(val groupFilter: (GameGroup) -> Boolean,
                        val gameFilter: (Game) -> Boolean = { _ -> true },
                        val datamodelFilter: (Datamodel) -> Boolean = { _ -> true }
                       ) : Filter() {

    var shouldUpdate: Boolean = true

    override fun update() {
        if (!shouldUpdate)
            return
        shouldUpdate = false

        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        clearAll()

        GameRegistry.data.gameGroupsList.filterTo(gameGroups, groupFilter)
        gameGroups.associateTo(gamesPerGroup) {
            it to GameList().apply {
                it.games.filterTo(this.list, gameFilter)
            }
        }
        // would normally flatmap but this is faster
        gamesPerGroup.values.forEach { gameList ->
            gameList.list.associateTo(datamodelsPerGame) {
                it to DatamodelList().apply {
                    it.placeableObjects.filterTo(this.list, datamodelFilter)
                }
            }
        }
    }

}