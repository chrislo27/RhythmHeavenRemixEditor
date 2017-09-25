package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.registry.Series


class PickerSelection {

    var filter: Filter = SeriesFilter.allSeriesFilters[Series.TENGOKU] ?: error("Default filter not found")
        set(value) {
            field = value
            field.update()
        }

}