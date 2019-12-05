package io.github.chrislo27.rhre3.editor.stage.theme

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.Base64Coder
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.stage.ColourPicker
import io.github.chrislo27.rhre3.theme.LightTheme
import io.github.chrislo27.rhre3.theme.LoadedThemes
import io.github.chrislo27.rhre3.theme.Theme
import io.github.chrislo27.rhre3.theme.Themes
import io.github.chrislo27.rhre3.util.*
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean


class ThemeEditorStage(val editor: Editor, val palette: UIPalette, parent: ThemeChooserStage, camera: OrthographicCamera,
                       pixelWidth: Float, pixelHeight: Float)
    : Stage<EditorScreen>(parent, camera, pixelWidth, pixelHeight) {

    private val themeChooserStage: ThemeChooserStage = parent

    private var themeFile: FileHandle? = null
    private var theme: Theme = object : LightTheme() {
        override val nameIsLocalization: Boolean = false
    }.apply {
        name = "!INVALID THEME!"
    }

    val chooseThemeStage = ChooseThemeStage()
    val chooseElementStage = ChooseElementStage()
    val editElementStage = EditElementStage()
    val editNameStage = EditNameStage()
    val editTextureStage = EditTextureStage()
    private val allStateStages: List<StateStage> = listOf(chooseThemeStage, chooseElementStage, editElementStage, editNameStage, editTextureStage)
    var state: State = State.ChooseTheme

    init {
        allStateStages.forEach { s ->
            s.visible = false
            this.elements += s
        }
    }

    fun update() {
        val newState = state
        themeChooserStage.title.text = newState.titleKey
        allStateStages.forEach { s ->
            s.visible = s == newState.getRelatedStage(this)
            s.onUpdate()
        }
        if (newState is State.EditElement) {
            editElementStage.field = newState.field
        }
    }

    private fun saveTheme() {
        themeFile?.writeString(JsonHandler.toJson(theme), false, "UTF-8")
    }

    abstract inner class StateStage : Stage<EditorScreen>(this@ThemeEditorStage, this@ThemeEditorStage.camera) {

        protected val contentStage: Stage<EditorScreen>
        protected val buttonBar: Stage<EditorScreen>

        init {
            contentStage = Stage(this, this.camera, 346f, 392f).apply {
                location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                             pixelX = 0f, pixelY = 0f, pixelWidth = 346f, pixelHeight = 392f)
            }
            this.elements += contentStage
            buttonBar = Stage(contentStage, contentStage.camera, 346f, 34f).apply {
                location.set(screenX = 0f, screenY = 0f, screenWidth = 1f, screenHeight = 0f,
                             pixelX = 0f, pixelY = 0f, pixelWidth = 0f, pixelHeight = 34f)
            }
            contentStage.elements += buttonBar
        }

        open fun onUpdate() {

        }
    }

    inner class ChooseThemeStage : StateStage() {
        val themeList: ThemeListStage<Theme>

        init {
            themeList = object : ThemeListStage<Theme>(editor, palette, contentStage, contentStage.camera, 362f, 352f) {
                override val itemList: List<Theme> get() = LoadedThemes.themes

                override fun getItemName(item: Theme): String = item.name
                override fun isItemNameLocalizationKey(item: Theme): Boolean = item.nameIsLocalization
                override fun getItemBgColor(item: Theme): Color = item.background
                override fun getItemLineColor(item: Theme): Color = item.trackLine
                override fun isItemSelected(item: Theme): Boolean = false
                override fun getBlueBarLimit(): Int = Themes.defaultThemes.size

                override fun onItemButtonSelected(leftClick: Boolean, realIndex: Int, buttonIndex: Int) {
                    if (leftClick) {
                        val theme = itemList[realIndex]
                        val isStock = realIndex < Themes.defaultThemes.size
                        if (isStock) {
                            // Copy theme and save to file
                            val newName = Localization["editor.themeEditor.themeNameCopy", theme.getRealName()]
                            theme.also {
                                val uuid = "${UUID.randomUUID()}-${System.nanoTime()}"
                                val name = it.name
                                it.name = uuid
                                var num = 0
                                var fh = LoadedThemes.THEMES_FOLDER.child("theme_${num}.json")
                                while (fh.exists()) {
                                    num++
                                    fh = LoadedThemes.THEMES_FOLDER.child("theme_${num}.json")
                                }
                                fh.writeString(JsonHandler.toJson(it), false, "UTF-8")
                                it.name = name
                                LoadedThemes.reloadThemes(editor.main.preferences, false)
                                // Find index
                                LoadedThemes.index = LoadedThemes.themes.indexOfFirst { t -> t.name == uuid }
                                this@ThemeEditorStage.also { tes ->
                                    tes.theme = LoadedThemes.currentTheme
                                    tes.theme.name = newName
                                    tes.themeFile = fh
                                    tes.saveTheme()
                                }
                                editor.theme = LoadedThemes.currentTheme
                            }
                        } else {
                            this@ThemeEditorStage.theme = theme
                            this@ThemeEditorStage.themeFile = LoadedThemes.fileHandles[theme]
                            LoadedThemes.index = realIndex
                            editor.theme = theme
                        }
                        val main = editor.main
                        main.themeUsesMenu = false
                        main.preferences.putBoolean(PreferenceKeys.THEME_USES_MENU, false).flush()

                        state = if (isStock) State.EditName else State.ChooseElement
                        update()
                    }
                }
            }.apply {
                location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                             pixelX = 0f, pixelY = 40f, pixelWidth = 362f, pixelHeight = 352f)
                Gdx.app.postRunnable {
                    this.resetButtons()
                }
            }
            contentStage.elements += themeList
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                this.addLabel(TextLabel(palette, this, this.stage).apply {
                    this.text = "editor.themeEditor.cancel"
                    this.isLocalizationKey = true
                    this.textWrapping = false
                    fontScaleMultiplier = 0.85f
                })
                this.leftClickAction = { _, _ ->
                    themeChooserStage.resetEverything()
                }
                this.location.set(0f, 0f, 1f, 1f)
            }
        }

        override fun onUpdate() {
            super.onUpdate()
            themeList.resetButtons()
            themeList.buttonScroll = 0
        }
    }

    inner class ChooseElementStage : StateStage() {
        val fieldList: ThemeListStage<ThemeField>

        init {
            fieldList = object : ThemeListStage<ThemeField>(editor, palette, contentStage, contentStage.camera, 362f, 352f) {
                override val itemList: List<ThemeField> get() = ThemeField.FIELDS

                override fun getItemName(item: ThemeField): String = "editor.themeEditor.element." + item.name
                override fun isItemNameLocalizationKey(item: ThemeField): Boolean = true
                override fun getItemBgColor(item: ThemeField): Color = item.bgColor(theme)
                override fun getItemLineColor(item: ThemeField): Color = item.lineColor(theme)
                override fun isItemSelected(item: ThemeField): Boolean = false

                override fun onItemButtonSelected(leftClick: Boolean, realIndex: Int, buttonIndex: Int) {
                    val f = itemList[realIndex]
                    state = State.EditElement(f)
                    update()
                }
            }.apply {
                location.set(screenX = 0f, screenY = 0f, screenWidth = 0f, screenHeight = 0f,
                             pixelX = 0f, pixelY = 40f, pixelWidth = 362f, pixelHeight = 352f)
                Gdx.app.postRunnable {
                    this.resetButtons()
                }
            }
            contentStage.elements += fieldList
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_back"))
                })
                this.location.set(0f, 0f, 0f, 1f, 0f, 0f, 34f, 0f)
                leftClickAction = { _, _ ->
                    saveTheme()
                    themeChooserStage.resetEverything()
                }
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.finishEditing"
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_nametag"))
                })
                this.location.set(0f, 0f, 0f, 1f, 34f + 4f, 0f, 34f, 0f)
                leftClickAction = { _, _ ->
                    state = State.EditName
                    update()
                }
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.editName.short"
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_photo"))
                })
                this.location.set(0f, 0f, 0f, 1f, 34f * 2 + 4f * 2, 0f, 34f, 0f)
                leftClickAction = { _, _ ->
                    state = State.EditTexture
                    update()
                }
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.editTexture"
            }

            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                this.location.set(0f, 0f, 0f, 1f, 34f * 3 + 4f * 3, 0f, 34f, 0f)
                this.addLabel(ImageLabel(palette, this, this.stage).apply {
                    this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
                    this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_folder"))
                })
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.openContainingFolder"
                leftClickAction = { _, _ ->
                    val f = themeFile
                    if (f != null) {
                        Gdx.net.openURI("file:///${f.parent().file().canonicalPath}")
                    }
                }
            }
        }

        override fun onUpdate() {
            super.onUpdate()
            fieldList.resetButtons()
        }
    }

    inner class EditElementStage : StateStage() {

        val colourPicker: ColourPicker<EditorScreen> = ColourPicker(palette.copy(fontScale = 0.9f), contentStage, contentStage, true).apply {
            this.location.set(0f, 1f, 0f, 0f, pixelY = -256f, pixelWidth = 346f, pixelHeight = 256f)
        }

        private val oldColor: Color = Color(1f, 1f, 1f, 1f)
        var field: ThemeField = ThemeField.FIELDS.first()
            set(value) {
                field = value
                oldColor.set(value.getter(theme))
                colourPicker.setColor(oldColor, false)
            }

        init {
            contentStage.elements += colourPicker
            colourPicker.onColourChange = { c ->
                field.setter(theme, c)
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_back"))
                })
                this.location.set(0f, 0f, 0f, 1f, 0f, 0f, 34f, 0f)
                leftClickAction = { _, _ ->
                    field.setter(theme, oldColor)
                    state = State.ChooseElement
                    update()
                }
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.cancel"
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.reset"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 0f, 0f, 1f, 34f + 4f, 0f, 150f, 0f)
                leftClickAction = { _, _ ->
                    field.setter(theme, oldColor)
                }
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.done"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 0f, 1f, 1f, 38f + 154f, 0f, -38f - 154f, 0f)
                leftClickAction = { _, _ ->
                    state = State.ChooseElement
                    update()
                    saveTheme()
                }
            }
        }
    }

    inner class EditNameStage : StateStage() {
        val field: TextField<EditorScreen>

        init {
            field = TextField(palette, contentStage, contentStage).apply {
                background = true
                canInputNewlines = false
                location.set(0f, 0.5f, 0f, 0f, pixelY = -20f, pixelWidth = 346f, pixelHeight = 40f)
            }
            contentStage.elements += field
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(ImageLabel(palette, this, this.stage).apply {
                    image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_back"))
                })
                this.location.set(0f, 0f, 0f, 1f, 0f, 0f, 34f, 0f)
                leftClickAction = { _, _ ->
                    state = State.ChooseElement
                    update()
                }
                this.tooltipTextIsLocalizationKey = true
                this.tooltipText = "editor.themeEditor.cancel"
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.done"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 0f, 1f, 1f, 34f + 4f, 0f, -38f, 0f)
                leftClickAction = { _, _ ->
                    theme.name = field.text
                    state = State.ChooseElement
                    update()
                    saveTheme()
                }
            }
        }

        override fun onUpdate() {
            super.onUpdate()
            field.text = theme.name
        }
    }

    inner class EditTextureStage : StateStage() {

        val label: TextLabel<EditorScreen>

        init {
            label = TextLabel(palette, contentStage, contentStage).apply {
                location.set(0f, 0f, 1f, 1f, pixelY = 128f, pixelHeight = -128f)
                this.isLocalizationKey = false
                this.textWrapping = false
                this.textAlign = Align.center
            }
            contentStage.elements += label
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.editTexture.select"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 2f, 1f, 1f, pixelY = 8f)
                val fileChooserOpen: AtomicBoolean = AtomicBoolean(false)
                tooltipTextIsLocalizationKey = true
                leftClickAction = { _, _ ->
                    if (!fileChooserOpen.get()) {
                        fileChooserOpen.set(true)
                        this@apply.let {
                            it.enabled = false
                            it.tooltipText = "closeChooser"
                        }
                        GlobalScope.launch {
                            val initialDirectory: File? = attemptRememberDirectory(editor.main, PreferenceKeys.FILE_CHOOSER_THEME_EDITOR_IMAGE) ?: getDefaultDirectory()
                            val fileFilters = listOf(FileChooserExtensionFilter(Localization["screen.open.fileFilterSupported"], "*.png"))
                            FileChooser.openFileChooser(Localization["editor.themeEditor.editTexture.select"], initialDirectory, null, fileFilters, fileFilters.first()) { file ->
                                // Check that we are still open
                                if (this@ThemeEditorStage.visible) {
                                    if (file != null) {
                                        Gdx.app.postRunnable {
                                            // Attempt to load texture
                                            try {
                                                val newTex: Texture = Texture(FileHandle(file))
                                                theme.texture = null
                                                // Set base64
                                                run {
                                                    val texData = newTex.textureData
                                                    texData.prepare()
                                                    val shouldDispose = texData.disposePixmap()
                                                    val pixmap = texData.consumePixmap()
                                                    val tmp = FileHandle.tempFile("rhre3-theme-editor-tex")
                                                    PixmapIO.writePNG(tmp, pixmap)
                                                    theme.texture = String(Base64Coder.encode(tmp.readBytes()))
                                                    tmp.delete()
                                                    if (shouldDispose)
                                                        pixmap.dispose()
                                                }
                                                val tex = theme.textureObj
                                                if (tex != null) {
                                                    theme.textureObj = null
                                                    tex.dispose()
                                                }
                                                theme.textureObj = newTex
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                label.text = Localization["editor.themeEditor.failedToLoadTexture"]
                                            }
                                        }
                                    }

                                    Gdx.app.postRunnable {
                                        update()
                                        saveTheme()
                                        onUpdate()
                                        fileChooserOpen.set(false)
                                        this@apply.let {
                                            it.enabled = true
                                            it.tooltipText = null
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.editTexture.remove"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 1f, 1f, 1f, pixelY = 4f)
                leftClickAction = { _, _ ->
                    val tex = theme.textureObj
                    if (tex != null) {
                        theme.textureObj = null
                        theme.texture = null
                        tex.dispose()
                    }
                    update()
                    onUpdate()
                    saveTheme()
                }
            }
            buttonBar.elements += Button(palette, buttonBar, buttonBar).apply {
                addLabel(TextLabel(palette, this, this.stage).apply {
                    text = "editor.themeEditor.done"
                    textWrapping = false
                    isLocalizationKey = true
                    fontScaleMultiplier = 0.85f
                })
                this.location.set(0f, 0f, 1f, 1f)
                leftClickAction = { _, _ ->
                    state = State.ChooseElement
                    update()
                    saveTheme()
                }
            }
        }

        override fun onUpdate() {
            super.onUpdate()
            val tex = theme.textureObj
            label.text = if (tex == null) Localization["editor.themeEditor.noTexture"] else "${tex.width}x${tex.height}"
        }
    }

    sealed class State {
        object ChooseTheme : State() {
            override val titleKey: String = "editor.themeEditor.chooseTheme"
            override fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage = themeEditor.chooseThemeStage
        }

        object ChooseElement : State() {
            override val titleKey: String = "editor.themeEditor.chooseElement"
            override fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage = themeEditor.chooseElementStage
        }

        object EditName : State() {
            override val titleKey: String = "editor.themeEditor.editName"
            override fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage = themeEditor.editNameStage
        }

        object EditTexture : State() {
            override val titleKey: String = "editor.themeEditor.editTexture"
            override fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage = themeEditor.editTextureStage
        }

        class EditElement(val field: ThemeField) : State() {
            override val titleKey: String = "editor.themeEditor.element.${field.name}"
            override fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage = themeEditor.editElementStage
        }

        abstract val titleKey: String
        abstract fun getRelatedStage(themeEditor: ThemeEditorStage): StateStage
    }

    data class ThemeField(val name: String, val setter: Theme.(Color) -> Unit, val getter: Theme.() -> Color,
                          val bgColor: Theme.() -> Color = getter, val lineColor: Theme.() -> Color = { CLEAR_COLOR }) {
        companion object {
            val CLEAR_COLOR: Color = Color(1f, 1f, 1f, 0f)
            val FIELDS: List<ThemeField> = listOf(
                    ThemeField("background", { background.set(it) }, { background }),
                    ThemeField("trackLine", { trackLine.set(it) }, { trackLine }, { background }, { trackLine }),
                    ThemeField("waveform", { waveform = it }, { waveform }, { background }, { trackLine }),
                    ThemeField("playalong.flicking", { playalongFlicking.set(it) }, { playalongFlicking }),
                    ThemeField("trackers.playback", { trackers.playback.set(it) }, { trackers.playback }),
                    ThemeField("trackers.musicStart", { trackers.musicStart.set(it) }, { trackers.musicStart }),
                    ThemeField("trackers.tempoChange", { trackers.tempoChange.set(it) }, { trackers.tempoChange }),
                    ThemeField("trackers.musicVolume", { trackers.musicVolume.set(it) }, { trackers.musicVolume }),
                    ThemeField("selection.selectionFill", { selection.selectionFill.set(it) }, { selection.selectionFill }),
                    ThemeField("selection.selectionBorder", { selection.selectionBorder.set(it) }, { selection.selectionBorder }),
                    ThemeField("entities.selectionTint", { entities.selectionTint.set(it) }, { entities.selectionTint }),
                    ThemeField("entities.nameColor", { entities.nameColor.set(it) }, { entities.nameColor }),
                    ThemeField("entities.cue", { entities.cue.set(it) }, { entities.cue }),
                    ThemeField("entities.pattern", { entities.pattern.set(it) }, { entities.pattern }),
                    ThemeField("entities.special", { entities.special.set(it) }, { entities.special }),
                    ThemeField("entities.keepTheBeat", { entities.keepTheBeat.set(it) }, { entities.keepTheBeat }),
                    ThemeField("entities.equidistant", { entities.equidistant.set(it) }, { entities.equidistant })
                                                 )
        }
    }

}