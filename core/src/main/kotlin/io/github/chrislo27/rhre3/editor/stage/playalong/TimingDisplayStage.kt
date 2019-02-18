package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import io.github.chrislo27.rhre3.playalong.InputResult
import io.github.chrislo27.rhre3.playalong.InputTiming
import io.github.chrislo27.rhre3.playalong.Playalong
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement


class TimingDisplayStage(private val playalongStage: PlayalongStage, parent: UIElement<EditorScreen>, camera: OrthographicCamera) : Stage<EditorScreen>(parent, camera) {

    companion object {
        val ACE_COLOUR: Color = Color.valueOf("FFF800")
        val GOOD_COLOUR: Color = Color.valueOf("6DE23B")
        val BARELY_COLOUR: Color = Color.valueOf("FF7C26")
        val MISS_COLOUR: Color = Color.valueOf("E82727")
    }

    data class Flash(val offset: Float, val timing: InputTiming, val startDuration: Float, var duration: Float = startDuration)

    private val flashes: MutableList<Flash> = mutableListOf()
    private val texRegionGreat: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_input_timing"), 0, 0, 128, 128)
    private val texRegionMiss: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_input_timing"), 128, 0, 128, 128)
    private val texRegionBarely: TextureRegion = TextureRegion(AssetRegistry.get<Texture>("playalong_input_timing"), 256, 0, 128, 128)

    init {
        val palette = playalongStage.palette

        fun addColourPane(color: Color, x: Float, width: Float) {
            this.elements += ColourPane(this, this).apply {
                this.colour.set(color)
                this.location.set(screenX = 0.5f - width / 2, screenWidth = width)
            }
        }

        val acePercentage = (Playalong.ACE_OFFSET) / Playalong.MAX_OFFSET_SEC
        val goodPercentage = (Playalong.GOOD_OFFSET) / Playalong.MAX_OFFSET_SEC
        val barelyPercentage = (Playalong.BARELY_OFFSET) / Playalong.MAX_OFFSET_SEC
        addColourPane(MISS_COLOUR, 0f, 1f)
        addColourPane(BARELY_COLOUR, barelyPercentage, barelyPercentage)
        addColourPane(GOOD_COLOUR, barelyPercentage + goodPercentage, goodPercentage)
        addColourPane(ACE_COLOUR, barelyPercentage + goodPercentage + acePercentage, acePercentage)
    }

    fun flash(input: InputResult) {
        val offsetNormalized: Float = (input.offset / Playalong.MAX_OFFSET_SEC).coerceIn(-1f, 1f)
        if (input.timing == InputTiming.MISS && offsetNormalized <= (Playalong.BARELY_OFFSET / Playalong.MAX_OFFSET_SEC)) return
        flashes += Flash(offsetNormalized, input.timing, 0.25f)
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)

        flashes.forEach { flash ->
            batch.setColor(1f, 1f, 1f, Interpolation.pow5Out.apply(flash.duration / flash.startDuration))
//            val flashWidth = 0.02f * location.realWidth
//            batch.fillRect(location.realX - flashWidth / 2 + (location.realWidth / 2) * (flash.offset + 1f), location.realY, flashWidth, location.realHeight)
//            batch.fillRect(location.realX - flashWidth / 2 / 3 + (location.realWidth / 2) * (flash.offset + 1f), location.realY, flashWidth / 3, location.realHeight)
            val texReg: TextureRegion = when {
                flash.timing == InputTiming.MISS -> texRegionMiss
                flash.timing == InputTiming.ACE || flash.timing == InputTiming.GOOD -> texRegionGreat
                else -> texRegionBarely
            }
            val squareSize = location.realHeight
            batch.draw(texReg, location.realX + (location.realWidth / 2) * (flash.offset + 1f) - squareSize / 2,
                       location.realY, squareSize, squareSize)

        }
        batch.setColor(1f, 1f, 1f, 1f)
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        flashes.forEach {
            it.duration -= Gdx.graphics.deltaTime
        }
        flashes.removeIf { it.duration <= 0f }
    }
}