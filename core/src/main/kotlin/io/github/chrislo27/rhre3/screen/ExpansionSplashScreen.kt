package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.util.gdxutils.drawCompressed
import io.github.chrislo27.toolboks.util.gdxutils.drawQuad
import io.github.chrislo27.toolboks.util.gdxutils.fillRect
import kotlin.math.roundToInt


class ExpansionSplashScreen(main: RHRE3Application, val nextScreen: Screen?)
    : ToolboksScreen<RHRE3Application, ExpansionSplashScreen>(main), HidesVersionText {

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
    private val gradientAlpha get() = Interpolation.pow2.apply(((timeElapsed) / 4).coerceAtMost(1f)).coerceIn(0f, 1f)
    private val logoAlpha get() = interpolation.apply(((timeElapsed - 2) / 2).coerceAtMost(1f)).coerceIn(0f, 1f)
    private val textAlpha get() = interpolation.apply(((timeElapsed - 4) / 2).coerceIn(0f, 1f))
    private val enterAlpha get() = Interpolation.circleOut.apply(((timeElapsed - 5.5f) / 0.5f).coerceIn(0f, 1f))
    private val colorAlpha get() = Interpolation.sine.apply(((timeElapsed - 6f) / 3f).coerceAtLeast(0f))

    // Animation:
    // Gradient begins to climb for first 2 seconds (to 50%)
    // EXPANSION text ascends for 2 seconds
    // Detailed text ascends in 2 seconds, logo scales down
    // Gradient can begin fading in and out, complete period of 6 seconds
    // ENTER text fades in in 0.5 seconds, @5.5 seconds

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

        val gradientHeight = camera.viewportHeight * 0.85f
        val gradientY = gradientHeight * gradientAlpha - gradientHeight
        val logoY = gradientHeight * logoAlpha - gradientHeight
        // Glowing gradient
        tempBottomColor.set(bottomColor).lerp(bottomColorGlow, colorAlpha)
        val bottomGlow = tempBottomColor.toFloatBits()
        batch.drawQuad(0f, gradientY, bottomGlow, camera.viewportWidth, gradientY, bottomGlow,
                       camera.viewportWidth, gradientY + gradientHeight, topColor.toFloatBits(), 0f, gradientY + gradientHeight, topColor.toFloatBits())

        batch.setColor(1f, 1f, 1f, 1f)
        val logo = AssetRegistry.get<Texture>("logo_1024")
        val logoSize = camera.viewportHeight * MathUtils.lerp(0.8f, 0.55f, textAlpha)
        val expansionSize = logoSize * 1.4f
        batch.draw(logo, camera.viewportWidth * 0.5f - logoSize / 2f, camera.viewportHeight - logoSize, logoSize, logoSize)

        batch.draw(AssetRegistry.get<Texture>("logo_expansion_text"), camera.viewportWidth * 0.5f - expansionSize / 2f, logoY * 2 + (camera.viewportHeight * 1.1f - expansionSize), expansionSize, expansionSize)

        val font = main.defaultBorderedFont
        font.setColor(1f, 1f, 1f, 1f)
        font.scaleFont()

        // keystroke text ➡
        val textY = gradientHeight * textAlpha - gradientHeight
        font.drawCompressed(batch, "Welcome to the RHRExpansion.", 0f, textY + camera.viewportHeight * 0.35f, camera.viewportWidth, Align.center)
        font.drawCompressed(batch, "➡ Resize the track\n➡ Store your own patterns\n➡ Use swing tempo\n[X]➡ []And more...", camera.viewportWidth * 0.35f, textY + camera.viewportHeight * 0.30f, camera.viewportWidth * 0.3f, Align.left)
        font.drawCompressed(batch, "View the release changelog for more details.", 0f, textY + camera.viewportHeight * 0.1125f, camera.viewportWidth, Align.center)

        font.setColor(1f, 1f, 1f, enterAlpha)
        font.drawCompressed(batch, "[[[#00FFFF${(enterAlpha * 255).roundToInt().toString(16).padStart(2, '0')}]ENTER[]]", 0f, textY + camera.viewportHeight * 0.05f, camera.viewportWidth, Align.center)

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
        } else if (enterAlpha >= 1f && (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE))) {
            main.preferences.putBoolean(PreferenceKeys.SEEN_EXPANSION_SPLASH, true).flush()
            main.screen = nextScreen
        }
    }

    override fun getDebugString(): String? {
        return "time: $timeElapsed\ngradient: $gradientAlpha\nlogo: $logoAlpha\ntext: $textAlpha\nenter: $enterAlpha\ncolor: $colorAlpha"
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}