package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import org.asynchttpclient.AsyncCompletionHandlerBase
import org.asynchttpclient.AsyncHandler
import org.asynchttpclient.HttpResponseBodyPart
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToLong


class AutoUpdaterScreen(main: RHRE3Application)
    : ToolboksScreen<RHRE3Application, AutoUpdaterScreen>(main) {
    
    private enum class Progress {
        ERROR, DOWNLOADING, EXTRACTING, READY_TO_COMPLETE
    }
    
    private var spinSeconds: Float = 0f
    private val jarFileLocation: File = File(RHRE3::class.java.protectionDomain.codeSource.location.toURI()) // Will crash if the jar file is not a file
    private val updaterFolder: File = jarFileLocation.resolveSibling("updater/")
    private val containingFolder: File = jarFileLocation.parentFile!!
    private var progress: Progress = Progress.DOWNLOADING
    private val worker: Thread
    
    override val stage: GenericStage<AutoUpdaterScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)
    private val changelogButton: Button<AutoUpdaterScreen>
    private val completeButton: Button<AutoUpdaterScreen>
    private val autocompleteCheckbox: TrueCheckbox<AutoUpdaterScreen>
    private val label: TextLabel<AutoUpdaterScreen>
    
    init {
        require(jarFileLocation.exists())
        require(jarFileLocation.isFile)
        require(jarFileLocation.name == "RHRE.jar")
        val palette = stage.palette
        
        stage.titleLabel.apply {
            this.isLocalizationKey = true
            this.text = "screen.autoUpdater.title"
        }
        stage.titleIcon.apply {
            this.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
        }
        stage.backButton.apply {
            this.visible = false // TODO handle cancellation?
            this.tooltipTextIsLocalizationKey = true
            this.tooltipText = "screen.autoUpdater.cancel"
        }
        stage.onBackButtonClick = {
            this.dispose()
            main.screen = ScreenRegistry["editorVersion"]
        }
        
        label = TextLabel(palette, stage.centreStage, stage.centreStage).apply {
            this.isLocalizationKey = false
            this.text = Localization["screen.autoUpdater.progress.preparing"]
            this.textWrapping = false
        }
        stage.centreStage.elements += label
        
        changelogButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
                Gdx.net.openURI(RHRE3.GITHUB_RELEASES + "/latest")
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.fontScaleMultiplier = 0.9f
                this.text = "screen.version.viewChangelog.short"
            })
            this.location.set(screenX = 0.85f, screenWidth = 0.15f)
        }
        stage.bottomStage.elements += changelogButton
        completeButton = Button(palette, stage.bottomStage, stage.bottomStage).apply {
            this.leftClickAction = { _, _ ->
            
            }
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.autoUpdater.complete"
            })
            this.location.set(screenX = 0.175f, screenWidth = 0.65f)
            this.visible = false
        }
        stage.bottomStage.elements += completeButton
        autocompleteCheckbox = TrueCheckbox(palette, stage.bottomStage, stage.bottomStage).apply {
            this.textLabel.apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.fontScaleMultiplier = 0.9f
                this.text = "screen.autoUpdater.noUserInput"
            }
            this.checked = false
            this.location.set(screenX = 0.175f, screenWidth = 0.65f)
        }
        stage.bottomStage.elements += autocompleteCheckbox
        // End of UI code
        
        if (updaterFolder.exists()) {
            updaterFolder.deleteRecursively()
        }
        updaterFolder.mkdir()
        
        worker = Thread(Runnable {
            try {
                /*
                Stages:
                1. Fetch metadata from GitHub API about latest release
                2. Download archive, store in updater folder
                3. Unpack archive in updater folder
                4. Copy all files from update EXCEPT *.jar, *.sh, and *.bat into main folder
                5. Advance to Complete Update stage for user input
                6. Copy jar file atomically and exit forcibly (System.exit())
                 */
                val releaseResponseBody = RHRE3Application.httpClient
                        .prepareGet("https://api.github.com/repos/chrislo27/RhythmHeavenRemixEditor/releases/latest")
                        .addHeader("Accept", "application/vnd.github.v3+json")
                        .execute().get().responseBody
                val releaseMeta: JsonNode = JsonHandler.OBJECT_MAPPER.readTree(releaseResponseBody)
                val assetNode = (releaseMeta["assets"] as ArrayNode).first {
                    val filename = it["name"].asText()
                    filename.startsWith("RHRE_") && filename.endsWith(".zip")
                }
                val zipUrl = assetNode["browser_download_url"].asText()
                val filesize = assetNode["size"].asLong(1L).coerceAtLeast(1L)
                
                val zipFileLoc = updaterFolder.resolve("RHRE_archive.zip").apply {
                    createNewFile()
                }
                val fileStream = FileOutputStream(zipFileLoc)
                val download = RHRE3Application.httpClient.prepareGet(zipUrl)
                        .execute(object : AsyncCompletionHandlerBase() {
                            private val speedUpdateRate = 500L
                            private var timeBetweenProgress: Long = System.currentTimeMillis()
                            private var lastSpeed = 0L
                            private var speedAcc = 0L
                            private var bytesSoFar = 0L
                            override fun onContentWriteProgress(amount: Long, current: Long, total: Long): AsyncHandler.State {
                                val time = System.currentTimeMillis() - timeBetweenProgress
                                speedAcc += amount
                                if (time >= speedUpdateRate) {
                                    timeBetweenProgress = System.currentTimeMillis()
                                    lastSpeed = (speedAcc / (time / 1000.0)).roundToLong()
                                    speedAcc = 0L
                                }
                                
                                Gdx.app.postRunnable {
                                    val percent = (current.toDouble() / total).coerceIn(0.0, 1.0)
                                    val barLength = 30
                                    val barPortion = "${"█".repeat((percent * barLength).toInt())}[][GRAY]${"█".repeat(((1.0 - percent) * barLength).toInt())}"
                                    val bar = "[[[WHITE]$barPortion[]]"
                                    label.text = Localization["screen.autoUpdater.progress.downloadingArchive",
                                            (percent * 100).roundToLong(), bar, bytesSoFar / 1024, total / 1024,
                                            if (current >= total || lastSpeed <= 0L) "---" else (lastSpeed / 1024)]
                                }
                                return super.onContentWriteProgress(amount, current, total)
                            }
                            
    
                            override fun onBodyPartReceived(content: HttpResponseBodyPart): AsyncHandler.State {
                                fileStream.channel.write(content.bodyByteBuffer)
                                val amount = content.length()
                                val total = filesize // FIXME response bytes won't always match filesize in metadata
                                val time = System.currentTimeMillis() - timeBetweenProgress
                                speedAcc += amount
                                if (time >= speedUpdateRate) {
                                    timeBetweenProgress = System.currentTimeMillis()
                                    lastSpeed = (speedAcc / (time / 1000.0)).roundToLong()
                                    speedAcc = 0L
                                }
                                
                                bytesSoFar += amount
                                
                                Gdx.app.postRunnable {
                                    val percent = (bytesSoFar.toDouble() / total).coerceIn(0.0, 1.0)
                                    val barLength = 30
                                    val barPortion = "${"█".repeat((percent * barLength).toInt())}[][GRAY]${"█".repeat(((1.0 - percent) * barLength).toInt())}"
                                    val bar = "[WHITE]$barPortion[]"
                                    label.text = Localization["screen.autoUpdater.progress.downloadingArchive",
                                            (percent * 100).roundToLong(), bar, bytesSoFar / 1024, total / 1024,
                                            if (bytesSoFar >= total || lastSpeed <= 0L) "---" else (lastSpeed / 1024)]
                                }
                                return AsyncHandler.State.CONTINUE
                            }
                        }).get()
                fileStream.close()
                Gdx.app.postRunnable {
                    progress = Progress.EXTRACTING
                    label.text = Localization["screen.autoUpdater.progress.extractingArchive"]
                }
            } catch (ie: InterruptedException) {
                ie.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
                Gdx.app.postRunnable {
                    progress = Progress.ERROR
                    label.text = Localization["screen.autoUpdater.error", "${e::class.java.canonicalName}: ${e.localizedMessage}"]
                    stage.backButton.visible = true
                    completeButton.visible = false
                    autocompleteCheckbox.visible = false
                }
            } finally {
                cleanup()
            }
        }, "Auto-Updater Worker").apply {
            this.isDaemon = false
        }
        worker.start()
    }
    
    private fun cleanup() {
        // Delete any temporary files created
        updaterFolder.deleteRecursively()
    }
    
    override fun renderUpdate() {
        super.renderUpdate()
        
        spinSeconds += Gdx.graphics.deltaTime * 3f
        if (spinSeconds > 600f) spinSeconds %= 600f
        stage.titleIcon.rotation = (MathUtils.sin(MathUtils.sin(spinSeconds * 2)) + spinSeconds * 0.9f) * -90f
    }
    
    override fun tickUpdate() {
    }
    
    override fun dispose() {
        // Cancel a download if it is happening
        worker.interrupt()
        cleanup()
    }
    
}
