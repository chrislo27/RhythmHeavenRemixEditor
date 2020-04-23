package rhmodding.bread.model


interface IAnimation {
    
    val steps: MutableList<out IAnimationStep>
    
    fun copy(): IAnimation
    
}

interface IAnimationStep {

    var spriteIndex: UShort
    var delay: UShort

    var stretchX: Float
    var stretchY: Float
    
    var rotation: Float
    
    var opacity: UByte
    
    fun copy(): IAnimationStep

}