package chrislo27.rhre.registry

import ionium.util.i18n.Localization
import org.luaj.vm2.LuaValue
import java.util.*

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