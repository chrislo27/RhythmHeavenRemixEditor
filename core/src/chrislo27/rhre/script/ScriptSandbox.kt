package chrislo27.rhre.script

import chrislo27.rhre.script.luaobj.LuaRemix
import chrislo27.rhre.track.Remix
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.Bit32Lib
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.CoerceJavaToLua
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib


object ScriptSandbox {

	fun runScriptInRemix(remix: Remix, script: String) {
		val globals = getBaseGlobals()

		val r: LuaValue = CoerceJavaToLua.coerce(LuaRemix(remix))
		println(r::class.java)
		globals.set("remix", r)

		LuaC.install(globals)

		val chunk: LuaValue = globals.load(script)
		chunk.call()
	}

	fun getBaseGlobals(): Globals {
		val globals = Globals()

		globals.load(JseBaseLib())
		globals.load(PackageLib())
		globals.load(Bit32Lib())
		globals.load(TableLib())
		globals.load(StringLib())
		globals.load(JseMathLib())

		return globals
	}

}