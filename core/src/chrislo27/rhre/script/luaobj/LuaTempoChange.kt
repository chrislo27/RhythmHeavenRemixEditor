package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.Remix
import chrislo27.rhre.track.TempoChange
import org.luaj.vm2.Globals


class LuaTempoChange(globals: Globals, remix: Remix, private val tempoChange: TempoChange) : LuaObj(globals, remix) {

    val beat: Float = tempoChange.beat
    val seconds: Float = tempoChange.seconds
    val tempo: Float = tempoChange.tempo

    init {
        this.set("beat", beat.toDouble())
        this.set("seconds", seconds.toDouble())
        this.set("tempo", tempo.toDouble())
    }

}