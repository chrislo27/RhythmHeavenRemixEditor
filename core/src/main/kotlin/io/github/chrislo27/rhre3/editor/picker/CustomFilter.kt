package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Series


class CustomFilter : SimpleFilter(
        {
            it.series == Series.CUSTOM || it.games.any { it.isCustom || it.series == Series.CUSTOM }
        },
        { it.isCustom || it.series == Series.CUSTOM }
                                 )
