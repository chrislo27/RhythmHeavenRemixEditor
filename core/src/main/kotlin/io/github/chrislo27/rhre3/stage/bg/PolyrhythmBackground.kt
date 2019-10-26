package io.github.chrislo27.rhre3.stage.bg


class PolyrhythmBackground(id: String) : BlockBasedBackground(id) {
    
    companion object {
        private val borderedFace = Face(3, 0)
        private val borderedRedFace = Face(0, 1)
        private val polyFace = Face(1, 1)
        private val polyRedFace = Face(2, 1)
        
        val POLY_FLOOR: Block = Block("poly_floor", polyFace, polyFace, polyFace)
        val POLY_REDLINE_FLOOR: Block = Block("poly_floor_redline", polyFace, polyRedFace, polyRedFace)
        val POLY_PLATFORM: Block = Block("poly_platform", borderedFace, borderedFace, borderedFace)
        val POLY_REDLINE_PLATFORM: Block = Block("poly_platform_redline", borderedFace, borderedRedFace, borderedRedFace)
    }
    
    override val map: Map = Map(17, 5, 16, 30f, 30f, xMultiplier = 1.1f, zMultiplier = 1.1f)

    override fun buildMap() {
        camera.apply {
            position.y = 11.75f
            position.x = 1f
            update()
        }

        map.iterateSorted { _, x, y, z ->
            map.setBlock(if (y >= map.sizeY - 2) Block.NOTHING else POLY_FLOOR, x, y, z)
            map.setMetadata(0.toByte(), x, y, z)
        }
        val aPlatformStart = 5
        val redLine = 4

        for (x in 0 until map.sizeX) {
            for (z in 0..2) {
                map.setBlock(Block.NOTHING, x, map.sizeY - 3, z)
            }
        }
        for (x in 0 until map.sizeX) {
            map.setBlock(POLY_FLOOR, x, map.sizeY - 2, aPlatformStart + 7)
            map.setBlock(POLY_FLOOR, x, map.sizeY - 1, aPlatformStart + 8)
        }
        
        for (x in 0 until map.sizeX) {
            map.setBlock(POLY_PLATFORM, x, map.sizeY - (if (x <= redLine) 2 else 3), aPlatformStart)
            map.setBlock(POLY_PLATFORM, x, map.sizeY - (if (x <= redLine) 2 else 3), aPlatformStart + 3)
        }

        for (z in 0 until map.sizeZ) {
            for (y in map.sizeY downTo 0) {
                val block = map.getBlock(redLine, y, z)
                if (block != Block.NOTHING) {
                    map.setBlock(when (block) {
                                     POLY_FLOOR -> POLY_REDLINE_FLOOR
                                     POLY_PLATFORM -> POLY_REDLINE_PLATFORM
                                     else -> block
                                 }, redLine, y, z)
                    break
                }
            }
        }
    }

}