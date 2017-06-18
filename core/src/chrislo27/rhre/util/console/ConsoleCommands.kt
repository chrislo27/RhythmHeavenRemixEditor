package chrislo27.rhre.util.console

import com.badlogic.gdx.Gdx
import java.util.*

object ConsoleCommands {

	fun handle(command: String, args: List<String>): Boolean {
		return when (command.toLowerCase(Locale.ROOT)) {
			"quit", "exit" -> {
				Gdx.app.exit()
				true
			}
			"dumpids" -> {


				false
			}

			else -> false
		}
	}

}