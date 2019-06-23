package io.github.chrislo27.rhre3.entity.model.special

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.MathUtils
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.model.IEditableText
import io.github.chrislo27.rhre3.entity.model.IStretchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.Subtitle
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.Subtitle.SubtitleType.SONG_ARTIST
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.Subtitle.SubtitleType.SONG_TITLE
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.Subtitle.SubtitleType.SUBTITLE
import io.github.chrislo27.rhre3.sfxdb.datamodel.impl.special.Subtitle.SubtitleType.TYPEWRITER
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.track.EditorRemix
import io.github.chrislo27.rhre3.track.Remix
import io.github.chrislo27.toolboks.ui.TextField
import kotlin.math.roundToInt


class SubtitleEntity(remix: Remix, datamodel: Subtitle)
    : ModelEntity<Subtitle>(remix, datamodel), IStretchable, IEditableText {

    override val isStretchable: Boolean = true
    var subtitle: String = ""
    override val renderText: String
        get() = "${datamodel.name}\n\"${subtitle.replace("\r", "").replace("\r\n", "${TextField.NEWLINE_WRAP}").replace('\r', TextField.NEWLINE_WRAP).replace('\n', TextField.NEWLINE_WRAP)}[]\""
    override var text: String
        get() = subtitle
        set(value) {
            subtitle = value
        }
    override val canInputNewlines: Boolean
        get() = datamodel.type.canInputNewlines

    init {
        bounds.height = 1f
    }
    
    fun getSubtitleText(): String {
        if (datamodel.type != Subtitle.SubtitleType.TYPEWRITER)
            return subtitle
        val sub = subtitle
        val firstRightBracket = sub.indexOf(']') + 1
        return sub.substring(0, MathUtils.lerp(firstRightBracket.coerceAtLeast(0).toFloat(), sub.length.toFloat(), (remix.beat - bounds.x) / bounds.width).roundToInt().coerceIn(0, sub.length))
    }

    override fun saveData(objectNode: ObjectNode) {
        super.saveData(objectNode)
        objectNode.put("subtitle", subtitle)
    }

    override fun readData(objectNode: ObjectNode) {
        super.readData(objectNode)
        subtitle = objectNode["subtitle"].asText("<failed to read text>")
    }

    override fun getRenderColor(editor: Editor, theme: Theme): Color {
        return theme.entities.special
    }

    override fun onStart() {
        when (datamodel.type) {
            SUBTITLE, TYPEWRITER -> {
                if (this !in remix.currentSubtitles) {
                    remix.currentSubtitles += this
                }
            }
            SONG_TITLE -> (remix as? EditorRemix)?.editor?.songTitle(subtitle)
            SONG_ARTIST -> (remix as? EditorRemix)?.editor?.songArtist(subtitle)
        }
    }

    override fun whilePlaying() {
    }

    override fun onEnd() {
        when (datamodel.type) {
            SUBTITLE, TYPEWRITER -> remix.currentSubtitles.remove(this)
            SONG_TITLE -> (remix as? EditorRemix)?.editor?.songTitle(null)
            SONG_ARTIST -> (remix as? EditorRemix)?.editor?.songArtist(null)
        }
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
