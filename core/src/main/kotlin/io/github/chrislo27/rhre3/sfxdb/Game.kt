package io.github.chrislo27.rhre3.sfxdb

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.DatamodelComparator
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel


data class Game(val id: String, val name: String, val series: Series,
                val objects: List<Datamodel>,
                val icon: Texture, val group: String, val groupDefault: Boolean,
                val priority: Int, val isCustom: Boolean, val noDisplay: Boolean, val searchHints: List<String>,
                val jsonless: Boolean, val isSpecial: Boolean)
    : Disposable, Comparable<Game> {

    val placeableObjects: List<Datamodel> by lazy {
        objects.filter { !it.hidden }.sortedWith(DatamodelComparator)
    }
    val hasCallAndResponse: Boolean by lazy {
        placeableObjects.any { it is ResponseModel && it.responseIDs.isNotEmpty() }
    }
    val objectsMap: Map<String, Datamodel> by lazy {
        objects.associateBy { it.id } + objects.filter { it.deprecatedIDs.isNotEmpty() }.flatMap { it.deprecatedIDs.map {dep -> dep to it} }
    }
    val placeableObjectsMap: Map<String, Datamodel> by lazy {
        placeableObjects.associateBy { it.id } + objects.filter { it.deprecatedIDs.isNotEmpty() }.flatMap { it.deprecatedIDs.map {dep -> dep to it} }
    }

    val gameGroup: GameGroup
        get() = GameRegistry.data.gameGroupsMap[group] ?: error("No valid game group for $id with group $group")

    val isFavourited: Boolean
        get() = GameMetadata.isGameFavourited(this)
    val recency: Int
        get() = GameMetadata.recents.indexOf(this)
    val isRecent: Boolean
        get() = recency != -1

    override fun compareTo(other: Game): Int {
        return GameGroupListComparator.compare(this, other)
    }

    override fun dispose() {
        objects.forEach(Disposable::dispose)
    }
}

object GameByNameComparator : Comparator<Game> {

    override fun compare(o1: Game?, o2: Game?): Int {
        if (o1 == null && o2 == null) {
            return 0
        } else if (o1 == null) {
            return -1
        } else if (o2 == null) {
            return 1
        }

        // higher priorities are first
        if (o1.priority > o2.priority) {
            return -1
        } else if (o2.priority > o1.priority) {
            return 1
        }

        return o1.name.compareTo(o2.name)
    }

}

object GameGroupListComparatorIgnorePriority : Comparator<Game> {

    override fun compare(o1: Game?, o2: Game?): Int {
        if (o1 == null && o2 == null) {
            return 0
        } else if (o1 == null) {
            return -1
        } else if (o2 == null) {
            return 1
        }

        if (o1.group == o2.group) {
            return when {
                o1.groupDefault -> -1
                o2.groupDefault -> 1
                else -> o1.id.compareTo(o2.id)
            }
        }

        return o1.id.compareTo(o2.id)
    }

}

object GameGroupListComparator : Comparator<Game> {

    override fun compare(o1: Game?, o2: Game?): Int {
        if (o1 == null && o2 == null) {
            return 0
        } else if (o1 == null) {
            return -1
        } else if (o2 == null) {
            return 1
        }

        // higher priorities are first
        if (o1.priority > o2.priority) {
            return -1
        } else if (o2.priority > o1.priority) {
            return 1
        }

        if (o1.group == o2.group) {
            return when {
                o1.groupDefault -> -1
                o2.groupDefault -> 1
                else -> o1.id.compareTo(o2.id)
            }
        }

        return o1.id.compareTo(o2.id)
    }

}

fun MutableList<Game>.sortByName() {
    this.sortWith(GameByNameComparator)
}

fun List<Game>.sortedByName(): List<Game> {
    return this.sortedWith(GameByNameComparator)
}
