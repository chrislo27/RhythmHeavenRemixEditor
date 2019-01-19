# MIDI capabilities

Did you know that RHRE3 has some (limited) MIDI capabilities?

If you have a MIDI device connected *before starting RHRE*, RHRE will
detect it and use it! (If RHRE is not picking it up, close RHRE, disconnect and reconnect
the MIDI device, and reopen RHRE.)

You can check if your device is detected by playing a few notes. You'll
hear the Glee Club "chorus sing" SFX play back.

## Is it real-time?
Please note that this MIDI feature *may not be real-time*. It depends on
your system's drivers if the MIDI input can be processed in real time or not.
More likely than not, it will not be real-time.

## What can I do with it?

With a MIDI device connected, you can do the following things:

* Edit pitches of selected entities
  * This even allows you to go beyond the usual +/- two octaves!
* If the [launch argument](Launch-arguments.md) `--midi-recording` is enabled:
  * While the **remix is playing**, entering in note inputs through MIDI will cause
  notes to be written.
  * Pressing and releasing the sustain pedal will toggle the remix between playing and paused states.
