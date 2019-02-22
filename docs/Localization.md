# Localization

Starting in `v3.8.0`, the localization system changed (as a result of
the backend system changing).

>All files noted are found inside the RHRE3.jar file in the `localization` directory.

>All text files noted use the encoding **UTF-8**.

## `langs.json`
This file is a simple json file that defines the languages to be loaded.
It is an array of objects with these fields (French given as an example):
```json
{
  "name": "Français (French)",
  "locale": {
    "language": "fr",
    "country": "",
    "variant": ""
  }
}
```

This defines the language to have the name `Français (French)`,
using the locale `fr`. This means that it will use the file
`default_fr.properties`.

The file used is defined simply as `default_LANGUAGE_COUNTRY_VARIANT.properties`.

>Note that if you only define country or variant without a language, or
variant without a language and/or country, you have to put
underscores as if they were present. Example: defining only the
variant `test` would need a properties file named
`default___test.properties` (three underscores).

>Note that if a properties file can't be found, the next most specific file
will be used. For example, if you defined the language with the locale
`default_fr_CA_var1` (French, Canada, variation 1), if that file
didn't exist it would look at `default_fr_CA.properties`,
 then `default_fr.properties`, then finally `default.properties`.


## `default.properties` files
This file, among its other types, hold the actual text data.
They follow the formatting outlined by this
[libGDX documentation](https://github.com/libgdx/libgdx/wiki/Internationalization-and-Localization).

Simply, it is a set of key/value pairs.
```
key=value
```

Places where there are `{0}`, `{1}`, etc. mean that the program replaces
that with a value at runtime. `\n` indicates a newline. `\` at the end
of a line means the string continues on the next line (without adding
an actual newline).

Escaping special characters is done by doubling it. For example, if
you want to put `{0}` WITHOUT acting as a replacement, put `{{0}`. Note
that this is contrary to the standard Java `MessageFormat`, and is a
libGDX-specific feature.

## Making testing faster and easier
If you remove the `langs.json` and/or `default.properties` files from the
jar file, you can make a folder named `localization` in the same
directory as the `RHRE.jar` file and it will look there for files if it isn't present
inside the jar.

Pressing **`F8+I`** will reload all the localization without closing and reopening
the program.
