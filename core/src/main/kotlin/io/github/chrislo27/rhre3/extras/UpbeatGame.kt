package io.github.chrislo27.rhre3.extras

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Vector2
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import rhmodding.bread.model.brcad.BRCAD
import java.nio.ByteBuffer
import kotlin.math.sin


class UpbeatGame : RhythmGame() {
    
    private val sheet: Texture = Texture("extras/upbeat/upbeat_spritesheet.png").apply { 
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    private val brcad: BRCAD = BRCAD.read(ByteBuffer.wrap(Gdx.files.internal("extras/upbeat/upbeat.bin").readBytes()))
    
    private val bgRegion: TextureRegion = TextureRegion(sheet, 994, 2, 28, 28)
    private val needleRegion: TextureRegion = TextureRegion(sheet, 8, 456, 400, 48)
    private val needlePivot: Vector2 = Vector2(232f, 24f)
    
    private var needleAngle: Float = 0f // 0f = centred vertically
    
    override fun _render(main: RHRE3Application, batch: SpriteBatch) {
        batch.packedColor = Color.WHITE_FLOAT_BITS
        val width = camera.viewportWidth
        val height = camera.viewportHeight
        batch.draw(bgRegion, 0f, 0f, width, height)

        // FIXME
        needleAngle = sin(Math.PI / 1f * (System.currentTimeMillis() / 1000.0)).toFloat() * 60f
        
        val needleScale = 1.5f
        batch.draw(needleRegion, width / 2f - needlePivot.x, height * 0.235f, needlePivot.x, needlePivot.y, needleRegion.regionWidth.toFloat(), needleRegion.regionHeight.toFloat(), needleScale, needleScale, needleAngle - 90f)
    }

    override fun dispose() {
        super.dispose()
        sheet.dispose()
    }
}