package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.track.PlayState
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.prepareStencilMask
import io.github.chrislo27.toolboks.util.gdxutils.useStencilMask
import rhmodding.bccadeditor.bccad.Animation
import rhmodding.bccadeditor.bccad.BCCAD


private val bccad: BCCAD by lazy { BCCAD(Gdx.files.internal("images/playalong/monsterGoal.bin").readBytes()) }
private val sheet: Texture by lazy { AssetRegistry.get<Texture>("playalong_monster_goal").apply {
    this.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
} }
private val frameTexReg: TextureRegion by lazy { TextureRegion(sheet, 233 + 1, 169 + 1, 142 - 2, 98 - 2) }
private val innerFrameTexReg: TextureRegion by lazy { TextureRegion(sheet, 246, 182, 116, 72) }
private val monsterMawCamera: OrthographicCamera = OrthographicCamera().also { camera ->
    camera.viewportWidth = RHRE3.WIDTH.toFloat()
    camera.viewportHeight = RHRE3.HEIGHT.toFloat()
    camera.update()
}

private val monsterAnimation: Animation = bccad.animations.first { it.name == "mouth_close" }
private val monsterAnimationDuration: Int = monsterAnimation.steps.sumBy { it.duration.toInt() }
private var currentFrame: Int = 0

fun Editor.renderPlayalongMonsterGoal(batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
    val playalong = remix.playalong

    batch.flush()

    val screenCompress: Float = MathUtils.lerp(1f, 0f, (currentFrame / 5f).coerceIn(0f, 1f))

    // Add black bars
    batch.setColor(0f, 0f, 0f, 1f)
    val zoomedWidth = camera.viewportWidth * camera.zoom
    val zoomedHeight = camera.viewportHeight * camera.zoom
    val left = camera.position.x - zoomedWidth / 2
    val right = camera.position.x + zoomedWidth / 2
    val bottom = camera.position.y - zoomedHeight / 2
    val top = camera.position.y + zoomedHeight / 2
    val leftBarWidth = camera.position.x - camera.viewportWidth / 2 - left
    val bottomBarHeight = camera.position.y - camera.viewportHeight / 2 * screenCompress - bottom
    batch.fillRect(left, bottom, leftBarWidth, zoomedHeight)
    batch.fillRect(right, bottom, -leftBarWidth, zoomedHeight)
    batch.fillRect(left, bottom, zoomedWidth, bottomBarHeight)
    batch.fillRect(left, top, zoomedWidth, -bottomBarHeight)
    batch.setColor(1f, 1f, 1f, 1f)

//    val oldZoom = staticCamera.zoom
    monsterMawCamera.zoom = 0.425f
    monsterMawCamera.position.x = 0f
    monsterMawCamera.position.y = 0f
    monsterMawCamera.update()
    batch.projectionMatrix = monsterMawCamera.combined
    shapeRenderer.projectionMatrix = monsterMawCamera.combined

    val newWidth = monsterMawCamera.viewportWidth * monsterMawCamera.zoom
    val newHeight = monsterMawCamera.viewportHeight * monsterMawCamera.zoom
    val l = monsterMawCamera.position.x - newWidth / 2
    val r = monsterMawCamera.position.x + newWidth / 2
    val b = monsterMawCamera.position.y - newHeight / 2
    val t = monsterMawCamera.position.y + newHeight / 2
    val lBarWidth = (monsterMawCamera.position.x - monsterMawCamera.viewportWidth / 2 / (camera.zoom / monsterMawCamera.zoom)) - l
    val bBarHeight = (monsterMawCamera.position.y - screenCompress * monsterMawCamera.viewportHeight / 2 / (camera.zoom / monsterMawCamera.zoom)) - b
    val innerWidth = (monsterMawCamera.position.x - l - lBarWidth) * 2
    val innerHeight = (monsterMawCamera.position.y - b - bBarHeight) * 2

    shapeRenderer.prepareStencilMask(batch) {
        begin(ShapeRenderer.ShapeType.Filled)
        rect(l, b, lBarWidth, newHeight)
        rect(r, b, -lBarWidth, newHeight)
        rect(l, b, newWidth, bBarHeight)
        rect(l, t, newWidth, -bBarHeight)
//        rect(monsterMawCamera.position.x - monsterMawCamera.viewportWidth / 2 * camera.zoom, monsterMawCamera.position.y - monsterMawCamera.viewportHeight / 2 * camera.zoom, monsterMawCamera.viewportWidth, monsterMawCamera.viewportHeight)
        end()
    }.useStencilMask {
        //        batch.draw(AssetRegistry.get<Texture>("credits_frog"), monsterMawCamera.position.x - monsterMawCamera.viewportWidth / 2, monsterMawCamera.position.y - monsterMawCamera.viewportHeight / 2, monsterMawCamera.viewportWidth, monsterMawCamera.viewportHeight)
        monsterAnimation.render(batch, sheet, bccad.sprites, currentFrame.coerceIn(0, monsterAnimationDuration - 1),
                                monsterMawCamera.position.x - monsterMawCamera.viewportHeight / 2 - 152,
                                monsterMawCamera.position.y - monsterMawCamera.viewportHeight / 2 - 204)
        batch.setColor(0f, 200f / 255f, 50f / 255f, 1f)
        val frameWidth = innerWidth * (frameTexReg.regionWidth.toFloat() / innerFrameTexReg.regionWidth)
        val frameHeight = innerHeight * (frameTexReg.regionHeight.toFloat() / innerFrameTexReg.regionHeight)
        batch.draw(frameTexReg, monsterMawCamera.position.x - frameWidth / 2,
                   monsterMawCamera.position.y - frameHeight / 2, frameWidth, frameHeight)
        batch.setColor(1f, 1f, 1f, 1f)
    }

    batch.flush()
    batch.projectionMatrix = camera.combined

    if (remix.playState != PlayState.STOPPED && playalong.untilMonsterChomps <= 0f) {
        currentFrame++
    }
    if (playalong.untilMonsterChomps > 0f) {
        currentFrame = 0
    }
}