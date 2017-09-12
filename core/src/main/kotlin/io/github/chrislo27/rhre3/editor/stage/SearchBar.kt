package io.github.chrislo27.rhre3.editor.stage

import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.registry.GameGroup
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import java.util.*


class SearchBar<S : ToolboksScreen<*, *>>(screenWidth: Float, val editor: Editor, val editorStage: EditorStage,
                                          val palette: UIPalette, parent: UIElement<S>, camera: OrthographicCamera)
    : Stage<S>(parent, camera) {

    enum class Filter(localization: String) {
        GAME_NAME("gameName"),
        ENTITY_NAME("entityName");

        companion object {
            val VALUES = values().toList()
        }

        val localizationKey = "editor.search.filter.$localization"
    }

    init {
        this.location.set(screenWidth = screenWidth)
        this.updatePositions()
    }

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
            editor.pickerSelection.isSearching = true
            editorStage.updateSelected(
                    if (!hadFocus) EditorStage.DirtyType.SEARCH_DIRTY else EditorStage.DirtyType.DIRTY)
        }

        override fun onRightClick(xPercent: Float, yPercent: Float) {
            super.onRightClick(xPercent, yPercent)
            hasFocus = true
            text = ""
            editorStage.updateSelected(EditorStage.DirtyType.SEARCH_DIRTY)
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

    fun filterGameGroups(query: String): List<GameGroup> {
        return GameRegistry.data.gameGroupsList.filter { group ->
            when (filterButton.filter) {
                Filter.GAME_NAME -> {
                    query in group.name.toLowerCase(Locale.ROOT) ||
                            group.games.any { game -> query in game.name.toLowerCase(Locale.ROOT) }
                }
                SearchBar.Filter.ENTITY_NAME -> {
                    group.games.any { game ->
                        game.placeableObjects.any { obj ->
                            !obj.hidden && query in obj.name.toLowerCase(Locale.ROOT)
                        }
                    }
                }
            }
        }
    }

    inner class FilterButton : Button<S>(palette, this, this), EditorStage.HasHoverText {

        var filter: Filter = Filter.GAME_NAME
            private set

        private val imageLabel = ImageLabel(palette, this, this.stage).apply {
            this.tint.a = 0.75f
            this.background = false
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
        }

        private val textures by lazy {
            mapOf(
                    Filter.GAME_NAME to TextureRegion(AssetRegistry.get<Texture>("ui_search_filter_gameName")),
                    Filter.ENTITY_NAME to TextureRegion(AssetRegistry.get<Texture>("ui_search_filter_entityName"))
                 )
        }

        init {
            addLabel(imageLabel)

            updateLabel()
        }

        override fun getHoverText(): String = Localization[filter.localizationKey]

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

        fun updateLabel() {
//            imageLabel.image = textures[filter]
        }
    }

    inner class ClearButton : Button<S>(palette, this, this), EditorStage.HasHoverText {

        override fun getHoverText(): String = Localization["editor.search.clear"]

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