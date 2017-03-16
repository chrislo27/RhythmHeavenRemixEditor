package chrislo27.rhre.script.luaobj

import chrislo27.rhre.entity.Entity
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.track.Remix
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue


class LuaEntity(globals: Globals, remix: Remix, private val entity: Entity) : LuaObj(globals, remix) {

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