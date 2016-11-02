//incoming Position attribute from our SpriteBatch
attribute vec2 Position;

//the transformation matrix of our SpriteBatch
uniform mat4 u_projTrans;

varying vec2 vTexCoord;

void main() {
vTexCoord = vec2(Position.xy);
    //transform our 2D screen space position into 3D world space
    gl_Position = u_projTrans * vec4(Position, 0.0, 1.0);
}