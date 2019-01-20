package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.ColourPane
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture


@Suppress("ConstantConditionIf")
class OnlineCounterScreen(main: RHRE3Application, title: String) : ToolboksScreen<RHRE3Application, OnlineCounterScreen>(main) {

    override val stage: GenericStage<OnlineCounterScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val request: CompletableFuture<Void>

    init {
        stage.titleLabel.text = title
        stage.titleLabel.isLocalizationKey = false
        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_info"))
        }
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            main.screen = ScreenRegistry.getNonNull("info")
        }

        val centre = stage.centreStage

        val centreLoadingIcon = LoadingIcon(main.uiPalette, centre)
        centre.elements += centreLoadingIcon.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.25f, screenY = 0.5f - 0.125f)
        }

        val barsStage = Stage(centre, centre.camera).apply {
            this.visible = false
        }
        centre.elements += barsStage

        val weeklyGraph = false
        request = RHRE3Application.httpClient.prepareGet("https://zorldo.auroranet.me:10443/rhre3/live/history")
                .addQueryParam("weekly", "$weeklyGraph")
                .execute().toCompletableFuture()
                .thenAccept { response ->
                    if (response.statusCode == 200) {
                        try {
                            val history = JsonHandler.fromJson<DailyOnlineHistoryObj>(response.responseBody)
                            val list = history.hours.map { (it.key.toIntOrNull() ?: -1) to it.value}.sortedByDescending(Pair<Int, HourlyHistoryObject>::first)
                            val max = list.maxBy { it.second.max }?.second?.max ?: 1
                            val minColor = Color.valueOf("#FF8C8C")
                            val maxColor = Color.valueOf("#95FF8C")
                            list.forEachIndexed { i, (hoursAgo, data) ->
                                val x = (i + 0.5f) / list.size
                                val width = 1f / list.size * 0.9f
                                if (!(data.max == 0 && data.min == 0)) {
                                    barsStage.elements += ColourPane(barsStage, barsStage).apply {
                                        this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0.25f + 0.6f * (data.max.toFloat() / max), screenHeight = 0.0125f)
                                        this.colour.set(maxColor)
                                    }
                                    barsStage.elements += ColourPane(barsStage, barsStage).apply {
                                        this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0.25f, screenHeight = 0.6f * (data.mean.toFloat() / max))
                                    }
                                    barsStage.elements += ColourPane(barsStage, barsStage).apply {
                                        this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0.25f + 0.6f * (data.min.toFloat() / max), screenHeight = 0.0125f)
                                        this.colour.set(minColor)
                                    }
                                }
                                barsStage.elements += TextLabel(main.uiPalette, barsStage, barsStage).apply {
                                    this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0.15f, screenHeight = 0.1f)
                                    this.isLocalizationKey = false
                                    this.textWrapping = false
                                    this.text = "$hoursAgo${if (weeklyGraph) "d" else "h"}"
                                    this.fontScaleMultiplier = 0.75f
                                }
                                barsStage.elements += TextLabel(main.uiPalette, barsStage, barsStage).apply {
                                    this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0.075f, screenHeight = 0.075f)
                                    this.isLocalizationKey = false
                                    this.textWrapping = false
                                    this.text = "${data.mean}"
                                    this.fontScaleMultiplier = 0.75f
                                }
                                barsStage.elements += TextLabel(main.uiPalette, barsStage, barsStage).apply {
                                    this.location.set(screenX = x - width / 2, screenWidth = width, screenY = 0f, screenHeight = 0.075f)
                                    this.isLocalizationKey = false
                                    this.textWrapping = false
                                    this.text = "[#$minColor]${data.min}[]/[#$maxColor]${data.max}[]"
                                    this.fontScaleMultiplier = 0.5f
                                }
                            }
                            barsStage.updatePositions()
                            barsStage.visible = true
                            centreLoadingIcon.visible = false
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
    }

    override fun renderUpdate() {
        super.renderUpdate()
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && stage.backButton.visible && stage.backButton.enabled) {
            stage.onBackButtonClick()
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun hide() {
        super.hide()
        request.cancel(true)
    }

    class DailyOnlineHistoryObj {
        val hours: MutableMap<String, HourlyHistoryObject> = mutableMapOf()
    }
    class HourlyHistoryObject {

        lateinit var hour: ZonedDateTime
        var min = 0
        var max = 0
        var mean = 0
        var median = 0
        var samplePoints = 0

    }
}