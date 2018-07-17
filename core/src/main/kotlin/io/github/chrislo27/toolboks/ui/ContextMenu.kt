package io.github.chrislo27.toolboks.ui

import io.github.chrislo27.toolboks.ToolboksScreen


/**
 * A stage that disappears if you click and none of its children respond.
 */
open class ContextMenu<S : ToolboksScreen<*, *>> : Stage<S> {

    constructor(parent: UIElement<S>) : super(parent.stage, parent.stage.camera)

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val result = super.touchDown(screenX, screenY, pointer, button)

        if (!result) {
            this.visible = false
        }

        return result
    }

}