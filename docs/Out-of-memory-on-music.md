## What do I do when I get an `OutOfMemoryError` when loading music?

As songs are loaded into memory now as of v3.3.0, a new requirement is that you have enough memory to load the entire song.

The amount of minimum memory allocated to the Java Virtual Machine is generally 50 MB + the size of the *uncompressed* sound file. It seems you have only allocated 256 MB of memory to the JVM.

Either in Command Prompt or by editing the batch file, add the JVM flag `-Xmx#m`, where # is the number of megabytes of RAM to allocate.

```
java -jar -Xmx1024m RHRE3.jar
```
would allocate 1024 MB (or 1 GB) of heap space to the Java Virtual Machine.

There is no way to reduce this requirement. You are working with an audio editor and it is expected you have the requirements to mass-edit audio.
