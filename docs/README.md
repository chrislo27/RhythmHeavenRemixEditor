# Readme

This readme is up to date with version `v3.6.7`.

## Launching the program
After decompressing/unzipping the zip archive, run the
`run_windows.bat` file by double clicking if you're on Windows, or
run the `run_macOS-linux.sh` file if you're on macOS or a UNIX operating
system.

Alternatively, open Command Prompt/Terminal inside
the folder *containing the jar file* and run
`java -jar RHRE3.jar`.

## How does a remix work?
Remixes in RHRE3 have changed since RHRE2.

They are now no longer dictated by the last cue, but instead go on forever
until you place an **End Remix** entity.
You can find this at `Misc. > Special Entities > End Remix`.

![End remix entity](readme/endRemix.png)

If you are familiar with music theory, this also is used in sheet music
to end the music piece.
You can only have **one** of these in the track at any given time.<br/>
If you liked the old RHRE2 way of ending remixes, there's an option in the
Info Screen.

Cues and patterns are examples of *entities*. RHRE3 adds three new entities:
random cue, equidistant, and keep the beat.

**Random cues** will randomly play a cue or pattern each time you play the remix.
This is useful for games like First Contact, or Ringside.

**Equidistant** entities are for patterns like Built to Scale (DS) or
Bouncy Road. By *stretching* the entity, you will change the time *between*
cues. If you want your "do-re-mi-fa-so" (Built to Scale DS) to have 2 beats
in between, just extend this entity to 2 beats long. The editor will take
care of adding the other cues for you!

**Keep the beat** entities are for games like Lockstep, Flockstep, or any other
game that has a consistent pattern. By *stretching* this entity, you can
make constant beats without laying down multiple patterns.

## Controls
If you are familiar with RHRE2's controls, you should have no problem
with RHRE3.

### Generic keystrokes
**`SPACEBAR`** - Play/stop remix<br/>
**`SHIFT+SPACEBAR`** - Pause/resume remix<br/>
**`CTRL+N`** - New remix<br/>
**`CTRL+O`** - Open remix<br/>
**`CTRL+S`** - Save remix<br/>
**`CTRL+Z`** - Undo<br/>
**`CTRL+SHIFT+Z`** or **`CTRL+Y`** - Redo<br/>
**`HOME`** - Jump to beat 0<br/>
**`END`** - Jump to last entity/End Remix entity<br>
**`UP`** or **`W`** - Scroll up (only in editor)<br>
**`DOWN`** or **`S`** - Scroll down (only in editor)<br>

On the top bar, you will see various buttons. Hover over them to view
what they're used for.

On the bottom half of the screen, you will see icons for each
game on the left, and patterns and cues for the selected
game on the right. Using the Selection tool, you can drag
from the pattern area up to the track to place down patterns.
**`SCROLL`** or click on the arrows to cycle through your options.

In the bottom-right, you will see tools:

![Tools](readme/tools.png)

From first to last: **Selection**, **Multipart Split**, **Tempo Change**,
**Music Volume**, and **Time Signature**. They will be explained below.
You can access them quickly by pressing `1-5` on the number keys.

To the left of the tool buttons you'll see a minimap.
**`LEFT CLICK`** will jump the camera to that position.
Hover over the minimap to bring up a preview. You can also go to the
Info and Settings menu to disable the minimap and/or the preview.

![Minimap and preview](readme/minimapPreview.png)

>If the preview doesn't appear, check the Info and Settings menu.
The preview may have disabled itself if your graphics driver is not able
to support it. ![Minimap unsupported](readme/minimapUnsupported.png)

### Selection tool
The selection tool will be your main tool. It allows you to
drag up cues and patterns from the pattern selector and create
selections on the track.

Holding **`LEFT CLICK`** (not on a selected entity) will start a selection.<br/>
You can hold **`SHIFT`** to make it additive (i.e.: you can add to a
pre-existing selection). Starting in `v3.1.0`, holding **`CONTROL`** will
make it an inverted selection.<br>
**`RIGHT CLICK`** will cancel the selection.

![Dragging](readme/selectionTool.png)

After you have made your selection, you can hold **`LEFT CLICK`** and
drag. You can drag it around anywhere as long as it's still fully in the track,
and doesn't collide with any other entities.

If you hold **`ALT`** while starting a drag, you will copy the selection.

If you hold **`CONTROL+ALT`** while starting a drag, you will make what's called
a *response-copy*. For games like First Contact, or Working Dough, making a
response-copy of the "leader" cues will make a special copy of the player
cues! This is one example of new functionality added to RHRE3.

Pressing **`BACKSPACE`** or **`DELETE`** with a selection will delete it.

**`SCROLL`** or **`UP`**/**`DOWN`** will change the pitch by semitones, if possible.

## Multipart Split tool
The multipart split tool is very simple: **`LEFT`** clicking on a *multipart entity*
(that is, an entity made up of multiple entities like patterns, random cues,
equidistants, and keep-the-beats) will split it into whatever it was made of.

Use this when you need to fine-tune a pattern's components.

## Tempo Change, Music Volume, Time Signature tools
These tools are very similar. They control what are called *trackers*.

>Note: wherever you see `SCROLL`, you can substitute this with **`UP`**
or **`DOWN`**. This is useful if you don't have access to a mouse, like
on a laptop.

All tracker-like tools share these controls:<br>
**`LEFT CLICK`** - Place tracker<br>
**`RIGHT CLICK`** - Remove tracker<br>
**`SCROLL`** - Change value<br>
Hold **`CONTROL`** - Change value by 5x

![Tempo changes](https://i.imgur.com/mS0qg3n.png)<br>
The **tempo change** tool has changed slightly from RHRE2. You can change the tempo
mid-remix. You can hold **`SHIFT`** to change it in increments of 0.1 BPM.
Holding **`CONTROL`** while holding `SHIFT` will change it in increments of
0.5 BPM. Starting in v3.6.0, clicking and dragging the ends
will allow you to have gradual speed ups or slow downs.

![Music volume changes](https://i.imgur.com/HQSTPeT.png)<br>
The **music volume** tool is new in RHRE3. You can change the music volume
mid-remix as a percentage from 0-200%. Starting in v3.6.0, clicking and
dragging the ends will allow you to make smooth transitions
for a perfect fade-out.

![4/4 time signature](https://i.imgur.com/SwG9g8n.png)<br>
The **time signature** tool is new in RHRE3. You can change the time signature
mid-remix as a value over 4 (that is, everything is in X/4 time). If you
are fluent with music theory, you will use this tool to your advantage
to create measures so your patterns don't sound "off". You can only place this
on whole, non-negative beats.

# Tapalong
The tapalong menu is recurring from RHRE2, this time it is inline with the
editor. The main purpose of tapalong is to be able to tap to the rhythm of
a song to determine its BPM.

![Tapalong](https://i.imgur.com/PYNsBYZ.png)

# Views
Views are useful ways of viewing your remix. You can mix and match views too.

## Game Boundaries
This highlights all the game boundaries and where they transition at.
This also works in presentation mode. A game is said to transition when
a new cue/pattern from a different game is introduced.

![Game boundaries](https://i.imgur.com/Ek397zm.png)

## Waveform
This shows the live audio data as the remix plays.
This also works in presentation mode.

![Waveform](readme/waveform.png)
