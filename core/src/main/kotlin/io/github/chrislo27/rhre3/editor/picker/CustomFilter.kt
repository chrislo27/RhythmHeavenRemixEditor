package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Game


class CustomFilter : SimpleFilter({ it.games.any(Game::isCustom) }, Game::isCustom)
