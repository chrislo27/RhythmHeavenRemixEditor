# Launch arguments

Below is a list of possible launch arguments for RHRE3.<br>
They are put after `java -jar RHRE3.jar`.

| Name | Since version | Description |
|------|:-------------:|-------------|
|  `--force-lazy-sound-load` | `v3.0.0` | Forces the registry to load every sound file. This will hang the editor at the end of the Loading games section for several seconds. |
| `--skip-git` | `v3.0.0` | Skips checking the Git repository completely. This is overridden by `--force-git-check`, though. |
| `--force-git-fetch` | `v3.0.0` | Forces a git fetch regardless of what the initial version check returns. |
| `--force-git-check` | `v3.1.0` | Forces a check to the Git repository. |
| `--verify-registry` | `v3.1.1` | Does a verification on the game registry. Will use all processors to check, so it is CPU heavy. Useful for database checking to ensure all pointers point to valid objects, etc. |
