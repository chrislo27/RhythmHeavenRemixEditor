#version 130

//SpriteBatch will use texture unit 0
uniform sampler2D u_texture;

uniform float time;
uniform vec2 amplitude; // def 0.5, 0.5
uniform vec2 frequency; // 1.0, 1.0
uniform float speed; // 1.0

varying vec2 vTexCoord;

const float pi = 3.14159;

void main() {
    vec2 uv = vTexCoord.xy;
    vec2 temp = vec2(uv.x, uv.y);
   
    float angularFre = 2.0 * pi * frequency.x;
    uv.x += sin(temp.y * angularFre + (time * (speed * 10.0))) * ((amplitude.x) / textureSize(u_texture, 0).x) * 10.0;
    angularFre = 2.0 * pi * frequency.y;
    uv.y += sin(temp.y * angularFre + (time * (speed * 10.0))) * ((amplitude.y) / textureSize(u_texture, 0).y) * 10.0;

    vec4 color = texture2D(u_texture, uv);

    gl_FragColor = color;
}