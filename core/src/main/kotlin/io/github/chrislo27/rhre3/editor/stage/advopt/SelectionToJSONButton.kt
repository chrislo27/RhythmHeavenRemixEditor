package io.github.chrislo27.rhre3.editor.stage.advopt

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.fasterxml.jackson.annotation.JsonInclude
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.entity.model.IRepitchable
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ui.*
import kotlin.math.roundToInt


class SelectionToJSONButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                            stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    companion object {
        val strings: List<String> = listOf("Store selection as JSON\nand copy to clipboard", "[CYAN]Copied successfully![]", "No selection...", "An [RED]error[] occurred\nPlease see console")
    }

    init {
        this.visible = false
    }

    private var resetTextIn: Float = 0f

    private val label: TextLabel<EditorScreen> = TextLabel(palette, this, stage).apply {
        this@SelectionToJSONButton.addLabel(this)
        this.fontScaleMultiplier = 0.5f
        this.text = strings[0]
        this.isLocalizationKey = false
    }

    override fun getHoverText(): String {
        return when (label.text) {
            strings[1] -> "Copied successfully to clipboard!"
            strings[2] -> "Make a selection first"
            strings[3] -> strings[3]
            else -> "Click to convert entity selection to JSON\nand copy to clipboard"
        }
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        super.render(screen, batch, shapeRenderer)
        if (resetTextIn > 0) {
            resetTextIn -= Gdx.graphics.deltaTime
            if (resetTextIn <= 0) {
                label.text = strings[0]
            }
        }
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        this.visible = editor.main.advancedOptions
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)

        val selection = editor.selection.toList().filterIsInstance<ModelEntity<*>>()

        if (selection.isEmpty()) {
            label.text = strings[2]
        } else {
            try {
                val bottommost = selection.minBy { it.bounds.y }!!
                val leftmost = selection.minBy { it.bounds.x }!!
                val json = JsonHandler.toJson(SmallPatternObject().also {
                    it.id = "INSERT_ID_HERE"
                    it.deprecatedIDs = listOf()
                    it.name = "INSERT NAME HERE"
                    it.cues = selection.map { entity ->
                        SmallCuePointer().also { pointer ->
                            pointer.id = entity.datamodel.id
                            pointer.duration = entity.bounds.width
                            pointer.beat = entity.bounds.x - leftmost.bounds.x
                            pointer.track = (entity.bounds.y - bottommost.bounds.y).roundToInt()
                            pointer.semitone = (entity as? IRepitchable)?.semitone ?: 0
                        }
                    }
                })

                Gdx.app.clipboard.contents = json
                Toolboks.LOGGER.info("\n$json\n")

                label.text = strings[1]
            } catch (e: Exception) {
                e.printStackTrace()
                label.text = strings[3]
            }
        }

        resetTextIn = 3f
    }

    class SmallCuePointer {

        lateinit var id: String
        var beat: Float = -1f

        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var duration: Float = 0f
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var semitone: Int = 0
//        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
//        var volume: Int = IVolumetric.DEFAULT_VOLUME
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        var track: Int = 0

//        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
//        var metadata: Map<String, Any?>? = null

    }

    class SmallPatternObject {
        var type: String = "pattern"
        lateinit var id: String
        lateinit var deprecatedIDs: List<String>
        lateinit var name: String
        var stretchable: Boolean = false
        lateinit var cues: List<SmallCuePointer>
    }
}