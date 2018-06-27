package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.graphics.Color
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
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.TextField
import io.github.chrislo27.toolboks.ui.TextLabel
import java.util.*

class PatternStoreScreen(main: RHRE3Application, val editor: Editor, val pattern: StoredPattern?, val entities: List<Entity>?)
    : ToolboksScreen<RHRE3Application, PatternStoreScreen>(main) {

    companion object {
        private const val ALLOW_SAME_NAMES = true
    }

    override val stage: GenericStage<PatternStoreScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val button: Button<PatternStoreScreen>
    private lateinit var textField: TextField<PatternStoreScreen>

    init {
        stage.titleLabel.text = if (pattern != null) "screen.patternStore.edit.title" else "screen.patternStore.title"
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

        val alreadyExists = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenY = 0.15f, screenHeight = 0.15f)
            this.isLocalizationKey = true
            this.text = "screen.patternStore.alreadyExists"
            this.visible = false
        }
        stage.centreStage.elements +=  alreadyExists

        if (pattern != null) {
            stage.bottomStage.elements += object : Button<PatternStoreScreen>(palette.copy(highlightedBackColor = Color(1f, 0f, 0f, 0.5f),
                                                                                           clickedBackColor = Color(1f, 0.5f, 0.5f, 0.5f)), stage.bottomStage, stage.bottomStage) {
                override fun onLeftClick(xPercent: Float, yPercent: Float) {
                    super.onLeftClick(xPercent, yPercent)
                    main.screen = PatternDeleteScreen(main, editor, pattern, this@PatternStoreScreen)
                }
            }.apply {
                val backBtnLoc = this@PatternStoreScreen.stage.backButton.location
                this.location.set(1f - backBtnLoc.screenX - backBtnLoc.screenWidth, backBtnLoc.screenY, backBtnLoc.screenWidth, backBtnLoc.screenHeight)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_x"))
                })
            }
        }

        button = object : Button<PatternStoreScreen>(palette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)

                if (pattern == null) {
                    PatternStorage.addPattern(StoredPattern(UUID.randomUUID(), textField.text.trim(), entitiesToJson(entities!!)))
                            .persist()
                } else {
                    PatternStorage.deletePattern(pattern)
                            .addPattern(StoredPattern(pattern.uuid, textField.text.trim(), pattern.data))
                            .persist()
                }
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
            this.fontScaleMultiplier = 0.75f
            this.location.set(screenX = 0.25f, screenWidth = 0.5f, screenY = 0.4125f, screenHeight = 0.1f)
        }
        stage.centreStage.elements += charsRemaining

        textField = object : TextField<PatternStoreScreen>(palette, stage.centreStage, stage.centreStage) {
            init {
                characterLimit = PatternStorage.MAX_PATTERN_NAME_SIZE
            }

            override fun onEnterPressed(): Boolean {
                if (text.isNotBlank()) {
                    button.onLeftClick(0f, 0f)
                    return true
                }
                return false
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                text = ""
            }

            override fun onTextChange(oldText: String) {
                super.onTextChange(oldText)

                val trimmed = text.trim()
                val already = PatternStorage.patterns.values.any { it !== pattern && it.name == trimmed }
                button.enabled = trimmed.isNotEmpty() && (ALLOW_SAME_NAMES || !already)
                alreadyExists.visible = already

                charsRemaining.text = "${trimmed.length} / ${PatternStorage.MAX_PATTERN_NAME_SIZE}"
            }
        }.apply {
            this.location.set(screenY = 0.5f, screenHeight = 0.1f, screenX = 0.25f, screenWidth = 0.5f)
            this.canPaste = true
            this.canInputNewlines = false
            this.background = true
            this.hasFocus = true
            onTextChange("")
        }

        stage.centreStage.elements += textField
        stage.bottomStage.elements += button

        if (entities?.size == 0)
            error("Entities are empty")

        stage.updatePositions()
        textField.apply {
            if (pattern != null) {
                text = pattern.name
                this.caret = text.length + 1
            }
        }
    }

    fun entitiesToJson(entities: List<Entity>): String {
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
