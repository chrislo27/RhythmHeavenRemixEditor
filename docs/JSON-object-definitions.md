# JSON Object Definitions for Databasing

All `data.json` files for each game are a `DataObject`.

## `DataObject` structure
```json
{
  "id": "lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "Human-Readable Name",
  "requiresVersion": "v3.0.0",
  "objects": []
}
```

The `id` field is the name of the folder this JSON file is in.
It should be lowerCamelCase.

The `name` field is a properly Title Case capitalized name. This is in English.
If this game appears in multiple series, for example both in RHDS and Megamix,
the name of the series used should be in parentheses after the name of the game,
and the name of the minigame should use the most recent English-localized name.
Example: `Space Dance (Megamix)` and `Space Dance (GBA)`.

* Series' names are as follows:
  * Rhythm Tengoku - GBA
  * Rhythm Heaven (Gold) - DS
  * Rhythm Heaven Fever - Fever
  * Rhythm Heaven Megamix - Megamix

The `requiresVersion` field is the minimum version of the program needed to
parse this file.

The `objects` array is an array of various object types, which will be
explained below. It is **very important** that each object type contain
the `type` field, which is used by the JSON deserializer to determine
what object type to deserialize at runtime. This is called *polymorphism*.

`deprecatedIDs` is an array of old IDs that are no longer used, but refer
to this current object for older save files. This field is always the same
even in other object types, and is always present whenever there is an
`id` field **EXCEPT FOR** `PatternCueObject`s.

## `CueObject` structure
```json
{
  "type": "cue",
  "id": "dataObjectID/lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "Human-Readable Cue Name",
  "duration": 1.0,
  // optional fields after this comment
  "stretchable": false,
  "repitchable": false,
  "fileExtension": "ogg"
}
```

A `CueObject` defines a sound to be loaded by the editor. It also contains
metadata such as the duration and its abilities.

The `id` field is structured like this: `dataObjectID/lowerCamelCaseSoundFileName`.
If the parent data object's ID is `spaceDance`, and this sound's name is `turnRight`,
therefore this ID is `spaceDance/turnRight`. If there are more folders, you
should include them in the path separated by more forward slashes. Example:
`flipperFlop/appreciation/nice` is the sound file `nice.ogg` inside the folder `appreciation/`
which has a parent folder of `flipperFlop`.

The `name` field is a name. This is in English, except for the
romanization of foreign language words. Avoid capitals. The only
time you should be using capitals are for the following: proper noun I,
"Remix X", "Fever" (in the context of the game).
If this is syllabic (part of a longer sound cue), you should add a hyphen with
spaces surrounding it to break up words. The program will automatically convert these
into newlines. **Do not use newline characters.**
Example (First Contact): `alien - 1`, `alien - 2`, etc.

The `duration` field is the duration of the cue in beats.

The `stretchable` field is a boolean indicating if the cue can be stretched or not.

The `repitchable` field is a boolean indicating if the cue can have its pitch changed.

The `fileExtension` field is a string indicating the file extension. This is
**ONLY** for backwards compatibility, and will print out a warning when used.
The default format is Ogg Vorbis (file extension `ogg`).

### `TempoBasedCueObject` subtype
```json
{
  "type": "tempoBasedCue",
  // etc...
  "baseBpm": 120.0
}
```

This cue subtype has a `baseBpm` field. The sound effect will be pitched
accordingly during playback based on the current tempo relative to the value
set in `baseBpm`. The duration of the sound relative to the original duration
will also affect the pitch (only applicable if the cue is `stretchable`).

### `FillbotsFillCueObject` subtype
```json
{
  "type": "fillbotsFillCue",
  // etc...
}
```

This simply indicates that the pitch should be modified during playback,
as it does in Fillbots for the filling sound effect.

### `LoopingCueObject` subtype
```json
{
  "type": "loopingCue",
  // etc...
  // optional fields after this comment
  "introSound": "soundCueID"
}
```

This cue subtype loops. It has an optional `introSound` field
which is the sound cue ID to play **once** at the start of the cue. This
is used for cues like Screwbot Factory's "whirring" noise.

## `PatternObject` and `PatternCueObject` structure
```json
{
  "type": "pattern",
  "id": "lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "Human-Readable Cue Name",
  "cues": [ // array of PatternCueObjects
    {
      "id": "cueID",
      "beat": 1.0,
      // optional fields after this comment
      "duration": 1.0,
      "semitone": 1,
      "track": 2,
      "stretchable": false
    }
  ]
}
```

A `PatternObject` is a series of cues bundled together. They contain default
settings for each cue, such as its position (in beats and track number), its
pitch adjustment (`semitone`), and `duration` (defaults to original duration).

The `id` field is structured like this: `dataObjectID_lowerCamelCase`.
If the parent data object's ID is `spaceDance`, and this pattern's ID is `turnRight`,
therefore this ID is `spaceDance_turnRight`. You'll notice this is similar to
the cue ID naming convention, but it always has underscores and never has forward
slashes.

A `PatternCueObject` is a cue definition inside of a pattern. Its `id` is
identical to the `CueObject` ID you intend to use. All settings are also
identical to whatever `CueObject` has, except some may be omitted. You should
look in the example to see what fields are allowed. **NOTE**: this object
does not use the `deprecatedIDs` field as it should always point to the
most recent ID.

