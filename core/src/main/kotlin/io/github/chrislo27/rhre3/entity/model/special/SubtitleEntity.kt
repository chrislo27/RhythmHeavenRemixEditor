package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.registry.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.track.Remix


class SubtitleEntity(remix: Remix, datamodel: Subtitle)
    : ModelEntity<Subtitle>(remix, datamodel), IStretchable {

    override val isStretchable: Boolean = true
    var subtitle: String = ""
    override val renderText: String
        get() = "${datamodel.name}\n\"$subtitle[]\""

    init {
        bounds.width = 1f
        bounds.height = 1f
    }

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)
        objectNode.put("subtitle", subtitle)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        subtitle = objectNode["subtitle"].asText("<failed to read text>")
    }

    override fun getRenderColor(): Color {
        return remix.editor.theme.entities.special
    }

    override fun onStart() {
        if (this !in remix.currentSubtitles) {
            remix.currentSubtitles += this
        }
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
        remix.currentSubtitles.remove(this)
    }

    override fun copy(remix: Remix): SubtitleEntity {
        return SubtitleEntity(remix, datamodel).also {
            it.updateBounds {
                it.bounds.set(this@SubtitleEntity.bounds)
            }
            it.subtitle = subtitle
        }
    }

}
