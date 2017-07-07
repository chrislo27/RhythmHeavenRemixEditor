package chrislo27.rhre.script

import chrislo27.rhre.registry.GameRegistry
import chrislo27.rhre.track.Remix
import org.luaj.vm2.Globals
import org.luaj.vm2.LuaValue
import org.luaj.vm2.compiler.LuaC
import org.luaj.vm2.lib.Bit32Lib
import org.luaj.vm2.lib.PackageLib
import org.luaj.vm2.lib.StringLib
import org.luaj.vm2.lib.TableLib
import org.luaj.vm2.lib.jse.JseBaseLib
import org.luaj.vm2.lib.jse.JseMathLib


object ScriptSandbox {

    fun runScriptInRemix(remix: Remix, script: String) {
        val chunk = parseChunk(remix, script)
        chunk.call()
    }

    fun parseChunk(remix: Remix, script: String): LuaValue {
        val globals = getBaseGlobals()

        globals.set("remix", remix.getLuaValue(globals))
        globals.set("registry", GameRegistry.luaValue)
//		globals.set("rhre2utils", LuaRHRE2Utils(globals, remix))

        LuaC.install(globals)

        return globals.load(script)
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