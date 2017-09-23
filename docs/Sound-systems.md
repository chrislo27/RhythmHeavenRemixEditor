# Sound Systems

Starting in `v3.3.0`, the internal sound system has been refactored
to support *two* versions: the legacy libGDX system (the only that
we've been using since RHRE0) and the new Beads system.

[Beads](http://www.beadsproject.net/) is a Java real-time sound
library. It also supports non-realtime operation and exporting to files.

Below is a table of what features are in each sound system. Choose wisely!

| System | Memory usage | Supported in hardware | Supports exporting | Instant music seeking |
|--------|--------------|--------------------|-----------------------|-----------------------|
| libGDX | Minimal | **Yes** | **No** | **No** |
| Beads | *Very high* | **No** | **Yes** | **Yes** |

As you can see, the Beads sound system requires a *lot* of memory.<br>
If you need to increase the memory allocated to the program (assuming
you do have the memory to spare for this), you can increase it using
a Java runtime argument. It also processes all the sound on the CPU
and not your sound card, so it also may stutter.

```
java -Xmx256M -jar RHRE3.jar
```

You can either run this from the terminal/command prompt, or edit the
batch/shell files provided. Note that that allocates 256 MB of heap space.<br>
By default, it may run with 64 MB max,
or the smaller of 1 GB vs 1/4 of your physical memory.

If you get `OutOfMemoryErrors` frequently, you should consider switching
back to the libGDX sound system, or allocating more memory.

