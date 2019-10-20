package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.sfxdb.Series


class SeriesFilter(val series: Series) : SimpleFilter({ it.series == series }) {

    companion object {
        val allSeriesFilters: Map<Series, SeriesFilter> = Series.VALUES.associateWith { SeriesFilter(it) }
    }

}