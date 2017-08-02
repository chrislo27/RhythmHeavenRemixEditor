package io.github.chrislo27.rhre3.editor

import io.github.chrislo27.rhre3.registry.Series


class PickerSelection {

    val seriesMap: MutableMap<Series, SeriesSelection> = mutableMapOf()

    data class SeriesSelection(var group: Int = 0, var variant: Int = 0, var pattern: Int = 0)

    var currentSeries: Series = Series.TENGOKU
    val currentSelection: SeriesSelection
        get() = seriesMap.getOrPut(currentSeries, { SeriesSelection() })

}