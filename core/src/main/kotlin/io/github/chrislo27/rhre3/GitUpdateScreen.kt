package io.github.chrislo27.rhre3

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Align
import io.github.chrislo27.rhre3.git.GitHelper
import io.github.chrislo27.rhre3.stage.GenericStage
import io.github.chrislo27.toolboks.ToolboksScreen
import io.github.chrislo27.toolboks.i18n.Localization
import io.github.chrislo27.toolboks.registry.AssetRegistry
import io.github.chrislo27.toolboks.ui.Stage
import io.github.chrislo27.toolboks.ui.TextLabel
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
        stage.elements += label

        stage.updatePositions()
    }

    fun fetch() {
        repoStatus = RepoStatus.UNKNOWN
        launch(CommonPool) {
            repoStatus = RepoStatus.DOING
            try {
                GitHelper.fetchOrClone(ScreenProgressMonitor())
                repoStatus = RepoStatus.DONE
            } catch(te: TransportException) {
                te.printStackTrace()
                repoStatus = RepoStatus.NO_INTERNET
                label.text = "screen.database.transportException"
            } catch (e: Exception) {
                e.printStackTrace()
                repoStatus = RepoStatus.ERROR
                label.text = "screen.database.error"
            }
        }
    }

    private enum class RepoStatus {
        UNKNOWN, DOING, DONE, ERROR, NO_INTERNET
    }

    private inner class ScreenProgressMonitor : ProgressMonitor {

        var currentTask: Int = 0

        var completedTaskWork: Int = 0
        var taskTotalWork: Int = ProgressMonitor.UNKNOWN
        var task: String? = ""

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

    override fun show() {
        super.show()

        stage as GenericStage
        if (stage.titleIcon.image == null) {
            stage.titleIcon.image = TextureRegion(AssetRegistry.get<Texture>("ui_icon_updatesfx"))
        }

        fetch()
    }

    override fun tickUpdate() {
    }

    override fun dispose() {
    }

}