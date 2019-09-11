package io.github.chrislo27.rhre3.desktop

import com.beust.jcommander.Parameter

class Arguments {
    
    @Parameter(names = ["--help", "-h", "-?"], description = "Prints this usage menu.", help = true)
    var printHelp: Boolean = false
    
    // -----------------------------------------------------------
    
    @Parameter(names = ["--force-lazy-sound-load"], description = "Forces lazy-loaded sounds to be loaded when initialized.")
    var lazySoundsForceLoad: Boolean = false
    
    @Parameter(names = ["--fps"], description = "Manually sets the target FPS. Will always be at least 30.")
    var fps: Int = 60
    
    @Parameter(names = ["--log-missing-localizations"], description = "Logs any missing localizations. Other locales are checked against the default properties file.")
    var logMissingLocalizations: Boolean = false
    
    @Parameter(names = ["--skip-git"], description = "Skips checking the online Git repository for the SFX Database completely. This is overridden by --force-git-check.")
    var skipGit: Boolean = false
    
    @Parameter(names = ["--force-git-fetch"], description = "Forces a fetch for the online Git repository for the SFX Database.")
    var forceGitFetch: Boolean = false
    
    @Parameter(names = ["--force-git-check"], description = "Forces checking the online Git repository for the SFX Database.")
    var forceGitCheck: Boolean = false
    
    @Parameter(names = ["--verify-sfxdb"], description = "Verifies and reports warnings/errors for the entire SFX Database after loading.")
    var verifySfxdb: Boolean = false
    
    @Parameter(names = ["--no-analytics"], description = "Disables sending of analytics and crash reports.")
    var noAnalytics: Boolean = false
    
    @Parameter(names = ["--no-online-counter"], description = "Disables the online user count feature.")
    var noOnlineCounter: Boolean = false
    
    @Parameter(names = ["--output-generated-datamodels"], description = "Writes out games that are generated internally in JSON format to console on start-up.")
    var outputGeneratedDatamodels: Boolean = false
    
    @Parameter(names = ["--output-custom-sfx"], description = "Writes out custom SFX that don't have data.json (i.e.: just sound files in a folder) in JSON format to console on start-up.")
    var outputCustomSfx: Boolean = false
    
    @Parameter(names = ["--show-tapalong-markers"], description = "Shows tapalong tap markers.")
    var showTapalongMarkers: Boolean = false
    
    @Parameter(names = ["--midi-recording"], description = "Enables MIDI recording, a hidden feature. Using a MIDI device while the remix is playing will write notes to the remix.")
    var midiRecording: Boolean = false
    
    @Parameter(names = ["--portable-mode"], description = "Puts the `.rhre3/` folder and preferences locally next to the RHRE.jar file.")
    var portableMode: Boolean = false
    
    // -----------------------------------------------------------
    
    @Parameter(names = ["--immmediate-anniversary"], hidden = true)
    var eventImmediateAnniversary: Boolean = false
    @Parameter(names = ["--immmediate-anniversary-like-new"], hidden = true)
    var eventImmediateAnniversaryLikeNew: Boolean = false
    @Parameter(names = ["--immmediate-xmas"], hidden = true)
    var eventImmediateXmas: Boolean = false
    
}