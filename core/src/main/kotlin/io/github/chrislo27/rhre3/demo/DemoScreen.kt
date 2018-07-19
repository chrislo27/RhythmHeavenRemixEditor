package io.github.chrislo27.rhre3.demo

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.concurrent.thread


class DemoScreen(main: RHRE3Application, val nextScreen: Screen?)
    : ToolboksScreen<RHRE3Application, DemoScreen>(main) {

    private enum class ButtonState(val text: String, val enabled: Boolean) {
        CHECKING("Checking internet time...", false),
        NO_INTERNET("[#FF5656]Failed to get time[]\n[LIGHT_GRAY]Click to try again[]", true),
        SUCCESS("[#56FF56]Proceed[]", true),
        TOO_EARLY("[#FF5656]Too early - please wait until the start time[]\n[LIGHT_GRAY]Click to close program[]", true),
        EXPIRED("[#FF5656]Time expired[]\n[LIGHT_GRAY]Click to close program[]", true);
    }

    override val stage: GenericStage<DemoScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val label: TextLabel<DemoScreen>
    private val button: Button<DemoScreen>

    init {
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("logo_ex_mono_128"))
        stage.titleLabel.isLocalizationKey = false
        stage.titleLabel.text = "RHRExpansion Demo Mode"

        label = TextLabel(main.uiPalette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.textAlign = Align.center
            this.text = "This is a special demo version of RHRE ${RHRE3.VERSION.copy(suffix = "")}.\nIt contains the new [CYAN]swing[], [CYAN]track resize[], and [CYAN]stored pattern[] features.\n\nThis version is only valid from\n[LIGHT_GRAY]${Demo.localStartTime.toString().replace("T", " ")} ${ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)}[] to [LIGHT_GRAY]${Demo.localEndTime.toString().replace("T", " ")} ${ZoneId.systemDefault().getDisplayName(TextStyle.SHORT, Locale.ROOT)}[],\nand will immediately exit once time expires.\n\nAn internet connection is required at startup.\n\nUpdated documentation is available at\n[LIGHT_GRAY]http://rhre.readthedocs.io/en/dev/README/[]"
        }
        stage.centreStage.elements += label

        button = object : Button<DemoScreen>(main.uiPalette, stage.bottomStage, stage.bottomStage) {
            val label: TextLabel<DemoScreen>
                get() = labels.first() as TextLabel<DemoScreen>

            var state = ButtonState.CHECKING
            var time: ZonedDateTime? = null

            init {
                addLabel(TextLabel(main.uiPalette, this, this.stage).apply {
                    this.isLocalizationKey = false
                    this.text = ""
                })
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                when (state) {
                    ButtonState.NO_INTERNET -> {
                        state = ButtonState.CHECKING
                        update()
                        check()
                    }
                    ButtonState.SUCCESS -> {
                        main.screen = nextScreen
                        Demo.startForceExitThread(time ?: ZonedDateTime.now())
                    }
                    ButtonState.EXPIRED -> {
                        Gdx.app.exit()
                        thread(isDaemon = true) {
                            Thread.sleep(1000L)
                            System.exit(0)
                        }
                    }
                    else -> {
                        // NO-OP
                    }
                }

                update()
            }

            fun update() {
                label.text = state.text
                enabled = state.enabled
            }

            fun check() {
                RHRE3Application.httpClient.prepareGet("https://google.com")
                        .execute().toCompletableFuture()
                        .handle { res, ex ->
                            Gdx.app.postRunnable {
                                if (ex != null) {
                                    ex.printStackTrace()

                                    state = ButtonState.NO_INTERNET
                                    update()
                                } else {
                                    try {
                                        val returnedDate = Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(res.getHeader("Date"))).atZone(ZoneId.of("Z"))
                                        time = returnedDate
                                        state = if (returnedDate in Demo.startTime..Demo.endTime) ButtonState.SUCCESS else (if (returnedDate < Demo.startTime) ButtonState.TOO_EARLY else ButtonState.EXPIRED)
                                        update()
                                    } catch (e: Exception) {
                                        e.printStackTrace()

                                        state = ButtonState.NO_INTERNET
                                        update()
                                    }
                                }
                            }
                        }

            }

            init {
                update()
                check()
            }
        }.apply {
            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
            this.enabled = false
        }
        stage.bottomStage.elements += button

        stage.bottomStage.elements += object : Button<DemoScreen>(main.uiPalette, stage.bottomStage, stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI("http://rhre.readthedocs.io/en/dev/README/")
            }
        }.apply {
            this.addLabel(
                    TextLabel(palette, this, this.stage).apply {
                        this.isLocalizationKey = false
                        this.text = "Go to\ndocs"
                    })
            this.alignment = Align.bottomRight
            this.location.set(screenHeight = 1f,
                              screenWidth = this.stage.percentageOfWidth(this.stage.location.realHeight))
            this.location.set(screenX = this.location.screenWidth)
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}