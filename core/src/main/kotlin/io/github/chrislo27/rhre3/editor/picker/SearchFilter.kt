package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.SearchBar
import io.github.chrislo27.rhre3.registry.Game
import io.github.chrislo27.rhre3.registry.GameGroupListComparatorIgnorePriority
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel
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

        fun addAllGamesFromGroups() {
            gameGroups.associateTo(gamesPerGroup) {
                it to GameList().apply {
                    this.list.addAll(it.games)
                }
            }
        }

        fun addAllDatamodelsFromGames() {
            gamesPerGroup.values.forEach { gameList ->
                gameList.list.associateTo(datamodelsPerGame) {
                    it to DatamodelList().apply {
                        this.list.addAll(it.placeableObjects)
                    }
                }
            }
        }

        fun addAllGameGroupsFromGames() {
            gameGroups.addAll(gamesPerGroup.keys)
        }

        when (filterButton.filter) {
            SearchBar.Filter.GAME_NAME -> {
                GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
                    query in group.name.toLowerCase(Locale.ROOT)
                            || group.games.any { game -> query in game.name.toLowerCase(Locale.ROOT) }
                }

                addAllGamesFromGroups()
                addAllDatamodelsFromGames()
            }
            SearchBar.Filter.ENTITY_NAME -> {
                GameRegistry.data.gameGroupsList.forEach { group ->
                    val result: List<List<Datamodel>> = group.games.mapNotNull { game ->
                        val objects = game.placeableObjects.filter {
                            query in it.name.toLowerCase(Locale.ROOT)
                        }

                        if (objects.isEmpty())
                            null
                        else
                            objects
                    }

                    if (result.isNotEmpty()) {
                        val gameList: List<Game> = result.map { it.first().game }.distinct()

                        gameGroups += group
                        gamesPerGroup.putAll(gameList.groupBy(Game::gameGroup)
                                                     .map {
                                                         it.key to GameList().apply {
                                                             this.list.addAll(it.value)
                                                         }
                                                     })
                        datamodelsPerGame.putAll(result.associate {
                            it.first().game to DatamodelList().apply {
                                this.list.addAll(it)
                            }
                        })
                    }
                }
            }
            SearchBar.Filter.CALL_AND_RESPONSE -> {
                GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
                    group.games.any { game ->
                        query in game.name.toLowerCase(Locale.ROOT) && game.hasCallAndResponse
                    }
                }

                addAllGamesFromGroups()
                gamesPerGroup.values.forEach { gameList ->
                    gameList.list.associateTo(datamodelsPerGame) {
                        it to DatamodelList().apply {
                            it.placeableObjects.filterTo(this.list) {
                                it is ResponseModel && it.responseIDs.isNotEmpty()
                            }
                        }
                    }
                }
            }
        }

        gameGroups.sortWith(compareBy(GameGroupListComparatorIgnorePriority) { it.games.first() })
    }
}