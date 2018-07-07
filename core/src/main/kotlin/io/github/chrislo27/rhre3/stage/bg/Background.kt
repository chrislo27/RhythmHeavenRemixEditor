package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.registry.AssetRegistry


abstract class Background(val id: String) {

    abstract fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer)

    companion object {
        val backgrounds: List<Background> by lazy { listOf(
                TengokuBackground("tengoku"),
                KarateManBackground("karateMan"),
                TilingBackground("tiled", 5f) { AssetRegistry["ui_bg"] }
                                                          ) }
        val backgroundMap: Map<String, Background> by lazy { backgrounds.associateBy(Background::id) + listOf("TENGOKU" to backgrounds.first { it.id == "tengoku" }, "KARATE_MAN" to backgrounds.first { it.id == "karateMan" }, "TILED" to backgrounds.first { it.id == "tiled" }) }
        val defaultBackground: Background
            get() = backgroundMap["TENGOKU"]!!
    }

}