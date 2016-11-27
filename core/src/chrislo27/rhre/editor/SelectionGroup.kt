package chrislo27.rhre.editor

import chrislo27.rhre.entity.Entity
import com.badlogic.gdx.math.Vector2

data class SelectionGroup(val list: List<Entity>, val oldPositions: List<Vector2>, val entityClickedOn: Entity, val offset: Vector2) {

	val relativePositions: List<Vector2> by lazy {
		val returning: MutableList<Vector2> = mutableListOf()

		list.forEachIndexed {i, e ->
			if (e === entityClickedOn) {

			}

			returning.add(i, Vector2(e.bounds.x - entityClickedOn.bounds.x, e.bounds.y - entityClickedOn.bounds.y))
		}

		return@lazy returning
	}

}
