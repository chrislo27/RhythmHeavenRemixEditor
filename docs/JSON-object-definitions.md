# JSON Object Definitions for Databasing

All `data.json` files for each game are a `DataObject`.

## `DataObject` structure
```json
{
  "id": "lowerCamelCaseID",
  "name": "Human-Readable Name",
  "requiresVersion": "v3.0.0",
  "objects": [],
  "series": "tengoku",
  // optional fields after this comment
  "group": "Human-Readable Name",
  "groupDefault": false,
  "priority": 0
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

The optional `group` field is the full name of the group this data object
belongs to, if it has variants. For example, `gleeClubEnMegamix` would
have the group value be `Glee Club (Megamix)` to be grouped with other
data objects that also have the same group value. Games with `groupDefault`
set to true will appear earlier in the variant list.

The optional `priority` field is an integer for how the games should
be sorted. Higher numbers come first, lower numbers come last.

## Deprecated IDs
`deprecatedIDs` is an array of old IDs that are no longer used, but refer
to this current object for older save files. This field is always the same
even in other object types, and is always present whenever there is an
`id` field **EXCEPT FOR** metadata-like objects like `CuePointerObject`,
or `DataObject`.

## `CuePointerObject` structure
```json
{
  "id": "cueID",
  "beat": 1.0,
  // optional fields after this comment
  "duration": 1.0,
  "semitone": 0,
  "track": 0
}
```

This object is special. First of all, it **doesn't** have deprecated IDs
like other ID-based objects do. This is specifically an object that
"points" to a `CueObject`. It's basically the definition for spawning
cue objects. The usage of this object WILL vary from other object types,
and if you need more data on what's used/isn't used per object, see the
specific comments. For example, `PatternObject` (below) uses the base
implementation of `CuePointerObject`.

As a result, this object will never appear by itself. The fields are similar
to `CueObject`'s own cues, so you can look there. **Note that** the only
fields shown here ARE the ones it has, and not every field from `CueObject`
is inherited.

## `CueObject` structure
```json
{
  "type": "cue",
  "id": "dataObjectID/lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "human-readable cue name",
  "duration": 1.0,
  // optional fields after this comment
  "stretchable": false,
  "repitchable": false,
  "fileExtension": "ogg",
  "loops": false,
  "baseBpm": 120.0,

  "introSound": "other/ID",
  "endingSound": "other/ID2",

  "responseIDs": []
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

The `introSound` and `endingSound` fields are optional string IDs to indicate
sounds that should be played at the beginning and end of the main sound cue,
respectively. Cues that are either intro or ending sounds will not be pickable.
These are useful for cues like Glee Club, where there is an intro singing sound,
and an ending sound (mouth shut).

The `responseIDs` array is an array of possible "response" sound IDs for
use with the call and response/copycat tool.

If `baseBpm` is used, the sound effect will be pitched
accordingly during playback based on the current tempo relative to the value
set in `baseBpm`.

The `loops` field is a boolean indicating if the sound should loop.

## `PatternObject` structure
```json
{
  "type": "pattern",
  "id": "gameID_lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "human-readable pattern name",
  "cues": [ // array of CuePointerObjects
    {
      // see CuePointerObject
    }
  ],
  // optional fields after this comment
  "stretchable": false
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

The optional `stretchable` field indicates if this pattern is stretchable.

The array of `CuePointerObject`s uses the standard cue pointer object fields.

## `EquidistantObject` structure

```json
{
  "type": "equidistant",
  "id": "gameID_lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "human-readable name",
  "distance": 1.0,
  "stretchable": false,
  "cues": [ // ORDERED array of CuePointerObjects
    {
      // see CuePointerObject
      // fields after this comment are IGNORED
      "beat", "duration"
    }
  ]
}
```

The `EquidistantObject` represents a pattern where each cue is
*equidistant* from each other. That is to say, if the `distance` is 2.0
beats, each cue will be 2.0 beats apart. The `stretchable` field indicates
if this entity is stretchable or not (ex: Bouncy Road).

See `PatternObject` for the ID and name structure.

The `CuePointerObjects` used *are in order* and do **NOT** use these fields:
`beat`, `duration`.

## `KeepTheBeatObject` structure

```json
{
  "type": "keepTheBeat",
  "id": "gameID_lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "human-readable name",
  "defaultDuration": 2.0,
  "cues": [ // ORDERED array of CuePointerObjects
    {
      // see CuePointerObject
    }
  ]
}
```

The `KeepTheBeatObject` is similar to the `EquidistantObject`, but it
repeats over and over. This is for things like
Lockstep marching patterns, or Flipper-Flop, but is not limited to
same-beat patterns. This is like a pattern that repeats or is truncated.

The `defaultDuration` field is just the duration when initially placed.

See `PatternObject` for the ID and name structure.

The `CuePointerObjects` used *are in order* and are not changed.

## `RandomCueObject` structure

```json
{
  "type": "randomCue",
  "id": "gameID_lowerCamelCaseID",
  "deprecatedIDs": [],
  "name": "human-readable name, usually 'random X'",
  "cues": [ // array of CuePointerObjects
    {
      // see CuePointerObject
      // "beat" field ignored
    }
  ],
  // optional fields after this comment
  "responseIDs": []
}
```

The `RandomCueObject` is like a pattern except it only chooses one of the
cues in the `cues` array at random **when played**.
The `CuePointerObject` is unchanged, but the `beat` field inside
will be ignored (always zero).

See `PatternObject` for the `id` and `name` fields.

See `CueObject` for the `responseIDs` array.

