package io.github.chrislo27.rhre3.editor

import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


class PickerSelection {

    val seriesMap: MutableMap<Series, SeriesSelection> = mutableMapOf()
    private val searchSeriesSelection by lazy { SeriesSelection() }

    data class SeriesSelection(var group: Int = 0, var groupScroll: Int = 0,
                               val groups: MutableList<GameGroup> = mutableListOf(),
                               val variants: MutableMap<Int, VariantSelection> = mutableMapOf()) {

        val maxGroupScroll: Int
            get() {
                return ((groups.size / Editor.ICON_COUNT_X) - Editor.ICON_COUNT_Y).coerceAtLeast(0)
            }

        fun getVariant(index: Int): VariantSelection {
            if (index < 0)
                error("Negative index $index")
            return variants.getOrPut(index, { VariantSelection(groups[index]) })
        }

        fun getCurrentVariant(): VariantSelection =
                getVariant(group)

    }

    class VariantSelection(val group: GameGroup, var variant: Int = 0, var variantScroll: Int = 0) {
        val patterns: MutableMap<Int, Int> = mutableMapOf()

        val maxScroll: Int
            get() {
                return (group.games.size - Editor.ICON_COUNT_Y).coerceAtLeast(0)
            }

        val placeableObjects: List<Datamodel>
            get() {
                return group.games[variant].placeableObjects
            }

        val maxPatternScroll: Int
            get() {
                return (placeableObjects.size - 1).coerceAtLeast(0)
            }

        var pattern: Int
            get() {
                return patterns.getOrPut(variant, { 0 })
            }
            set(value) {
                patterns[variant] = value.coerceIn(0, maxPatternScroll)
            }

        fun getCurrentPlaceable(): Datamodel? {
            return placeableObjects.getOrNull(pattern)
        }
    }

    var currentSeries: Series = Series.TENGOKU
        set(value) {
            field = value
            isSearching = false
        }
    var isSearching: Boolean = false
    val currentSelection: SeriesSelection
        get() =
            if (isSearching)
                searchSeriesSelection
            else
                seriesMap.getOrPut(currentSeries, { SeriesSelection() })

}