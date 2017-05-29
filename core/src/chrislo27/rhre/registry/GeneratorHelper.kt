package chrislo27.rhre.registry

import chrislo27.rhre.json.GameObject
import com.badlogic.gdx.files.FileHandle
import java.util.*


object GeneratorHelpers {

	val map: Map<String, GeneratorHelper> = mapOf(
			"manzaiBirds" to ManzaiBirdsGeneratorHelper(),
			"flipperFlopEn" to FlipperFlopGeneratorHelper("flipperFlopEn"),
			"bossaNovaEn" to BossaNovaGeneratorHelper("bossaNovaEn")
												 )

}

abstract class GeneratorHelper {

	abstract fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>)

}

class FlipperFlopGeneratorHelper(val id: String) : GeneratorHelper() {

	override fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>) {
		val addedCues: MutableList<SoundCue> = mutableListOf()

		addedCues.add(
				SoundCue("$id/ding", gameObject.gameID!!, "ogg",
						 "ding", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/flip", gameObject.gameID!!, "ogg",
						 "flip", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/flipB1", gameObject.gameID!!, "ogg",
						 "flip - back - 1", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/flipB2", gameObject.gameID!!, "ogg",
						 "flip - back - 2", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/uh1", gameObject.gameID!!, "ogg",
						 "uh - 1", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/uh2", gameObject.gameID!!, "ogg",
						 "uh - 2", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/uh3", gameObject.gameID!!, "ogg",
						 "uh - 3", mutableListOf(),
						 0.5f, false, 0f))

		run appreciation@ {
			addedCues.add(
					SoundCue("$id/appreciation/good", gameObject.gameID!!, "ogg",
							 "appreciation - good", mutableListOf(),
							 1f, false, 0f))
			addedCues.add(
					SoundCue("$id/appreciation/goodjob", gameObject.gameID!!, "ogg",
							 "appreciation - good job", mutableListOf(),
							 1f, false, 0f))
			addedCues.add(
					SoundCue("$id/appreciation/nice", gameObject.gameID!!, "ogg",
							 "appreciation - nice", mutableListOf(),
							 1f, false, 0f))
			addedCues.add(
					SoundCue("$id/appreciation/welldone", gameObject.gameID!!, "ogg",
							 "appreciation - well done!", mutableListOf(),
							 1f, false, 0f))
			addedCues.add(
					SoundCue("$id/appreciation/yes", gameObject.gameID!!, "ogg",
							 "appreciation - yes", mutableListOf(),
							 1f, false, 0f))
		}

		addedCues.add(
				SoundCue("$id/appreciation/thatsit1", gameObject.gameID!!, "ogg",
						 "that's it - that's", mutableListOf(),
						 0.5f, false, 0f))
		addedCues.add(
				SoundCue("$id/appreciation/thatsit2", gameObject.gameID!!, "ogg",
						 "that's it - it!", mutableListOf(),
						 0.5f, false, 0f))

		patterns.add(Pattern("${id}_flippingA", gameObject.gameID!!,
							 "flipping", false, mutableListOf(
				Pattern.PatternCue("$id/flip", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flip", id, 1f, 0, 0.5f, 0)
															 ), false,
							 mutableListOf()))
		patterns.add(Pattern("${id}_flippingB", gameObject.gameID!!,
							 "flipping (back)", false, mutableListOf(
				Pattern.PatternCue("$id/flipB1", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flipB2", id, 1f, 0, 0.5f, 0)
																	), false,
							 mutableListOf()))

		patterns.add(Pattern("${id}_thatsit", gameObject.gameID!!,
							 "that's it!", false, mutableListOf(
				Pattern.PatternCue("$id/appreciation/thatsit1", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/appreciation/thatsit2", id, 0.5f, 0, 0.5f, 0)
															   ), false,
							 mutableListOf()))

		patterns.add(Pattern("${id}_triple", gameObject.gameID!!,
							 "triple", false, mutableListOf(
				Pattern.PatternCue("$id/ding", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ding", id, 0.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ding", id, 1f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flip", id, 2f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flip", id, 2.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flip", id, 3f, 0, 0.5f, 0)
														   ), false,
							 mutableListOf()))
		patterns.add(Pattern("${id}_tripleBack", gameObject.gameID!!,
							 "triple (back)", false, mutableListOf(
				Pattern.PatternCue("$id/ding", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ding", id, 0.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ding", id, 1f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flipB1", id, 2f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flipB2", id, 2.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/flipB1", id, 3f, 0, 0.5f, 0)
																  ), false,
							 mutableListOf()))

		run attention@ {
			(1..8).mapTo(addedCues) {
				SoundCue("$id/attention/attention$it", gameObject.gameID!!, "ogg",
						 "attention - part $it", mutableListOf(),
						 if (it in 2..3 || it == 7) 0.5f else if (it == 4 || it == 8) 1f else 0.25f, false, 0f)
			}

			patterns.add(Pattern("${id}_attention", gameObject.gameID!!,
								 "attention... company!", false, mutableListOf(
					Pattern.PatternCue("$id/attention/attention1", id, 0f, 0, 0.25f, 0),
					Pattern.PatternCue("$id/attention/attention2", id, 0.25f, 0, 0.5f, 0),
					Pattern.PatternCue("$id/attention/attention3", id, 0.75f, 0, 0.5f, 0),
					Pattern.PatternCue("$id/attention/attention4", id, 1.25f, 0, 1f, 0),
					Pattern.PatternCue("$id/attention/attention5", id, 2.25f, 0, 0.25f, 0),
					Pattern.PatternCue("$id/attention/attention6", id, 2.5f, 0, 0.25f, 0),
					Pattern.PatternCue("$id/attention/attention7", id, 2.75f, 0, 0.5f, 0),
					Pattern.PatternCue("$id/attention/attention8", id, 3.25f, 0, 1f, 0)
																			  ), false,
								 mutableListOf()))
		}

		run count@ {
			val flipperRollCounts: String = """$id/count/flipperRollCount1
$id/count/flipperRollCount2
$id/count/flipperRollCount3
$id/count/flipperRollCount4
$id/count/flipperRollCount5
$id/count/flipperRollCount6
$id/count/flipperRollCount7
$id/count/flipperRollCount8
$id/count/flipperRollCount9
$id/count/flipperRollCount10
$id/count/flipperRollCountNow
$id/count/flipperRollCountA
$id/count/flipperRollCountB
$id/count/flipperRollCountC
$id/count/flipperRollCountS"""

			flipperRollCounts.lines().forEach {
				addedCues.add(
						SoundCue(it, gameObject.gameID!!, "ogg",
								 "count " + it.replace("$id/count/flipperRollCount", ""), mutableListOf(),
								 0.25f, false, 0f))
			}
			"""$id/count/flopCount1
$id/count/flopCount2
$id/count/flopCount3
$id/count/flopCount3B
$id/count/flopCount4
$id/count/flopCount4B""".lines().forEach {
				addedCues.add(
						SoundCue(it, gameObject.gameID!!, "ogg",
								 "flopping count " + it.replace("$id/count/flopCount", ""), mutableListOf(),
								 1f, false, 0f))
			}

			flipperRollCounts.lines().takeWhile { !it.endsWith("A") }.forEach {
				val type: String = it.replace("$id/count/flipperRollCount", "").toLowerCase(Locale.ROOT)
				val count: Int = if (type == "now") 1 else Integer.parseInt(type)
				val pluralize: Boolean = count != 1
				val patternCues = mutableListOf<Pattern.PatternCue>()

				patternCues.add(Pattern.PatternCue(it, id, 0f, 0, 0.25f, 0))

				patternCues.add(Pattern.PatternCue("$id/count/flipperRollCountA", id, 0.5f, 0, 0.25f, 0))
				patternCues.add(Pattern.PatternCue("$id/count/flipperRollCountB", id, 0.75f, 0, 0.25f, 0))
				patternCues.add(Pattern.PatternCue("$id/count/flipperRollCountC", id, 1f, 0, 0.25f, 0))
				if (pluralize) {
					patternCues.add(Pattern.PatternCue("$id/count/flipperRollCountS", id, 1.25f, 0, 0.25f, 0))
				}

				for (i in 1..count) {
					val num: String = if (i % 4 == 0) "4" else "${i % 4}"
					patternCues.add(Pattern.PatternCue(
							"$id/count/flopCount$num${if ((num == "3" || num == "4") && i > 4) "B" else ""}", id,
							1f + i, 0, 1f, 0))
				}

				patterns.add(Pattern("${id}_count_$type", gameObject.gameID!!,
									 "$type flipper roll${if (pluralize) "s" else ""}!", false, patternCues, false,
									 mutableListOf()))
			}
		}

		soundCues.addAll(addedCues)
	}

}

class BossaNovaGeneratorHelper(val id: String) : GeneratorHelper() {

	private val allCues: String = """$id/ball
$id/ball_echo
$id/ball_hard
$id/ball_low
$id/ball_pop
$id/click
$id/cloud1
$id/cloud2
$id/cube
$id/cube_echo
$id/cube_heavy
$id/female_bom1
$id/female_bom2
$id/female_bom3
$id/female_bom4
$id/female_bom5
$id/female_bom6
$id/female_bom7
$id/female_bom8
$id/female_bom9
$id/female_bom10
$id/female_bom_laugh
$id/female_cough1
$id/female_giggle1
$id/female_giggle2
$id/female_giggle3
$id/female_mmm
$id/female_pin1
$id/female_pin2
$id/female_pin3
$id/female_ss1
$id/female_ss2
$id/female_ss3
$id/female_yelp
$id/hack
$id/male_ahh1
$id/male_cough1
$id/male_d1
$id/male_d2
$id/male_d3
$id/male_don1
$id/male_don2
$id/male_don3
$id/male_don4
$id/male_don5
$id/male_don6
$id/male_don7
$id/male_don8
$id/male_don9
$id/male_don10
$id/male_don11
$id/male_don12
$id/male_grunt1
$id/male_mmm
$id/male_ooh1
$id/male_ss1
$id/male_ss2
$id/male_ss3
$id/male_t
$id/male_tt
$id/male_turn1
$id/male_turn2
$id/male_turn3
$id/male_turn4
$id/male_turn5
$id/male_turn6
$id/sniff
$id/ss
$id/thwack"""

	override fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>) {
		val addedCues: MutableList<SoundCue> = mutableListOf()
		val lines = allCues.lines()

		lines.forEach {
			addedCues.add(
					SoundCue(it, gameObject.gameID!!, "ogg",
							 it.replace("$id/", "")
									 .replace("_", " "),
							 mutableListOf(),
							 if (it.endsWith("cloud1")) {
								 1f
							 } else if (it.endsWith("cloud2")) {
								 2f
							 } else if (it.contains("bom") || it.contains("don")) {
								 1.5f
							 } else if (it.contains("turn") || it.contains("spin")) {
								 1f
							 } else {
								 0.5f
							 },
							 false,
							 0f))
		}

		patterns.add(Pattern("${id}_startPattern", gameObject.gameID!!,
							 "starting pattern", false, mutableListOf(
				Pattern.PatternCue("$id/ball", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/cube", id, 1f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ball", id, 1.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ball", id, 2f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/cube", id, 2.5f, 0, 0.5f, 0)
															), false,
							 mutableListOf()))
		patterns.add(Pattern("${id}_pattern", gameObject.gameID!!,
							 "pattern", false, mutableListOf(
				Pattern.PatternCue("$id/ball", id, 0f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ball", id, 0.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/cube", id, 1.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ball", id, 2f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/ball", id, 2.5f, 0, 0.5f, 0),
				Pattern.PatternCue("$id/cube", id, 3f, 0, 0.5f, 0)
															   ), false,
							 mutableListOf()))
		patterns.add(Pattern("${id}_cloudSpin", gameObject.gameID!!,
							 "cloud spin", false, mutableListOf(
				Pattern.PatternCue("$id/cloud1", id, 0f, 0, 1f, 0),
				Pattern.PatternCue("$id/cloud2", id, 1f, 0, 2f, 0)
															   ), false,
							 mutableListOf()))

		soundCues.addAll(addedCues)
	}

}

class ManzaiBirdsGeneratorHelper : GeneratorHelper() {

	val paths: String = """manzaiBirds/aichini_aichinna
manzaiBirds/amette_amena
manzaiBirds/chainani_nicchaina
manzaiBirds/denwari_denwa
manzaiBirds/domakiyo_otonokoi
manzaiBirds/futonga_futtonda
manzaiBirds/hiromega_hiromeida
manzaiBirds/houchaga_houchou
manzaiBirds/igakari_katta
manzaiBirds/ikugawa_ikura
manzaiBirds/kaero_burikaeru
manzaiBirds/karega_kare
manzaiBirds/kusaga_kusai
manzaiBirds/megaminiwa_megani
manzaiBirds/mikanga_mikannai
manzaiBirds/nekoga_nekoronda
manzaiBirds/okanewa_okkane
manzaiBirds/okurezu_kitte_okure
manzaiBirds/omochino_kimochi
manzaiBirds/omoino_hoka_omoi
manzaiBirds/puringa_tappurin
manzaiBirds/rakugawa_rakugana
manzaiBirds/roukada_katarouka
manzaiBirds/saiyo_minasai
manzaiBirds/sakana_kana_masakana
manzaiBirds/saruga_saru
manzaiBirds/shaiinni_nanari_nashain
manzaiBirds/suikawa_yasuika
manzaiBirds/taiga_tabetaina
manzaiBirds/tainini_kittai
manzaiBirds/taiyo_gami_taiyou
manzaiBirds/toireni_ittoire
manzaiBirds/torinikuga_torininkui
manzaiBirds/umette_umena"""

	override fun process(gameFh: FileHandle, gameObject: GameObject, patterns: MutableList<Pattern>,
						 soundCues: MutableList<SoundCue>) {
		val addedCues: MutableList<SoundCue> = mutableListOf()
		val lines = paths.lines()

		lines.forEach {
			addedCues.add(
					SoundCue(it, gameObject.gameID!!, "ogg", it.replace("manzaiBirds/", "").replace("_", " "),
							 mutableListOf(), 2.5f, false,
							 0f, true, null, 95f, false, null))
		}

		addedCues.forEach {

			var patternCues = mutableListOf<Pattern.PatternCue>()

			patternCues.add(Pattern.PatternCue(it.id, gameObject.gameID!!, 0f, 0, 2.5f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/haihai1", gameObject.gameID!!, 2.5f, 0, 0f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/haihai1", gameObject.gameID!!, 3.0f, 0, 0f, 0))

			patterns.add(Pattern("manzaiBirds_" + it.id, gameObject.gameID!!, it.name, false, patternCues, false,
								 mutableListOf()))

			// -----------------------------------------------------------

			patternCues = mutableListOf<Pattern.PatternCue>()

			patternCues.add(Pattern.PatternCue(it.id, gameObject.gameID!!, 0f, 0, 1.5f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/boing", gameObject.gameID!!, 1.5f, 0, 0f, 0))
			patternCues.add(Pattern.PatternCue("manzaiBirds/donaiyanen", gameObject.gameID!!, 2.5f, 0, 0f, 0))

			patterns.add(Pattern("manzaiBirds_" + it.id + "_boing", gameObject.gameID!!, it.name + " BOING!", false,
								 patternCues, false, mutableListOf()))

		}

		soundCues.addAll(addedCues)
	}

}
