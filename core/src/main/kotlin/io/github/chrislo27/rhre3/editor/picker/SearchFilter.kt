package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.CALL_AND_RESPONSE
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.ENTITY_NAME
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.FAVOURITES
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.GAME_NAME
import io.github.chrislo27.rhre3.editor.stage.SearchBar.Filter.USE_IN_REMIX
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.Game
import io.github.chrislo27.rhre3.sfxdb.GameGroupListComparatorIgnorePriority
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel
import java.util.*


class SearchFilter(val editorStage: EditorStage) : Filter() {
    
    var query = ""
    
    private fun Game.queryMatchesGame(): Boolean {
        return query in this.name.toLowerCase(Locale.ROOT) || this.searchHints.any { query in it.toLowerCase(Locale.ROOT) }
    }
    
    override fun update() {
        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap
        
        clearAll()
        
        val filterButton = editorStage.searchBar.filterButton
        
        fun addAllGamesFromGroups() {
            gameGroups.associateWithTo(gamesPerGroup) {
                GameList().apply {
                    this.list.addAll(it.games)
                }
            }
        }
        
        fun addAllDatamodelsFromGames() {
            gamesPerGroup.values.forEach { gameList ->
                gameList.list.associateWithTo(datamodelsPerGame) {
                    DatamodelList().apply {
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
                // Searches the game group name and also game names
                // If a game group matches, all its games are added
                // If a game matches but not its game group, that game group is added with just the game matches
                
                // Find game group matches
                val matchingGameGroups = SFXDatabase.data.gameGroupsList.filter { group ->
                    query in group.name.toLowerCase(Locale.ROOT)
                }.toMutableList()
                val matchingGameGroupsSet = matchingGameGroups.toSet()
                matchingGameGroups.associateWithTo(gamesPerGroup) {
                    GameList().apply {
                        this.list.addAll(it.games)
                    }
                }
                
                // Find individual games that match
                val indivGames = SFXDatabase.data.gameList.filter { game ->
                    game.gameGroup !in matchingGameGroupsSet && game.queryMatchesGame()
                }
                indivGames.forEach { game ->
                    // Add the game group
                    if (game.gameGroup !in matchingGameGroups) {
                        matchingGameGroups.add(game.gameGroup)
                    }
                    gamesPerGroup.getOrPut(game.gameGroup) { GameList() }.list.add(game)
                }
                
                gameGroups.addAll(matchingGameGroups)
                addAllDatamodelsFromGames()
            }
            ENTITY_NAME -> {
                SFXDatabase.data.gameGroupsList.forEach { group ->
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
                val gamesWithCAR = SFXDatabase.data.gameList.filter { it.hasCallAndResponse && it.queryMatchesGame() }
                gameGroups.addAll(gamesWithCAR.map { it.gameGroup }.distinct())
                gamesWithCAR.forEach { game ->
                    gamesPerGroup.getOrPut(game.gameGroup) { GameList() }.list.add(game)
                }
                
                gamesPerGroup.values.forEach { gameList ->
                    gameList.list.associateWithTo(datamodelsPerGame) {
                        DatamodelList().apply {
                            it.placeableObjects.filterTo(this.list) {
                                it is ResponseModel && it.responseIDs.isNotEmpty()
                            }
                        }
                    }
                }
            }
            FAVOURITES -> {
                SFXDatabase.data.gameGroupsList.filterTo(gameGroups) { group ->
                    (group.isFavourited && query in group.name.toLowerCase(Locale.ROOT))
                            || group.games.any { game ->
                        game.isFavourited && game.queryMatchesGame()
                    }
                }
                
                gameGroups.associateWithTo(gamesPerGroup) {
                    GameList().apply {
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
                        .asSequence()
                        .filterIsInstance<ModelEntity<*>>()
                        .map(ModelEntity<*>::datamodel)
                        .map(Datamodel::game)
                        .distinct()
                        .filter { it.queryMatchesGame() }
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