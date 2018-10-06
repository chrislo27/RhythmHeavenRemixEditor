package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.registry.AssetRegistry


abstract class Background(val id: String) {

    abstract fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float)

    companion object {
        val backgrounds: List<Background> by lazy {
            listOf(
                    TengokuBackground("tengoku"),
                    KarateManBackground("karateMan"),
                    TilingBackground("rhdsPolkaDots", 5f, speedX = 3f, speedY = -3f, widthCoeff = 0.5f, heightCoeff = 0.5f) { AssetRegistry["bg_polkadot"] },
                    SpaceDanceBackground("spaceDance"),
                    RetroBackground("retro"),
                    TilingBackground("tapTrial", 5f, speedX = 0f, speedY = 1f) { AssetRegistry["bg_tapTrial"] },
                    TilingBackground("tiled", 5f, speedX = 1f, speedY = 1f) { AssetRegistry["bg_tile"] }
                  )
        }
        val backgroundMap: Map<String, Background> by lazy { backgrounds.associateBy(Background::id) }
        val defaultBackground: Background
            get() = backgroundMap["tengoku"]!!
    }

}