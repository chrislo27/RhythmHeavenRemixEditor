package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.getInputX
import io.github.chrislo27.toolboks.util.gdxutils.getInputY
import io.github.chrislo27.toolboks.util.gdxutils.setHSB


class TestDSScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, TestDSScreen>(main) {

    data class TapPoint(var x: Float, var y: Float, var veloX: Float, var veloY: Float,
                        var lifetime: Float, var maxLifetime: Float, var lockLifetime: Boolean = false)

    private val bgColor = Color()

    private val tapPoints: MutableList<TapPoint> = mutableListOf()
    private var currentTapPoint: TapPoint? = null

    override fun render(delta: Float) {
        super.render(delta)
        val batch = main.batch
        val camera = main.defaultCamera

        bgColor.setHSB(MathHelper.getSawtoothWave(30f), 1f, 0.58f)

        batch.begin()
        batch.color = bgColor
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
        batch.setColor(1f, 1f, 1f, 1f)

        val tapPointTex: Texture = AssetRegistry["gamemode_tappoint"]
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
            val innerCircleSize = innerCircleSizePx * radiusScale
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

            if (!tp.lockLifetime) {
                tp.lifetime -= deltaTime
            }
        }

        batch.setColor(1f, 1f, 1f, 1f)

        batch.end()

        tapPoints.removeIf { it.lifetime <= 0f }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = ScreenRegistry["editor"]
        }

        val mouseX = main.defaultCamera.getInputX()
        val mouseY = main.defaultCamera.getInputY()

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            val point: TapPoint = currentTapPoint ?: TapPoint(mouseX, mouseY, 0f, 0f, 1f, 0.25f).also {
                currentTapPoint = it
                tapPoints += it
            }
            point.maxLifetime = 0.125f
            if (!point.lockLifetime) {
                point.lifetime += Gdx.graphics.deltaTime
                if (point.lifetime > point.maxLifetime) {
                    point.lifetime = point.maxLifetime
                    point.lockLifetime = true
                }
            }
            point.veloX = mouseX - point.x
            point.veloY = mouseY - point.y
            point.x = mouseX
            point.y = mouseY
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT) && currentTapPoint != null) {
            currentTapPoint?.let { point ->
                point.veloX = (mouseX - point.x) * 24f
                point.veloY = (mouseY - point.y) * 25f
                point.lockLifetime = false
            }
            currentTapPoint = null
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}