package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.SearchBar
import io.github.chrislo27.rhre3.registry.GameGroupListComparatorIgnorePriority
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.ResponseModel
import java.util.*


class SearchFilter(val editorStage: EditorStage) : Filter() {

    var query = ""

    override fun update() {
        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        clearAll()

        val filterButton = editorStage.searchBar.filterButton

        GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
            when (filterButton.filter) {
                SearchBar.Filter.GAME_NAME -> {
                    query in group.name.toLowerCase(Locale.ROOT)
                            || group.games.any { game -> query in game.name.toLowerCase(Locale.ROOT) }
                }
                SearchBar.Filter.ENTITY_NAME -> {
                    group.games.any { game ->
                        game.placeableObjects.any { obj ->
                            !obj.hidden && query in obj.name.toLowerCase(Locale.ROOT)
                        }
                    }
                }
                SearchBar.Filter.CALL_AND_RESPONSE -> {
                    (query in group.name.toLowerCase(Locale.ROOT)
                            || group.games.any { game -> query in game.name.toLowerCase(Locale.ROOT) })
                            && group.games.any { game ->
                        game.placeableObjects.any { obj ->
                            obj is ResponseModel && obj.responseIDs.isNotEmpty()
                        }
                    }
                }
            }
        }.sortWith(compareBy(
                GameGroupListComparatorIgnorePriority) { it.games.first() })

        gameGroups.associateTo(gamesPerGroup) {
            it to GameList().apply {
                it.games.filterTo(this.list, {true})
            }
        }
        // would normally flatmap but this is faster
        gamesPerGroup.values.forEach { gameList ->
            gameList.list.associateTo(datamodelsPerGame) {
                it to DatamodelList().apply {
                    it.placeableObjects.filterTo(this.list, {true})
                }
            }
        }
    }
}