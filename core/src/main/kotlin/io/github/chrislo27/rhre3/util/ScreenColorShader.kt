package io.github.chrislo27.rhre3.util

import com.badlogic.gdx.graphics.glutils.ShaderProgram


object ScreenColorShader {
    
    const val vertexShader: String = """
attribute vec4 ${ShaderProgram.POSITION_ATTRIBUTE};
attribute vec4 ${ShaderProgram.COLOR_ATTRIBUTE};
attribute vec2 ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
uniform mat4 u_projTrans;
varying vec4 v_color;
varying vec2 v_texCoords;

void main()
{
   v_color = ${ShaderProgram.COLOR_ATTRIBUTE};
   // v_color.a = v_color.a * (255.0/254.0);
   v_texCoords = ${ShaderProgram.TEXCOORD_ATTRIBUTE}0;
   gl_Position =  u_projTrans * ${ShaderProgram.POSITION_ATTRIBUTE};
}
"""
    const val fragmentShader: String = """
#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP 
#endif
varying LOWP vec4 v_color;
varying vec2 v_texCoords;

uniform sampler2D u_texture;
uniform vec4 screenColor;

void main()
{
  vec3 s = 1.0 - (1.0 - screenColor.rgb) * (1.0 - screenColor.rgb);
  vec4 texel = texture2D(u_texture, v_texCoords);
  gl_FragColor = vec4(s * (1.0 - texel.rgb) + v_color.rgb * texel.rgb, v_color.a * texel.a);
}
"""
    
    fun createShader(): ShaderProgram = ShaderProgram(vertexShader, fragmentShader)
    
}