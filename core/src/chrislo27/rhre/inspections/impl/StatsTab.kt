package chrislo27.rhre.inspections.impl

import chrislo27.rhre.Main
import chrislo27.rhre.entity.HasGame
import chrislo27.rhre.entity.PatternEntity
import chrislo27.rhre.entity.SoundEntity
import chrislo27.rhre.inspections.InspectionTab
import chrislo27.rhre.registry.Series
import chrislo27.rhre.track.Remix
import chrislo27.rhre.util.PieChartRenderer
import chrislo27.rhre.util.Slice
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import ionium.util.Utils
import ionium.util.i18n.Localization

class StatsTab : InspectionTab() {
	override val name: String = "inspections.stats.title"

	private var cues: Int = 0
	private var patterns: Int = 0
	private var tempoChanges: Int = 0
	private var duration: String = ""

	var seriesChart: Map<Slice, Float> = mapOf()
	var gameChart: Map<Slice, Float> = mapOf()

	private lateinit var remix: Remix

	override fun initialize(remix: Remix) {
		this.remix = remix
		cues = remix.entities.filterIsInstance<SoundEntity>().count()
		patterns = remix.entities.filterIsInstance<PatternEntity>().count()
		tempoChanges = remix.tempoChanges.getCount()
		val beatInSeconds = remix.tempoChanges.beatsToSeconds(remix.endTime)
		duration = String.format("%1$02d:%2$02.3f", (Math.abs(beatInSeconds) / 60).toInt(),
								 Math.abs(beatInSeconds) % 60)

		val games = remix.entities.filterIsInstance(HasGame::class.java).map(HasGame::game).distinct()

		gameChart = games.mapIndexed { index, game ->
			val count: Int = remix.entities.filter { it is HasGame && it.game == game }.count()

			return@mapIndexed Slice(Color().set(Utils.HSBtoRGBA8888(index.toFloat() / games.size, 0.9f, 0.75f)),
									game.name) to count.toFloat()
		}.sortedBy { it.second }.toMap()

		seriesChart = games.map{it.series}.distinct().mapIndexed { index, series ->
			val count: Int = remix.entities.filter { it is HasGame && it.game.series == series }.count()

			return@mapIndexed Slice(Color().set(Utils.HSBtoRGBA8888(index.toFloat() / Series.values.size, 0.9f, 0.75f)),
									series.getLocalizedName()) to count.toFloat()
		}.sortedBy { it.second }.toMap()

	}

	override fun render(main: Main, batch: SpriteBatch, startX: Float, startY: Float, width: Float, height: Float,
						mouseXPx: Float, mouseYPx: Float) {
		Main.drawCompressed(main.font, batch,
							Localization.get("inspections.stats", cues, patterns, tempoChanges, duration),
							startX + 32, startY + main.font.lineHeight, width - 64, Align.center)

		var str: String? = null
		batch.end()
		main.shapes.begin(ShapeRenderer.ShapeType.Filled)
		val radius: Float = 100f
		PieChartRenderer.render(main.shapes, startX + width * 0.25f, startY + main.font.lineHeight * 3 + radius, radius,
								gameChart, mouseXPx, mouseYPx, { slice, percent ->
									str = Localization.get("inspections.stats.percentage", slice.name,
														   "" + Math.round(
																   percent * remix.entities.size) + " / " + remix.entities.size,
														   String.format("%.2f", percent * 100))
								})
		PieChartRenderer.render(main.shapes, startX + width * 0.5f, startY + main.font.lineHeight * 3 + radius, radius,
								seriesChart, mouseXPx, mouseYPx, { slice, percent ->
									str = Localization.get("inspections.stats.percentage", slice.name,
														   "" + Math.round(
																   percent * remix.entities.size) + " / " + remix.entities.size,
														   String.format("%.2f", percent * 100))
								})
		main.shapes.color = Color.WHITE
		main.shapes.end()
		batch.begin()

		main.font.data.setScale(0.75f)
		Main.drawCompressed(main.font, batch, Localization.get("inspections.stats.gamePercentages"),
							startX + width * 0.25f - radius,
							startY + main.font.lineHeight * 6 + radius * 2, radius * 2, Align.center)
		Main.drawCompressed(main.font, batch, Localization.get("inspections.stats.seriesPercentages"),
							startX + width * 0.5f - radius,
							startY + main.font.lineHeight * 6 + radius * 2, radius * 2, Align.center)
		main.font.data.setScale(1f)

		if (str != null) {
			Main.drawCompressed(main.font, batch, str!!,
								startX + 32, startY + main.font.lineHeight * 2, width - 64, Align.left)
		}
	}

}
