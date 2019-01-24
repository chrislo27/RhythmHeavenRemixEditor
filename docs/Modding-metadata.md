# Modding Metadata

Modding metadata is new to v3.17.0. It contains game- and pattern-specific
metadata to assist with game modding.<br>
If you're looking for the specifics of game SFX data and patterns, [see this article](JSON-object-definitions.md) instead.

## Supported Modding Game IDs

These game IDs refer to Rhythm Heaven installments and not minigames.
They are *region-specific!* For the time being, it seems that the only games
with any modding documentation are North American releases (excluding Rhythm Tengoku).

| Name | Region | ID |
|---|---|---|
| Rhythm Tengoku (リズム天国) (GBA) | Japan | `gba` |
| Rhythm Heaven (NDS) | North America | `rhds` |
| Rhythm Heaven Fever (Wii) | North America | `rhFever` |
| Rhythm Heaven Megamix (3DS) | North America | `rhMegamix` |

<details><summary>Reserved IDs</summary>
<br>
<p>These IDs are reserved for future use, but are <strong>not</strong> currently usable.</p>
<table class="table table-striped table-bordered">
<thead>
<tr>
<th>Name</th>
<th>Region</th>
<th>ID</th>
</tr>
</thead>
<tbody>
<tr>
<td>Rhythm Tengoku Arcade (ARCADE)</td>
<td>Japan</td>
<td><code>gbaArcade</code></td>
</tr>
<tr>
<td>Rhythm Tengoku Gold (リズム天国ゴールド) (NDS)</td>
<td>Japan</td>
<td><code>rhdsJa</code></td>
</tr>
<tr>
<td>Rhythm Paradise (NDS)</td>
<td>Europe</td>
<td><code>rhdsEu</code></td>
</tr>
<tr>
<td>Rhythm World (리듬세상) (NDS)</td>
<td>Korea</td>
<td><code>rhdsKo</code></td>
</tr>
<tr>
<td>Minna no Rhythm Tengoku (みんなのリズム天国) (Wii)</td>
<td>Japan</td>
<td><code>rhFeverJa</code></td>
</tr>
<tr>
<td>Beat the Beat: Rhythm Paradise (Wii)</td>
<td>Europe</td>
<td><code>rhFeverEu</code></td>
</tr>
<tr>
<td>Rhythm World Wii (리듬 세상 Wii) (Wii)</td>
<td>Korea</td>
<td><code>rhFeverKo</code></td>
</tr>
<tr>
<td>Rhythm Tengoku The Best+ (リズム天国ザ・ベスト+) (3DS)</td>
<td>Japan</td>
<td><code>rhMegamixJa</code></td>
</tr>
<tr>
<td>Rhythm Paradise Megamix (3DS)</td>
<td>Europe</td>
<td><code>rhMegamixEu</code></td>
</tr>
<tr>
<td>Rhythm World The Best+ (리듬세상・더베스트+) (3DS)</td>
<td>Korea</td>
<td><code>rhMegamixKo</code></td>
</tr>
</tbody>
</table>
</details>
<br>

## File and folder structure
Various `json` files will be in the SFX database under the `moddingMetadata/` folder.
They are grouped into folders of the modding game ID.

Custom modding metadata can be put in `.rhre3/customModdingMetadata/` with the same structure
as above. Files that are identically named in the custom folder will overwrite something with
the same file name in the stock SFX database.

## JSON structure
Each json file is **an array of objects**.<br>
Each object has these fields:

| Field | Type | Description |
|---|---|---|
| applyTo | array of strings | Array of strings to apply this data to |
| data | object | Key/value pairs of data |

The `data` object is a set of key/value pairs.<br>
The **key** must be from a list of known keys described below.<br>
The **value** should be a string or a function.

Various function types are listed below this section.

Example (actual information may not be correct!):<br>
```json
[
  {
    "applyTo": ["screwbotFactoryEn", "screwbotFactoryJa"],
    "data": {
      "engine": "0x2D",
      "name": "rvlRobot",
      "tempoFile": "0234D2",
      "index": "0x12345"
    }
  },
  {
    "applyTo": ["screwbotFactoryEn_blackRobot", "screwbotFactoryJa_blackRobot"],
    "data": {
      "sub": "0x58"
    }
  },
  {
    "applyTo": ["screwbotFactoryEn_whiteRobot", "screwbotFactoryJa_whiteRobot"],
    "data": {
      "sub": "0x56"
    }
  },
  {
    "applyTo": ["screwbotFactoryEn_blackRobotFaster", "screwbotFactoryJa_blackRobotFaster"],
    "data": {
      "sub": "0x59"
    }
  },
  {
    "applyTo": ["screwbotFactoryEn_whiteRobotFast", "screwbotFactoryJa_whiteRobotFast"],
    "data": {
      "sub": "0x57"
    }
  },
  {
    "applyTo": ["bouncyRoad_15bounces", "bouncyRoadMegamix_15bounces"],
    "data": {
      "sub": {
        "function": "widthRange",
        "2.0": "0x59",
        "1.0": "0x58",
        "0.6667": "0x57",
        "0.5": "0x56",
        "else": "undefined"
      }
    }
  }
]
```

## Width Range function

The width range function returns a string based on the current **width** of the entity.
As such, this function can only be applied on entity-types, i.e.: not GameObject.
Think of this function as a piecewise-defined one operating on the width.

There must be a field named `function` with the value `"widthRange"`.<br>
All other fields are either `ranges` or have the key `"else"`.<br>
`"else"` is used when none of the conditions match.
If it is not present, its value defaults to an empty string.

### Range syntax
Ranges can either be a single value or a span of values.<br>
A single value means that the width should be exactly that value, +/- 0.0001.<br>
A range is defined as `"lower .. upper"`, where `lower` is the lower bound (inclusive)
and `upper` is the upper bound (inclusive). The whitespace and decimal places are optional.

#### Example
```json
"sub": {
  "function": "widthRange",
  "0.0 .. 1.0": "between 0.0 and 1.0",
  "3.0": "exactly 3.0",
  "else": "none of the provided"
}
```