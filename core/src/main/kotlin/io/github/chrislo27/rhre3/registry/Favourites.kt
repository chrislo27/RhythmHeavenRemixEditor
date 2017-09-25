package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.util.JsonHandler


object Favourites {

    private class FavouritesObject {

        var games: List<String> = mutableListOf()
        var gameGroups: List<String> = mutableListOf()

        fun distinctify() {
            games = games.distinct()
            gameGroups = gameGroups.distinct()
        }

    }

    lateinit var preferences: Preferences
    val games: Map<Game, Boolean> = mutableMapOf()
    val groups: Map<GameGroup, Boolean> = mutableMapOf()

    fun isGroupFavourited(group: GameGroup): Boolean {
        return groups[group] == true
    }

    fun isGameFavourited(game: Game): Boolean {
        return games[game] == true
    }

    fun setFavourited(game: Game, yes: Boolean) {
        games as MutableMap
        games[game] = yes
    }

    fun setFavourited(group: GameGroup, yes: Boolean) {
        groups as MutableMap
        groups[group] = yes
    }

    fun initialize() {
        val obj: FavouritesObject = JsonHandler.fromJson(preferences.getString(PreferenceKeys.FAVOURITES, "{}"))
        obj.distinctify()

        games as MutableMap
        groups as MutableMap

        games.clear()
        groups.clear()

        obj.games.forEach {
            val game = GameRegistry.data.gameMap[it] ?: return@forEach
            games[game] = true
        }
        obj.gameGroups.forEach {
            val group = GameRegistry.data.gameGroupsMap[it] ?: return@forEach
            groups[group] = true
        }

        // ensures old IDs are gone
        persist()
    }

    fun persist() {
        val obj = FavouritesObject()

        obj.games = games.filter { it.value }.map { it.key.id }
        obj.gameGroups = groups.filter { it.value }.map { it.key.name }

        preferences.putString(PreferenceKeys.FAVOURITES, JsonHandler.toJson(obj))
        preferences.flush()
    }

}