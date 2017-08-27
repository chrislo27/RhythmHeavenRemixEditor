package io.github.chrislo27.rhre3.credits


import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import java.util.*

/**
 * A decal-like projector that uses PaperSprites.
 */
open class PaperProjection(var scaleCoeff: Float = 1.0f) {

    val sprites: MutableList<PaperSpriteable> = mutableListOf()
    val comparator: Comparator<PaperSpriteable> = PaperSpriteComparator().reversed()

    open fun render(batch: SpriteBatch, sprites: MutableList<out PaperSpriteable>) {
        // ensure Z-order
        sprites.sortWith(comparator)

        sprites.forEach { able ->
            val it = able.getPaperSprite()
            val oldScaleX = it.scaleX
            val oldScaleY = it.scaleY

            if (it.z < 0f) {
                return@forEach
            }
            val realZ = it.z + 1f

            // set new projection scale
            it.setScale((oldScaleX / realZ) * scaleCoeff, (oldScaleY / realZ) * scaleCoeff)

            // render
            it.draw(batch)

            // reset scale
            it.setScale(oldScaleX, oldScaleY)
        }
    }

    fun render(batch: SpriteBatch) {
        render(batch, sprites)
    }

}

class PaperSprite : Sprite, PaperSpriteable {

    var z: Float = 0f

    constructor() : super()
    constructor(texture: Texture?) : super(texture)
    constructor(texture: Texture?, srcWidth: Int, srcHeight: Int) : super(texture, srcWidth, srcHeight)
    constructor(texture: Texture?, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) : super(texture, srcX, srcY,
                                                                                                srcWidth, srcHeight)
    constructor(region: TextureRegion?) : super(region)
    constructor(region: TextureRegion?, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int) : super(region, srcX, srcY,
                                                                                                     srcWidth,
                                                                                                     srcHeight)
    constructor(sprite: Sprite?) : super(sprite)

    init {
        setOriginCenter()
    }

    fun setZ(z: Float): PaperSprite {
        this.z = z
        return this
    }

    fun setPosition(x: Float, y: Float, z: Float) {
        return setZ(z).setPosition(x, y)
    }

    override fun getPaperSprite(): PaperSprite = this
}

interface PaperSpriteable {

    fun getPaperSprite(): PaperSprite

}

/**
 * Compares PaperSprites from farthest having priority
 */
private class PaperSpriteComparator : Comparator<PaperSpriteable> {

    override fun compare(o1: PaperSpriteable?, o2: PaperSpriteable?): Int {
        if (o1 == null) {
            if (o2 == null)
                return 0
            else
                return 1
        }
        if (o2 == null)
            return -1

        if (o1.getPaperSprite().z > o2.getPaperSprite().z)
            return 1

        if (o1.getPaperSprite().z < o2.getPaperSprite().z)
            return -1

        return 0
    }

}