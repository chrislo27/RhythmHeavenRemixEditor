package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.Remix
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaError
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction


class LuaRHRE2Utils(globals: Globals, remix: Remix) : LuaObj(globals, remix) {

	init {
		this.set("extractGame", object : OneArgFunction() {
			override fun call(arg: LuaValue): LuaValue {
				if (!arg.isstring())
					throw LuaError(arg)

				val s = arg.tostring().toString()

				if (s.indexOf('/') == -1 && s.indexOf('_') == -1)
					return LuaValue.NIL

				return LuaValue.valueOf(if (s.indexOf('/') != -1) s.substringBefore('/') else s.substringBefore('_'))
			}

		})
	}

}