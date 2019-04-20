package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.ClickOccupation
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.TextureModel
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.Remix


class TextureEntity(remix: Remix, datamodel: TextureModel)
    : ModelEntity<TextureModel>(remix, datamodel), IStretchable {

    var textureHash: String? = null
    override val isStretchable: Boolean = true
    override val renderText: String
        get() = if (textureHash != null) datamodel.name else "${datamodel.name}\n<no texture>"
    override val glassEffect: Boolean = false

    init {
        this.bounds.height = 1f
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.cue
    }

    override fun renderWithGlass(editor: Editor, batch: SpriteBatch, glass: Boolean) {
        val textureHash = textureHash
        if (textureHash != null) {
            val tex = remix.textureCache[textureHash] ?: return super.render(editor, batch)
            val renderBacking = this.isSelected || editor.clickOccupation is ClickOccupation.CreatingSelection
            if (renderBacking) {
                super.renderWithGlass(editor, batch, glass)
            }
            if (this.isSelected) {
                batch.color = editor.theme.entities.selectionTint
            }
            if (renderBacking) {
                batch.color = batch.color.apply { a *= 0.25f }
            }
            val ratio = tex.height.toFloat() / tex.width
            batch.draw(tex, bounds.x + lerpDifference.x, bounds.y + lerpDifference.y, bounds.width + lerpDifference.width, (bounds.width + lerpDifference.height) * ratio / (Editor.ENTITY_HEIGHT / Editor.ENTITY_WIDTH))
            batch.setColor(1f, 1f, 1f, 1f)
        } else {
            super.renderWithGlass(editor, batch, glass)
        }
    }

    override fun onStart() {
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
    }

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)
        objectNode.put("texHash", textureHash)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        textureHash = objectNode["texHash"].asText(null)
    }

    override fun copy(remix: Remix): TextureEntity {
        return TextureEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@TextureEntity.bounds)
            }
            // Set image ID
            it.textureHash = this@TextureEntity.textureHash
        }
    }

}