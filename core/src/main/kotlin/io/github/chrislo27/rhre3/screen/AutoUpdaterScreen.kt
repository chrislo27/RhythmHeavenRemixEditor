package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.TrueCheckbox
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Button
import io.github.chrislo27.toolboks.ui.TextLabel
import java.io.File


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
