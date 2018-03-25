package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Colors
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.analytics.AnalyticsHandler
import io.github.chrislo27.rhre3.discord.DiscordHelper
import io.github.chrislo27.rhre3.discord.PresenceState
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
import io.github.chrislo27.toolboks.ui.*
import io.netty.handler.codec.http.cookie.Cookie
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
        private const val PICOSONG_NEXT_URL = "http://picosong.com/next/"
        private const val PICOSONG_SKIP_FIELDS_URL = "http://picosong.com/success/"
    }

    override val stage: Stage<UploadRemixScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    @Volatile
    private var isUploading = false

    private val http: AsyncHttpClient
        get() = RHRE3Application.httpClient

    private val gotoButton: Button<UploadRemixScreen>
    private val copyButton: Button<UploadRemixScreen>
    @Volatile
    private var picosongEditID: String? = null
    private val mainLabel: TextLabel<UploadRemixScreen>
    private val editStage: Stage<UploadRemixScreen>
    private val verifyFields: Map<String, TextField<UploadRemixScreen>>
    private val titleField: TextField<UploadRemixScreen>
    private val artistField: TextField<UploadRemixScreen>
    private val albumField: TextField<UploadRemixScreen>
    private val yearField: TextField<UploadRemixScreen>
    private val trackField: TextField<UploadRemixScreen>
    private val genreField: TextField<UploadRemixScreen>

    init {
        stage as GenericStage
        verifyFields = mutableMapOf()
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

        copyButton = object : Button<UploadRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {

            private val textLabel: TextLabel<UploadRemixScreen>
                get() = this.labels.first() as TextLabel
            private var timeUntilCopyReset = System.currentTimeMillis()

            override fun render(screen: UploadRemixScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                if (timeUntilCopyReset > 0) {
                    if (System.currentTimeMillis() > timeUntilCopyReset) {
                        timeUntilCopyReset = 0L
                        textLabel.text = "screen.upload.finalize.button"
                    }
                }
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (!picosongEditID.isNullOrBlank()) {
                    Gdx.app.clipboard.contents = "$PICOSONG_SKIP_FIELDS_URL$picosongEditID/"
                    timeUntilCopyReset = System.currentTimeMillis() + 2000L
                    textLabel.text = "screen.upload.copied"
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.upload.finalize.button"
            })

            this.visible = false

            this.location.set(screenX = 0.25f, screenWidth = 0.5f)
        }
        stage.bottomStage.elements += copyButton

        // Edit stage + fields + labels
        editStage = object : Stage<UploadRemixScreen>(stage.centreStage, stage.centreStage.camera) {
            override fun keyTyped(character: Char): Boolean {
                if (character == '\t') {
                    val list = verifyFields.values.toList()
                    val indexOfFocused = list.indexOfFirst(TextField<UploadRemixScreen>::hasFocus)
                    if (indexOfFocused == -1) {
                        list.first().hasFocus = true
                    } else if (indexOfFocused >= list.size - 1) {
                        list.forEach {
                            it.hasFocus = false
                        }
                    } else {
                        list[indexOfFocused].hasFocus = false
                        list[indexOfFocused + 1].hasFocus = true
                    }
                    return true
                }

                return super.keyTyped(character)
            }
        }
        editStage.visible = false
        stage.centreStage.elements += editStage

        // Description label
        val descHeight = 0.25f
        editStage.elements += TextLabel(palette, editStage, editStage).apply {
            this.isLocalizationKey = true
            this.textAlign = Align.center
            this.text = "screen.upload.edit"
            this.location.set(screenX = 0f, screenY = 1f - descHeight, screenWidth = 1f, screenHeight = descHeight)
        }

        // Labels
        val labelList = listOf("title", "artist", "album", "year", "track", "genre")
        val labelWidth = 0.2f
        val labelPadding = 0.025f
        val labelHeight = (1f - descHeight) / labelList.size
        labelList.forEachIndexed { index, type ->
            editStage.elements += TextLabel(palette, editStage, editStage).apply {
                this.isLocalizationKey = true
                this.textAlign = Align.right
                this.text = "screen.upload.edit.$type"
                this.location.set(screenX = 0f, screenY = 1f - descHeight - labelHeight * (index + 1),
                                  screenWidth = labelWidth - labelPadding, screenHeight = labelHeight)
                this.textColor = Color.LIGHT_GRAY
            }
        }

        // Fields
        val fieldHeight = labelHeight * 0.8f
        val fieldGap = (1f - fieldHeight / labelHeight) / 2 * labelHeight
        val fieldPalette = palette.copy(backColor = Color(0.25f, 0.25f, 0.25f, 1f))

        titleField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 300

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 1 + fieldGap,
                              screenWidth = 0.75f, screenHeight = fieldHeight)
        }
        artistField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 300

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 2 + fieldGap,
                              screenWidth = 0.75f, screenHeight = fieldHeight)
        }
        albumField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 300

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 3 + fieldGap,
                              screenWidth = 0.75f, screenHeight = fieldHeight)
        }
        yearField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 6 // value must be less or equal to 999999
            this.canTypeText = Char::isDigit

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 4 + fieldGap,
                              screenWidth = 0.75f / 2, screenHeight = fieldHeight)
        }
        trackField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 20
            this.canTypeText = { it.isDigit() || it == '/' || it == ' ' }

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 5 + fieldGap,
                              screenWidth = 0.75f / 2, screenHeight = fieldHeight)
        }
        genreField = TextField(fieldPalette, editStage, editStage).apply {
            this.canPaste = true
            this.background = true
            this.characterLimit = 300

            this.location.set(screenX = labelWidth, screenY = 1f - descHeight - labelHeight * 6 + fieldGap,
                              screenWidth = 0.75f / 2, screenHeight = fieldHeight)
        }

        verifyFields["title"] = titleField
        verifyFields["artist"] = artistField
        verifyFields["album"] = albumField
        verifyFields["year"] = yearField
        verifyFields["track"] = trackField
        verifyFields["genre"] = genreField
        editStage.elements.addAll(verifyFields.values)

        // Clear all fields
        editStage.elements += object : Button<UploadRemixScreen>(palette, editStage, editStage) {

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                clearAllFields()
                verifyFields.values.also {
                    it.forEach { it.hasFocus = false }
                    it.first().hasFocus = true
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.upload.edit.clearAll"
            })

            this.location.set(screenX = labelWidth + 0.4f, screenY = 1f - descHeight - labelHeight * 6 + fieldGap,
                              screenWidth = 0.35f, screenHeight = labelHeight * 2 - fieldGap * 2)
        }

        // Save and go to URL button
        gotoButton = object : Button<UploadRemixScreen>(palette, stage.bottomStage, stage.bottomStage) {

            private val textLabel: TextLabel<UploadRemixScreen>
                get() = this.labels.first() as TextLabel
            private val fieldsNotBlank by lazy { "screen.upload.save" to Colors.get("PICOSONG") }
            private val fieldsBlank by lazy { "screen.upload.save.blank" to Colors.get("LIGHT_GRAY") }

            override fun render(screen: UploadRemixScreen, batch: SpriteBatch, shapeRenderer: ShapeRenderer) {
                super.render(screen, batch, shapeRenderer)
                if (areFieldsEmpty()) {
                    textLabel.text = fieldsBlank.first
                    textLabel.textColor = fieldsBlank.second
                } else {
                    textLabel.text = fieldsNotBlank.first
                    textLabel.textColor = fieldsNotBlank.second
                }
            }

            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                if (!picosongEditID.isNullOrBlank() && !isUploading) {
                    if (areFieldsEmpty()) {
                        Gdx.net.openURI("$PICOSONG_SKIP_FIELDS_URL$picosongEditID/")
                        this.visible = false
                        editStage.visible = false

                        copyButton.visible = true
                        mainLabel.text = Localization["screen.upload.finalize"]
                    } else {
                        this.visible = false
                        editStage.visible = false

                        mainLabel.text = Localization["screen.upload.edit.finishing"]
                        isUploading = true
                        launch(CommonPool) {
                            val (csrfmiddlewaretoken, cookies) = getCsrfTokenAndGetCookies()

                            val req = http.preparePost("$PICOSONG_NEXT_URL$picosongEditID/")
                                    .addHeader("Accept", "application/json, text/javascript, */*")
                                    .addHeader("Accept-Encoding", "gzip, deflate")
                                    .addHeader("Accept-Language", "en-US,en;q=0.5")
                                    .addHeader("Connection", "keep-alive")
                                    .addHeader("Host", "picosong.com")
                                    .addHeader("Referer", "http://picosong.com/")
                                    .addHeader("User-Agent", PICOSONG_USER_AGENT)
                                    .addHeader("X-Requested-With", "XMLHttpRequest")
                                    .setCookies(cookies)
                                    .addFormParam("csrfmiddlewaretoken", "$csrfmiddlewaretoken")

                            verifyFields.entries.forEach {
                                //                                if (it.value.text.isNotBlank())
                                req.addFormParam(it.key, it.value.text)
                            }

                            try {
                                val res = req.execute().get()

                                if (res.statusCode == 302) {
                                    val location = res.headers["Location"] ?: error(
                                            "Redirect location header not found:\n$res")
                                    copyButton.visible = true
                                    mainLabel.text = Localization["screen.upload.finalize"]
                                    Gdx.net.openURI(location)
                                } else {
                                    error("Bad status code (not 302):\n$res")
                                }
                            } catch (t: Throwable) {
                                t.printStackTrace()
                                mainLabel.text = Localization["screen.upload.failed", t.javaClass.name, t.message]
                            } finally {
                                isUploading = false
                            }
                        }
                    }

                    // Analytics
                    launch(CommonPool) {
                        val regex = "<h2 class=\"short-link text-center\"><a href=\"http://picosong\\.com/([a-zA-Z0-9]+)\">".toRegex()
                        val body = http.prepareGet("$PICOSONG_SKIP_FIELDS_URL$picosongEditID/")
                                .addHeader("Accept", "application/json, text/javascript, */*")
                                .addHeader("Accept-Encoding", "gzip, deflate")
                                .addHeader("Accept-Language", "en-US,en;q=0.5")
                                .addHeader("Connection", "keep-alive")
                                .addHeader("Host", "picosong.com")
                                .addHeader("Referer", "http://picosong.com/")
                                .addHeader("User-Agent", PICOSONG_USER_AGENT)
                                .execute().get().responseBody

                        val short: String = regex.find(body)?.groups?.get(1)?.value ?: "N/A"

                        AnalyticsHandler.track("Upload Remix",
                                               mapOf(
                                                       "fields" to verifyFields.entries.associate { it.key to it.value.text },
                                                       "url" to "$PICOSONG_MAIN_URL$short"
                                                    ))
                    }
                }
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.upload.save"
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
            DiscordHelper.updatePresence(PresenceState.Uploading)

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
                    val (csrfmiddlewaretoken, cookies) = getCsrfTokenAndGetCookies()

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
                        picosongEditID = redirectStr.substring(6) // Remove "/next/" from start

                        mainLabel.text = ""
                        gotoButton.visible = true
                        clearAllFields()
                        editStage.visible = true
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

    private fun getCsrfTokenAndGetCookies(): Pair<String?, List<Cookie>> {
        val initial = http.prepareGet(PICOSONG_MAIN_URL)
                .execute().get()

        val cookies = initial.cookies
        val csrfmiddlewaretoken = cookies.find { it.name() == "csrftoken" }?.value()

        return csrfmiddlewaretoken to cookies
    }

    private fun clearAllFields() {
        verifyFields.forEach { it.value.text = "" }
    }

    private fun areFieldsEmpty(): Boolean {
        return verifyFields.all { it.value.text.isBlank() }
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

    override fun hide() {
        super.hide()
        editStage.visible = false
        clearAllFields()
        gotoButton.visible = false
        copyButton.visible = false
    }
}
