package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.CALL_AND_RESPONSE
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.ENTITY_NAME
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.FAVOURITES
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.GAME_NAME
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.USE_IN_REMIX
import io.github.chrislo27.rhre3.entity.model.ModelEntity
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
            GAME_NAME -> {
                GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
                    query in group.name.toLowerCase(Locale.ROOT)
                            || group.games.any { game -> query in game.name.toLowerCase(Locale.ROOT) || game.searchHints.any { query in it.toLowerCase(Locale.ROOT) } }
                }

                gameGroups.associateTo(gamesPerGroup) {
                    it to GameList().apply {
                        if (query in it.name.toLowerCase(Locale.ROOT)) {
                            this.list.addAll(it.games)
                        } else {
                            it.games.filterTo(this.list) { game -> query in game.name.toLowerCase(Locale.ROOT) || game.searchHints.any { query in it.toLowerCase(Locale.ROOT) } }
                        }
                    }
                }
                addAllDatamodelsFromGames()
            }
            ENTITY_NAME -> {
                GameRegistry.data.gameGroupsList.forEach { group ->
                    val result: List<List<Datamodel>> = group.games.mapNotNull { game ->
                        game.placeableObjects.filter {
                            query in it.name.toLowerCase(Locale.ROOT)
                        }.takeIf { it.isNotEmpty() }
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
            CALL_AND_RESPONSE -> {
                GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
                    group.games.any { game ->
                        game.hasCallAndResponse && (query in game.name.toLowerCase(Locale.ROOT) || game.searchHints.any { query in it.toLowerCase(Locale.ROOT) })
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
            FAVOURITES -> {
                GameRegistry.data.gameGroupsList.filterTo(gameGroups) { group ->
                    (group.isFavourited && query in group.name.toLowerCase(Locale.ROOT))
                            || group.games.any { game ->
                        game.isFavourited && (query in game.name.toLowerCase(Locale.ROOT) || game.searchHints.any { query in it.toLowerCase(Locale.ROOT) })
                    }
                }

                gameGroups.associateTo(gamesPerGroup) {
                    it to GameList().apply {
                        if (it.isFavourited) {
                            this.list.addAll(it.games)
                        } else {
                            it.games.filterTo(this.list, Game::isFavourited)
                        }
                    }
                }

                addAllDatamodelsFromGames()
            }
            USE_IN_REMIX -> {
                val remix = editorStage.editor.remix

                remix.entities
                        .filterIsInstance<ModelEntity<*>>()
                        .map(ModelEntity<*>::datamodel)
                        .map(Datamodel::game)
                        .distinct()
                        .filter { game -> (query in game.name.toLowerCase(Locale.ROOT) || game.searchHints.any { query in it.toLowerCase(Locale.ROOT) }) }
                        .groupBy(Game::gameGroup)
                        .asSequence()
                        .associateTo(gamesPerGroup) {
                            it.key to GameList().apply {
                                list.addAll(it.value)
                            }
                        }

                addAllGameGroupsFromGames()
                addAllDatamodelsFromGames()
            }
        }
    }

    override fun sort() {
        super.sort()
        gameGroups as MutableList
        gameGroups.sortWith(compareBy(GameGroupListComparatorIgnorePriority) { it.games.first() })
    }
}