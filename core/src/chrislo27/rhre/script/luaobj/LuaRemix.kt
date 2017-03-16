package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.Remix
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.jse.CoerceJavaToLua


class LuaRemix(private val luaRemix: LuaRemix, private val remix: Remix) {

	val cues: LuaTable
	val playbackStart: Float = remix.playbackStart
	val musicStart: Float = remix.musicStartTime
	val tempoChanges: LuaTable
	val length = remix.getDuration()
	val musicVolume = remix.musicVolume

	init {
		val eMap = mutableListOf<Pair<LuaValue, LuaValue>>()
		remix.entities.forEachIndexed { i, en ->
			eMap.add(Pair(CoerceJavaToLua.coerce(i + 1), CoerceJavaToLua.coerce(LuaEntity(this, en))))
		}
		cues = LuaValue.tableOf(eMap.map { it.first }.toTypedArray(), eMap.map { it.second }.toTypedArray())

		val tMap = mutableListOf<Pair<LuaValue, LuaValue>>()
		remix.tempoChanges.getBeatMap().values.forEachIndexed { index, tempoChange ->
			tMap.add(Pair(CoerceJavaToLua.coerce(index + 1),
						  CoerceJavaToLua.coerce(LuaTempoChange(this, tempoChange!!))))
		}
		tempoChanges = LuaValue.tableOf(tMap.map { it.first }.toTypedArray(), tMap.map { it.second }.toTypedArray())
	}

}