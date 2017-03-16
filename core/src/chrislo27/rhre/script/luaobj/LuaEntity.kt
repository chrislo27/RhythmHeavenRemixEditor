package chrislo27.rhre.script.luaobj

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue


class LuaEntity(private val luaRemix: LuaRemix, private val entity: Entity) : LuaTable() {

	val beat: Float = entity.bounds.x
	val duration: Float = entity.bounds.width
	val track: Int = entity.bounds.y.toInt() + 1
	val id: String = entity.id
	val isPattern: Boolean = entity is PatternEntity
	val semitone: Int = entity.semitone

	init {
		this.set("beat", beat.toDouble())
		this.set("duration", duration.toDouble())
		this.set("track", track)
		this.set("id", id)
		this.set("isPattern", LuaValue.valueOf(isPattern))
		this.set("semitone", semitone)
	}

}