package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils


object PieChartRenderer {

    fun render(shape: ShapeRenderer, x: Float, y: Float, radius: Float, data: Map<Slice, Float>, mouseX: Float,
               mouseY: Float, onHover: ((Map.Entry<Slice, Float>, Float) -> Unit)? = null,
               startingDegree: Float = 0f, degreesPerSegment: Int = 6) {
        val total = data.map { it.value }.sum()

        var currentDegree = 0f
        data.forEach {
            val degreePiece: Float = it.value / total * 360
            val oldColor = shape.color
            shape.color = it.key.color

            shape.arc(x, y, radius, currentDegree + startingDegree, degreePiece,
                      (degreePiece / degreesPerSegment).toInt().coerceAtLeast(2))

            shape.color = oldColor
            currentDegree += degreePiece
        }

        if (onHover != null) {
            val distance = Math.sqrt(Math.pow(x - mouseX.toDouble(), 2.0) + Math.pow(y - mouseY.toDouble(), 2.0))
            if (distance <= radius) {
                var atan2: Float = MathUtils.atan2(mouseY - y, mouseX - x)
                if (atan2 < 0)
                    atan2 += MathUtils.PI2
                val angle: Float = (atan2 * MathUtils.radDeg + startingDegree) % 360f

                currentDegree = 0f
                for (it in data) {
                    val degreePiece: Float = it.value / total * 360
                    if (angle in (currentDegree + startingDegree)..(currentDegree + startingDegree + degreePiece)) {
                        onHover(it, it.value / total)
                        break
                    }

                    currentDegree += degreePiece
                }
            }
        }
    }

}

data class Slice(val color: Color, var name: String)