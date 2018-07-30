# JSON Object Definitions for Databasing

## ID rules
IDs must only consist of ASCII alphanumerics, -, spaces, /, and _.

`DataObject` IDs are always just lowerCamelCase.
`CueObject` IDs are always `gameID/filename`.
Everything else is always `gameID_id`.

Examples:<br>
`coolGame` - Name of the game<br>
`coolGame/buzzer` - A sound in the `coolGame` folder named `buzzer`<br>
`coolGame_pattern` - A pattern for `coolGame`

## Data types
There are several data types available to you to use. Each field
has a specific data type that cannot be swapped with another.

| Name | Syntax | Example | Purpose |
|------|--------|---------|---|
| string | "<stuff>" | "hello" | Text |
| boolean | `true`/`false` | `true` | To indicate truth values |
| integer | 0-9, no decimals | 150 | Non-decimal numbers |
| decimal | 0-9, decimals allowed | 1.0 | Numbers that can have decimals |
| array | `[]` | `["first", "second", "third"]` | A "list" of other types |

## `DataObject` structure
All `data.json` files for each game are a `DataObject`.

```json
{
  "id": "lowerCamelCaseID",
  "name": "Human-Readable Name",
  "objects": [],
  "series": "tengoku",
  // optional fields after this comment
  "group": "Human-Readable Name",
  "groupDefault": false,
  "priority": 0,
  "noDisplay": false
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

The `objects` array is an array of various object types, which will be
explained below. It is **very important** that each object definition contain
the `type` field, which is used to determine the object's type.

The `series` field is a string showing what series this game belongs to.<br>
>Note: if this is not present it defaults to "other".

| Series | Field Value |
|-------------|------------|
| Other | `Other` |
| GBA | `tengoku` |
| DS | `ds` |
| Fever | `fever` |
| Megamix | `megamix` |
| Side | `side` |

The optional `group` field is the full name of the group this data object
belongs to, if it has variants. For example, `gleeClubEnMegamix` would
have the group value be `Glee Club (Megamix)` to be grouped with other
data objects that also have the same group value. Games with `groupDefault`
set to true will appear earlier in the variant list.

The optional `priority` field is an integer for how the games should
be sorted. Higher numbers come first, lower numbers come last.

The `noDisplay` field if true will indicate that the game display
(with the icon and name) will not render. This is used for "games" like
Count-Ins, or Special Entities.

## Deprecated IDs
`deprecatedIDs` is an array of old IDs that are no longer used, but refer
to this current object for older save files. This field is always the same
even in other object types, and is always present whenever there is an
`id` field **EXCEPT FOR** `CuePointerObject` and `DataObject`.

## `CuePointerObject` structure
```json
{
  "id": "cueID",
  "beat": 1.0,
  // optional fields after this comment
  "duration": 1.0,
  "track": 0,
  "semitone": 0,
  "volume": 100,
  "metadata": {}
}
```

`CuePointerObject` is used to store extra data like pitch for creating entities.
You will find them in multipart objects like patterns.

**Note that** the only fields shown here ARE the ones it has, but
not every field from `CueObject` may be used depending on the situation.

The `semitone` property is an integer indicating the number of semitones
to repitch by. This works on normally non-repitchable entities, but is
not recommended. Has no effect on non-SFX entities.

The `volume` property is an integer indicating the volume percentage.
The default value is 100.
The range of values should be kept at 0-300. Has no effect on non-SFX entities.
(All negative values will be coerced to zero. Values higher than 300 are not changed, however.)

The `metadata` object can be null, and acts as a key/value map.<br>
Below is a list of potential properties:

| Entity Type | Field Name | Field Type | Field Values |
|-------------|------------|------------|--------------|
| `subtitleEntity` | `subtitleText` | `string` | \<user-defined\> |

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
metadata such as the duration and its editable abilities.

The `id` field is structured like this: `dataObjectID/lowerCamelCaseSoundFileName`.
If the parent data object's ID is `spaceDance`, and this sound's name is `turnRight`,
therefore this ID is `spaceDance/turnRight`. If there are more folders, you
should include them in the path separated by more forward slashes. Example:
`flipperFlop/appreciation/nice` is the sound file `nice.ogg` inside the folder `appreciation/`
which has a parent folder of `flipperFlop`.

The `name` field is a name. This is in English, except for the
romanization/latinization of foreign language words. Avoid capitals. The only
time you should be using capitals are for the following: proper noun I,
"Remix X", "Fever" (in the context of the game).
If this is syllabic (part of a longer sound cue), you should add a hyphen with
spaces surrounding it to break up words. The program will automatically convert these
into newlines. **Do not use newline characters.**<br>
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
set in `baseBpm`. Example: When playing at 180 BPM and the base BPM is 120, the sound effect will be sped up by 1.5x.

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
See the ID rules section for more info.

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
beats, each cue will be 2.0 beats apart based on left endpoints.
The `stretchable` field indicates if this entity is stretchable or not (ex: Bouncy Road).

Examples of games with equidistant objects: Bouncy Road, Sneaky Spirits, Built to Scale (DS)

See `PatternObject` for the ID and name structure.

The `CuePointerObjects` used *are in order* and do **NOT** use the `beat` and `duration` fields.

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
same-beat patterns. This is a type of pattern that can repeat itself.

Examples of games with keep-the-beat objects: Lockstep, Tap Troupe, Flipper-Flop, Rhythm Rally

The `defaultDuration` field is the duration when initially placed. It also
acts as the interval for when to repeat the pattern in `cues`.
>Note, if the total duration of `cues` is longer than `defaultDuration`, the repeating interval will be this longer value.

See `PatternObject` for the ID and name structure.

The `CuePointerObjects` used *are in order* and use all fields.

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
objects in the `cues` array at random **when played**.
The `CuePointerObject` is unchanged, but the `beat` field inside
will be ignored.

You are not limited to just using `CueObjects` in the `cues` array.

Examples of games with random cue objects: Ringside (has variants), First Contact (speech sounds)

See `PatternObject` for the `id` and `name` fields.

See `CueObject` for the `responseIDs` array.

