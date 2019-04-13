package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry


class InspectionsScreen(main: RHRE3Application, val remix: Remix)
    : ToolboksScreen<RHRE3Application, InspectionsScreen>(main) {

    companion object {
        val GLYPHS = listOf("◉", "○", "\uE149", "\uE14A")
    }

    override val stage: GenericStage<InspectionsScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections_big"))
        stage.titleLabel.text = "screen.inspections.title"
        stage.onBackButtonClick = { main.screen = ScreenRegistry["editor"] }
        stage.backButton.visible = true
    }

    override fun renderUpdate() {
        super.renderUpdate()

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.enabled && stage.backButton.visible) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}