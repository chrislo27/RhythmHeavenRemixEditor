# OpenGL "Not supported" on Windows 10 with an Intel GPU

On Windows 10 with an Intel CPU with integrated graphics, you may come across this error (or something similar):
```
Exception in thread "LWJGL Application" com.badlogic.gdx.utils.GdxRuntimeException: OpenGL 2.0 or higher with the FBO extension is required. OpenGL version: 1.1.0
Type: OpenGL
Version: 1:1:0
Vendor: Microsoft Corporation
Renderer: GDI Generic
   at com.badlogic.gdx.backends.lwjgl.LwjglGraphics.initiateGLInstances(LwjglGraphics.java:347)
   at com.badlogic.gdx.backends.lwjgl.LwjglGraphics.initiateGL(LwjglGraphics.java:226)
   at com.badlogic.gdx.backends.lwjgl.LwjglGraphics.setupDisplay(LwjglGraphics.java:217)
   at com.badlogic.gdx.backends.lwjgl.LwjglApplication.mainLoop(LwjglApplication.java:144)
   at com.badlogic.gdx.backends.lwjgl.LwjglApplication$1.run(LwjglApplication.java:126)
```

Please use Java 8u45. You may download it on Oracle's website (requires registration),
or from FileHippo below. Note that this may only be a fix if and only if you are certain your GPU supports OpenGL 2 or higher, and are running Windows 10 on an Intel CPU with integrated graphics.<br>
[Download Java 8u45 from FileHippo](https://filehippo.com/download_jre/60816/)
