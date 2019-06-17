package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.sfxdb.Game


class FavouritesFilter : SimpleFilter(
        { it.isFavourited || it.games.any(Game::isFavourited) },
        gameFilter = { it.isFavourited || it.gameGroup.isFavourited }
                                     )