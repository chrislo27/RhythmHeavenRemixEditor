package io.github.chrislo27.rhre3.editor.stage.playalong

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask


class FlickingStage<S : ToolboksScreen<*, *>>(parent: UIElement<S>, parameterStage: Stage<S>)
    : ColourPane<S>(parent, parameterStage) {

    data class TapPoint(var x: Float, var y: Float, var veloX: Float, var veloY: Float,
                        var lifetime: Float, var maxLifetime: Float, var isHeldDown: Boolean = false,
                        var holdDuration: Float = 0f, var didFireSlideEvent: Boolean = false)

    private val tapPoints: MutableList<TapPoint> = mutableListOf()
    private var currentTapPoint: TapPoint? = null

    var onTapDown: (tapPoint: TapPoint) -> Unit = {}
    var onTapRelease: (tapPoint: TapPoint, short: Boolean) -> Unit = { _, _ -> }
    var onFlick: (tapPoint: TapPoint) -> Unit = {}
    var onSlide: (tapPoint: TapPoint) -> Unit = {}

    override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)

        batch.setColor(1f, 1f, 1f, 1f)

        currentTapPoint?.also { point ->
            updateTapPoint(point, true)
        }

        shapeRenderer.prepareStencilMask(batch) {
            Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT)
            begin(ShapeRenderer.ShapeType.Filled)
            rect(location.realX - 1, location.realY, location.realWidth + 1, location.realHeight)
            end()
        }.useStencilMask {
            val tapPointTex: Texture = AssetRegistry["playalong_tappoint"]
            tapPoints.forEach { tp ->
                val radiusScale = tp.lifetime / tp.maxLifetime
                val mainCircleSizePx = 128

                // Main body
                batch.setColor(0f, 0f, 0f, 0.6f)
                val mainCircleSize = mainCircleSizePx * radiusScale
                batch.draw(tapPointTex, tp.x - mainCircleSize / 2, tp.y - mainCircleSize / 2,
                           mainCircleSize, mainCircleSize,
                           0, 0, mainCircleSizePx, mainCircleSizePx, false, false)

                // Eyes
                val eyeSizePx = 16
                batch.setColor(1f, 1f, 1f, 1f)
                for (i in 0 until 2) {
                    val sign = if (i % 2 == 0) -1 else 1
                    batch.draw(tapPointTex, tp.x + sign * (eyeSizePx / 2 + 4f) - eyeSizePx / 2f, tp.y + mainCircleSize / 2 + 4f,
                               eyeSizePx.toFloat(), eyeSizePx.toFloat(),
                               mainCircleSizePx + 16, 0, eyeSizePx, eyeSizePx, false, false)
                }

                // Inner circle + face
                batch.setColor(1f, 1f, 1f, 0.8f)
                val innerCircleSizePx = 30
                val innerCircleSize = innerCircleSizePx * radiusScale * MathUtils.lerp(0.8f, 1f, MathHelper.getBaseCosineWave(0.1f))
                batch.draw(tapPointTex, tp.x - innerCircleSize / 2, tp.y - innerCircleSize / 2,
                           innerCircleSize, innerCircleSize,
                           144, 32, innerCircleSizePx, innerCircleSizePx, false, false)
                val innerFaceSize = innerCircleSizePx * (radiusScale / 0.85f).coerceIn(0f, 1f)
                batch.setColor(1f, 1f, 1f, 1f)
                batch.draw(tapPointTex, tp.x - innerFaceSize / 2f, tp.y - innerFaceSize / 2f,
                           innerFaceSize, innerFaceSize,
                           144 + 32, 32, innerCircleSizePx, innerCircleSizePx, false, false)


                batch.setColor(1f, 1f, 1f, 1f)
                // Update position
                val deltaTime = Gdx.graphics.deltaTime
                tp.x += tp.veloX * deltaTime
                tp.y += tp.veloY * deltaTime

                if (!tp.isHeldDown) {
                    tp.lifetime -= deltaTime
                } else {
                    tp.holdDuration += Gdx.graphics.deltaTime
                }
            }
        }

        tapPoints.removeIf { it.lifetime <= 0f }
    }

    fun tapDown(mouseX: Float = stage.camera.getInputX(), mouseY: Float = stage.camera.getInputY()) {
        val point: TapPoint = currentTapPoint ?: TapPoint(mouseX, mouseY, 0f, 0f, 1f, 0.25f).also {
            currentTapPoint = it
            tapPoints += it
            onTapDown(it)
        }
        updateTapPoint(point, false)
    }

    fun tapUp(mouseX: Float = stage.camera.getInputX(), mouseY: Float = stage.camera.getInputY()) {
        currentTapPoint?.let { point ->
            point.veloX = (mouseX - point.x) * 25f
            point.veloY = (mouseY - point.y) * 25f
            point.isHeldDown = false

            val veloScalar = Math.sqrt(point.veloX * point.veloX + point.veloY * point.veloY * 1.0).toFloat()
            val isFlick = veloScalar > 700f
            if (isFlick) {
//                println("FLICK   with power $veloScalar\n    Duration: ${point.holdDuration}")
                onFlick(point)
            } else {
//                println("RELEASE with power $veloScalar\n    Duration: ${point.holdDuration}")
                if (point.holdDuration <= 0.1f) {
//                    println("    Short tap")
                }
            }
            onTapRelease(point, !isFlick && point.holdDuration <= 0.1f)
        }
        currentTapPoint = null
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val old = super.touchDown(screenX, screenY, pointer, button)

        if (isMouseOver() && pointer == 0 && button == Input.Buttons.LEFT && visible) {
            tapDown()
            return true
        }

        return old
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val old = super.touchUp(screenX, screenY, pointer, button)

        if (currentTapPoint != null && pointer == 0 && button == Input.Buttons.LEFT && visible) {
            tapUp()
            return true
        }

        return old
    }

    fun updateTapPoint(point: TapPoint, fireSlideEvents: Boolean) {
        val mouseX = stage.camera.getInputX().coerceIn(this.location.realX, this.location.realX + this.location.realWidth)
        val mouseY = stage.camera.getInputY().coerceIn(this.location.realY, this.location.realY + this.location.realHeight)
        point.maxLifetime = 0.125f
        if (!point.isHeldDown) {
            point.lifetime += Gdx.graphics.deltaTime
            if (point.lifetime > point.maxLifetime) {
                point.lifetime = point.maxLifetime
                point.isHeldDown = true
            }
        }
        point.veloX = mouseX - point.x
        point.veloY = mouseY - point.y
        point.x = mouseX
        point.y = mouseY

        if (fireSlideEvents) {
            val veloScalar = Math.sqrt(point.veloX * point.veloX + point.veloY * point.veloY * 1.0).toFloat()
            if (veloScalar > 65f) {
                if (!point.didFireSlideEvent) {
                    onSlide(point)
                    point.didFireSlideEvent = true
                }
            } else {
                point.didFireSlideEvent = false
            }
        }
    }
}