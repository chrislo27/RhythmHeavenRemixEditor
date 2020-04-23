package rhmodding.bread.model.brcad

import rhmodding.bread.model.ISprite
import rhmodding.bread.util.Unknown


class Sprite : ISprite {

    @Unknown
    var unknown: Short = 0

    override val parts: MutableList<SpritePart> = mutableListOf()

    override fun copy(): Sprite {
        return Sprite().also {
            it.unknown = unknown
            parts.mapTo(it.parts) { it.copy() }
        }
    }

    override fun toString(): String {
        return "Sprite=[numParts=${parts.size}, unknown=0x${unknown.toString(16)}, parts=[${parts.joinToString(separator = "\n")}]]"
    }

}