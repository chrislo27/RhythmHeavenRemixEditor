package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color


@Suppress("PropertyName")
class BTSDSBackground(id: String, val floorColor: Color = Color.valueOf("3DFD46FF")) : BlockBasedBackground(id) {
    
    override val map: Map = Map(21, 6, 23)
    
    private val floorFace = Face(0, 0, floorColor)
    private val floor2Face = Face(0, 0, floorColor.cpy().sub(6f / 255f, 23f / 255f, 7f / 255f, 0f))
    private val floor3Face = Face(0, 0, floorColor.cpy().sub(17f / 255f, 74f / 255f, 20f / 255f, 0f))
    private val floor4Face = Face(0, 0, floorColor.cpy().sub(24f / 255f, 100f / 255f, 26f / 255f, 0f))
    private val floor5Face = Face(0, 0, floorColor.cpy().sub(29f / 255f, 124f / 255f, 34f / 255f, 0f))
    private val conveyorFace = Face(0, 2, floor5Face.tint.cpy(), 1f, 0, 3)
    private val whitePlatformFace = Face(0, 0)
    private val redStripesTopFace = Face(1, 0)
    private val redStripesSideFace = Face(2, 0)

    val FLOOR: Block = Block("floor", floorFace, floorFace, floorFace)
    val FLOOR2: Block = Block("floor2", floor2Face, floor2Face, floor2Face)
    val FLOOR3: Block = Block("floor3", floor3Face, floor3Face, floor3Face)
    val FLOOR4: Block = Block("floor4", floor4Face, floor4Face, floor4Face)
    val FLOOR5: Block = Block("floor5", floor5Face, floor5Face, floor5Face)
    val CONVEYOR: Block = Block("conveyor", floor5Face, conveyorFace, floor5Face)
    val FLOOR_LIST: List<Block> = listOf(FLOOR, FLOOR2, FLOOR3, FLOOR4, FLOOR5)
    val PLATFORM: Block = Block("platform", floor2Face, whitePlatformFace, floor2Face)
    val PLATFORM_CENTRE: Block = Block("platform_centre", redStripesSideFace, redStripesTopFace, redStripesSideFace)
    
    override fun buildMap() {
        camera.apply {
            position.y = 15f
            position.x = 4f
            update()
        }
        flashEnabled = true

        map.iterateSorted { _, x, y, z ->
            map.setBlock(FLOOR_LIST.getOrElse(map.sizeY - y - 1) { FLOOR }, x, y, z)
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
                map.setBlock(CONVEYOR, map.sizeX / 2, map.sizeY - 5, z)
            }
        }
        val platform = 9
        for (zi in 0..1) {
            for (x in 0 until map.sizeX) {
                map.setBlock(Block.NOTHING, x, map.sizeY - 2, trenchZStart + platform + zi)
                if (x == map.sizeX / 2) {
                    map.setBlock(PLATFORM_CENTRE, x, map.sizeY - 3, trenchZStart + platform + zi)
                } else {
                    map.setBlock(PLATFORM, x, map.sizeY - 3, trenchZStart + platform + zi)
                    map.setBlock(FLOOR4, x, map.sizeY - 4, trenchZStart + platform + zi)
                }
                map.setBlock(FLOOR3, x, map.sizeY - 3, trenchZStart + platform - 5 + zi)
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
}