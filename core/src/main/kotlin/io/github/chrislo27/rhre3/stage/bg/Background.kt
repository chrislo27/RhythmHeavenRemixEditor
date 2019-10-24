package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.toolboks.registry.AssetRegistry


abstract class Background(val id: String) {

    abstract fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer, delta: Float)

    companion object {
        val backgroundsNames: Map<Background, String> by lazy {
            linkedMapOf(
                    TengokuBackground("tengoku") to "Tengoku",
                    KarateManBackground("karateMan") to "Karate Man",
                    TilingBackground("rhdsPolkaDots", 5f, speedX = 3f, speedY = -3f, widthCoeff = 0.5f, heightCoeff = 0.5f) { AssetRegistry["bg_polkadot"] } to "DS Game Select",
                    SpaceDanceBackground("spaceDance") to "Space Dance",
                    RetroBackground("retro") to "Retro",
                    TilingBackground("tapTrial", 5f, speedX = 0f, speedY = 1f) { AssetRegistry["bg_tapTrial"] } to "Tap Trial",
                    TilingBackground("tiled", 5f, speedX = 1f, speedY = 1f) { AssetRegistry["bg_tile"] } to "Notes",
                    LaunchPartyBackground("launchParty") to "Launch Party",
                    KittiesBackground("kitties") to "Kitties!",
                    SeesawBackground("seesaw") to "See-Saw",
                    KarateManStripesBackground("karateManStripes1", stripe1 = Color.valueOf("FEC652"),
                    stripe2 = Color.valueOf("FFE86C")) to "Karate Man GBA 1",
                    KarateManStripesBackground("karateManStripes2") to "Karate Man GBA 2",
                    BTSDSBackground("btsDS") to "Built to Scale DS"
                  )
        }
        val backgrounds: List<Background> by lazy { backgroundsNames.keys.toList() }
        val backgroundMap: Map<String, Background> by lazy { backgrounds.associateBy(Background::id) }
        val defaultBackground: Background
            get() = backgroundMap.getValue("tengoku")
    }

}