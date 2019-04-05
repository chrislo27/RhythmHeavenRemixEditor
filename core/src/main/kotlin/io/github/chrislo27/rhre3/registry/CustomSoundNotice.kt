package io.github.chrislo27.rhre3.registry

import io.github.chrislo27.rhre3.RHRE3


object CustomSoundNotice {

    const val DURATION = 1.0f

    @JvmStatic
    fun getCustomSoundNotice(): String {
        return """"Welcome to Team Fortress 2. After nine years in development, hopefully it will have been worth the wait.
To listen to a commentary node, put your crosshair over the floating commentary symbol and press your primary fire.
To stop a commentary node, put your crosshair over the rotating node and press your primary fire again.
Some commentary nodes may take control of the game in order to show something to you.
In these cases, simply press your primary fire again to stop the commentary.
In addition, your secondary fire will cycle you through all the commentary nodes in the level.
Please let me know what you think after you have had a chance to play.
I can be reached at gaben@valvesoftware.com, and my favorite class is the Spy. Thanks, and have fun!"""
    }

    @JvmStatic
    fun getActualCustomSoundNotice(): String {
        return """This folder is where you can add custom sounds without having to go through the hassle of the json setup.
However, you will be a lot more limited in your abilities.


If you'd like to use the full flexibility of the JSON databasing format, try using the RHRE SFX Database Editor:
https://github.com/chrislo27/RSDE


The sounds must be placed in a folder inside this directory.
Each folder is a "game", and you can have multiple folders.
If your folder name already is an existing stock ID, the folder contents
will overwrite the stock game.


Each sound will have these properties:
  * $DURATION beats long by default
  * Stretchable (can change duration)
  * Repitchable (can change pitch)
  * The cues will loop ONLY if the name (not extension) of the file ends with ".loop".
    * For example, "siren.ogg" will NOT loop, but "siren.loop.ogg" WILL loop.

Keep in mind that the editor may run out of memory if you have too many large sound files.

It is advised that you generate JSON versions of the custom games for more flexibility.
You can use data.json files if you need to quickly test databasing changes. For documentation on it, view: https://rhre.readthedocs.io/en/latest/JSON-object-definitions/

Supported audio formats are as follows: ${RHRE3.SUPPORTED_DECODING_SOUND_TYPES}.

Optionally, if you put an icon.png in the folder it will use it as the game icon."""
    }

}