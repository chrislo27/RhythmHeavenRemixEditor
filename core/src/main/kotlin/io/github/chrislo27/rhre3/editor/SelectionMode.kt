package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.math.Rectangle
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.toolboks.util.gdxutils.intersects


enum class SelectionMode {

    REPLACE {
        override fun createNewSelection(entities: List<Entity>, existingSelection: List<Entity>, selection: Rectangle): List<Entity> {
            return getNewCaptured(entities, selection)
        }

        override fun wouldEntityBeIncluded(entity: Entity, selection: Rectangle, entities: List<Entity>, existingSelection: List<Entity>): Boolean {
            return entity.bounds.intersects(selection)
        }
    },
    INVERT {
        override fun createNewSelection(entities: List<Entity>, existingSelection: List<Entity>, selection: Rectangle): List<Entity> {
            return mutableListOf<Entity>().also { list ->
                list.addAll(existingSelection)
                getNewCaptured(entities, selection).forEach {
                    if (it in list) {
                        list -= it
                    } else {
                        list += it
                    }
                }
            }
        }

        override fun wouldEntityBeIncluded(entity: Entity, selection: Rectangle, entities: List<Entity>, existingSelection: List<Entity>): Boolean {
            return entity.bounds.intersects(selection) xor (entity in existingSelection)
        }
    },
    ADD {
        override fun createNewSelection(entities: List<Entity>, existingSelection: List<Entity>, selection: Rectangle): List<Entity> {
            return (existingSelection.toList() + getNewCaptured(entities, selection)).distinct()
        }

        override fun wouldEntityBeIncluded(entity: Entity, selection: Rectangle, entities: List<Entity>, existingSelection: List<Entity>): Boolean {
            return entity.bounds.intersects(selection) || entity in existingSelection
        }
    };

    abstract fun createNewSelection(entities: List<Entity>, existingSelection: List<Entity>, selection: Rectangle): List<Entity>
    abstract fun wouldEntityBeIncluded(entity: Entity, selection: Rectangle, entities: List<Entity>, existingSelection: List<Entity>): Boolean

    protected fun getNewCaptured(entities: List<Entity>, selection: Rectangle): List<Entity> =
            entities.filter { it.bounds.intersects(selection) }

}