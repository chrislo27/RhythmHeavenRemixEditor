package chrislo27.rhre.registry

import ionium.util.i18n.Localization
import org.luaj.vm2.LuaValue
import java.util.*


data class Game(val id: String, val name: String, val soundCues: List<SoundCue>,
				val patterns: List<Pattern>, val series: Series, val icon: String?,
				val iconIsRawPath: Boolean = false) {

	fun isCustom() = series == Series.CUSTOM

	fun getPattern(id: String): Pattern? {
		return patterns.find { it.id == "${this.id}_$id" }
	}

	fun getCue(id: String): SoundCue? {
		return soundCues.find { it.id == "${this.id}/$id" }
	}

	val luaValue: LuaValue by lazy {
		val l = LuaValue.tableOf()

		l.set("id", id)
		l.set("name", name)
		l.set("series", series.luaValue)
		l.set("cues", LuaValue.listOf(soundCues.map { it.luaValue }.toTypedArray()))
		l.set("patterns", LuaValue.listOf(patterns.map { it.luaValue }.toTypedArray()))

		return@lazy l
	}

}

enum class Series(val i10nKey: String) {

	UNKNOWN("other"), TENGOKU("tengoku"), DS("ds"),
	FEVER("fever"), MEGAMIX("megamix"), SIDE("side"),
	CUSTOM("custom");

	companion object {
		@JvmField
		val values: Array<Series> = values()
	}

	fun getLocalizedName(): String = Localization.get("series." + i10nKey)

	val luaValue: LuaValue by lazy {
		LuaValue.valueOf(i10nKey.toUpperCase(Locale.ROOT))
	}

}