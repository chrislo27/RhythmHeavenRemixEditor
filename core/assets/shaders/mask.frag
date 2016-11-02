varying vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture; //default GL_TEXTURE0, expected by SpriteBatch
uniform sampler2D u_mask; // GL_TEXTURE1

void main(void) {
    vec4 sentIn = texture2D(u_texture, vTexCoord);

    // get the mask; we will only use the alpha channel of the mask
    float mask = texture2D(u_mask, vTexCoord).a;

    // decrease the alpha of the sentIn texture by the mask's alpha
    gl_FragColor = vColor * mix(sentIn, vec4(sentIn.rgb, 0.0), 1.0 - mask);
}