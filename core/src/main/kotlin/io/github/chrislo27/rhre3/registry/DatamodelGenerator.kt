package io.github.chrislo27.rhre3.registry

import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.registry.datamodel.impl.*
import io.github.chrislo27.rhre3.registry.json.DataObject
import java.util.*


abstract class DatamodelGenerator {

    companion object {

        val generators: Map<String, DatamodelGenerator> =
                mapOf(
                        "flipperFlopEn" to FlipperFlopGenerator("flipperFlopEn"),
                        "bossaNovaEn" to BossaNovaGenerator("bossaNovaEn"),
                        "manzaiBirds" to ManzaiBirdsGenerator()
                     )

    }

    abstract fun process(folder: FileHandle, dataObject: DataObject, game: Game)

}

class FlipperFlopGenerator(val id: String) : DatamodelGenerator() {

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        game.objects as MutableList

        listOf(
                "$id/ding" to "ding",
                "$id/flip" to "flip",
                "$id/flipB1" to "flip - back - 1",
                "$id/flipB2" to "flip - back - 2",
                "$id/roll" to "roll",
                "$id/uh1" to "uh - 1",
                "$id/uh2" to "uh - 2",
                "$id/uh3" to "uh - 3"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        0.5f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        listOf(
                "$id/appreciation/good" to "appreciation - good",
                "$id/appreciation/goodjob" to "appreciation - good job",
                "$id/appreciation/nice" to "appreciation - nice",
                "$id/appreciation/welldone" to "appreciation - well done!",
                "$id/appreciation/yes" to "appreciation - yes"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        1f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        listOf(
                "$id/appreciation/thatsit1" to "that's it - that's",
                "$id/appreciation/thatsit2" to "that's it - it!"
              )
                .mapTo(game.objects) {
                    Cue(game, it.first, mutableListOf(), it.second,
                        0.5f, false, false,
                        GameRegistry.SFX_FOLDER.child(
                                it.first + ".ogg"),
                        null, null, mutableListOf(), 0f, false)
                }

        game.objects +=
                KeepTheBeat(game, "${id}_flippingA", mutableListOf(),
                            "flipping", 2f,
                            mutableListOf(
                                    CuePointer(
                                            "$id/flip", 0f, duration = 1f),
                                    CuePointer(
                                            "$id/flip", 1f, duration = 1f)
                                         ))
        game.objects +=
                KeepTheBeat(game, "${id}_flippingB", mutableListOf(),
                            "flipping - back", 2f,
                            mutableListOf(
                                    CuePointer(
                                            "$id/flipB1", 0f, duration = 1f),
                                    CuePointer(
                                            "$id/flipB2", 1f, duration = 1f)
                                         ))
        game.objects +=
                Pattern(game, "${id}_thatsit", mutableListOf(),
                        "that's it!",
                        mutableListOf(
                                CuePointer(
                                        "$id/appreciation/thatsit1",
                                        0f),
                                CuePointer(
                                        "$id/appreciation/thatsit2",
                                        0.5f)
                                     ), false)

        game.objects +=
                Pattern(game, "${id}_triple", mutableListOf(),
                        "triple",
                        mutableListOf(
                                CuePointer(
                                        "$id/ding", 0f),
                                CuePointer(
                                        "$id/ding", 0.5f),
                                CuePointer(
                                        "$id/ding", 1f),
                                CuePointer(
                                        "$id/flip", 2f),
                                CuePointer(
                                        "$id/flip", 2.5f),
                                CuePointer(
                                        "$id/flip", 3f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_tripleBack", mutableListOf(),
                        "triple - back",
                        mutableListOf(
                                CuePointer(
                                        "$id/ding", 0f),
                                CuePointer(
                                        "$id/ding", 0.5f),
                                CuePointer(
                                        "$id/ding", 1f),
                                CuePointer(
                                        "$id/flipB1", 2f),
                                CuePointer(
                                        "$id/flipB2", 2.5f),
                                CuePointer(
                                        "$id/flipB1", 3f)
                                     ), false)

        run attention@{
            (1..8).mapTo(game.objects) {
                Cue(game, "$id/attention/attention$it",
                    mutableListOf(),
                    "attention - part $it",
                    if (it in 2..3 || it == 7) 0.5f else if (it == 4 || it == 8) 1f else 0.25f,
                    false, false,
                    GameRegistry.SFX_FOLDER.child("$id/attention/attention$it.ogg"),
                    null, null, mutableListOf(), 0f, false)
            }

            game.objects +=
                    Pattern(game, "${id}_attention", mutableListOf(),
                            "attention... company!",
                            mutableListOf(
                                    CuePointer(
                                            "$id/attention/attention1", 0.0f),
                                    CuePointer(
                                            "$id/attention/attention2", 0.25f),
                                    CuePointer(
                                            "$id/attention/attention3", 0.75f),
                                    CuePointer(
                                            "$id/attention/attention4", 1.25f),
                                    CuePointer(
                                            "$id/attention/attention5", 2.25f),
                                    CuePointer(
                                            "$id/attention/attention6", 2.5f),
                                    CuePointer(
                                            "$id/attention/attention7", 2.75f),
                                    CuePointer(
                                            "$id/attention/attention8", 3.25f)
                                         ), false)
        }

        run count@{
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
                game.objects +=
                        Cue(game, it, mutableListOf(),
                            "count ${it.replace(
                                    "$id/count/flipperRollCount",
                                    "")}",
                            0.25f, false, false,
                            GameRegistry.SFX_FOLDER.child("$it.ogg"),
                            null, null, mutableListOf(), 0f, false)
            }
            """$id/count/flopCount1
$id/count/flopCount1B
$id/count/flopCount2
$id/count/flopCount2B
$id/count/flopCount3
$id/count/flopCount3B
$id/count/flopCount4
$id/count/flopCount4B""".lines().forEach {
                game.objects +=
                        Cue(game, it, mutableListOf(),
                            "flopping count ${it.replace(
                                    "$id/count/flopCount", "")}",
                            1f, false, false,
                            GameRegistry.SFX_FOLDER.child("$it.ogg"),
                            null, null, mutableListOf(), 0f, false)
            }

            flipperRollCounts.lines().takeWhile { !it.endsWith("A") }.forEach {
                val type: String = it.replace("$id/count/flipperRollCount", "").toLowerCase(Locale.ROOT)
                val count: Int = if (type == "now") 1 else Integer.parseInt(type)
                val pluralize: Boolean = count != 1
                val patternCues = mutableListOf<CuePointer>()

                patternCues +=
                        CuePointer(it, 0f)

                patternCues +=
                        CuePointer("$id/count/flipperRollCountA",
                                   0.5f)
                patternCues +=
                        CuePointer("$id/count/flipperRollCountB",
                                   0.75f)
                patternCues +=
                        CuePointer("$id/count/flipperRollCountC", 1f)

                if (pluralize) {
                    patternCues +=
                            CuePointer("$id/count/flipperRollCountS",
                                       1.25f)
                }

                for (i in 1..count) {
                    val num: String = if (i % 4 == 0) "4" else "${i % 4}"
                    patternCues +=
                            CuePointer(
                                    "$id/count/flopCount$num${if (i > 4) "B" else ""}", 1f + i)
                }

                game.objects +=
                        Pattern(game, "${id}_count_$type",
                                mutableListOf(),
                                "$type flipper roll${if (pluralize) "s" else ""}!",
                                patternCues, false)
            }
        }
    }

}

class BossaNovaGenerator(val id: String) : DatamodelGenerator() {

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

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        val lines = allCues.lines()

        game.objects as MutableList

        lines.forEach {
            game.objects +=
                    Cue(game, it, mutableListOf(),
                        it.replace("$id/", "").replace("_", " "),
                        if (it.endsWith("cloud1")) {
                            1f
                        } else if (it.endsWith("cloud2")) {
                            2f
                        } else if (it.contains("bom") || it.contains(
                                        "don")) {
                            1.5f
                        } else if (it.contains("turn") || it.contains(
                                        "spin")) {
                            1f
                        } else {
                            0.5f
                        },
                        false, false,
                        GameRegistry.SFX_FOLDER.child("$it.ogg"),
                        null, null, mutableListOf(), 0f, false)
        }

        game.objects +=
                Pattern(game, "${id}_startPattern", mutableListOf(),
                        "starting pattern",
                        mutableListOf(
                                CuePointer(
                                        "$id/ball", 0f),
                                CuePointer(
                                        "$id/cube", 1f),
                                CuePointer(
                                        "$id/ball", 1.5f),
                                CuePointer(
                                        "$id/ball", 2f),
                                CuePointer(
                                        "$id/cube", 2.5f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_pattern", mutableListOf(),
                        "pattern",
                        mutableListOf(
                                CuePointer(
                                        "$id/ball", 0f),
                                CuePointer(
                                        "$id/ball", 0.5f),
                                CuePointer(
                                        "$id/cube", 1.5f),
                                CuePointer(
                                        "$id/ball", 2f),
                                CuePointer(
                                        "$id/ball", 2.5f),
                                CuePointer(
                                        "$id/cube", 3f)
                                     ), false)
        game.objects +=
                Pattern(game, "${id}_cloudSpin", mutableListOf(),
                        "cloud spin",
                        mutableListOf(
                                CuePointer(
                                        "$id/cloud1", 0f),
                                CuePointer(
                                        "$id/cloud2", 1f)
                                     ), false)
        game.objects += Pattern(game, "${id}_femalePattern1", mutableListOf(), "pattern - female - 1",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom1", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss2", 2.5f, track = 0),
                                              CuePointer("${id}/female_bom2", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern2", mutableListOf(), "pattern - female - 2",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom3", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss2", 2.5f, track = 0),
                                              CuePointer("${id}/female_bom4", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern3", mutableListOf(), "pattern - female - 3",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom10", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss3", 2.5f, track = 0),
                                              CuePointer("${id}/female_bom6", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern4", mutableListOf(), "pattern - female - 4",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom_laugh", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss2", 2.5f, track = 0),
                                              CuePointer("${id}/female_bom3", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern5", mutableListOf(), "pattern - female - 5",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom7", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss2", 2.5f, track = 0),
                                              CuePointer("${id}/female_pin3", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern6", mutableListOf(), "pattern - female - 6",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom9", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss3", 2.5f, track = 0),
                                              CuePointer("${id}/female_pin3", 3.0f, track = 2)), false)
        game.objects += Pattern(game, "${id}_femalePattern7", mutableListOf(), "pattern - female - 7",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/female_ss1", 0.5f, track = 0),
                                              CuePointer("${id}/female_bom10", 1.5f, track = 2),
                                              CuePointer("${id}/female_ss1", 2.0f, track = 0),
                                              CuePointer("${id}/female_ss3", 2.5f, track = 0),
                                              CuePointer("${id}/female_pin2", 3.0f, track = 2)), false)

        game.objects += Pattern(game, "${id}_malePattern1", mutableListOf(), "pattern - male - 1",
                                mutableListOf(CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_don1", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don4", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern2", mutableListOf(), "pattern - male - 2",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don3", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don4", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern3", mutableListOf(), "pattern - male - 3",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don6", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don2", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern4", mutableListOf(), "pattern - male - 4",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don2", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don8", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern5", mutableListOf(), "pattern - male - 5",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don9", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don11", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern6", mutableListOf(), "pattern - male - 6",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don7", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_d1", 2.0f, track = 2),
                                              CuePointer("${id}/male_don12", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern7", mutableListOf(), "pattern - male - 7",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don1", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_t", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn1", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern8", mutableListOf(), "pattern - male - 8",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don3", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_tt", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn2", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern9", mutableListOf(), "pattern - male - 9",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don4", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_t", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn3", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern10", mutableListOf(), "pattern - male - 10",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don3", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_t", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn4", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern11", mutableListOf(), "pattern - male - 11",
                                mutableListOf(CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_don3", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_t", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn5", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
        game.objects += Pattern(game, "${id}_malePattern12", mutableListOf(), "pattern - male - 12",
                                mutableListOf(CuePointer("${id}_pattern", 0.0f, track = 1),
                                              CuePointer("${id}/male_d3", 0.0f, track = 2),
                                              CuePointer("${id}/male_don9", 0.5f, track = 2),
                                              CuePointer("${id}/male_ss3", 1.5f, track = 0),
                                              CuePointer("${id}/male_tt", 2.0f, track = 2),
                                              CuePointer("${id}/male_turn6", 2.5f, track = 2),
                                              CuePointer("${id}/male_ss2", 3.5f, track = 0)), false)
    }

}

class ManzaiBirdsGenerator : DatamodelGenerator() {

    val paths: String = """manzaiBirds/aichini_aichinna
manzaiBirds/amette_amena
manzaiBirds/chainani_itchaina
manzaiBirds/denwari_denwa
manzaiBirds/futonga_futtonda
manzaiBirds/hiromega_hiromeida
manzaiBirds/ikaga_okotta
manzaiBirds/ikugawa_ikura
manzaiBirds/kaeroga_furikaeru
manzaiBirds/karega_kare
manzaiBirds/koucha_o_koutchau
manzaiBirds/kusaga_kusai
manzaiBirds/megane_niwa_meganei
manzaiBirds/mikanga_mikkannai
manzaiBirds/nekoga_nekoronda
manzaiBirds/okanewa_okkane
manzaiBirds/okurezu_kite_okure
manzaiBirds/omochino_kimochi
manzaiBirds/omoino_hoka_omoi
manzaiBirds/puringa_tappurin
manzaiBirds/rakudawa_rakudana
manzaiBirds/roukade_katarouka
manzaiBirds/saio_minasai
manzaiBirds/sakana_kana_masakana
manzaiBirds/saruga_saru
manzaiBirds/shaiinni_nanari_nashain
manzaiBirds/suikawa_yasuika
manzaiBirds/taiga_tabetaina
manzaiBirds/taini_ikitai
manzaiBirds/taiyouga_mitaiyou
manzaiBirds/toireni_ittoire
manzaiBirds/tonakaiwa_otonakai
manzaiBirds/torinikuga_torininkui
manzaiBirds/umette_umeina"""
    val deprecations: Map<String, String> = mapOf(
            "manzaiBirds/mikanga_mikkannai" to "manzaiBirds/mikanga_mikannai",
            "manzaiBirds/rakudawa_rakudana" to "manzaiBirds/rakugawa_rakugana",
            "manzaiBirds/umette_umeina" to "manzaiBirds/umette_umena",
            "manzaiBirds/koucha_o_koutchau" to "manzaiBirds/houchaga_houchou",
            "manzaiBirds/kaeroga_furikaeru" to "manzaiBirds/kaero_burikaeru",
            "manzaiBirds/okurezu_kite_okure" to "manzaiBirds/okurezu_kitte_okure",
            "manzaiBirds/roukade_katarouka" to "manzaiBirds/roukada_katarouka",
            "manzaiBirds/chainani_itchaina" to "manzaiBirds/chainani_nicchaina",
            "manzaiBirds/taini_ikitai" to "manzaiBirds/tainini_kittai",
            "manzaiBirds/tonakaiwa_otonakai" to "manzaiBirds/domakiyo_otonokoi",
            "manzaiBirds/saio_minasai" to "manzaiBirds/saiyo_minasai",
            "manzaiBirds/taiyouga_mitaiyou" to "manzaiBirds/taiyo_gami_taiyou",
            "manzaiBirds/megane_niwa_meganei" to "manzaiBirds/megaminiwa_megani",
            "manzaiBirds/ikaga_okotta" to "manzaiBirds/igakari_katta"
                                                 )

    override fun process(folder: FileHandle, dataObject: DataObject, game: Game) {
        val lines = paths.lines()
        val addedCues: MutableList<Cue> = mutableListOf()

        game.objects as MutableList

        lines.mapTo(addedCues) {
            Cue(game, it, if (deprecations.containsKey(it)) listOf(deprecations[it]!!) else listOf(),
                it.replace("manzaiBirds/", "").replace("_", " "),
                2.5f, false, false,
                GameRegistry.SFX_FOLDER.child("$it.ogg"),
                null, null, mutableListOf(),
                95f, false)
        }

        game.objects.addAll(addedCues)

        val patterns = addedCues.map {
            Pattern(game, "manzaiBirds_${it.id.replace("manzaiBirds/", "")}",
                    mutableListOf("manzaiBirds_${it.id}") + it.deprecatedIDs.map { "manzaiBirds_$it" },
                    it.name,
                    mutableListOf(
                            CuePointer(
                                    it.id, 0f, 2.5f),
                            CuePointer(
                                    "manzaiBirds/haihai1",
                                    2.5f),
                            CuePointer(
                                    "manzaiBirds/haihai1",
                                    3f)
                                 ), false)
        }
        val boings = addedCues.map {
            Pattern(game, "manzaiBirds_${it.id.replace("manzaiBirds/", "")}_boing",
                    mutableListOf("manzaiBirds_${it.id}_boing") + it.deprecatedIDs.map { "manzaiBirds_$it" },
                    "${it.name} BOING!",
                    mutableListOf(
                            CuePointer(
                                    it.id, 0f, 1.5f),
                            CuePointer(
                                    "manzaiBirds/boing",
                                    1.5f),
                            CuePointer(
                                    "manzaiBirds/donaiyanen",
                                    2.5f)
                                 ), false)
        }

        game.objects += RandomCue(game, "manzaiBirds_randomNormal",
                                  listOf(), "random \"hai hai!\" phrase",
                                  patterns.map { CuePointer(it.id, 0f) },
                                  listOf())
        game.objects += RandomCue(game, "manzaiBirds_randomBoing",
                                  listOf(), "random boing!",
                                  boings.map { CuePointer(it.id, 0f) },
                                  listOf())

        patterns.zip(boings).forEach {
            game.objects += it.first
            game.objects += it.second
        }
    }

}
