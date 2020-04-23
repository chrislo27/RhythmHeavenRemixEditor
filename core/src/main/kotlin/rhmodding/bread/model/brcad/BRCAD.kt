package rhmodding.bread.model.brcad

import rhmodding.bread.model.IDataModel
import rhmodding.bread.util.Unknown
import java.nio.ByteBuffer
import java.nio.ByteOrder


class BRCAD : IDataModel {

    var spritesheetNumber: UShort = 0u
    @Unknown
    var spritesheetControlWord2: UShort = 0u
    override var sheetW: UShort = 0u
    override var sheetH: UShort = 0u

    @Unknown
    var unknownAfterSpriteCount: Short = 0
    @Unknown
    var unknownAfterAnimationCount: Short = 0

    override val sprites: MutableList<Sprite> = mutableListOf()
    override val animations: MutableList<Animation> = mutableListOf()

    companion object {
        const val HEADER_MAGIC: Int = 0x0132B4D8

        fun read(bytes: ByteBuffer): BRCAD {
            val magic = bytes.int
            if (magic != HEADER_MAGIC) {
                throw IllegalStateException("BRCAD did not have magic header ${HEADER_MAGIC.toString(16)}, got ${magic.toString(16)}")
            }
            if (bytes.int != 0x0) {
                throw IllegalStateException("Expected next int after magic to be 0")
            }

            return BRCAD().apply {
                spritesheetNumber = bytes.short.toUShort()
                spritesheetControlWord2 = bytes.short.toUShort()
                sheetW = bytes.short.toUShort()
                sheetH = bytes.short.toUShort()

                // Sprites
                val numEntries: Int = bytes.short.toInt()
                unknownAfterSpriteCount = bytes.short
                val entries = mutableListOf<Sprite>()

                repeat(numEntries) {
                    entries += Sprite().apply {
                        val numParts = bytes.short.toUShort().toInt()
                        unknown = bytes.short
                        repeat(numParts) {
                            parts += SpritePart().apply {
                                regionX = bytes.short.toUShort()
                                regionY = bytes.short.toUShort()
                                regionW = bytes.short.toUShort()
                                regionH = bytes.short.toUShort()
                                unknown = bytes.int
                                posX = bytes.short
                                posY = bytes.short
                                stretchX = bytes.float
                                stretchY = bytes.float
                                rotation = bytes.float
                                flipX = bytes.get() != 0.toByte()
                                flipY = bytes.get() != 0.toByte()
                                opacity = bytes.get().toUByte()
                                unknownLast = bytes.get()
                            }
                        }
                    }
                }

                sprites.clear()
                sprites.addAll(entries)

                // Animations
                animations.clear()
                val numAnimations = bytes.short.toUShort().toInt()
                unknownAfterAnimationCount = bytes.short
                repeat(numAnimations) {
//                    println("Animation #$it: byte pos ${bytes.position()}")
                    animations += Animation().apply {
                        val numSteps = bytes.short.toUShort().toInt()
                        unknown = bytes.short
                        repeat(numSteps) {
                            steps += AnimationStep().apply {
                                spriteIndex = bytes.short.toUShort()
                                delay = bytes.short.toUShort()
                                unknown1 = bytes.int
                                stretchX = bytes.float
                                stretchY = bytes.float
                                rotation = bytes.float
                                opacity = bytes.get().toUByte()
                                unknown3 = bytes.get()
                                unknown4 = bytes.get()
                                unknown5 = bytes.get()
                            }
                        }
                    }
                }
            }
        }
    }

    fun toBytes(): ByteBuffer {
        // Compute the size of the buffer
        // Header: 16 bytes
        // Sprite counts: 4 bytes
        // For each sprite:
        //   Part count: 4 bytes
        //   For each part: 32 bytes
        // Animation counts: 4 bytes
        // For each animation:
        //   Step count: 4 bytes
        //   For each step: 24 bytes

        val size = 16 + 4 + (4 * sprites.size + (sprites.sumBy { it.parts.size } * 32)) + 4 + (4 * animations.size + (animations.sumBy { it.steps.size } * 24))
        val buffer: ByteBuffer = ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)

        // Header
        buffer.putInt(HEADER_MAGIC).putInt(0x0)
        buffer.putShort(spritesheetNumber.toShort()).putShort(spritesheetControlWord2.toShort())
        buffer.putShort(sheetW.toShort()).putShort(sheetH.toShort())

        // Sprites
        buffer.putShort(sprites.size.toShort()).putShort(unknownAfterSpriteCount)
        sprites.forEach { sprite ->
            buffer.putShort(sprite.parts.size.toShort()).putShort(sprite.unknown)
            sprite.parts.forEach { part ->
                buffer.putShort(part.regionX.toShort()).putShort(part.regionY.toShort()).putShort(part.regionW.toShort()).putShort(part.regionH.toShort())
                buffer.putInt(part.unknown)
                buffer.putShort(part.posX).putShort(part.posY)
                buffer.putFloat(part.stretchX).putFloat(part.stretchY)
                buffer.putFloat(part.rotation)
                buffer.put((if (part.flipX) 1 else 0).toByte()).put((if (part.flipY) 1 else 0).toByte())
                buffer.put(part.opacity.toByte())
                buffer.put(part.unknownLast)
            }
        }

        // Animations
        buffer.putShort(animations.size.toShort()).putShort(unknownAfterAnimationCount)
        animations.forEach { ani ->
            buffer.putShort(ani.steps.size.toShort()).putShort(ani.unknown)
            ani.steps.forEach { step ->
                buffer.putShort(step.spriteIndex.toShort())
                buffer.putShort(step.delay.toShort())
                buffer.putInt(step.unknown1)
                buffer.putFloat(step.stretchX).putFloat(step.stretchY)
                buffer.putFloat(step.rotation)
                buffer.put(step.opacity.toByte())
                buffer.put(step.unknown3).put(step.unknown4).put(step.unknown5)
            }
        }

        return buffer
    }

    override fun toString(): String {
        return """BRCAD=[
            |  spritesheetNum=$spritesheetNumber, width=$sheetW, height=$sheetH,
            |  numSprites=${sprites.size},
            |  sprites=[${sprites.joinToString(separator = "\n")}],
            |  numAnimations=${animations.size},
            |  animations=[${animations.joinToString(separator = "\n")}]
            |]""".trimMargin()
    }
}