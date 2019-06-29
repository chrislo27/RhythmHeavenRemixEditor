package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.Editor.ScrollMode
import io.github.chrislo27.rhre3.editor.Tool
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class ScrollModeButton(val editor: Editor, palette: UIPalette,
                       parent: UIElement<EditorScreen>,
                       stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    companion object {
        private val texRegions: Map<ScrollMode, TextureRegion> by lazy {
            ScrollMode.values().associate {
                it to TextureRegion(AssetRegistry.get<Texture>(it.icon))
            }
        }
    }

    init {
        addLabel(ImageLabel(palette, this, this.stage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.image = null
        })
    }

    private val label: ImageLabel<EditorScreen>
        get() = labels.first() as ImageLabel
    private var lastScrollMode: ScrollMode = editor.scrollMode

    override var tooltipText: String?
        set(_) {}
        get() {
            return Localization["editor.scrollMode"] + "\n" + if (editor.currentTool != Tool.SELECTION) {
                Localization["editor.scrollMode.useSelectionTool", "${Tool.SELECTION.index + 1}"]
            } else {
                Localization[lastScrollMode.buttonLocalization]
            }
        }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        if (lastScrollMode != editor.scrollMode || label.image == null) {
            lastScrollMode = editor.scrollMode
            label.image = texRegions[lastScrollMode]
        }

        this.enabled = editor.currentTool == Tool.SELECTION

        super.render(screen, batch, shapeRenderer)
    }

    override fun onRightClick(xPercent: Float, yPercent: Float) {
        super.onRightClick(xPercent, yPercent)
        editor.cycleScrollMode(-1)
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        editor.cycleScrollMode(1)
    }
}