# Launch arguments

Below is a list of possible launch arguments for RHRE3.<br>
They are put after `java -jar RHRE3.jar`.

Example: `java -jar RHRE3.jar --verify-registry`

| Name | Versions | Description |
|------|:-------------:|-------------|
|  `--force-lazy-sound-load` | `v3.0.0` | Forces the registry to load every sound file into memory. This will hang the editor at the end of the Loading games section for several seconds. |
| `--skip-git` | `v3.0.0` | Skips checking the Git repository completely. This is overridden by `--force-git-check`, though. |
| `--force-git-fetch` | `v3.0.0` | Forces a Git fetch. This will skip the initial check, but can be forced with `-force-git-check`. |
| `--force-git-check` | `v3.1.0` | Forces a check to the Git repository. |
| `--verify-registry` | `v3.1.1` | Does a verification on the game registry. Useful for database checking to ensure all pointers point to valid objects, etc. |
| `--no-analytics` | `v3.10.0` | Disables sending of analytics. |
| `--force-expansion-splash` | `v3.12.0` | Forces the RHRExpansion splash screen to appear on startup. |
| `--no-online-counter` | `v3.12.0` | Prevents the program from sending and retrieving online user counts. |
| `--output-generated-datamodels` | `v3.12.0` | Writes out games that are generated internally in JSON format to console on start-up. |
| `--output-custom-sfx` | `v3.12.0` | Writes out games that don't have data.json (i.e.: just sound files in a folder) in JSON format to console on start-up. |
| `--show-tapalong-markers` | `v3.17.0` | Shows tapalong tap markers, a hidden feature. |
| `--midi-recording` | `v3.17.0` | Enables [midi recording](Midi-capabilities.md), a hidden feature. Using a midi device while the remix is playing will write notes to the remix. |
| `--portable-mode` | `v3.17.0` | Puts the `.rhre3/` folder and preferences locally next to the RHRE.jar file. Useful for flash drives. |

## Historical

| Name | Versions | Description |
|------|:-------------:|-------------|
| `--beads-sound-system` | `v3.3.0` to `v3.6.4` | Forces the use of the Beads sound system. This does nothing as the libGDX sound system is disabled due to a bug (as of v3.3.0). |
