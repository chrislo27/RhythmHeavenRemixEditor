#ifdef GL_ES
precision mediump float;
#endif

//input from vertex shader
varying vec4 vColor;

void main() {
    gl_FragColor = vColor;
}