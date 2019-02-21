package io.github.chrislo27.rhre3.playalong


enum class MonsterPresets(val speed: Float, val localizationKey: String) {

    EASY(70f, "playalong.monsterGoal.easy"),
    MEDIUM(80f, "playalong.monsterGoal.medium"),
    HARD(85f, "playalong.monsterGoal.hard"),
    SUPER_HARD(90f, "playalong.monsterGoal.superHard");

    companion object {
        val VALUES = values().toList()
    }
}