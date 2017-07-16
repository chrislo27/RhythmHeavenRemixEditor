# JSON Object Definitions for Databasing

All `data.json` files for each game are a `DataObject`.

## `DataObject` structure
```json
{
  "id": "lowerCamelCaseID",
  "name": "Human-Readable Name",
  "requiresVersion": "v3.0.0",
  "objects": []
}
```

The `id` field is the name of the folder this JSON file is in.
It should be lowerCamelCase.

The `name` field is a properly Title Case capitalized name. This is in English.
If this is syllabic (part of a longer sound cue), you should add a hyphen with
spaces surrounding it to break up words. The program will automatically convert these
into newlines. **Do not use newline characters.**
Example (First Contact): `alien - 1`, `alien - 2`, etc.

The `requiresVersion` field is the minimum version of the program needed to
parse this file.

The `objects` array is an array of various object types, which will be
explained below. It is **very important** that each object type contain
the `type` field, which is used by the JSON deserializer to determine
what object type to deserialize at runtime. This is called *polymorphism*.

## Object types
### `CueObject`
```json
{
  "type": "cue",
  "id": "dataObjectID/lowerCamelCaseID",
  "name": "Human-Readable Cue Name",
  "duration": 1.0,
  "more later"
  // optional fields after this comment
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

The `name` field should be a human readable name in English.

---

### `PatternObject` and `PatternCueObject`
```json
{
  "type": "pattern",
  "id": "lowerCamelCaseID",
  "name": "Human-Readable Cue Name",
  "cues": [
    {
      "id": "cueID",
      "beat": 1.0,
      // optional fields after this comment
      "duration": 1.0,
      "semitone": 1,
      "track": 2
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
look in the example to see what fields are allowed.


