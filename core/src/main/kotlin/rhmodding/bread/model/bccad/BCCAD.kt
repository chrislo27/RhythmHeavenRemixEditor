package rhmodding.bread.model.bccad

import com.badlogic.gdx.graphics.Color
import rhmodding.bread.model.IDataModel
import java.nio.ByteBuffer
import java.nio.ByteOrder


class BCCAD : IDataModel {
    
    companion object {
        fun read(bytes: ByteBuffer): BCCAD {
            bytes.order(ByteOrder.LITTLE_ENDIAN)
            return BCCAD().apply {
                timestamp = bytes.int
                sheetW = bytes.short.toUShort()
                sheetH = bytes.short.toUShort()
                repeat(bytes.int) {
                    sprites += Sprite().apply {
                        repeat(bytes.int) {
                            parts += SpritePart().apply {
                                regionX = bytes.short.toUShort()
                                regionY = bytes.short.toUShort()
                                regionW = bytes.short.toUShort()
                                regionH = bytes.short.toUShort()
                                posX = bytes.short
                                posY = bytes.short
                                stretchX = bytes.float
                                stretchY = bytes.float
                                rotation = bytes.float
                                flipX = bytes.get() != 0.toByte()
                                flipY = bytes.get() != 0.toByte()
                                multColor = Color((bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f, 1f)
                                screenColor = Color((bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f,1f)
                                opacity = bytes.get().toUByte()
                                repeat(12) {
                                    unknownData[it] = bytes.get()
                                }
                                designation = bytes.get().toUByte()
                                unknown = bytes.short
                                tlDepth = bytes.float
                                blDepth = bytes.float
                                trDepth = bytes.float
                                brDepth = bytes.float
                            }
                        }
                    }
                }
                repeat(bytes.int) {
                    var s = ""
                    val n = bytes.get().toInt()
                    repeat(n) {
                        s += bytes.get().toChar()
                    }
                    repeat(4 - ((n + 1) % 4)) {
                        bytes.get()
                    }
                    animations += Animation().apply {
                        name = s
                        interpolationInt = bytes.int
                        repeat(bytes.int) {
                            steps.add(AnimationStep().apply {
                                spriteIndex = bytes.short.toUShort()
                                delay = bytes.short.toUShort()
                                translateX = bytes.short
                                translateY = bytes.short
                                depth = bytes.float
                                stretchX = bytes.float
                                stretchY = bytes.float
                                rotation = bytes.float
                                color = Color((bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f, (bytes.get().toInt() and 0xFF) / 255f, 1f)
                                bytes.get()
                                unknown1 = bytes.get()
                                unknown2 = bytes.get()
                                opacity = (bytes.short.toInt() and 0xFF).toUByte()
                            })
                        }
                    }
                }
                bytes.get()
            }
        }
    }
    
    var timestamp: Int = 0
    override var sheetW: UShort = 1u
    override var sheetH: UShort = 1u
    override val sprites: MutableList<Sprite> = mutableListOf()
    override val animations: MutableList<Animation> = mutableListOf()
    
    fun toBytes(): ByteBuffer {
        // Compute size of buffer
        // Header: 8 bytes
        // Num sprites: 4 bytes
        // For each sprite: 4 bytes
        //   For each sprite part: 64 bytes
        // Num animations: 4 bytes
        // For each animation: 1 + (1*name.length in bytes, padded to nearest four bytes) + 16
        //   For each animation step: 32 bytes
        // One end byte
        
        val bytes = ByteBuffer.allocate(8 + 4 +
                                                (4 * sprites.size + (64 * sprites.sumBy { it.parts.size }) +
                                                        4 + (animations.sumBy { 1 + it.name.length + (4 - ((it.name.length + 1) % 4)) + 8 } +
                                                        animations.sumBy { 32 * it.steps.size })) + 1)
                .order(ByteOrder.LITTLE_ENDIAN)
        
        bytes.putInt(timestamp)
                .putShort(sheetW.toShort())
                .putShort(sheetH.toShort())
                .putInt(sprites.size)
        
        sprites.forEach { sprite ->
            bytes.putInt(sprite.parts.size)
            sprite.parts.forEach { p ->
                with(p) {
                    bytes.putShort(regionX.toShort())
                            .putShort(regionY.toShort())
                            .putShort(regionW.toShort())
                            .putShort(regionH.toShort())
                            .putShort(posX)
                            .putShort(posY)
                            .putFloat(stretchX)
                            .putFloat(stretchY)
                            .putFloat(rotation)
                            .put((if (flipX) 1 else 0).toByte())
                            .put((if (flipY) 1 else 0).toByte())
                            .put((multColor.r * 255).toByte())
                            .put((multColor.g * 255).toByte())
                            .put((multColor.b * 255).toByte())
                            .put((screenColor.r * 255).toByte())
                            .put((screenColor.g * 255).toByte())
                            .put((screenColor.b * 255).toByte())
                            .put(opacity.toByte())
                    repeat(12) {
                        bytes.put(unknownData[it])
                    }
                    bytes.put(designation.toByte())
                            .putShort(unknown)
                            .putFloat(tlDepth)
                            .putFloat(blDepth)
                            .putFloat(trDepth)
                            .putFloat(brDepth)
                }
            }
        }
        
        bytes.putInt(animations.size)
        animations.forEach { a ->
            with(a) {
                bytes.put(name.length.toByte())
                name.toCharArray().forEach { b -> bytes.put(b.toByte()) }
                repeat(4 - ((name.length + 1) % 4)) { bytes.put(0.toByte()) }
                
                bytes.putInt(interpolationInt)
                        .putInt(steps.size)
                
                steps.forEach { s ->
                    with(s) {
                        bytes.putShort(spriteIndex.toShort())
                                .putShort(delay.toShort())
                                .putShort(translateX)
                                .putShort(translateY)
                                .putFloat(depth)
                                .putFloat(stretchX)
                                .putFloat(stretchY)
                                .putFloat(rotation)
                                .put((color.r * 255).toByte())
                                .put((color.g * 255).toByte())
                                .put((color.b * 255).toByte())
                                .put(0.toByte())
                                .put(unknown1)
                                .put(unknown2)
                                .putShort(opacity.toShort())
                    }
                }
            }
        }
        bytes.put(0.toByte())
        return bytes
    }
    
    override fun toString(): String {
        return """BCCAD=[
            |  timestamp=$timestamp, width=$sheetW, height=$sheetH,
            |  numSprites=${sprites.size},
            |  sprites=[${sprites.joinToString(separator = "\n")}],
            |  numAnimations=${animations.size},
            |  animations=[${animations.joinToString(separator = "\n")}]
            |]""".trimMargin()
    }
    
}