package io.github.chrislo27.rhre3.desktop

import com.badlogic.gdx.Files
import com.badlogic.gdx.graphics.Color
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.toolboks.desktop.ToolboksDesktopLauncher
import io.github.chrislo27.toolboks.lazysound.LazySound
import io.github.chrislo27.toolboks.logging.Logger
import java.io.File

object DesktopLauncher {

    @JvmStatic fun main(args: Array<String>) {
        RHRE3.launchArguments = args.toList()

        // ONEDAY console commands
//        thread(isDaemon = true) {
//            while (true) {
//                val input: String = readLine() ?: break
//                val arguments: List<String> = input.split("\\s+".toRegex())
//
//                try {
////                    if (ConsoleCommands.handle(main, arguments.first(), arguments.drop(1)))
////                        break
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
//        }

        val logger = Logger()
        val app = RHRE3Application(logger, File(System.getProperty("user.home") + "/.rhre3/logs"))
        ToolboksDesktopLauncher(app)
                .editConfig {
                    this.width = app.emulatedSize.first
                    this.height = app.emulatedSize.second
                    this.title = app.getTitle()
                    this.fullscreen = false
                    this.foregroundFPS = 60
                    this.backgroundFPS = 60
                    this.resizable = true
                    this.vSyncEnabled = true
                    this.initialBackgroundColor = Color(0f, 0f, 0f, 1f)
                    this.allowSoftwareMode = true
                    this.audioDeviceSimultaneousSources = 250
                    this.useHDPI = true

                    RHRE3.skipGitScreen = "--skip-git" in args
                    RHRE3.forceGitFetch = "--force-git-fetch" in args
                    RHRE3.forceGitCheck = "--force-git-check" in args
                    RHRE3.verifyRegistry = "--verify-registry" in args
                    RHRE3.immediateAnniversary = if ("--immediate-anniversary-like-new" in args) 2 else (if ("--immediate-anniversary" in args) 1 else 0)
                    RHRE3.noAnalytics = "--no-analytics" in args
                    LazySound.loadLazilyWithAssetManager = "--force-lazy-sound-load" !in args

                    val sizes: List<Int> = listOf(256, 128, 64, 32, 24, 16)

                    sizes.forEach {
                        this.addIcon("images/icon/$it.png", Files.FileType.Internal)
                    }
                }
                .launch()
    }

}
