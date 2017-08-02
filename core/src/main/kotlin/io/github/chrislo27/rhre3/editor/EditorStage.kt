package io.github.chrislo27.rhre3.editor

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.registry.Series
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.*


class EditorStage(parent: UIElement<EditorScreen>?,
                  camera: OrthographicCamera, val main: RHRE3Application)
    : Stage<EditorScreen>(parent, camera), Palettable {

    override var palette: UIPalette = main.uiPalette.copy(
            backColor = Color(main.uiPalette.backColor).apply { this.a = 0.5f })
    val messageBarStage: Stage<EditorScreen>
    val buttonBarStage: Stage<EditorScreen>
    val pickerStage: Stage<EditorScreen>
    val minimapBarStage: Stage<EditorScreen>
    val centreAreaStage: Stage<EditorScreen>
    val patternAreaStage: Stage<EditorScreen>

    val gameButtons: List<GameButton>
    val variantButtons: List<GameButton>
    val seriesButtons: List<SeriesButton>

    val topOfMinimapBar: Float
        get() {
            return minimapBarStage.location.realY + minimapBarStage.location.realHeight
        }

    init {
        println("EDITOR STAGE INIT")
        gameButtons = mutableListOf()
        variantButtons = mutableListOf()
        seriesButtons = mutableListOf()

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
                              screenHeight = ((Editor.ICON_SIZE + Editor.ICON_PADDING) * Editor.ICON_COUNT_Y) / RHRE3.HEIGHT
                             )
            this.elements += ColourPane(this, this).apply {
                this.colour.set(Editor.TRANSLUCENT_BLACK)
            }
            this.elements += ColourPane(this, this).apply {
                this.colour.set(1f, 1f, 1f, 1f)
                this.location.set(screenX = 0.5f, screenWidth = 0f, screenHeight = 1f, pixelX = 1f, pixelWidth = 1f)
            }
        }
        elements += pickerStage
        patternAreaStage = Stage(this, camera).apply {
            this.location.set(screenY = pickerStage.location.screenY,
                              screenHeight = pickerStage.location.screenHeight,
                              screenX = 0.5f,
                              screenWidth = 0.5f)
            this.elements += ColourPane(this, this).apply {
                this.colour.set(0f, 1f, 0f, 0.5f)
            }
        }
        elements += patternAreaStage
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
                    object : TextLabel<EditorScreen>(palette, messageBarStage, messageBarStage){
                        private var lastVersionTextWidth: Float = -1f

                        override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                            super.render(screen, batch, shapeRenderer)
                            if (main.versionTextWidth != lastVersionTextWidth) {
                                lastVersionTextWidth = main.versionTextWidth
                                this.location.set(screenWidth = 1f - (main.versionTextWidth / messageBarStage.location.realWidth))
                                this.stage.updatePositions()
                            }
                        }
                    }.apply {
                        this.fontScaleMultiplier = 0.5f
                        this.textAlign = Align.bottomLeft
                        this.textWrapping = false
                        this.location.set(0f, -0.5f,
                                          1f,
                                          1.5f,
                                          pixelWidth = -8f)
                        this.isLocalizationKey = false
                    }
        }

        // Picker area
        run picker@ {
            pickerStage.updatePositions()
            gameButtons as MutableList
            variantButtons as MutableList

            val iconWidth = pickerStage.percentageOfWidth(Editor.ICON_SIZE)
            val iconHeight = pickerStage.percentageOfHeight(Editor.ICON_SIZE)
            val iconWidthPadded = pickerStage.percentageOfWidth(Editor.ICON_SIZE + Editor.ICON_PADDING)
            val iconHeightPadded = pickerStage.percentageOfHeight(Editor.ICON_SIZE + Editor.ICON_PADDING)
            val startX = pickerStage.percentageOfWidth(Editor.ICON_PADDING) * 0.5f
            val startY = (1f - pickerStage.percentageOfHeight(Editor.ICON_SIZE + Editor.ICON_PADDING * 0.5f))

            fun UIElement<*>.setLocation(x: Int, y: Int) {
                this.location.set(
                        screenX = startX + iconWidthPadded * x,
                        screenY = startY - iconHeightPadded * y,
                        screenWidth = iconWidth,
                        screenHeight = iconHeight
                                 )
            }

            for (x in 0 until Editor.ICON_COUNT_X + 3) {
                for (y in 0 until Editor.ICON_COUNT_Y) {
                   if (x == Editor.ICON_COUNT_X || x == Editor.ICON_COUNT_X + 2) {
                        if (y != 0 && y != Editor.ICON_COUNT_Y - 1)
                            continue
                       val isUp: Boolean = y == 0
                       val isVariant: Boolean = x == Editor.ICON_COUNT_X + 2
                       val button = Button(palette, pickerStage, pickerStage).apply {
                           this.setLocation(x, y)
                           this.background = false
                           this.addLabel(
                                   object : TextLabel<EditorScreen>(palette, this, this.stage){
                                       override fun getFont(): BitmapFont {
                                           return main.defaultBorderedFont
                                       }
                                   }.apply {
                                       this.setText(
                                               if (isUp) Editor.ARROWS[2] else Editor.ARROWS[3],
                                               Align.center, false, false
                                                   )
                                       this.background = false
                                   })
                       }

                       pickerStage.elements += button
                   } else {
                       val button = GameButton(x, y, palette, pickerStage, pickerStage).apply {
                           this.setLocation(x, y)
                       }
                       gameButtons += button
                       if (x == Editor.ICON_COUNT_X + 2) {
                           variantButtons += button
                       }
                   }
                }
            }
            pickerStage.elements.addAll(gameButtons)
        }

        // Minimap area
        run minimap@ {
            minimapBarStage.updatePositions()
            seriesButtons as MutableList

            val buttonWidth: Float = minimapBarStage.percentageOfWidth(Editor.ICON_SIZE)
            val buttonHeight: Float = 1f

            Series.VALUES.forEachIndexed { index, series ->
                seriesButtons +=
                        SeriesButton(series, palette, minimapBarStage, minimapBarStage).apply {
                            this.location.set(
                                    screenWidth = buttonWidth,
                                    screenHeight = buttonHeight,
                                    screenX = index * buttonWidth
                                             )
                            this.addLabel(
                                    ImageLabel(palette, this, this.stage).apply {
                                        this.image = TextureRegion(AssetRegistry.get<Texture>(series.textureId))
                                        this.renderType = ImageLabel.ImageRendering.RENDER_FULL
                                    }
                                         )
                        }
            }
            minimapBarStage.elements.addAll(seriesButtons)
        }

        // Button bar
        run buttonBar@ {
            buttonBarStage.updatePositions()
            val stageWidth = buttonBarStage.location.realWidth
            val stageHeight = buttonBarStage.location.realHeight
            val padding = Editor.BUTTON_PADDING / stageWidth
            val size = Editor.BUTTON_SIZE / stageWidth
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

    class GameButton(val x: Int, val y: Int,
                     palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage)

    class SeriesButton(val series: Series,
                       palette: UIPalette, parent: UIElement<EditorScreen>, stage: Stage<EditorScreen>)
        : Button<EditorScreen>(palette, parent, stage)

}
