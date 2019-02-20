package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


fun Editor.renderBackground(batch: SpriteBatch, shapeRenderer: ShapeRenderer, camera: OrthographicCamera, updateDelta: Boolean) {
    val bgColour = theme.background
    batch.setColor(bgColour.r, bgColour.g, bgColour.b, 1f)
    batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
    batch.setColor(1f, 1f, 1f, 1f)

    if (main.preferences.getBoolean(PreferenceKeys.THEME_USES_MENU, false)) {
        GenericStage.backgroundImpl.render(camera, batch, shapeRenderer, if (updateDelta) Gdx.graphics.deltaTime else 0f)
    }

    theme.textureObj?.let { themeTex ->
        batch.draw(themeTex, 0f, 0f, camera.viewportWidth, camera.viewportHeight)
    }

//        if (Toolboks.debugMode && remix.tempos.map.isEmpty()) {
//            this.renderImplicitTempo(batch)
//        }
}