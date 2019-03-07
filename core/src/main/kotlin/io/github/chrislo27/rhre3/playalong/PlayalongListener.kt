package io.github.chrislo27.rhre3.playalong


interface PlayalongListener {

    fun onMonsterGoalFail()

    fun onSkillStarGet()

    fun onPerfectFail()

    fun onPerfectHit()

    fun onInput(inputAction: InputAction, inputResult: InputResult, start: Boolean)

    fun onScoreUpdate()

}

object NoOpPlayalongListener : PlayalongListener {
    override fun onMonsterGoalFail() {
    }

    override fun onSkillStarGet() {
    }

    override fun onPerfectFail() {
    }

    override fun onPerfectHit() {
    }

    override fun onInput(inputAction: InputAction, inputResult: InputResult, start: Boolean) {
    }

    override fun onScoreUpdate() {
    }
}