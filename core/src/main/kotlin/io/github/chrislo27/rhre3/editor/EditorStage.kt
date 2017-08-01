package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class EditorStage(parent: UIElement<EditorScreen>?,
                  camera: OrthographicCamera, val main: RHRE3Application)
    : Stage<EditorScreen>(parent, camera), Palettable {

    override var palette: UIPalette = main.uiPalette
    val messageBarStage: Stage<EditorScreen>
    val buttonBarStage: Stage<EditorScreen>
    val pickerStage: Stage<EditorScreen>
    val minimapBarStage: Stage<EditorScreen>
    val centreAreaStage: Stage<EditorScreen>

    init {
        messageBarStage = Stage(this, camera).apply {
            this.location.set(0f, 0f,
                              1f, Editor.MESSAGE_BAR_HEIGHT / RHRE3.HEIGHT.toFloat())
        }
        elements += messageBarStage
        buttonBarStage = Stage(this, camera).apply {
            this.location.set(screenX = (Editor.BUTTON_PADDING / RHRE3.WIDTH),
                              screenY = 1f - ((Editor.BUTTON_PADDING + Editor.BUTTON_SIZE) / RHRE3.HEIGHT),
                              screenWidth = 1f - (Editor.BUTTON_PADDING / RHRE3.WIDTH) * 2f,
                              screenHeight = Editor.BUTTON_SIZE / RHRE3.HEIGHT)
        }
        elements += buttonBarStage
        pickerStage = Stage(this, camera).apply {
            this.location.set(screenY = messageBarStage.location.screenY + messageBarStage.location.screenHeight,
                              screenHeight = ((Editor.ICON_SIZE + Editor.ICON_PADDING) * Editor.ICON_COUNT_Y + Editor.ICON_PADDING) / RHRE3.HEIGHT
                             )
            this.elements += ColourPane(this, this).apply {
                this.colour.set(0f, 1f, 0f, 0.5f)
            }
        }
        elements += pickerStage
        minimapBarStage = Stage(this, camera).apply {
            this.location.set(screenY = pickerStage.location.screenY + pickerStage.location.screenHeight,
                              screenHeight = Editor.ICON_SIZE / RHRE3.HEIGHT)
            this.elements += ColourPane(this, this).apply {
                this.colour.set(1f, 0f, 0f, 0.5f)
            }
        }
        elements += minimapBarStage
        centreAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = minimapBarStage.location.screenY + minimapBarStage.location.screenHeight)
            this.location.set(
                    screenHeight = (buttonBarStage.location.screenY - this.location.screenY - (Editor.BUTTON_PADDING / RHRE3.HEIGHT)))
            this.elements += ColourPane(this, this).apply {
                this.colour.set(0f, 0f, 1f, 0.5f)
            }
        }
        elements += centreAreaStage
        this.updatePositions()

        // Message bar
        run messageBar@ {
            messageBarStage.updatePositions()
            messageBarStage.elements +=
                    ColourPane(messageBarStage, messageBarStage).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                        this.colour.a = 0.75f
                    }
            messageBarStage.elements +=
                    TextLabel(palette, messageBarStage, messageBarStage).apply {
                        this.fontScaleMultiplier = 0.5f
                        this.textAlign = Align.bottomLeft
                        this.textWrapping = false
                        this.location.set(0f, -0.5f,
                                          1f - (main.versionTextWidth / RHRE3.WIDTH),
                                          1.5f,
                                          pixelWidth = -8f)
                        this.isLocalizationKey = false
                    }
        }

        // Picker area
        run picker@ {

        }

        // Minimap area
        run minimap@ {

        }

        // Button bar
        run buttonBar@ {
            buttonBarStage.updatePositions()
            val stageWidth = buttonBarStage.location.realWidth
            val stageHeight = buttonBarStage.location.realHeight
            val padding = Editor.BUTTON_PADDING / stageWidth
            val size = Editor.BUTTON_SIZE / stageWidth
            val palette = palette.copy(backColor = Color(0f, 0f, 0f, 0.5f))
            buttonBarStage.elements +=
                    ColourPane(buttonBarStage, buttonBarStage).apply {
                        this.colour.set(Editor.TRANSLUCENT_BLACK)
                        this.colour.a = 0.5f
                        this.location.set(
                                screenX = -(Editor.BUTTON_PADDING / stageWidth),
                                screenY = -(Editor.BUTTON_PADDING / stageHeight),
                                screenWidth = 1f + (Editor.BUTTON_PADDING / stageWidth) * 2f,
                                screenHeight = 1f + (Editor.BUTTON_PADDING / stageHeight) * 2f
                                         )
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = (Editor.BUTTON_SIZE) / stageWidth)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("newFile")
                        })
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = (Editor.BUTTON_SIZE) / stageWidth,
                                          screenX = size + padding)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("openFile")
                        })
                    }
            buttonBarStage.elements +=
                    Button(palette, buttonBarStage, buttonBarStage).apply {
                        this.location.set(screenWidth = (Editor.BUTTON_SIZE) / stageWidth,
                                          screenX = size * 2 + padding * 2)
                        this.addLabel(ImageLabel(palette, this, this.stage).apply {
                            this.image = AssetRegistry.get<TextureAtlas>("ui-icons").findRegion("saveFile")
                        })
                    }
        }

        this.updatePositions()
    }

}
