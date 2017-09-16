package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Pool


object RectanglePool : Pool<Rectangle>(64) {

    inline fun use(func: (Rectangle) -> Unit) {
        val rect = obtain()
        func(rect)
        free(rect)
    }

    override fun newObject(): Rectangle {
        return Rectangle()
    }

    override fun reset(`object`: Rectangle?) {
        super.reset(`object`)
        `object`?.set(0f, 0f, 0f, 0f)
    }
}