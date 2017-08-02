package io.github.chrislo27.rhre3.editor

import io.github.chrislo27.rhre3.registry.Series


class PickerSelection {

    val seriesMap: MutableMap<Series, SeriesSelection> = mutableMapOf()

    data class SeriesSelection(var group: Int = 0, var groupScroll: Int = 0,
                               val variants: MutableMap<Int, VariantSelection> = mutableMapOf()) {

        operator fun get(index: Int): VariantSelection {
            if (index < 0)
                error("Negative index $index")
            return variants.getOrPut(index, { VariantSelection() })
        }

    }

    data class VariantSelection(var variant: Int = 0, var variantScroll: Int = 0,
                                var pattern: Int = 0)

    var currentSeries: Series = Series.TENGOKU
    val currentSelection: SeriesSelection
        get() = seriesMap.getOrPut(currentSeries, { SeriesSelection() })

}