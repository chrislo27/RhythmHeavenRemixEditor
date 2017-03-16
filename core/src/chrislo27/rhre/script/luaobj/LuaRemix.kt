package chrislo27.rhre.script.luaobj

import chrislo27.rhre.track.Remix


class LuaRemix(private val remix: Remix) {

	fun hello() {
		println("hello from lua hopefully")
	}

	fun hello(x: Int) {
		println("int hello " + x)
	}

}