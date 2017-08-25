package io.github.chrislo27.rhre3.stage

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Checkbox
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.UIElement
import io.github.chrislo27.toolboks.ui.UIPalette


open class TrueCheckbox<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : Checkbox<S>(palette, parent, stage) {

    override val uncheckedTex: TextureRegion? = TextureRegion(AssetRegistry.get<Texture>("ui_icon_uncheckedbox"))
    override val checkedTex: TextureRegion? = TextureRegion(AssetRegistry.get<Texture>("ui_icon_checkedbox"))

}

open class FalseCheckbox<S : ToolboksScreen<*, *>>(palette: UIPalette, parent: UIElement<S>, stage: Stage<S>)
    : Checkbox<S>(palette, parent, stage) {

    override val uncheckedTex: TextureRegion? = TextureRegion(AssetRegistry.get<Texture>("ui_icon_uncheckedbox"))
    override val checkedTex: TextureRegion? = TextureRegion(AssetRegistry.get<Texture>("ui_icon_xcheckedbox"))

}
