package io.github.chrislo27.rhre3.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.graphics.Color
import com.beust.jcommander.JCommander
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.logging.Logger
import java.io.File

object DesktopLauncher {
    
    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/chrislo27/RhythmHeavenRemixEditor/issues/273
        System.setProperty("jna.nosys", "true")
        
        RHRE3.launchArguments = args.toList()
        
        val arguments = Arguments()
        val jcommander = JCommander.newBuilder().addObject(arguments).build()
        jcommander.parse(*args)
        
        if (arguments.printHelp) {
            println("${RHRE3.TITLE} ${RHRE3.VERSION}\n${RHRE3.GITHUB}\n\n${StringBuilder().apply { jcommander.usage(this) }}")
            return
        }
        
        val logger = Logger()
        val portable = arguments.portableMode
        val app = RHRE3Application(logger, File(if (portable) ".rhre3/logs/" else System.getProperty("user.home") + "/.rhre3/logs/"))
        ToolboksDesktopLauncher(app)
                .editConfig {
                    this.width = app.emulatedSize.first
                    this.height = app.emulatedSize.second
                    this.title = app.getTitle()
                    this.fullscreen = false
                    this.foregroundFPS = arguments.fps.coerceAtLeast(30)
                    this.backgroundFPS = this.foregroundFPS.coerceIn(30, 60)
                    this.resizable = true
                    this.vSyncEnabled = this.foregroundFPS <= 60
                    this.initialBackgroundColor = Color(0f, 0f, 0f, 1f)
                    this.allowSoftwareMode = true
                    this.audioDeviceSimultaneousSources = 250
                    this.useHDPI = true
                    if (portable) {
                        this.preferencesFileType = Files.FileType.Local
                        this.preferencesDirectory = ".rhre3/.prefs/"
                    }
                    
                    RHRE3.portableMode = portable
                    RHRE3.skipGitScreen = arguments.skipGit
                    RHRE3.forceGitFetch = arguments.forceGitFetch
                    RHRE3.forceGitCheck = arguments.forceGitCheck
                    RHRE3.verifySfxDb = arguments.verifySfxdb
                    RHRE3.immediateEvent = when {
                        arguments.eventImmediateAnniversaryLikeNew -> 2
                        arguments.eventImmediateAnniversary -> 1
                        arguments.eventImmediateXmas -> 3
                        else -> 0
                    }
                    RHRE3.noAnalytics = arguments.noAnalytics
                    RHRE3.noOnlineCounter = arguments.noOnlineCounter
                    RHRE3.outputGeneratedDatamodels = arguments.outputGeneratedDatamodels
                    RHRE3.outputCustomSfx = arguments.outputCustomSfx
                    RHRE3.showTapalongMarkers = arguments.showTapalongMarkers
                    RHRE3.midiRecording = arguments.midiRecording
                    RHRE3.logMissingLocalizations = arguments.logMissingLocalizations
                    RHRE3.disableCustomSounds = arguments.disableCustomSounds
                    LazySound.loadLazilyWithAssetManager = !arguments.lazySoundsForceLoad
                    
                    val sizes: List<Int> = listOf(256, 128, 64, 32, 24, 16)
                    sizes.forEach {
                        this.addIcon("images/icon/$it.png", Files.FileType.Internal)
                    }
                    
                    listOf(24, 16).forEach {
                        this.addIcon("images/icon/$it.png", Files.FileType.Internal)
                    }
                }
                .launch()
    }
    
}
