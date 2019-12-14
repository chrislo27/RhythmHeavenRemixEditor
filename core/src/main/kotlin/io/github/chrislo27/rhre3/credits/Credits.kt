package io.github.chrislo27.rhre3.credits

import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.sfxdb.SFXDatabase
import io.github.chrislo27.toolboks.i18n.Localization


object Credits {

    private val sfxCreditsFallback: String = listOf("Lvl100Feraligatr", "GenericArrangements", "Draster", "NP", "Eggman199", "Huebird", "Miracle22", "MF5K", "The Golden Station", "GuardedLolz", "GlitchyPSIX", "sp00pster", "Maxanum").joinToString(separator = ", ")

    fun generateList(): List<Credit> {
        return listOf(
                "title" crediting RHRE3.GITHUB,
                "programming" crediting "chrislo27\n${Localization["credits.title.programming.contributions", "Kamayana"]}",
                "localization" crediting
                        """[LIGHT_GRAY]Français (French)[]
                |Pengu123, minenice55, inkedsplat, chrislo27
                |
                |[LIGHT_GRAY]Español (Spanish)[]
                |GlitchyPSIX, quantic, SJGarnet, (◉.◉)☂, Killble, meuol, Cosmicfab, Suwa-ko
                |
                |[LIGHT_GRAY]Deutsch (German)[]
                |Zenon""".trimMargin(),
                "sfx" crediting (SFXDatabase.let { if (!it.isDataLoading()) it.data.sfxCredits.joinToString(separator = ", ") else null } ?: sfxCreditsFallback),
                "gfx" crediting "GlitchyPSIX, lilbitdun, Steppy, Tickflow",
                "specialThanks" crediting "baguette, GlitchyPSIX, Miracle22, Pengu123, TheRhythmKid, TheGarnet_, " +
                        "(◉.◉)☂, GrueKun, nerd, ChorusSquid, ArsenArsen, Clone5184, danthonywalker, jos, susmobile, " +
                        "Lvl100Feraligatr, SuicuneWiFi, Dracobot, AngryTapper, Zenon, inkedsplat, RobSetback, Mixelz, " +
                        "iRonnoc5, sp00pster, Alchemyking, SportaDerp9000, PikaMasterJesi, flyance, Draster, Malalaika, " +
                        "Rabbidking, Fringession, minenice55, Chillius, Mezian, EBPB2K, GinoTitan, bin5s5, " +
                        "garbo, nave, RHModding and Custom Remix Tourney Discord servers",
                "resources" crediting
                        """Rhythm Heaven assets by Nintendo
[#FF8900]Kotlin[]
[DARK_GRAY]lib[][#E10000]GDX[]
LWJGL
Toolboks
Beads
Async HTTP Client
Jackson
JGit
Apache Commons IO
SLF4J
OSHI
jump3r
musique (forked)
Segment
java-discord-rpc
bccad-editor
JCommander
SoundStretch and SoundTouch
zip4j""",
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

        val text: String = if (isTitle) RHRE3.TITLE_3 else Localization[localization]

    }

}