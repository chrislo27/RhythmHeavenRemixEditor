package io.github.chrislo27.rhre3.registry.datamodel

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.toolboks.version.Version


data class Game(val id: String, val name: String, val series: Series, val requiresVersion: Version, val objects: List<Datamodel>,
                val icon: Texture, val group: String, val groupDefault: Boolean, val priority: Int) : Disposable {

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

        if (o1.group == o2.group) {
            if (o1.groupDefault) {
                return -1
            } else if (o2.groupDefault) {
                return 1
            }
        }

        return o1.name.compareTo(o2.name)
    }

}

fun MutableList<Game>.sortByName() {
    this.sortWith(GameByNameComparator)
}

fun List<Game>.sortedByName(): List<Game> {
    return this.sortedWith(GameByNameComparator)
}
