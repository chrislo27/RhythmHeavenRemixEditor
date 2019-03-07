package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.github.chrislo27.rhre3.RHRE3Application


fun BitmapFont.scaleFont(camera: OrthographicCamera) {
    this.setUseIntegerPositions(false)
    this.data.setScale(camera.viewportWidth / RHRE3Application.instance.defaultCamera.viewportWidth,
                       camera.viewportHeight / RHRE3Application.instance.defaultCamera.viewportHeight)
}

fun BitmapFont.unscaleFont() {
    this.setUseIntegerPositions(true)
    this.data.setScale(1f)
}