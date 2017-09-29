package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.*


class StatsButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                       stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_inspections"))
        })
    }

    override fun getHoverText(): String {
        return Localization["screen.stats.title"]
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.main.screen = ScreenRegistry.getNonNull("stats")
    }
}