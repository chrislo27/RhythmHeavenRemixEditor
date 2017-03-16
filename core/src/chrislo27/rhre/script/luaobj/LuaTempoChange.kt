package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.TempoChange


class LuaTempoChange(private val luaRemix: LuaRemix, private val tempoChange: TempoChange) {

	val beat: Float = tempoChange.beat
	val seconds: Float = tempoChange.seconds
	val tempo: Float = tempoChange.tempo

}