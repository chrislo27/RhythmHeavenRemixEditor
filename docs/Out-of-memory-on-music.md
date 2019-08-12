## What do I do when there's not enough memory when loading music?

As songs are loaded into memory now as of v3.3.0, a new requirement is that you have enough memory to load the entire song.

>Note: the maximum uncompressed size for music is around 2 GB
(more than 2,147,483,647 bytes is not permitted).
The editor **cannot** load/decompress any more data than that.

The amount of minimum memory allocated to the Java Virtual Machine is generally 50 MB + the size of the *uncompressed* sound file.
By default (Java 8) the default amount is up to 256 MB, and in Java 9+
it tries to allocate up to a quarter of your system memory.

In order to force the Java Virtual Machine to be allowed to use more memory,
you have to edit either the **bat**ch file for Windows, or **sh**ell file
for Linux/macOS. Replace the line that starts with `java -jar` with the below:

```
java -jar -Xmx1500m RHRE.jar
```

>`Xmx1500m` indicates that you want a maximum of 1500 MB of RAM allocated. Change that
value if you are still having out of memory errors.

Note that as of v3.18.7 the default maximum memory allocated is 1024 MB.

Now, you must **ALWAYS** run RHRE through that file, or else the *settings
will be ignored*.

There is no way to omit this requirement.
You are working with an audio editor and it is expected you have the requirements to have it in memory.
