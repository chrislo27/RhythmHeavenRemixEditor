package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.Preferences
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.util.JsonHandler


object GameMetadata {

    const val MAX_RECENTLY_USED: Int = Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y

    private class FavouritesObject {

        var games: List<String> = mutableListOf()
        var gameGroups: List<String> = mutableListOf()

        fun distinctify() {
            games = games.distinct()
            gameGroups = gameGroups.distinct()
        }

    }

    private class RecentsObject {

        var games: List<String> = mutableListOf()

        fun distinctify() {
            games = games.distinct()
        }

    }

    private lateinit var preferences: Preferences
    val games: Map<Game, Boolean> = mutableMapOf()
    val groups: Map<GameGroup, Boolean> = mutableMapOf()
    val recents: MutableList<Game> = mutableListOf()

    fun setPreferencesInstance(preferences: Preferences) {
        this.preferences = preferences
    }

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

        run {
            val recentsObj: RecentsObject = JsonHandler.fromJson(
                    preferences.getString(PreferenceKeys.RECENT_GAMES, "{}"))
            recentsObj.distinctify()

            recents.clear()

            recentsObj.games.mapNotNullTo(recents) {
                GameRegistry.data.gameMap[it]
            }
        }
    }

    fun persist() {
        val obj = FavouritesObject()

        obj.games = games.filter { it.value }.map { it.key.id }
        obj.gameGroups = groups.filter { it.value }.map { it.key.name }

        preferences.putString(PreferenceKeys.FAVOURITES, JsonHandler.toJson(obj))
        preferences.putString(PreferenceKeys.RECENT_GAMES, JsonHandler.toJson(RecentsObject().apply {
            recents.mapTo(games as MutableList, Game::id)
        }))
        preferences.flush()
    }

}