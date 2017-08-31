package io.github.chrislo27.rhre3.git

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import io.github.chrislo27.rhre3.RHRE3
import io.github.chrislo27.toolboks.Toolboks
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.lib.NullProgressMonitor
import org.eclipse.jgit.lib.ProgressMonitor
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.lib.RepositoryBuilder
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.URIish
import java.net.URL


object GitHelper {

    val SOUNDS_DIR by lazy {
        val f: FileHandle = Gdx.files.external(".rhre3/sfx/${RHRE3.DATABASE_BRANCH}/")

        f.mkdirs()

        f
    }

    fun makeRepositoryBuilder(mustExist: Boolean = false): RepositoryBuilder =
            RepositoryBuilder().setWorkTree(SOUNDS_DIR.file()).setMustExist(mustExist)

    fun <T> temporarilyUseRepo(mustExist: Boolean = false, action: Repository.() -> T): T {
        val repo = makeRepositoryBuilder(mustExist = mustExist).build()
        val result: T = repo.action()

        repo.close()

        return result
    }

    fun doesGitFolderExist(): Boolean {
        return temporarilyUseRepo {
            this.objectDatabase.exists()
        }
    }

    fun initGitRepo() {
        val git = Git.init().setDirectory(SOUNDS_DIR.file()).call()

        git.remoteAdd().apply {
            setName("origin")
            setUri(URIish(RHRE3.DATABASE_URL))
            call()
        }

        git.close()
    }

    fun initGitRepoIfGitRepoDoesNotExist(): Boolean {
        if (!doesGitFolderExist()) {
            initGitRepo()
            return true
        }

        return false
    }

    fun reset() {
        Toolboks.LOGGER.info("Resetting...")
        temporarilyUseRepo(true) {
            val git = Git(this)
            val result = git.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef("origin/${RHRE3.DATABASE_BRANCH}")
                    .call()
        }
    }

    fun fetchOrClone(progressMonitor: ProgressMonitor = NullProgressMonitor.INSTANCE) {
        if (doesGitFolderExist()) {
            Toolboks.LOGGER.info("Fetching...")
            ensureRemoteExists()
            temporarilyUseRepo(true) {
                val git = Git(this)
                this.directory.resolve("index.lock").delete() // mega naughty
                val result = git.fetch()
                        .setRemote("origin")
                        .setProgressMonitor(progressMonitor)
                        .setRefSpecs(
                                RefSpec("refs/heads/${RHRE3.DATABASE_BRANCH}:refs/remotes/origin/${RHRE3.DATABASE_BRANCH}"))
                        .setCheckFetchedObjects(true)
                        .call()
                if (!result.messages.isNullOrEmpty()) {
                    Toolboks.LOGGER.info("Fetch result has messages: ${result.messages}")
                }
            }
            reset()
        } else {
            Toolboks.LOGGER.info("Cloning...")
            val file = SOUNDS_DIR.file()
            file.deleteRecursively()
            Git.cloneRepository()
                    .setBranch(RHRE3.DATABASE_BRANCH)
                    .setProgressMonitor(progressMonitor)
                    .setRemote("origin")
                    .setURI(RHRE3.DATABASE_URL)
                    .setDirectory(file)
                    .call()
        }
    }

    fun ensureRemoteExists() {
        temporarilyUseRepo() {
            val git = Git(this)
            if ("origin" !in git.repository.remoteNames) {
                val remoteAdd = git.remoteAdd()
                remoteAdd.setName("origin")
                remoteAdd.setUri(URIish(URL(RHRE3.DATABASE_URL)))
                remoteAdd.call()
            }
        }
    }

}