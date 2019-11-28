# Themes

You may have noticed that RHRE comes with some themes built-in.
Have you ever wanted to tweak a theme or make an entirely new one?

You can, and this is very simple to do!

Every time you start the editor, a folder `~/.rhre3/themes` (`<user>/.rhre3/themes` on Windows) will be made,
and inside of that folder will be one more folder called `examples`. Inside
the `examples` folder, every built-in theme will be put into a
`example_X.json` file, where `X` is a number. You can open
json files with a text editor
(like [Notepad++](https://notepad-plus-plus.org/), don't use Windows Notepad).

Below is a sample json file:<br>
```json
{
  "name" : "(Example) Classic Light",
  "background" : "#EBEBEB",
  "trackLine" : "#191919",
  "waveform" : "#191919",
  "texture" : "<insert optional Base64 encoded RGBA8888 PNG here>",
  "trackers" : {
    "playback" : "#00FF00",
    "musicStart" : "#FF0000",
    "musicVolume" : "#FF6600",
    "tempoChange" : "#6666E5"
  },
  "entities" : {
    "selectionTint" : "#00BFBF",
    "nameColor" : "#000000",
    "cue" : "#D8D8D8",
    "pattern" : "#D8D8FF",
    "special" : "#FFD4BA",
    "equidistant" : "#FFB2BF",
    "keepTheBeat" : "#FFE27C"
  },
  "selection" : {
    "selectionFill" : "#19BFBF54",
    "selectionBorder" : "#19D8D8"
  }
}
```

You'll notice that most of these are just RGB hex values. The `name`
is self-explanatory. Most of the colours are grouped into sections,
like `trackers` or `entities`.

> Note: if you see a longer hex colour like `#19BFBF54`, the last *two*
digits are alpha/transparency values. If left out, these default to `FF` (full opacity).

The **optional** `texture` field accepts a Base64 encoded PNG image. The `RGBA8888` simply
means each pixel is 32-bits, and has transparency. You can use a website
like [this one](https://www.browserling.com/tools/image-to-base64) to
convert your images for you. If this field is omitted, blank, invalid, or the
text is something like `<text here>` in angle brackets, no texture will
be loaded.

<u>Changelog:</u>

| Version | Description |
|---|---|
| v3.4.0 | The `subtitle` field name in entities was changed to `special`. |
| v3.6.0 | the `timeSignature` field inside the `trackers` group was removed. Time signatures now use the `trackLine` colour. |
| v3.19.0 | All fields are optional and inherit from the Classic Light theme if missing. |
