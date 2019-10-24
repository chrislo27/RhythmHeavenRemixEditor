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


class BTSDSBackground(id: String)
    : Background(id) {
    
    data class Face(val spriteX: Int, val spriteY: Int, val tint: Color = Color(1f, 1f, 1f, 1f)) {
        companion object {
            val FACELESS: Face = Face(3, 1)
        }
    }
    
    data class Block(val id: String, val xFace: Face, val yFace: Face, val zFace: Face) {
        companion object {
            val NOTHING: Block = Block("nothing", Face.FACELESS)
            
            private val greenFace = Face(0, 0, Color.valueOf("3DFD46FF"))
            private val green2Face = Face(0, 0, Color.valueOf("37E63FFF"))
            private val green3Face = Face(0, 0, Color.valueOf("2CB332FF"))
            private val green4Face = Face(0, 0, Color.valueOf("25992CFF"))
            private val green5Face = Face(0, 0, Color.valueOf("208124FF"))
            private val whiteFace = Face(0, 0)
            private val redStripesTopFace = Face(1, 0)
            private val redStripesSideFace = Face(2, 0)
            
            val FLOOR: Block = Block("floor", greenFace, greenFace, greenFace)
            val FLOOR2: Block = Block("floor2", green2Face, green2Face, green2Face)
            val FLOOR3: Block = Block("floor3", green3Face, green3Face, green3Face)
            val FLOOR4: Block = Block("floor4", green4Face, green4Face, green4Face)
            val FLOOR5: Block = Block("floor5", green5Face, green5Face, green5Face)
            val FLOOR_LIST: List<Block> = listOf(FLOOR, FLOOR2, FLOOR3, FLOOR4, FLOOR5)
            val PLATFORM: Block = Block("platform", green2Face, whiteFace, green2Face)
            val PLATFORM_CENTRE: Block = Block("platform_centre", redStripesSideFace, redStripesTopFace, redStripesSideFace)
        }
        
        constructor(id: String, face: Face) : this(id, face, face, face)
    }
    
    class Map(val sizeX: Int, val sizeY: Int, val sizeZ: Int) {
        val xAngle: Float = 15f
        val zAngle: Float = 40f
        val xMultiplier: Float = 1.1f
        val zMultiplier: Float = 1f
        
        val xOffsetX: Float = xMultiplier * (cos(Math.toRadians(xAngle.toDouble()))).toFloat()
        val xOffsetY: Float = xMultiplier * (sin(Math.toRadians(xAngle.toDouble()))).toFloat()
        
        val zOffsetX: Float = zMultiplier * -(cos(Math.toRadians(zAngle.toDouble()))).toFloat()
        val zOffsetY: Float = zMultiplier * (sin(Math.toRadians(zAngle.toDouble()))).toFloat()
        
        private val backing: Array<Array<Array<Block>>> = Array(sizeX) { Array(sizeY) { Array(sizeZ) { Block.FLOOR } } }
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
    private val spriteSize: Int = 128
    private val spritesheet: Texture by lazy { AssetRegistry.get<Texture>("bg_btsds_spritesheet") }
    private val camera: OrthographicCamera = OrthographicCamera().apply {
        setToOrtho(false, 16f, 9f)
        position.y = 15f
        position.x = 4f
        update()
    }
    private val flashColor1: Color = Color(1f, 1f, 1f, 1f)
    private val flashColor2: Color = Color(1f, 1f, 1f, 0f)
    
    val map: Map = Map(21, 6, 23)
    
    init {
        buildMap()
    }
    
    fun buildMap() {
        map.iterateSorted { _, x, y, z ->
            map.setBlock(Block.FLOOR_LIST.getOrElse(map.sizeY - y - 1) { Block.FLOOR }, x, y, z)
            map.setMetadata(0.toByte(), x, y, z)
        }
        val trenchZStart = 4
        for (x in -4..4) {
            for (z in trenchZStart until map.sizeZ) {
                map.setBlock(Block.NOTHING, x + map.sizeX / 2, map.sizeY - 1, z)
                if (x in -3..3) {
                    map.setBlock(Block.NOTHING, x + map.sizeX / 2, map.sizeY - 2, z)
                }
                if (x in -2..2) {
                    map.setBlock(Block.NOTHING, x + map.sizeX / 2, map.sizeY - 3, z)
                }
                if (x in -1..1) {
                    map.setBlock(Block.NOTHING, x + map.sizeX / 2, map.sizeY - 4, z)
                }
            }
        }
        val platform = 9
        for (zi in 0..1) {
            for (x in 0 until map.sizeX) {
                map.setBlock(Block.NOTHING, x, map.sizeY - 2, trenchZStart + platform + zi)
                if (x == map.sizeX / 2) {
                    map.setBlock(Block.PLATFORM_CENTRE, x, map.sizeY - 3, trenchZStart + platform + zi)
                } else {
                    map.setBlock(Block.PLATFORM, x, map.sizeY - 3, trenchZStart + platform + zi)
                    map.setBlock(Block.FLOOR4, x, map.sizeY - 4, trenchZStart + platform + zi)
                }
                map.setBlock(Block.FLOOR3, x, map.sizeY - 3, trenchZStart + platform - 5 + zi)
            }
        }
        
        // Flash metadata
        map.setMetadata(1.toByte(), map.sizeX / 2 + 3, map.sizeY - 3, trenchZStart + 8)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 3, map.sizeY - 3, trenchZStart + 6)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 3, map.sizeY - 3, trenchZStart + 3)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 3, map.sizeY - 3, trenchZStart + 8)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 3, map.sizeY - 3, trenchZStart + 6)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 3, map.sizeY - 3, trenchZStart + 3)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 5, map.sizeY - 1, trenchZStart + 7)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 5, map.sizeY - 1, trenchZStart + 3)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 5, map.sizeY - 1, trenchZStart + 7)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 5, map.sizeY - 1, trenchZStart + 3)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 1, map.sizeY - 5, trenchZStart + 15)
        map.setMetadata(1.toByte(), map.sizeX / 2 - 1, map.sizeY - 5, trenchZStart + 17)
        map.setMetadata(1.toByte(), map.sizeX / 2 + 3, map.sizeY - 3, trenchZStart + 15)
        
        map.setMetadata(2.toByte(), map.sizeX / 2 + 2, map.sizeY - 4, trenchZStart + 8)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 2, map.sizeY - 4, trenchZStart + 7)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 2, map.sizeY - 4, trenchZStart + 13)
        map.setMetadata(2.toByte(), map.sizeX / 2 + 2, map.sizeY - 4, trenchZStart + 14)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 4, map.sizeY - 2, trenchZStart + 8)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 4, map.sizeY - 2, trenchZStart + 2)
        map.setMetadata(2.toByte(), map.sizeX / 2 + 6, map.sizeY - 1, trenchZStart + 8)
        map.setMetadata(2.toByte(), map.sizeX / 2 + 7, map.sizeY - 1, trenchZStart + 4)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 7, map.sizeY - 1, trenchZStart + 1)
        map.setMetadata(2.toByte(), map.sizeX / 2 - 6, map.sizeY - 1, trenchZStart + 4)
        map.setMetadata(2.toByte(), map.sizeX / 2 + 4, map.sizeY - 2, trenchZStart + 4)
    }
    
    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float) {
        batch.setColor(0f, 0f, 0f, 1f)
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
        batch.setColor(0f, 0f, 0f, 1f)

//        this.camera.apply {
//            position.y = 15f
//            position.x = 4f
//            update()
//        }
        
        flashColor1.a = (MathUtils.sin(MathUtils.PI2 * MathHelper.getSawtoothWave(2f))).coerceAtLeast(0f) * 0.9f
        flashColor2.a = (MathUtils.sin(MathUtils.PI2 * MathHelper.getSawtoothWave(System.currentTimeMillis() + 1000L, 2f))).coerceAtLeast(0f) * 0.9f
        
        if (Toolboks.debugMode && Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            buildMap()
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
                    batch.drawQuad(rootX + map.zOffsetX, rootY + map.zOffsetY, f.tint.toFloatBits(),
                                   rootX, rootY, f.tint.toFloatBits(),
                                   rootX, rootY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX, rootY + map.zOffsetY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY) * spritesheetCellV,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY) * spritesheetCellV
                                  )
                }
                if (block.zFace != Face.FACELESS && map.getBlock(x, y, z - 1) == Block.NOTHING) {
                    val f = block.zFace
                    batch.drawQuad(rootX, rootY, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX, rootY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY) * spritesheetCellV,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY) * spritesheetCellV
                                  )
                }
                if (block.yFace != Face.FACELESS && map.getBlock(x, y + 1, z) == Block.NOTHING) {
                    val f = block.yFace
                    batch.drawQuad(rootX, rootY + 1f, f.tint.toFloatBits(),
                                   rootX + map.xOffsetX, rootY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX + map.xOffsetX, rootY + map.zOffsetY + map.xOffsetY + 1f, f.tint.toFloatBits(),
                                   rootX + map.zOffsetX, rootY + map.zOffsetY + 1f, f.tint.toFloatBits(),
                                   spritesheet,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY + 1) * spritesheetCellV,
                                   (f.spriteX + 1) * spritesheetCellU, (f.spriteY) * spritesheetCellV,
                                   (f.spriteX) * spritesheetCellU, (f.spriteY) * spritesheetCellV
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
