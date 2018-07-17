package io.github.chrislo27.toolboks.ui

import io.github.chrislo27.toolboks.ToolboksScreen


abstract class Label<S : ToolboksScreen<*, *>>
    : UIElement<S>, Palettable, Backgrounded {
    override var palette: UIPalette

    constructor(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>) : super(parent, stage) {
        this.palette = palette
    }

}