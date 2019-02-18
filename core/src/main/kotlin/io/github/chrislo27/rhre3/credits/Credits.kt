package io.github.chrislo27.rhre3.credits

import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.toolboks.i18n.Localization


object Credits {

    private val sfxCreditsFallback: String = "baguette, Huebird, Miracle22, Eggman199, Mariofan5000, The Golden Station, GuardedLolz, GlitchyPSIX, sp00pster, Lvl100Feraligatr, WaluigiTime64, Draster, Maxanum"

    fun generateList(): List<Credit> {
        return listOf(
                "title" crediting RHRE3.GITHUB,
                "programming" crediting "chrislo27",
                "localization" crediting
                        """[LIGHT_GRAY]Français (French)[]
                |Pengu123, minenice55, inkedsplat
                |
                |[LIGHT_GRAY]Español (Spanish)[]
                |GlitchyPSIX, quantic, SJGarnet, (◉.◉)☂, Killble
                |
                |[LIGHT_GRAY]Deutsch (German)[]
                |Zenon""".trimMargin(),
                "sfx" crediting (GameRegistry.let { if (!it.isDataLoading()) it.data.sfxCredits.joinToString(separator = ", ") else null } ?: sfxCreditsFallback),
                "gfx" crediting "GlitchyPSIX, lilbitdun, Steppy, Tickflow",
                "specialThanks" crediting "baguette, GlitchyPSIX, Miracle22, Pengu123, TheRhythmKid, TheGarnet_, " +
                        "(◉.◉)☂, GrueKun, nerd, ChorusSquid, ArsenArsen, Clone5184, danthonywalker, jos, susmobile, " +
                        "Lvl100Feraligatr, SuicuneWiFi, Dracobot, AngryTapper, Zenon, inkedsplat, RobSetback, Mixelz, " +
                        "iRonnoc5, sp00pster, Alchemyking, SportaDerp9000, PikaMasterJesi, flyance, Draster, Malalaika, " +
                        "Rabbidking, Fringession, minenice55, RHModding Discord server",
                "resources" crediting
                        "Rhythm Heaven assets by Nintendo\n" +
                        "[#FF8900]Kotlin[]\n" +
                        "[DARK_GRAY]lib[][#E10000]GDX[]\n" +
                        "LWJGL\n" +
                        "Toolboks\n" +
                        "Beads\n" +
                        "Async HTTP Client\n" +
                        "Jackson\n" +
                        "JGit\n" +
                        "Apache Commons IO\n" +
                        "SLF4J\n" +
                        "OSHI\n" +
                        "jump3r\n" +
                        "musique (forked)\n" +
                        "Segment\n" +
                        "java-discord-rpc\n" +
                        "bccad-editor",
                "donators" crediting "",
                "you" crediting ""
                     )
    }

    private infix fun String.crediting(persons: String): Credit =
            Credit(this, persons)

    data class Credit(val type: String, val persons: String) {

        private val localization: String by lazy {
            "credits.title.$type"
        }
        private val isTitle by lazy { type == "title" }

        val text: String
            get() = if (isTitle) RHRE3.TITLE_3 else Localization[localization]

    }

}