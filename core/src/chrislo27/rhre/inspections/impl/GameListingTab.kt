package chrislo27.rhre.inspections.impl

import chrislo27.rhre.Main
import chrislo27.rhre.entity.HasGame
import chrislo27.rhre.inspections.InspectionTab
import chrislo27.rhre.registry.Game
import chrislo27.rhre.track.Remix
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.utils.Align
import ionium.util.MathHelper
import ionium.util.Utils
import ionium.util.i18n.Localization
import java.util.*

abstract class GameListingTab : InspectionTab() {
    protected var map: LinkedHashMap<String, List<Game>> = linkedMapOf()
    protected abstract val noneText: String

    protected abstract fun populateMap(remix: Remix, list: List<Game>): LinkedHashMap<String, List<Game>>

    override fun initialize(remix: Remix) {
        map = populateMap(remix, remix.entities.filterIsInstance(HasGame::class.java).map(HasGame::game).distinct())
    }

    override fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
                        mouseXPx: Float, mouseYPx: Float) {
        if (map.isEmpty()) {
            main.font.draw(batch, Localization.get(noneText), startX + 32, startY + height / 2 + main.font.lineHeight,
                           width - 64, Align.center, true)
        } else {
            val line: Float = main.font.lineHeight * 1.5f
            val originalX: Float = startX + 32
            var x: Float = originalX
            var y: Float = startY + height - 8
            val longest: Float = Math.min(map.map { Utils.getWidth(main.font, Localization.get(it.key)) }.max() ?: 256f,
                                          256f)
            var hoveredGame: Game? = null
            map.forEach { element ->
                Main.drawCompressed(main.font, batch, Localization.get(element.key), x, y - main.font.capHeight / 2,
                                    longest, Align.left)

                var iconX: Float = 0f
                val iconSize: Float = 32f
                element.value.forEachIndexed { i, it ->
                    fun sx() = x + iconX + longest + 32

                    if (sx() + iconSize > startX + width) {
                        iconX = 0f
                        y -= line
                    }

                    batch.draw(it.iconTexture, sx(), y - iconSize, iconSize, iconSize)
                    if (MathHelper.intersects(sx(), y - iconSize, iconSize, iconSize, mouseXPx, mouseYPx, 1f, 1f)) {
                        hoveredGame = it
                    }
                    iconX += 40f
                }

                y -= line
                x = originalX
            }

            if (hoveredGame != null) {
                val game = hoveredGame!!
                batch.setColor(0f, 0f, 0f, 0.75f)

                val text = game.name
                val boxwidth = Utils.getWidth(main.font, text) + 16
                val drawX = Math.min(mouseXPx, startX + width - boxwidth)

                ionium.templates.Main.fillRect(batch, drawX, mouseYPx, boxwidth, Utils.getHeight(main.font, text) + 16)
                batch.setColor(1f, 1f, 1f, 1f)
                main.font.draw(batch, text, drawX + 8, mouseYPx + 8 + main.font.capHeight)

            }
        }
    }

}
