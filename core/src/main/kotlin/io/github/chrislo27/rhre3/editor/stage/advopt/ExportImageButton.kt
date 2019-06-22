package io.github.chrislo27.rhre3.editor.stage.advopt

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.PixmapIO
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.ScreenUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.editor.Editor
import io.github.chrislo27.rhre3.editor.stage.EditorStage
import io.github.chrislo27.rhre3.entity.model.ModelEntity
import io.github.chrislo27.rhre3.screen.EditorScreen
import io.github.chrislo27.rhre3.util.FileChooser
import io.github.chrislo27.rhre3.util.FileChooserExtensionFilter
import io.github.chrislo27.rhre3.util.getDefaultDirectory
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ui.*
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.math.sqrt


class ExportImageButton(val editor: Editor, palette: UIPalette, parent: UIElement<EditorScreen>,
                        stage: Stage<EditorScreen>)
    : Button<EditorScreen>(palette, parent, stage), EditorStage.HasHoverText {

    init {
        addLabel(TextLabel(palette, this, this.stage).apply {
            this.text = "Export\nImage"
            this.textWrapping = false
            this.isLocalizationKey = false
            this.fontScaleMultiplier = 0.35f
        })
    }

    override fun render(screen: EditorScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
        this.enabled = editor.remix.duration.isFinite()
        super.render(screen, batch, shapeRenderer)
    }

    override fun frameUpdate(screen: EditorScreen) {
        super.frameUpdate(screen)

        this.visible = editor.main.advancedOptions
    }

    override fun getHoverText(): String {
        return "Export remix as image"
    }

    override fun onLeftClick(xPercent: Float, yPercent: Float) {
        super.onLeftClick(xPercent, yPercent)
        val filters = listOf(FileChooserExtensionFilter("Supported image files", "*.png"))
        FileChooser.saveFileChooser("Choose image to export to", getDefaultDirectory(), null, filters) { file ->
            val remix = editor.remix
            val duration = remix.duration
            if (file != null && duration > 0f) {
                Gdx.app.postRunnable {
                    val singleRowWidth = (duration + 1) * Editor.ENTITY_WIDTH
                    val singleRowHeight = ((remix.trackCount + 4) * Editor.ENTITY_HEIGHT)
                    val rowEstimate = (sqrt(singleRowWidth / singleRowHeight).roundToInt()).coerceAtLeast(1)
                    val rows = rowEstimate
                    val pixmap = Pixmap(((duration + 1) * Editor.ENTITY_WIDTH / rows).roundToInt(), singleRowHeight.roundToInt() * rows, Pixmap.Format.RGBA8888)

                    val oldCamX = editor.camera.position.x
                    val oldCamZoom = editor.camera.zoom
                    val buffer = FrameBuffer(Pixmap.Format.RGB888, RHRE3.WIDTH, RHRE3.HEIGHT, false, true)
                    val wasTextOnScreen = ModelEntity.attemptTextOnScreen
                    ModelEntity.attemptTextOnScreen = false

                    buffer.begin()

                    Toolboks.LOGGER.debug("Pixmap size: ${pixmap.width} by ${pixmap.height} with $rows rows")

                    val stepValue = (editor.camera.viewportWidth * Editor.ENTITY_WIDTH).roundToInt()
                    for (x in 0..(pixmap.width * rows) step stepValue) {
                        editor.camera.position.x = x / Editor.ENTITY_WIDTH + editor.camera.viewportWidth / 2f - 0.5f
                        editor.camera.zoom = 1f
                        editor.camera.update()

                        editor.render(updateDelta = false, otherUI = false, noGlassEffect = true, disableThemeUsesMenu = true)

                        val bufPix = ScreenUtils.getFrameBufferPixmap(0, 0, buffer.width, buffer.height)
                        val currentRow = x / (pixmap.width)
                        pixmap.drawPixmap(bufPix, x % (pixmap.width), (rows - 1 - currentRow) * (pixmap.height / rows), 0, (Editor.ENTITY_HEIGHT * 4.5f).roundToInt(), bufPix.width, pixmap.height / rows)
                        // if the end of this section spans into the next row, render that section too
                        val endRow = (x + stepValue) / (pixmap.width)
                        if (endRow > currentRow) {
                            Toolboks.LOGGER.debug("Stepping into next row: $currentRow -> $endRow")
                            pixmap.drawPixmap(bufPix, (x + stepValue) % (pixmap.width) - stepValue, (rows - 1 - endRow) * (pixmap.height / rows), 0, (Editor.ENTITY_HEIGHT * 4.5f).roundToInt(), bufPix.width, pixmap.height / rows)
                        }
                        bufPix.dispose()

                        Toolboks.LOGGER.debug("Copying to pixmap: ${x / (pixmap.width.toFloat() * rows) * 100}%")
                    }

                    editor.camera.position.x = oldCamX
                    editor.camera.zoom = oldCamZoom
                    editor.camera.update()

                    buffer.end()

                    Toolboks.LOGGER.debug("Writing to file...")
                    try {
                        val writer = PixmapIO.PNG((pixmap.width.toFloat() * pixmap.height.toFloat() * 1.5f).toInt()) // Guess at deflated size.
                        try {
                            writer.setFlipY(true)
                            writer.write(FileHandle(file), pixmap)
                        } finally {
                            writer.dispose()
                        }
                    } catch (ex: IOException) {
                        throw GdxRuntimeException("Error writing PNG: $file", ex)
                    }
                    Toolboks.LOGGER.debug("Done.")
                    pixmap.dispose()
                    buffer.dispose()
                    ModelEntity.attemptTextOnScreen = wasTextOnScreen
                }
            }
        }
    }
}