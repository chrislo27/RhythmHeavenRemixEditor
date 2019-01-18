# Modding Metadata

Modding metadata is new to v3.16.1. It contains game- and pattern-specific
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

## `moddingMetadata`
The `moddingMetadata` field (where supported) is a key/value map of
modding game IDs to *functions*. The provided function for a game will be used when
the user has set that as their modding game (in Advanced Options).
Various function types are listed below this section.

## Static function

The static function always returns the same string. Its syntax is
simply just a string as the value.

#### Example
```json
"moddingMetadata": {
  "rhMegamix": "0x111"
}
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
"moddingMetadata": {
  "rhMegamix": {
    "function": "widthRange",
    "0.0 .. 1.0": "between 0.0 and 1.0",
    "3.0": "exactly 3.0",
    "else": "none of the provided"
  }
}
```