package io.github.chrislo27.rhre3.editor

import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.registry.datamodel.Datamodel


class PickerSelection {

    val seriesMap: MutableMap<Series, SeriesSelection> = mutableMapOf()

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
    }

    var currentSeries: Series = Series.TENGOKU
    val currentSelection: SeriesSelection
        get() = seriesMap.getOrPut(currentSeries, { SeriesSelection() })

}