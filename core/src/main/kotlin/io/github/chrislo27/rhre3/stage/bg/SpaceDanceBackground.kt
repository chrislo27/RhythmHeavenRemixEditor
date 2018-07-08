package io.github.chrislo27.rhre3.stage.bg

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class SpaceDanceBackground(id: String, val color: Color = Color.valueOf("0029D6"))
    : Background(id) {

    val starfield1: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_starfield"), 1, 2, 510, 374) }
    val starfield2: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_starfield"), 513, 2, 510, 374) }
    val smallTwinkle: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 2, 74, 78, 78) }
    val bigTwinkle: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 82, 74, 78, 78) }
    val bigDiamond: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 162, 74, 78, 78) }
    val smallDiamond: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 242, 74, 78, 78) }
    val starburst: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 114, 2, 70, 70) }
    val dentedStar: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 186, 2, 70, 70) }
    val starCross: TextureRegion by lazy { TextureRegion(AssetRegistry.get<Texture>("bg_sd_stars"), 258, 2, 70, 70) }

    var veloX: Float = 0f
    var veloY: Float = 0f
    var x: Float = 0f
    var y: Float = 0f

    val timeBetweenDirChanges: Float = 10f
    var timeToChangeDir: Float = 0f

    private var first = true

    fun changeDirection() {
        veloX = MathUtils.random(75f) * MathUtils.randomSign()
        veloY = MathUtils.random(75f) * MathUtils.randomSign()
    }

    override fun render(camera: OrthographicCamera, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (first) {
            first = false
            starfield1.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            starfield2.texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            changeDirection()
        }

        val width = camera.viewportWidth
        val height = camera.viewportHeight
        val ratioX = width / RHRE3.WIDTH
        val ratioY = height / RHRE3.HEIGHT

        batch.color = color
        batch.fillRect(0f, 0f, width, height)
        batch.setColor(1f, 1f, 1f, 1f)

        run {
            for (a in -1..1) {
                for (b in -1..1) {
                    batch.draw(starfield2, (x * 0.6f - width * 0.05f) + width * a, (y * 0.6f - height * 0.05f) + height * b, width, height)
                    batch.draw(starfield2, (x * 0.75f - width * 0.1f) + width * a, (y * 0.75f + height * 0.1f) + height * b, width, height)
                    batch.draw(starfield1, (x) + width * a, (y) + height * b, width, height)
                }
            }
        }

        x += Gdx.graphics.deltaTime * veloX
        y += Gdx.graphics.deltaTime * veloY
        x %= width
        y %= height

        timeToChangeDir += Gdx.graphics.deltaTime
        if (timeToChangeDir >= timeBetweenDirChanges) {
            timeToChangeDir %= timeBetweenDirChanges
            changeDirection()
        }
    }

}