package chrislo27.rhre.util

import chrislo27.rhre.editor.Editor

object CustomSoundUtil {

	const val DURATION = 0.5f

	@JvmStatic
	fun getCustomSoundNotice(): String {
		return "Welcome to Team Fortress 2. After nine years in development, hopefully it will have been worth the wait. " +
				"To listen to a commentary node, put your crosshair over the floating commentary symbol and press your primary fire. " +
				"To stop a commentary node, put your crosshair over the rotating node and press your primary fire again. " +
				"Some commentary nodes may take control of the game in order to show something to you. " +
				"In these cases, simply press your primary fire again to stop the commentary. In addition," +
				" your secondary fire will cycle you through all the commentary nodes in the level. " +
				"Please let me know what you think after you have had a chance to play. " +
				"I can be reached at gaben@valvesoftware.com, and my favorite class is the Spy. Thanks, and have fun!"
	}

	@JvmStatic
	fun getActualCustomSoundNotice(): String {
		return "This folder is where you can add custom sounds without having to go through the hassle of the json setup.\nHowever, " +
				"you will be a lot more limited.\n\n" +
				"The sounds must be placed in a folder inside this directory. " +
				"Each folder is a \"game\", and you can have multiple folders.\n\n" +
				"Each sound will be limited to these factors:\n" +
				"Duration: $DURATION beats\n" +
				"You will be able to stretch the cues.\n" +
				"You will be able to change the duration of the cues.\n" +
				"The cues will not loop. If you want them to loop, you will have to manually add a game using the JSON files.\n\n" +
				"You can have at most ${Editor.ICON_COUNT_X * Editor.ICON_COUNT_Y} custom games. " +
				"If you have too many they may not show up.\n\n" +
				"It is advised that you generate JSON versions of the custom games for more flexibility.\n\n" +
				"Supported audio formats are ogg, wav, and mp3.\n" +
				"Optionally, if you put an icon.png in the folder it will use it."
	}

}
