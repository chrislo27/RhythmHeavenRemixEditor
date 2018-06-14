package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Series


class SeriesFilter(val series: Series) : SimpleFilter({ it.series == series }) {

    companion object {
        val allSeriesFilters: Map<Series, Filter> = Series.VALUES.associate {
            it to SeriesFilter(it)
        }
    }

}