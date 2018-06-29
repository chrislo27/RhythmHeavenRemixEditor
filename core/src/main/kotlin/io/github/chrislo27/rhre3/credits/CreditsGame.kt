package io.github.chrislo27.rhre3.credits

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import io.github.chrislo27.toolboks.util.gdxutils.isControlDown
import io.github.chrislo27.toolboks.util.gdxutils.isShiftDown
import rhmodding.bccadeditor.bccad.BCCAD


class CreditsGame(main: RHRE3Application) : ToolboksScreen<RHRE3Application, CreditsGame>(main) {

    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 1280f, 720f)
    }
    private val bccad: BCCAD = BCCAD(Gdx.files.internal("credits/frog.bccad").file())
    private val sheet: Texture by lazy { AssetRegistry.get<Texture>("credits_frog") }

    var index: Int = 0
    var aniIndex = 0
    var frameNum = 0

    init {
        println(bccad.sprites.first())
        println(bccad.animations.first())
    }

    override fun render(delta: Float) {
        super.render(delta)

        val batch = main.batch
        val oldProjMatrix = batch.projectionMatrix
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.color = Color.LIGHT_GRAY
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
        
        batch.setColor(1f, 1f, 1f, 1f)
        bccad.sprites[index].render(batch, sheet, 0f, 0f)
        bccad.animations[aniIndex].render(batch, sheet, bccad.sprites, frameNum, 256f, 0f)

        val font = main.defaultFont
        font.scaleFont()
        font.setColor(0f, 0f, 0f, 1f)

        font.draw(batch, "sprite $index\nanimation $aniIndex - ${bccad.animations[aniIndex].name}\n$frameNum", 200f, 100f)
        font.setColor(1f, 1f, 1f, 1f)
        font.unscaleFont()

        batch.end()

        batch.projectionMatrix = oldProjMatrix

        frameNum++
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            main.screen = ScreenRegistry["info"]
        }

        if (Gdx.input.isShiftDown()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                aniIndex += if (Gdx.input.isControlDown()) 5 else 1
                if (aniIndex >= bccad.animations.size)
                    aniIndex = 0
                println(bccad.animations[aniIndex])
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                aniIndex -= if (Gdx.input.isControlDown()) 5 else 1
                if (aniIndex < 0)
                    aniIndex = bccad.animations.size - 1
                println(bccad.animations[aniIndex])
            }
        } else {
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
                index += if (Gdx.input.isControlDown()) 5 else 1
                if (index >= bccad.sprites.size)
                    index = 0
                println(bccad.sprites[index])
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
                index -= if (Gdx.input.isControlDown()) 5 else 1
                if (index < 0)
                    index = bccad.sprites.size - 1
                println(bccad.sprites[index])
            }
        }
    }

    private fun BitmapFont.scaleFont() {
        this.setUseIntegerPositions(false)
        this.data.setScale(camera.viewportWidth / main.defaultCamera.viewportWidth,
                           camera.viewportHeight / main.defaultCamera.viewportHeight)
    }

    private fun BitmapFont.unscaleFont() {
        this.setUseIntegerPositions(true)
        this.data.setScale(1f)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }
}