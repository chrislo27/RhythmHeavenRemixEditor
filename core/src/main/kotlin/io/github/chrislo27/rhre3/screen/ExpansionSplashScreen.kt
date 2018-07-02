package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.MathHelper
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.fillRect


class ExpansionSplashScreen(main: RHRE3Application, val nextScreen: Screen?)
    : ToolboksScreen<RHRE3Application, ExpansionSplashScreen>(main) {

    private val camera = OrthographicCamera().apply {
        viewportWidth = RHRE3.WIDTH.toFloat()
        viewportHeight = RHRE3.HEIGHT.toFloat()
        position.x = viewportWidth / 2f
        position.y = viewportHeight / 2f
        update()
    }
    private val topColor: Color = Color(0f, 0f, 0f, 1f)
    private val bottomColor: Color = Color.valueOf("2b4a73")
    private val bottomColorGlow: Color = Color.valueOf("3C669E")
    private val tempBottomColor: Color = Color(bottomColor)
    private var timeElapsed = 0f
    private val interpolation: Interpolation get() = Interpolation.pow5Out
    private val logoAlpha get() = interpolation.apply((timeElapsed / 4).coerceAtMost(1f)).coerceIn(0f, 1f)
    private val textAlpha get() = interpolation.apply(((timeElapsed - 2) / 2).coerceIn(0f, 1f)).coerceIn(0f, 1f)

    private fun BitmapFont.scaleFont() {
        this.setUseIntegerPositions(false)
        this.data.setScale(camera.viewportWidth / main.defaultCamera.viewportWidth,
                           camera.viewportHeight / main.defaultCamera.viewportHeight)
    }

    private fun BitmapFont.unscaleFont() {
        this.setUseIntegerPositions(true)
        this.data.setScale(1f)
    }

    override fun render(delta: Float) {
        super.render(delta)

        val batch = main.batch
        val oldProj = batch.projectionMatrix
        camera.update()
        batch.projectionMatrix = camera.combined
        batch.begin()
        batch.setColor(0f, 0f, 0f, 1f)
        batch.fillRect(0f, 0f, camera.viewportWidth, camera.viewportHeight)
        batch.setColor(1f, 1f, 1f, 1f)

        val gradientHeight = camera.viewportHeight * 0.75f
        val gradientY = gradientHeight * logoAlpha - gradientHeight
        // Glowing gradient
        tempBottomColor.set(bottomColor).lerp(bottomColorGlow, MathHelper.getBaseCosineWave(3f))
        val bottomGlow = tempBottomColor.toFloatBits()
        batch.drawQuad(0f, gradientY, bottomGlow, camera.viewportWidth, gradientY, bottomGlow,
                       camera.viewportWidth, gradientY + gradientHeight, topColor.toFloatBits(), 0f, gradientY + gradientHeight, topColor.toFloatBits())

        batch.setColor(1f, 1f, 1f, 1f)
        val logo = AssetRegistry.get<Texture>("logo_1024")
        val logoSize = camera.viewportWidth * 0.3f
        val expansionSize = logoSize * 1.4f
        batch.draw(logo, camera.viewportWidth * 0.5f - logoSize / 2f, camera.viewportHeight - logoSize, logoSize, logoSize)

        batch.draw(AssetRegistry.get<Texture>("logo_expansion_text"), camera.viewportWidth * 0.5f - expansionSize / 2f, gradientY * 2 + (camera.viewportHeight * 1.1f - expansionSize), expansionSize, expansionSize)

        val font = main.defaultBorderedFont
        font.setColor(1f, 1f, 1f, 1f)
        font.scaleFont()

        // keystroke text ➡
        val textY = gradientHeight * textAlpha - gradientHeight
        font.drawCompressed(batch, "Welcome to the RHRExpansion.", 0f, textY + camera.viewportHeight * 0.35f, camera.viewportWidth, Align.center)
        font.drawCompressed(batch, "➡ Resize the track\n➡ Store your own patterns\n➡ Use swing tempo\n[X]➡ []And more...", camera.viewportWidth * 0.35f, textY + camera.viewportHeight * 0.30f, camera.viewportWidth * 0.3f, Align.left)
        font.drawCompressed(batch, "View the release changelog for all the details.", 0f, textY + camera.viewportHeight * 0.1125f, camera.viewportWidth, Align.center)
        font.drawCompressed(batch, "[[[CYAN]ENTER[]]", 0f, textY + camera.viewportHeight * 0.05f, camera.viewportWidth, Align.center)

        font.setColor(1f, 1f, 1f, 1f)
        font.unscaleFont()


        batch.end()
        batch.projectionMatrix = oldProj

        timeElapsed += Gdx.graphics.deltaTime
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            timeElapsed = 0f
        } else if (textAlpha >= 1f && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            main.preferences.putBoolean(PreferenceKeys.SEEN_EXPANSION_SPLASH, true).flush()
            main.screen = nextScreen
        }
    }

    override fun getDebugString(): String? {
        return "time: $timeElapsed"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}