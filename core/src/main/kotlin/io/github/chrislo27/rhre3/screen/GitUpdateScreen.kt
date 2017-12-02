package io.github.chrislo27.rhre3.screen

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.PreferenceKeys
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.rhre3.RHRE3Application
import io.github.chrislo27.rhre3.git.CurrentObject
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.stage.SpinningWheel
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
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.experimental.*
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ProgressMonitor


class GitUpdateScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, GitUpdateScreen>(main) {

    override val stage: Stage<GitUpdateScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val label: TextLabel<GitUpdateScreen>
    @Volatile private var repoStatus: RepoStatus = RepoStatus.UNKNOWN
    @Volatile private var coroutine: Job? = null
    private val spinner: SpinningWheel<GitUpdateScreen>

    init {
        stage as GenericStage
        val palette = main.uiPalette

        stage.titleLabel.setText("screen.database.title")

        label = TextLabel(palette, stage.centreStage, stage.centreStage)
        label.setText("", Align.center, wrapping = true, isLocalization = false)
        stage.centreStage.elements += label

        spinner = SpinningWheel(palette, stage.centreStage,
                                stage.centreStage).apply {
            this.renderType = ImageLabel.ImageRendering.ASPECT_RATIO
            this.location.set(screenHeight = 0.125f, screenY = 0.125f / 2f)
        }
        stage.centreStage.elements += spinner

        stage.bottomStage.elements += object : Button<GitUpdateScreen>(palette, stage.bottomStage,
                                                                       stage.bottomStage) {
            override fun onLeftClick(xPercent: Float, yPercent: Float) {
                super.onLeftClick(xPercent, yPercent)
                Gdx.net.openURI(
                        if (repoStatus == RepoStatus.NO_INTERNET_CANNOT_CONTINUE) RHRE3.GITHUB_RELEASES else RHRE3.DATABASE_RELEASES)
            }

            private var setToReleases = false

            override fun frameUpdate(screen: GitUpdateScreen) {
                if (repoStatus == RepoStatus.NO_INTERNET_CANNOT_CONTINUE && !setToReleases) {
                    (labels.first() as TextLabel).text = "screen.version.button"
                    setToReleases = true
                }

                super.frameUpdate(screen)
            }
        }.apply {
            this.addLabel(TextLabel(palette, this, this.stage).apply {
                this.isLocalizationKey = true
                this.textWrapping = false
                this.text = "screen.info.database"
//                this.fontScaleMultiplier = 0.9f
            })

            this.location.set(screenX = 0.15f, screenWidth = 0.7f)
        }

        stage.updatePositions()
    }

    fun fetch() {
        if (repoStatus == RepoStatus.DOING)
            return
        val nano = System.nanoTime()
        repoStatus = RepoStatus.UNKNOWN
        coroutine = launch(CommonPool) {
            repoStatus = RepoStatus.DOING
            val lastVersion = main.preferences.getInteger(PreferenceKeys.DATABASE_VERSION_BRANCH, -1)
            main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION_BRANCH, -1).flush()

            fun restoreDatabaseVersion() {
                main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION_BRANCH, lastVersion).flush()
            }

            try {
                if (!RHRE3.forceGitFetch || RHRE3.forceGitCheck || RHRE3.DATABASE_BRANCH != RHRE3.DEV_DATABASE_BRANCH) {
                    label.text = Localization["screen.database.checkingGithub"]
                    try {
                        val current = JsonHandler.fromJson<CurrentObject>(
                                khttp.get(RHRE3.DATABASE_CURRENT_VERSION).text)
                        val ver: Version = Version.fromString(current.requiresVersion)

                        Toolboks.LOGGER.info(
                                "Pulled GitHub version in ${(System.nanoTime() - nano) / 1_000_000f} ms, got ${current.version} vs real $lastVersion")

                        val githubVersion: Version? = async(CommonPool) {
                            val nano = System.nanoTime()
                            while (main.githubVersion == Version.RETRIEVING) {
                                delay(100L)
                                if (System.nanoTime() - nano >= 1000L * 1_000_000L) {
                                    break
                                }
                            }
                            return@async main.githubVersion
                        }.await()

                        if (ver > RHRE3.VERSION && githubVersion != null && githubVersion >= ver) {
                            label.text = Localization["screen.database.incompatibleVersion${if (lastVersion >= 0) ".canContinue" else ""}", current.requiresVersion]
                            repoStatus = if (lastVersion < 0) RepoStatus.NO_INTERNET_CANNOT_CONTINUE else RepoStatus.NO_INTERNET_CAN_CONTINUE
                            Toolboks.LOGGER.info(
                                    "Incompatible versions: requires ${current.requiresVersion}, have ${RHRE3.VERSION}")
                            restoreDatabaseVersion()
                            return@launch
                        } else {
//                            if (current.version == lastVersion && !Toolboks.debugMode) {
//                                repoStatus = RepoStatus.DONE
//                                main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION_BRANCH, lastVersion).flush()
//                                GitHelper.reset()
//                                return@launch
//                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // let fetch handle the big boy exceptions
                    }
                }
                GitHelper.ensureRemoteExists()
                GitHelper.fetchOrClone(ScreenProgressMonitor())
                repoStatus = RepoStatus.DONE
                run {
                    val str = GitHelper.SOUNDS_DIR.child("current.json").readString("UTF-8")
                    val obj = JsonHandler.fromJson<CurrentObject>(str)

                    if (obj.version < 0)
                        error("Current database version json object has a negative version of ${obj.version}")

                    main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION_BRANCH, obj.version).flush()
                }

                val time = (System.nanoTime() - nano) / 1_000_000.0
                Toolboks.LOGGER.info("Finished fetch/clone in $time ms")
            } catch (te: TransportException) {
                te.printStackTrace()
                try {
                    GitHelper.ensureRemoteExists()
                    GitHelper.reset()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                repoStatus = if (lastVersion < 0) RepoStatus.NO_INTERNET_CANNOT_CONTINUE else RepoStatus.NO_INTERNET_CAN_CONTINUE
                label.text = Localization["screen.database.transportException." + if (lastVersion < 0) "failed" else "safe"]
                restoreDatabaseVersion()
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    GitHelper.ensureRemoteExists()
                    GitHelper.reset()
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
                repoStatus = RepoStatus.ERROR
                label.text = Localization["screen.database.error"]
                restoreDatabaseVersion()
            }
        }
    }

    private enum class RepoStatus {
        UNKNOWN, DOING, DONE, ERROR, NO_INTERNET_CAN_CONTINUE, NO_INTERNET_CANNOT_CONTINUE, INCOMPAT_VERSION
    }

    private inner class ScreenProgressMonitor : ProgressMonitor {

        var currentTask: Int = 0

        var completedTaskWork: Int = 0
        var taskTotalWork: Int = ProgressMonitor.UNKNOWN
        var task: String? = ""
            set(value) {
                field = if (value == "Updating references") {
                    "Updating references (may take a while)"
                } else {
                    value
                }
            }

        init {
            updateLabel()
        }

        private fun updateLabel() {
            label.text = "${task ?: Localization["screen.database.pending"]}\n" +
                    "$completedTaskWork / $taskTotalWork\n" +
                    Localization["screen.database.tasksCompleted", currentTask.toString()]

        }

        override fun update(completed: Int) {
            completedTaskWork += completed

            updateLabel()
        }

        override fun start(totalTasks: Int) {
            updateLabel()
        }

        override fun beginTask(title: String?, totalWork: Int) {
            currentTask++
            task = title
            completedTaskWork = 0
            taskTotalWork = totalWork

            updateLabel()
        }

        override fun endTask() {
            updateLabel()
        }

        override fun isCancelled(): Boolean {
            return false
        }

    }

    private fun toNextScreen() {
        main.screen = ScreenRegistry["registryLoad"]
    }

    override fun renderUpdate() {
        super.renderUpdate()

        spinner.visible = repoStatus == RepoStatus.DOING

        if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                && (repoStatus == RepoStatus.NO_INTERNET_CAN_CONTINUE))
                || repoStatus == RepoStatus.DONE || (RHRE3.DATABASE_BRANCH == RHRE3.DEV_DATABASE_BRANCH
                && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))) {
            coroutine?.cancel()
            coroutine = null
            toNextScreen()
        }
    }

    override fun show() {
        super.show()

        stage as GenericStage
        if (stage.titleIcon.image == null) {
            stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
        }

        fetch()
    }

    override fun hide() {
        super.hide()

        repoStatus = RepoStatus.UNKNOWN
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}