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

The `id` and `name` field are self-explanatory.
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
  "id": "lowerCamelCaseID",
  "name": "Human-Readable Cue Name",
  "duration": 1.0,
  "more later"
  // optional fields after this comment
}
```

A `CueObject` defines a sound to be loaded by the editor. It also contains
metadata such as the duration and its abilities.

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



