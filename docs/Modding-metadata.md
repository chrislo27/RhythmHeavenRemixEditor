# Modding Metadata

>This page relates to the [JSON Object Definitions for Databasing](JSON-object-definitions.md).

Modding metadata is a bit complicated and is covered in its own separate article here.

## Supported Modding Game IDs

| Name | Region | ID |
|---|---|---|
| Rhythm Tengoku (リズム天国) (GBA) | Japan | `gba` |
| Rhythm Heaven (NDS) | North America | `rhds` |
| Rhythm Heaven Fever (Wii) | North America | `rhFever` |
| Rhythm Heaven Megamix (3DS) | North America | `rhMegamix` |

## `moddingMetadata`
The `moddingMetadata` field (where supported) is a key/value map of
modding game IDs to *functions*. The provided function for a game will be used when
the user has set that as their modding game (in Advanced Options).
Various function types are listed below this section.

## Function types
Below is a list of function types.
### Static

The static function

When \(a \ne 0\), there are two solutions to \(ax^2 + bx + c = 0\) and they are
$$x = {-b \pm \sqrt{b^2-4ac} \over 2a}.$$

Example:<br>
```json
"moddingMetadata": {
  "rhMegamix": "0x100 0"
}
```