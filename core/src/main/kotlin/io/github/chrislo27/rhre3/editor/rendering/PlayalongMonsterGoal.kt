package io.github.chrislo27.rhre3.editor.rendering

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
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
import java.util.*


private val bccad: BCCAD by lazy { BCCAD(Base64.getDecoder().decode(Gdx.files.internal("images/playalong/monsterGoal.bin").readString("UTF-8"))) }
private val sheet: Texture by lazy { AssetRegistry.get<Texture>("playalong_monster_goal") }
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

    val screenCompress: Float = MathUtils.lerp(1f, 0f, (currentFrame / 10f).coerceIn(0f, 1f))

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
        monsterAnimation.render(batch, sheet, bccad.sprites, (currentFrame / 2).coerceIn(0, monsterAnimationDuration - 1),
                                monsterMawCamera.position.x - monsterMawCamera.viewportHeight / 2 - 152,
                                monsterMawCamera.position.y - monsterMawCamera.viewportHeight / 2 - 204)
        batch.setColor(0.25f, 0.9f, 0.25f, 1f)
        val w = (monsterMawCamera.viewportWidth / (camera.zoom / monsterMawCamera.zoom)) * 1.05f
        val h = (monsterMawCamera.viewportHeight / (camera.zoom / monsterMawCamera.zoom)) * 1.075f * screenCompress
        batch.fillRect(monsterMawCamera.position.x - w / 2, monsterMawCamera.position.y - h / 2, w, h)
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