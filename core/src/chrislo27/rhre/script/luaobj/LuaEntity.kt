package chrislo27.rhre.script.luaobj

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity


class LuaEntity(private val luaRemix: LuaRemix, private val entity: Entity) {

	val beat: Float = entity.bounds.x
	val duration: Float = entity.bounds.width
	val track: Int = entity.bounds.y.toInt() + 1
	val id: String = entity.id
	val isPattern: Boolean = entity is PatternEntity
	val semitone: Int = entity.semitone

}