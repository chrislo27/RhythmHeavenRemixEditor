#version 130

//SpriteBatch will use texture unit 0
uniform sampler2D u_texture;

varying vec2 vTexCoord;


void main() {
    vec2 uv = vTexCoord.yx;
    vec2 temp = vec2(uv.y, uv.x);
    
    vec4 color = texture2D(u_texture, uv);

    gl_FragColor = color;
}