package chrislo27.rhre.version

import com.google.gson.Gson
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import ionium.templates.Main
import kotlin.concurrent.thread


object VersionChecker {

	val url: String = "https://api.github.com/repos/chrislo27/RhythmHeavenRemixEditor2/releases/latest"
	@Volatile var versionState: VersionState = VersionState.GETTING
		private set

	@Volatile
	var releaseObject: ReleaseObject? = null
		private set

	init {
		thread(isDaemon = true) {
			getVersion()
		}
	}

	@Synchronized
	private fun getVersion() {
		try {
			Main.logger.info("Getting version from $url...")
			val nano = System.nanoTime()
			releaseObject = Gson().fromJson(Unirest.get(url).asString().body, ReleaseObject::class.java)
			val release: ReleaseObject = releaseObject!!
			Main.githubVersion = release.tag_name
			val isSame: Boolean = release.tag_name == Main.version
			versionState = if (isSame || Main.version.endsWith("-SNAPSHOT")) VersionState.UP_TO_DATE else VersionState.AVAILABLE

			Main.logger.info(
					"Version gotten successfully! Took ${(System.nanoTime() - nano) / 1000000f} ms | State: $versionState | GitHub version: ${Main.githubVersion}")
		} catch (e: UnirestException) {
			Main.logger.warn("Failed to get version!")
			versionState = VersionState.FAILED
			e.printStackTrace()
		}
	}


}

enum class VersionState {

	GETTING, UP_TO_DATE, AVAILABLE, FAILED

}

class ReleaseObject {

	var html_url: String? = null
	var tag_name: String? = null
	var id: Int = -1
	var name: String? = null
	var body: String? = null
	var published_at: String? = null
	var assets: List<AssetObject>? = null

	class AssetObject {

		var browser_download_url: String? = null
		var name: String? = null
		var size: Long = -1
		var download_count: Long = -1

	}

}
