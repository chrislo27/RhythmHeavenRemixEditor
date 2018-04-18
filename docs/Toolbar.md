# Toolbar

![Entire toolbar](readme/toolbar/entire.png)

The toolbar is the topmost bar with various buttons on it.
You can hover over each button to see what they do, but each one's
functionality will be explored in more detail below.

## File I/O
![File I/O buttons](readme/toolbar/fileio.png)

The **New Remix**, **Open Remix**, and **Save Remix** buttons are
generally self-explanatory.

The **New Remix** button will ask you
if you want to start fresh. Be careful, because you cannot undo this!

The **Open Remix** button will ask you what remix file you'd like to open.
You can also open MIDI files (.mid) and load them in as Glee Club
sing cues by default. Be careful, because you will lose all unsaved changes.

The **Save Remix** button will ask you where to save your remix.

## Exporting
![Export button](readme/toolbar/export.png)

This will allow you to export your remixes as WAV, MP3, OGG (Vorbis), or FLAC
files, as long as you have placed an End Remix entity.
If you export the remix as an MP3, you can upload directly
to [picosong](http://picosong.com) afterwards too.

[See this article for more info.](Exporting.md)

## Undo/Redo
![Undo and redo buttons](readme/toolbar/undoredo.png)

These buttons allow you to undo and redo most actions, respectively.
Keyboard shortcuts are **`CTRL+Z`** for undoing, and **`CTRL+Y`**/**`CTRL+SHIFT+Z`** for redoing.

## Music Select, Metronome, and Tapalong
![Music Select, Metronome, and Tapalong](readme/toolbar/tapalong.png)

The first button allows you to select the music. Right clicking on the
button will mute the music temporarily.

The metronome button toggles the metronome. It also ticks at a higher pitch
for the start of each measure (if defined).

The tapalong button brings up a menu to help you find the BPM of your
music.

## Scroll Mode
![Scroll mode](readme/toolbar/scrollmode.png)

This button allows you to toggle what action scrolling (or pressing up and down)
does on a selection. By default, it changes the pitch. An alternative mode
is changing the volume.

[See this section for more details](Readme.md#scroll-mode)

## Playback
![Playback buttons](readme/toolbar/playback.png)

The playback buttons are self-explanatory.
Pressing play will start the remix from the [Playback Start](README.md#playback-start)
marker (or from where it was paused). <br>
Pressing pause will pause the remix, and pressing play again will continue playback.
Pressing stop will stop the remix and reset its position to the Playback Start marker.

## Jump to Beat and Snap Intervals
![Left: Jump to Beat field | Right: Snap Intervals](readme/toolbar/jumpto.png)

The first element is a text field. Type in a beat number to jump straight to it.

The second element is a button. Left click to change the snap intervals to increasingly
smaller increments (wraps around to 1/4), and right click to set it to 1/4.

## Presentation Mode, Views, and Theme Chooser
![Left: Presentation Mode | Middle: Views | Right: Theme Chooser](readme/toolbar/views.png)

The left button is the Presentation Mode button. Use this mode when
recording a remix for use on a video sharing site.
A progress bar will appear, with credits and a link to the project's
GitHub page, along with the current game being played and the current BPM.

The button with an eye on it is the Views menu. You can select different view types there.
<br>[See this section for more details.](README.md#views)

The button with the colour palettes is the Theme Chooser. You can pick
from a variety of pre-built themes, or make your own.
<br>[See this article for more details.](Themes.md)

## Window settings
![Left: Reset Window | Right: Fullscreen](readme/toolbar/window.png)

The left button resets the window to the default size. It will also
revert the program to be in windowed mode if it was in fullscreen previously.

The right button puts the program in fullscreen, if possible.

## Language and Info and Settings
![Left: Language | Right: Info and Settings](readme/toolbar/info.png)

The left button cycles through the languages (right click goes in reverse).
Note: all localizations are not 100% accurate. We appreciate corrections
and suggestions!

The right button goes to the Info and Settings screen. From there you can change
some settings, and view update info and the credits.
