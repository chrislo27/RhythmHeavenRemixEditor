package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Matrix4
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.cos
import kotlin.math.sin


abstract class BlockBasedBackground(id: String)
    : Background(id) {
    
    data class Face(val spriteX: Int, val spriteY: Int, val tint: Color = Color(1f, 1f, 1f, 1f),
                    val interpolateTime: Float = 0f, val lerpX: Int = 0, val lerpY: Int = 0) {
        companion object {
            val FACELESS: Face = Face(3, 1)
        }
    }
    
    data class Block(val id: String, val xFace: Face, val yFace: Face, val zFace: Face) {
        companion object {
            val NOTHING: Block = Block("nothing", Face.FACELESS)
        }
        
        constructor(id: String, face: Face) : this(id, face, face, face)
    }
    
    class Map(val sizeX: Int, val sizeY: Int, val sizeZ: Int, val xAngle: Float = 15f, val zAngle: Float = 40f, val xMultiplier: Float = 1.1f, val zMultiplier: Float = 1f) {

        val xOffsetX: Float = xMultiplier * (cos(Math.toRadians(xAngle.toDouble()))).toFloat()
        val xOffsetY: Float = xMultiplier * (sin(Math.toRadians(xAngle.toDouble()))).toFloat()
        
        val zOffsetX: Float = zMultiplier * -(cos(Math.toRadians(zAngle.toDouble()))).toFloat()
        val zOffsetY: Float = zMultiplier * (sin(Math.toRadians(zAngle.toDouble()))).toFloat()
        
        private val backing: Array<Array<Array<Block>>> = Array(sizeX) { Array(sizeY) { Array(sizeZ) { Block.NOTHING } } }
        private val metadata: Array<Array<ByteArray>> = Array(sizeX) { Array(sizeY) { ByteArray(sizeZ) { 0.toByte() } } }
        
        fun getBlock(x: Int, y: Int, z: Int): Block = if (x !in 0 until sizeX || y !in 0 until sizeY || z !in 0 until sizeZ) Block.NOTHING else backing[x][y][z]
        fun setBlock(block: Block, x: Int, y: Int, z: Int) {
            backing[x][y][z] = block
        }
        
        fun getMetadata(x: Int, y: Int, z: Int): Byte = if (x !in 0 until sizeX || y !in 0 until sizeY || z !in 0 until sizeZ) 0 else metadata[x][y][z]
        fun setMetadata(data: Byte, x: Int, y: Int, z: Int) {
            metadata[x][y][z] = data
        }
        
        inline fun iterateSorted(consumer: (block: Block, x: Int, y: Int, z: Int) -> Unit) {
            for (z in sizeZ - 1 downTo 0) {
                for (x in sizeX - 1 downTo 0) {
                    for (y in 0 until sizeY) {
                        consumer(getBlock(x, y, z), x, y, z)
                    }
                }
            }
        }
    }
    
    private val tmpMatrix = Matrix4()
    protected val spriteSize: Int = 128
    protected val spritesheet: Texture by lazy { AssetRegistry.get<Texture>("bg_btsds_spritesheet") }
    protected val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 16f, 9f)
        update()
    }
    protected val flashColor1: Color = Color(1f, 1f, 1f, 1f)
    protected val flashColor2: Color = Color(1f, 1f, 1f, 0f)
    protected var flashEnabled = false
    
    abstract val map: Map
    private var mapBuilt = false
    
    abstract fun buildMap()
    
    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        if (!mapBuilt) {
            buildMap()
            mapBuilt = true
        }
        if (Toolboks.debugMode && Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            buildMap()
        }
        
        batch.setColor(0f, 0f, 0f, 1f)
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
        batch.setColor(0f, 0f, 0f, 1f)
        
        if (flashEnabled) {
            flashColor1.a = (MathUtils.sin(MathUtils.PI2 * MathHelper.getSawtoothWave(2f))).coerceAtLeast(0f) * 0.9f
            flashColor2.a = (MathUtils.sin(MathUtils.PI2 * MathHelper.getSawtoothWave(System.currentTimeMillis() + 1000L, 2f))).coerceAtLeast(0f) * 0.9f
        } else {
            flashColor1.a = 0f
            flashColor2.a = 0f
        }
        tmpMatrix.set(batch.projectionMatrix)
        batch.projectionMatrix = this.camera.combined
        
        val spritesheetCellU = spriteSize.toFloat() / spritesheet.width
        val spritesheetCellV = spriteSize.toFloat() / spritesheet.height
        
        map.iterateSorted { block, x, y, z ->
            if (block != Block.NOTHING) {
                val rootX = x * map.xOffsetX + z * map.zOffsetX
                val rootY = x * map.xOffsetY + z * map.zOffsetY + y
                if (block.xFace != Face.FACELESS && map.getBlock(x - 1, y, z) == Block.NOTHING) {
                    val f = block.xFace
                    val spriteX = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteX.toFloat(), f.lerpX.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteX.toFloat()
                    val spriteY = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteY.toFloat(), f.lerpY.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteY.toFloat()
                    batch.drawQuad(rootX + map.zOffsetX, rootY + map.zOffsetY, f.tint.toFloatBits(),
                                   rootX, rootY, f.tint.toFloatBits(),
                                   rootX, rootY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX, rootY + map.zOffsetY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (spriteX) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY) * spritesheetCellV,
                                   (spriteX) * spritesheetCellU, (spriteY) * spritesheetCellV
                                  )
                }
                if (block.zFace != Face.FACELESS && map.getBlock(x, y, z - 1) == Block.NOTHING) {
                    val f = block.zFace
                    val spriteX = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteX.toFloat(), f.lerpX.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteX.toFloat()
                    val spriteY = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteY.toFloat(), f.lerpY.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteY.toFloat()
                    batch.drawQuad(rootX, rootY, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX, rootY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (spriteX) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY) * spritesheetCellV,
                                   (spriteX) * spritesheetCellU, (spriteY) * spritesheetCellV
                                  )
                }
                if (block.yFace != Face.FACELESS && map.getBlock(x, y + 1, z) == Block.NOTHING) {
                    val f = block.yFace
                    val spriteX = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteX.toFloat(), f.lerpX.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteX.toFloat()
                    val spriteY = if (f.interpolateTime > 0f) MathUtils.lerp(f.spriteY.toFloat(), f.lerpY.toFloat(), MathHelper.getSawtoothWave(f.interpolateTime)) else f.spriteY.toFloat()
                    batch.drawQuad(rootX, rootY + 1f, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX + map.xOffsetX, rootY + map.zOffsetY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX, rootY + map.zOffsetY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (spriteX) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY + 1) * spritesheetCellV,
                                   (spriteX + 1) * spritesheetCellU, (spriteY) * spritesheetCellV,
                                   (spriteX) * spritesheetCellU, (spriteY) * spritesheetCellV
                                  )
                    if (map.getMetadata(x, y, z) > 0) {
                        val flash = if (map.getMetadata(x, y, z) > 1) flashColor2 else flashColor1
                        batch.drawQuad(rootX, rootY + 1f, flash.toFloatBits(),
                                       rootX + map.xOffsetX, rootY + map.xOffsetY + 1f, flash.toFloatBits(),
                                       rootX + map.zOffsetX + map.xOffsetX, rootY + map.zOffsetY + map.xOffsetY + 1f, flash.toFloatBits(),
                                       rootX + map.zOffsetX, rootY + map.zOffsetY + 1f, flash.toFloatBits()
                                      )
                    }
                }
            }
        }
        
        
        batch.projectionMatrix = tmpMatrix
    }
    
}
