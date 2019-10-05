package io.github.chrislo27.rhre3.sfxdb

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.sfxdb.datamodel.Datamodel
import io.github.chrislo27.rhre3.sfxdb.datamodel.DatamodelComparator
import io.github.chrislo27.rhre3.sfxdb.datamodel.ResponseModel
import io.github.chrislo27.toolboks.registry.AssetRegistry


data class Game(val id: String, val rawName: String, val series: Series,
                val objects: List<Datamodel>,
                val iconFh: FileHandle, val language: Language?, val group: String, val groupDefault: Boolean,
                val priority: Int, val isCustom: Boolean, val noDisplay: Boolean, val searchHints: List<String>,
                val jsonless: Boolean, val isSpecial: Boolean)
    : Disposable, Comparable<Game> {

    val name: String = if (language != null) "$rawName (${language.langName})" else rawName

    val placeableObjects: List<Datamodel> by lazy {
        objects.filter { !it.hidden }.sortedWith(DatamodelComparator)
    }
    val hasCallAndResponse: Boolean by lazy {
        placeableObjects.any { it is ResponseModel && it.responseIDs.isNotEmpty() }
    }
    val objectsMap: Map<String, Datamodel> by lazy {
        objects.associateBy { it.id } + objects.filter { it.deprecatedIDs.isNotEmpty() }.flatMap { it.deprecatedIDs.map { dep -> dep to it } }
    }
    val placeableObjectsMap: Map<String, Datamodel> by lazy {
        placeableObjects.associateBy { it.id } + objects.filter { it.deprecatedIDs.isNotEmpty() }.flatMap { it.deprecatedIDs.map { dep -> dep to it } }
    }

    val gameGroup: GameGroup
        get() = SFXDatabase.data.gameGroupsMap[group] ?: error("No valid game group for $id with group $group")

    val isFavourited: Boolean
        get() = GameMetadata.isGameFavourited(this)
    val recency: Int
        get() = GameMetadata.recents.indexOf(this)
    val isRecent: Boolean
        get() = recency != -1

    val icon: Texture = if (language != null && !(id.startsWith("countIn") && id.length == 9 /* Count-In games don't get a language code baked in */)) {
        // Add on language code
        val pixmap = Pixmap(if (iconFh.exists()) iconFh else FileHandle(AssetRegistry.assetMap.getValue("sfxdb_missing_icon")))
        // Draw language code
        val langPixmap: Pixmap = AssetRegistry["sfxdb_langicon_${language.code}_pixmap"]
        pixmap.drawPixmap(langPixmap, 0, 0, langPixmap.width, langPixmap.height, 0, 0, pixmap.width, pixmap.height)
        val newTex = Texture(pixmap)
        pixmap.dispose()
        newTex
    } else if (!iconFh.exists()) {
        AssetRegistry["sfxdb_missing_icon"]
    } else {
        Texture(iconFh)
    }

    override fun compareTo(other: Game): Int {
        return GameGroupListComparator.compare(this, other)
    }

    override fun dispose() {
        objects.forEach(Disposable::dispose)
        if (!icon.isManaged) {
            icon.dispose()
        }
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
