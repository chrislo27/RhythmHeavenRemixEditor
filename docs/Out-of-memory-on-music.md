## What do I do when I get an `OutOfMemoryError` when loading music?

As songs are loaded into memory now as of v3.3.0, a new requirement is that you have enough memory to load the entire song.

>Note: the maximum uncompressed size for music is around 2 GB
(more than 2,147,483,647 bytes is not permitted).
The editor **cannot** load/decompress any more data than that.

The amount of minimum memory allocated to the Java Virtual Machine is generally 50 MB + the size of the *uncompressed* sound file.
By default (Java 8) the default amount is up to 256 MB, and in Java 9
it tries to allocate a quarter of your system memory.

Either in Command Prompt or by editing the batch file, add the JVM flag `-Xmx#m`, where # is the number of megabytes of RAM to allocate.

```
java -jar -Xmx1024m RHRE3.jar
```
would allocate 1024 MB (or 1 GB) of heap space to the Java Virtual Machine.

There is no way to reduce this requirement. You are working with an audio editor and it is expected you have the requirements to mass-edit audio.
