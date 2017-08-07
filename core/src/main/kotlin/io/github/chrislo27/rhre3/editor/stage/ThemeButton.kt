package io.github.chrislo27.rhre3.editor.stage

import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.ui.*


class ThemeButton(palette: UIPalette, parent: UIElement<EditorScreen>,
                  stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage) {

    val contextMenu: ThemeContextMenu by lazy {
        ThemeContextMenu(stage).apply {
            this.location.set(screenWidth = this.stage.percentageOfWidth(256f),
                              screenHeight = this.stage.percentageOfHeight(256f))
            this.location.set(screenX = this@ThemeButton.location.screenX,
                              screenY = this@ThemeButton.location.screenY - this.location.screenHeight)
            this.elements +=
                    ColourPane(this, this.stage).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                    }
            this.visible = false
        }
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        contextMenu.visible = true
    }
}

class ThemeContextMenu(parent: UIElement<EditorScreen>) : ContextMenu<EditorScreen>(parent) {

}
