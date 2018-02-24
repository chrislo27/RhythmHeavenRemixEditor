package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.registry.GameRegistry
import io.github.chrislo27.rhre3.registry.datamodel.impl.Cue
import io.github.chrislo27.rhre3.soundsystem.beads.BeadsSoundSystem
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.LoadingIcon
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.ImageLabel
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.asynchttpclient.AsyncCompletionHandlerBase
import org.asynchttpclient.AsyncHandler
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.request.body.multipart.FilePart
import org.asynchttpclient.request.body.multipart.StringPart
import java.io.File
import java.nio.charset.Charset
import kotlin.math.roundToLong


class UploadRemixScreen(main: RHRE3Application, private val file: File, private val filename: String)
    : ToolboksScreen<RHRE3Application, UploadRemixScreen>(main) {

    companion object {
        const val MAX_PICOSONG_BYTES: Int = 16000000
        private const val PICOSONG_USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36"
        private const val PICOSONG_UPLOAD_URL = "http://picosong.com/async-upload/"
        const val PICOSONG_MAIN_URL = "http://picosong.com/"
        const val PICOSONG_TOS_URL = "http://picosong.com/tos"
        const val PICOSONG_AUP_URL = "http://picosong.com/aup"
    }

    override val stage: Stage<UploadRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    @Volatile
    private var isUploading = false

    private val http: AsyncHttpClient
        get() = RHRE3Application.httpClient

    private val gotoButton: Button<UploadRemixScreen>
    @Volatile
    private var picosongUrl: String? = null
    private val mainLabel: TextLabel<UploadRemixScreen>

    init {
        stage as GenericStage
        val palette = main.uiPalette
        stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_update"))
        stage.titleLabel.text = "screen.upload.title"
        stage.backButton.visible = true
        stage.onBackButtonClick = {
            if (!isUploading) {
                main.screen = ScreenRegistry.getNonNull("editor")
            }
        }

        stage.centreStage.elements += object : LoadingIcon<UploadRemixScreen>(palette, stage.centreStage) {
            override var visible: Boolean = true
                get() = super.visible && isUploading
        }.apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.125f, screenY = 0.125f / 2f)
        }

        mainLabel = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.location.set(screenHeight = 0.75f, screenY = 0.25f)
            this.textAlign = Align.center
            this.isLocalizationKey = false
            this.text = ""
        }
        stage.centreStage.elements += mainLabel

        gotoButton = object : Button<UploadRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {

            private var timeUntilCopyReset = 0.0f

            override fun render(screen: UploadRemixScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                if (timeUntilCopyReset > 0) {
                    timeUntilCopyReset -= Gdx.graphics.deltaTime
                    if (timeUntilCopyReset <= 0) {
                        timeUntilCopyReset = 0f
                        (this.labels.firstOrNull() as? TextLabel?)?.text = "screen.upload.button"
                    }
                }
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                picosongUrl?.let(Gdx.net::openURI)
            }

            override fun onRightClick(xPercent: Float, yPercent: Float) {
                super.onRightClick(xPercent, yPercent)
                picosongUrl?.let {
                    Gdx.app.clipboard.contents = it
                    (this.labels.firstOrNull() as? TextLabel?)?.text = "screen.upload.button.copied"
                    timeUntilCopyReset = 2f
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.upload.button"
            })

            this.visible = false

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }
        stage.bottomStage.elements += gotoButton
    }

    override fun renderUpdate() {
        super.renderUpdate()

        stage as GenericStage
        stage.backButton.enabled = !isUploading
    }

    override fun show() {
        super.show()
        if (!isUploading) {
            isUploading = true

            mainLabel.text = Localization["screen.upload.preparing"]

            launch(CommonPool) {
                fun playEndSound(success: Boolean) {
                    BeadsSoundSystem.resume()
                    if (success) {
                        (GameRegistry.data.objectMap["mrUpbeatWii/applause"] as? Cue)
                    } else {
                        (GameRegistry.data.objectMap["mountainManeuver/toot"] as? Cue)
                    }?.sound?.sound?.play(loop = false, pitch = 1f, rate = 1f, volume = 1f)
                            ?: Toolboks.LOGGER.warn("Export SFX (success=$success) not found")
                }
                try {
                    val initial = http.prepareGet(PICOSONG_MAIN_URL)
                            .execute().get()

                    val cookies = initial.cookies
                    val csrfmiddlewaretoken = cookies.find { it.name() == "csrftoken" }?.value()

                    val upload = http.preparePost(PICOSONG_UPLOAD_URL)
                            .addHeader("Accept", "application/json, text/javascript, */*")
                            .addHeader("Accept-Encoding", "gzip, deflate")
                            .addHeader("Accept-Language", "en-US,en;q=0.5")
                            .addHeader("Connection", "keep-alive")
                            .addHeader("Host", "picosong.com")
                            .addHeader("Referer", "http://picosong.com/")
                            .addHeader("User-Agent", PICOSONG_USER_AGENT)
                            .addHeader("X-Requested-With", "XMLHttpRequest")
                            .setBodyParts(listOf(
                                    StringPart("csrfmiddlewaretoken", csrfmiddlewaretoken),
                                    FilePart("file", file, "audio/mp3", Charset.forName("UTF-8"),
                                             filename)
                                                ))
                            .setCookies(cookies)
                            .execute(object : AsyncCompletionHandlerBase() {
                                override fun onContentWritten(): AsyncHandler.State {
                                    return super.onContentWritten()
                                }

                                private val speedUpdateRate = 500L
                                private var timeBetweenProgress: Long = System.currentTimeMillis()
                                private var lastSpeed = 0L
                                private var speedAcc = 0L

                                override fun onContentWriteProgress(amount: Long, current: Long,
                                                                    total: Long): AsyncHandler.State {
                                    val time = System.currentTimeMillis() - timeBetweenProgress

                                    speedAcc += amount

                                    if (time >= speedUpdateRate) {
                                        timeBetweenProgress = System.currentTimeMillis()
                                        lastSpeed = (speedAcc / (time / 1000f)).roundToLong()
                                        speedAcc = 0L
                                    }

                                    mainLabel.text = Localization["screen.upload.uploading",
                                            current, total, (current.toDouble() / total * 100).roundToLong(),
                                            if (current >= total || lastSpeed <= 0) "---" else (lastSpeed / 1024)]
                                    return super.onContentWriteProgress(amount, current, total)
                                }
                            })
                            .get()

                    val result = JsonHandler.OBJECT_MAPPER.readTree(upload.responseBody)

                    if (result["status"]?.asText(null) == "success") {
                        val redirectStr = result["redirect"]?.asText(null) ?: error("No redirect url given!")
                        val redirectUrl = PICOSONG_MAIN_URL + redirectStr.substring(1) // Remove first /

                        mainLabel.text = Localization["screen.upload.done", redirectUrl]
                        picosongUrl = redirectUrl
                        gotoButton.visible = true
//                        Gdx.net.openURI(redirectUrl)
                    } else {
                        error("Non-successful result:\n${upload.responseBody}")
                    }

                    playEndSound(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    mainLabel.text = Localization["screen.upload.failed", e.javaClass.name, e.message]
                    playEndSound(false)
                } finally {
                    isUploading = false
                }
            }
        }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}
