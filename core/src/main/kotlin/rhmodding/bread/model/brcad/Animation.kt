package rhmodding.bread.model.brcad

import rhmodding.bread.model.IAnimation
import rhmodding.bread.util.Unknown


class Animation : IAnimation {

    @Unknown
    var unknown: Short = 0
    override val steps: MutableList<AnimationStep> = mutableListOf()
    
    override fun copy(): Animation {
        return Animation().also {
            it.unknown = unknown
            steps.mapTo(it.steps) { it.copy() }
        }
    }
    
    override fun toString(): String {
        return "Animation=[numSteps=${steps.size}, steps=[${steps.joinToString(separator = "\n")}]]"
    }

}