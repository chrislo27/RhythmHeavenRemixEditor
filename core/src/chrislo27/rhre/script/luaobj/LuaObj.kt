package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.Remix
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaTable


open class LuaObj(internal val globals: Globals, internal val remix: Remix) : LuaTable() {

	internal fun resetRemixGlobal() {
		globals.set("remix", remix.getLuaValue(globals))
	}

}