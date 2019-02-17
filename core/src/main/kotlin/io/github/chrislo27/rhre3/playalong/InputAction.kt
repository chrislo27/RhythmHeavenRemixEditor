package io.github.chrislo27.rhre3.playalong


open class InputAction(val beat: Float, val duration: Float,
                       val input: PlayalongInput, val method: PlayalongMethod)
    : Comparable<InputAction> {

    val isInstantaneous: Boolean get() = method.instantaneous

    override fun compareTo(other: InputAction): Int {
        return when {
            this.beat < other.beat -> -1
            this.beat > other.beat -> 1
            else -> 0
        }
    }
}