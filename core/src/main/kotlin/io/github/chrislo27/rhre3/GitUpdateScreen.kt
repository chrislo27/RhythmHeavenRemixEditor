package io.github.chrislo27.rhre3

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.git.CurrentObject
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.rhre3.util.JsonHandler
import io.github.chrislo27.toolboks.Toolboks
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.registry.ScreenRegistry
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
import io.github.chrislo27.toolboks.version.Version
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.launch
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.lib.ProgressMonitor


class GitUpdateScreen(main: RHRE3Application) : ToolboksScreen<RHRE3Application, GitUpdateScreen>(main) {

    override val stage: Stage<GitUpdateScreen> = GenericStage(main.uiPalette, null, main.defaultCamera)

    private val label: TextLabel<GitUpdateScreen>
    private @Volatile var repoStatus: RepoStatus = RepoStatus.UNKNOWN

    init {
        stage as GenericStage

        stage.titleLabel.setText("screen.database.title")

        label = TextLabel(main.uiPalette, stage.centreStage, stage.centreStage)
        label.setText("", Align.center, wrapping = true, isLocalization = false)
        stage.centreStage.elements += label

        stage.updatePositions()

        println("LABEL: " + label.location.realHeight + " compared to stage ${label.stage.location.realHeight}")
    }

    fun fetch() {
        if (repoStatus == RepoStatus.DOING)
            return
        val nano = System.nanoTime()
        repoStatus = RepoStatus.UNKNOWN
        launch(CommonPool) {
            repoStatus = RepoStatus.DOING
            val lastVersion = main.preferences.getInteger(PreferenceKeys.DATABASE_VERSION, -1)
            main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION, -1).flush()
            try {
                run {
                    label.text = Localization["screen.database.checkingGithub"]
                    try {
                        val current = JsonHandler.fromJson<CurrentObject>(
                                khttp.get(RHRE3.DATABASE_CURRENT_VERSION).text)
                        Toolboks.LOGGER.info("Pulled GitHub version in ${(System.nanoTime() - nano) / 1_000_000f} ms")
                        val ver: Version = Version.fromString(current.requiresVersion)

                        if (ver > RHRE3.VERSION) {
                            label.text = Localization["screen.database.incompatibleVersion${if (lastVersion >= 0) ".canContinue" else ""}", current.requiresVersion]
                            repoStatus = if (lastVersion < 0) RepoStatus.NO_INTERNET_CANNOT_CONTINUE else RepoStatus.NO_INTERNET_CAN_CONTINUE
                            Toolboks.LOGGER.info(
                                    "Incompatible versions: requires ${current.requiresVersion}, have ${RHRE3.VERSION}")
                            return@launch
                        } else {
                            if (current.version == lastVersion) {
                                repoStatus = RepoStatus.DONE
                                return@launch
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // let fetch handle the big boy exceptions
                    }
                }
                GitHelper.fetchOrClone(ScreenProgressMonitor())
                repoStatus = RepoStatus.DONE
                run {
                    val str = GitHelper.SOUNDS_DIR.child("current.json").readString("UTF-8")
                    val obj = JsonHandler.fromJson<CurrentObject>(str)

                    if (obj.version < 0)
                        error("Current database version json object has a negative version of ${obj.version}")

                    main.preferences.putInteger(PreferenceKeys.DATABASE_VERSION, obj.version).flush()
                }

                val time = (System.nanoTime() - nano) / 1_000_000.0
                Toolboks.LOGGER.info("Finished fetch/clone in $time ms")
            } catch (te: TransportException) {
                te.printStackTrace()
                GitHelper.reset()
                repoStatus = if (lastVersion < 0) RepoStatus.NO_INTERNET_CANNOT_CONTINUE else RepoStatus.NO_INTERNET_CAN_CONTINUE
                label.text = Localization["screen.database.transportException." + if (lastVersion < 0) "failed" else "safe"]
            } catch (e: Exception) {
                e.printStackTrace()
                repoStatus = RepoStatus.ERROR
                label.text = Localization["screen.database.error"]
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

        if ((Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
                && (repoStatus == RepoStatus.NO_INTERNET_CAN_CONTINUE))
                || repoStatus == RepoStatus.DONE) {
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