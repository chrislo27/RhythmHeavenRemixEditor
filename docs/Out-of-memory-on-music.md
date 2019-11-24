## What do I do when there's not enough memory when loading music?

As songs are loaded into memory now as of v3.3.0, a new requirement is that you have enough memory to load the entire song.

>Note: the maximum uncompressed size for music is around 2 GB
(more than 2,147,483,647 bytes is not permitted).
The editor **cannot** load/decompress any more data than that.

The amount of minimum memory allocated to the Java Virtual Machine is generally 50 MB + the size of the *uncompressed* sound file.
By default (Java 8) the default amount is up to 256 MB, and in Java 9+
it tries to allocate up to a quarter of your system memory.

In order to force the Java Virtual Machine to be allowed to use more memory,
you have to edit either the **bat**ch file for Windows (`run_windows.bat`), or **sh**ell file
for Linux/macOS (`run_macOS-linux.sh`). Open the file with Notepad++ or your favourite text editor,
and replace the line that starts with `java -jar` with the below:

```
java -jar -Xmx1500m RHRE.jar
```

`Xmx1500m` indicates that you want a maximum of 1500 MB of memory allocated. Change that
value if you are still having out of memory errors. Obviously, you can't allocate more than what your
system has available.

Note that as of v3.18.7, the default maximum memory allocated is 1024 MB. Also, as of v3.19.0, sound memory management has been improved.

Now, you must **ALWAYS** run RHRE through that file, or else these *settings
will be ignored*.

### Could not reserve enough space for ... KB object heap
If you're getting this error and you allocated 1500 MB or more, you will require a 64-bit version of Java.
Check the bit-ness of your installation by running `java -version` in the command prompt and seeing if 64-Bit appears in the text.
