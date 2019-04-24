package io.github.chrislo27.rhre3.editor.picker

import io.github.chrislo27.rhre3.patternstorage.ClipboardStoredPattern
import io.github.chrislo27.rhre3.patternstorage.PatternStorage
import io.github.chrislo27.rhre3.patternstorage.StoredPattern


class StoredPatternsFilter : Filter() {

    val currentPattern: StoredPattern?
        get() = patternList.getOrNull(currentDatamodelList.currentIndex)
    private val patternList: MutableList<StoredPattern> = mutableListOf()
    override val currentDatamodelList: DatamodelList = DatamodelList()
    override val areDatamodelsEmpty: Boolean
        get() = currentDatamodelList.isEmpty

    override fun update() {
        gameGroups as MutableList
        gamesPerGroup as MutableMap
        datamodelsPerGame as MutableMap

        clearAll()
        patternList.clear()

        patternList += ClipboardStoredPattern
        PatternStorage.patterns.values.mapTo(patternList) { it }

        currentDatamodelList.list.apply {
            clear()
            patternList.mapTo(this, StoredPattern::datamodel)
        }
        currentDatamodelList.currentIndex = currentDatamodelList.currentIndex.coerceIn(0, (currentDatamodelList.list.size - 1).coerceAtLeast(0))
    }

}