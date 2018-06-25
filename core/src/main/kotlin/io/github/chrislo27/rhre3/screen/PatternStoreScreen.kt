package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.entity.Entity
import io.github.chrislo27.rhre3.patternstorage.PatternStorage
import io.github.chrislo27.rhre3.patternstorage.StoredPattern
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel
import java.util.*

class PatternStoreScreen(main: RHRE3Application, val editor: Editor, val entities: List<Entity>)
    : ToolboksScreen<RHRE3Application, PatternStoreScreen>(main) {

    override val stage: GenericStage<PatternStoreScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val button: Button<PatternStoreScreen>
    private lateinit var textField: TextField<PatternStoreScreen>

    init {
        stage.titleLabel.text = "screen.patternStore.title"
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_pattern_store"))
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry["editor"]
        }

        val palette = main.uiPalette

        stage.centreStage.elements += TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenY = 0.75f, screenHeight = 0.15f)
            this.isLocalizationKey = true
            this.text = "screen.patternStore.enterName"
        }

        button = object : Button<PatternStoreScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                PatternStorage.addPattern(StoredPattern(UUID.randomUUID(), textField.text.trim(), entitiesToJson())).persist()
                editor.stage.updateSelected()
                main.screen = ScreenRegistry["editor"]
            }
        }.apply {
            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.text = "screen.patternStore.button"
            })
            this.enabled = false
        }

        val charsRemaining = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.text = "0 / ?"
            this.textAlign = Align.right
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenY = 0.4f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += charsRemaining

        textField = object : TextField<PatternStoreScreen>(palette, stage.centreStage, stage.centreStage) {
            init {
                characterLimit = PatternStorage.MAX_PATTERN_NAME_SIZE
                onTextChange("")
            }

            override fun onEnterPressed(): Boolean {
                if (text.isNotBlank()) {
                    button.onLeftClick(0f, 0f)
                    return true
                }
                return false
            }

            override fun onTextChange(oldText: String) {
                super.onTextChange(oldText)
                button.enabled = text.isNotBlank()
                charsRemaining.text = "${text.length} / ${PatternStorage.MAX_PATTERN_NAME_SIZE}"
            }
        }.apply {
            this.location.set(screenY = 0.5f, screenHeight = 0.1f, screenX = 0.25f, screenWidth = 0.5f)
            this.canPaste = true
            this.canInputNewlines = false
            this.background = true
            this.hasFocus = true
        }

        stage.centreStage.elements += textField
        stage.bottomStage.elements += button

        if (entities.isEmpty())
            error("Entities are empty")
    }

    fun entitiesToJson(): String {
        val array = JsonHandler.OBJECT_MAPPER.createArrayNode()

        val oldBounds: Map<Entity, Rectangle> = entities.associate { it to Rectangle(it.bounds) }
        val baseX: Float = entities.minBy { it.bounds.x }?.bounds?.x ?: 0f
        val baseY: Int = entities.minBy { it.bounds.y }?.bounds?.y?.toInt() ?: 0

        entities.forEach {
            it.updateBounds {
                it.bounds.x -= baseX
                it.bounds.y -= baseY
            }
        }

        entities.forEach { entity ->
            val node = array.addObject()

            node.put("type", entity.jsonType)

            entity.saveData(node)
        }

        // Restore bounds
        entities.forEach {
            it.updateBounds { it.bounds.set(oldBounds[it]) }
        }

        return JsonHandler.toJson(array)
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
