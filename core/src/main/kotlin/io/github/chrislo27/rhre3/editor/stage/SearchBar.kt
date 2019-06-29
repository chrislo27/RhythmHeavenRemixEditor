package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.picker.SearchFilter
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class SearchBar<S : ToolboksScreen<*, *>>(screenWidth: Float, val editor: Editor, val editorStage: EditorStage,
                                          val palette: UIPalette, parent: UIElement<S>, camera: OrthographicCamera)
    : Stage<S>(parent, camera) {

    enum class Filter(val tag: String) {
        GAME_NAME("gameName"),
        ENTITY_NAME("entityName"),
        FAVOURITES("favourites"),
        USE_IN_REMIX("useInRemix"),
        CALL_AND_RESPONSE("callAndResponse");

        companion object {
            val VALUES = values().toList()
        }

        val localizationKey = "editor.search.filter.$tag"
    }

    init {
        this.location.set(screenWidth = screenWidth)
        this.updatePositions()
    }

    private val searchFilter: SearchFilter get() = editorStage.searchFilter
    val textField = object : TextField<S>(palette, this@SearchBar, this@SearchBar) {
        init {
            this.textWhenEmptyColor = Color.LIGHT_GRAY
        }

        override fun render(screen: S, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
            super.render(screen, batch, shapeRenderer)
            this.textWhenEmpty = Localization["picker.search"]
        }

        override fun onTextChange(oldText: String) {
            super.onTextChange(oldText)
            editorStage.updateSelected(EditorStage.DirtyType.SEARCH_DIRTY)
        }

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            val hadFocus = hasFocus
            super.onLeftClick(xPercent, yPercent)
            editor.pickerSelection.filter = searchFilter
            editorStage.updateSelected(
                    if (!hadFocus) EditorStage.DirtyType.SEARCH_DIRTY else EditorStage.DirtyType.DIRTY)
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            hasFocus = true
            text = ""
            editorStage.updateSelected(EditorStage.DirtyType.SEARCH_DIRTY)
            editor.pickerSelection.filter = searchFilter
        }
    }
    val clearButton: ClearButton = ClearButton()
    val filterButton: FilterButton = FilterButton()

    init {
        this.updatePositions()
        val height = location.realHeight
        val width = percentageOfWidth(height)
        filterButton.apply {
            this.location.set(screenWidth = width)
            this.location.set(screenX = 1f - this.location.screenWidth)
            this.background = false

            this.updateLabel()
        }
        clearButton.apply {
            this.location.set(screenWidth = width)
            this.location.set(screenX = filterButton.location.screenX - this.location.screenWidth)
            this.background = false

            this.addLabel(ImageLabel(palette, this, this.stage).apply {
                this.image = TextureRegion(AssetRegistry.get<Texture>("ui_search_clear"))
                this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                this.tint.a = 0.75f
            })
        }
        textField.apply {
            this.location.set(screenWidth = clearButton.location.screenX)
        }

        elements += textField
        elements += clearButton
        elements += filterButton
    }

    inner class FilterButton : Button<S>(palette, this, this) {

        var filter: Filter = Filter.GAME_NAME
            private set

        private val imageLabel = ImageLabel(palette, this, this.stage).apply {
            this.tint.a = 0.75f
            this.background = false
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }

        private val textures by lazy {
            Filter.VALUES.associate { it to TextureRegion(AssetRegistry.get<Texture>("ui_search_filter_${it.tag}")) }
        }

        init {
            addLabel(imageLabel)

            updateLabel()
        }

        override var tooltipText: String?
            set(_) {}
            get() = Localization[filter.localizationKey]

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            var index = Filter.VALUES.indexOf(filter) + 1
            if (index >= Filter.VALUES.size) {
                index = 0
            }

            filter = Filter.VALUES[index]
            editorStage.updateSelected(EditorStage.DirtyType.SEARCH_DIRTY)

            updateLabel()
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            var index = Filter.VALUES.indexOf(filter) - 1
            if (index < 0) {
                index = Filter.VALUES.size - 1
            }

            filter = Filter.VALUES[index]
            editorStage.updateSelected(EditorStage.DirtyType.SEARCH_DIRTY)

            updateLabel()
        }

        fun updateLabel() {
            imageLabel.image = textures[filter]
        }
    }

    inner class ClearButton : Button<S>(palette, this, this) {

        override var tooltipText: String?
            set(_) {}
            get() = Localization["editor.search.clear"]

        override fun onLeftClick(xPercent: Float, yPercent: Float) {
            super.onLeftClick(xPercent, yPercent)
            textField.text = ""
            textField.touchDown((textField.location.realX + textField.location.realWidth / 2).toInt(),
                                (textField.location.realY + textField.location.realHeight / 2).toInt(),
                                0, Input.Buttons.LEFT)
            textField.hasFocus = true
        }
    }

}