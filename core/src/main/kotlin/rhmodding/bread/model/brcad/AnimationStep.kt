package rhmodding.bread.model.brcad

import rhmodding.bread.model.IAnimationStep
import rhmodding.bread.util.Unknown

class AnimationStep : IAnimationStep {
    
    override var spriteIndex: UShort = 0u
    override var delay: UShort = 1u
    @Unknown
    var unknown1: Int = 0
    override var stretchX: Float = 1f
    override var stretchY: Float = 1f
    override var rotation: Float = 0f
    override var opacity: UByte = 255u

    @Unknown
    var unknown3: Byte = 0
    @Unknown
    var unknown4: Byte = 0
    @Unknown
    var unknown5: Byte = 0
    
    override fun copy(): AnimationStep {
        return AnimationStep().also {
            it.spriteIndex = spriteIndex
            it.delay = delay
            it.unknown1 = unknown1
            it.stretchX = stretchX
            it.stretchY = stretchY
            it.rotation = rotation
            it.opacity = opacity
            it.unknown3 = unknown3
            it.unknown4 = unknown4
            it.unknown5 = unknown5
        }
    }
    
    override fun toString(): String {
        return "AnimationStep=[spriteIndex=$spriteIndex, delay=$delay, stretch=[$stretchX, $stretchY], rotation=$rotation, opacity=$opacity, unk1=0x${unknown1.toString(16)}, unk3=0x${unknown3.toString(16)}, unk4=0x${unknown4.toString(16)}, unk5=0x${unknown5.toString(16)}]"
    }
}