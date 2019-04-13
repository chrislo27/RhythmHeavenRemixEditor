# Remix file format

The `.rhre3` file format is a standard compressed ZIP file. You can open
it with programs such as [7zip](https://www.7-zip.org/).

All JSON fields are **non-null** unless otherwise specified. Not all RHRE3
`remix.json` files are directly backwards compatible.

## `music.bin`
The `music.bin` file is present only if the remix has music. It is a
copy of the original music file in the same format it was originally.

## Textures
You may see several `hexadecimal.png` entries. These
are the textures used in the remix. They are referred to in `remix.json`.
They are always PNG files.

## `remix.json`
The `remix.json` file has all the remix data in it.

It is a top-level object with the following fields:

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `version` | string | N | The version this remix was saved in. |
| `databaseVersion` | integer | N | The SFX Database used in this remix. |
| `playbackStart` | double | Optional, defaults to 0.0. | Where the playback start marker is, in beats. |
| `musicStartSec` | double | Optional, defaults to 0.0. | Where the music start marker is, in seconds. |
| `trackCount` | integer | Optional, defaults to 5 if not present. Non-null. | The number of tracks available. Usually ranges from 1 to 10. Can be greater than 10. Values less than zero are coerced to 1. |
| `isAutosave` | boolean | Optional, defaults to false. | Whether this remix is an autosave. Autosaves are not autosaved. |
| `midiInstruments` | integer | Optional, defaults to 0. | The number of midi instruments that were in the original midi. This is only used for the Glee Club visualization. Positive values only. |
| `musicData` | music data object (see below) | N | The music data object. See its table below.
| `textures` | array of strings | Optional and nullable, defaults to null | If not null, contains a list of hexadecimal hashes referring to the textures in the containing `rhre3` file. |
| `entities` | array of entity objects (see below) | N | Contains the entities used in the remix. |
| `trackers` | trackers object (see below) | N | Contains the trackers (tempo changes and music volume changes) used in the remix. |
| `timeSignatures` | array of time signatures (see below) | N | Contains the time signatures used in the remix. |

### `musicData` object fields:

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `present` | boolean | N | Whether or not music is present. |
| `filename` | string | Only present if the `present` field is `true`. | The original file name of the music. |
| `extension` | string | Only present if the `present` field is `true`. | The file extension/type of the music. This is used to determine how to decode the `music.bin` file. |

### `trackers` object

The `trackers` object has two object fields, `tempos` and `musicVolumes`.
Both objects only have their own `trackers` array of either tempo changes or music volume changes, respectively.

#### Tempo Change object fields

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `beat` | double | N | The beat this tracker is on. |
| `seconds` | double | Technically optional, always provided by RHRE3. If not present, it is calculated on-the-fly. | The seconds this tracker is on. RHRE3 provides, but does not use this value (it is calculated on-the-fly). |
| `bpm` | double | N | The BPM (beats per minute) that this tempo change switches to. |
| `swingRatio` | integer | Optional, defaults to 50. | The swing ratio of this tempo change. Example: A value of 60 means the first "beat" is lengthened to a 60/40 ratio, giving a swing effect. |
| `swingDivision` | double | Optional, defaults to 1.0. | The swing "note". 1.0 indicates 8th note swing, and 0.5 indicates 16th note swing. The formula `8 / division` gives you the Xth note (8th, 16th, 32nd, etc.). |

#### Music Volume Change object fields

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `beat` | double | N | The beat this tracker starts on. |
| `width` | double | Optional, defaults to 0.0. | The width of the tracker. Can be zero, but not negative. |
| `volume` | integer | N | The volume in percentage points. Coerced to 0-200. |

### `timeSignatures` object fields

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `beat` | integer | N | The beat this time signature is on. |
| `divisions` | integer | Optional, defaults to 4. | The X in X/Y time signatures. |
| `measure` | integer | Optional (calculated on-the-fly in RHRE). | The measure this time signature is on. |
| `beatUnit` | integer | Optional, defaults to 4. | The Y in X/Y time signature. Typically a power of two. |

### Entity object fields

Entity objects can have varying fields depending on the type of entity.

| Field Name | Field Type | Optional? | Description |
|---|---|---|---|
| `type` | string | N | The type of entity. This is always `model`. |
| `beat | double | N | The beat where this entity starts (left face). |
| `track` | integer | N | The position on the track where this entity sits (bottom face). 0 is the bottom-most track. |
| `width` | double | N | The width of the entity in beats. |
| `height` | integer | N | The height of the entity in track segments. |
| `datamodel` | string | Always present when `type` is `model`, which is always true. | The database ID of this entity. |
| `semitone` | integer | Optional, defaults to 0. | The number of semitones to pitch this entity up or down. If the entity cannot be repitched at all, this has no effect. If the entity is a shake entity (`datamodel` is `special_shakeEntity`), this controls the shake intensity (`2^(semitone / 12)`). |
| `volume` | integer | Optional, defaults to 100. | The number of percentage points of volume that this entity should be played back at. Always between 0-300 in RHRE3. |
| `texHash` | string | Optional. **Can be null.** Appears only when `datamodel` is `special_textureEntity`, | The texture hash that this texture entity refers to. If null or not present, it indicates that this texture entity has no texture yet. |
| `subtitle` | string | Only present if `datamodel` is a subtitle entity, specifically `special_subtitleEntity`, `special_songTitleEntity`, and `special_songArtistEntity`. | The subtitle text for this subtitle entity. If not present, defaults to "`<failed to read text>`". |

Example:<br>
```json
{
  "version": "v3.13.0",
  "databaseVersion": 61,
  "playbackStart": 0.0,
  "musicStartSec": -0.07472682,
  "trackCount": 5,
  "isAutosave": false,
  "midiInstruments": 0,
  "musicData": {
    "present": true,
    "filename": "Hi_Louis_-_Scatman_John.mp3",
    "extension": "mp3"
  },
  "textures": [
    "36187c46ebffc0a52ce7f83efc50ad062939f423"
  ],
  "entities": [
    {
      "type": "model",
      "beat": -0.625,
      "track": -1,
      "width": 3.125,
      "height": 1,
      "datamodel": "special_textureEntity",
      "texHash": "36187c46ebffc0a52ce7f83efc50ad062939f423"
    },
    {
      "type": "model",
      "beat": 0.0,
      "track": 0,
      "width": 1.875,
      "height": 1,
      "datamodel": "special_subtitleEntity",
      "subtitle": "Ski-bop po-dee"
    },
    {
      "type": "model",
      "beat": 2.0,
      "track": 0,
      "width": 2.0,
      "height": 1,
      "datamodel": "spaceDanceEn_andpose1"
    }
  ],
  "trackers": {
    "tempos": {
      "trackers": [
        {
          "beat": 0.0,
          "seconds": 0.0,
          "bpm": 115.0,
          "swingRatio": 50,
          "swingDivision": 1.0
        }
      ]
    },
    "musicVolumes": {
      "trackers": [
        {
          "beat": 0.0,
          "width": 0.0,
          "volume": 100
        }
      ]
    }
  },
  "timeSignatures": [
    {
      "beat": 0,
      "divisions": 4,
      "measure": 1
    }
  ]
}
```

