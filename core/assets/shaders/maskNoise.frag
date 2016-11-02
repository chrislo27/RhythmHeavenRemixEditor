varying vec4 vColor;
varying vec2 vTexCoord;

uniform sampler2D u_texture; //default GL_TEXTURE0, expected by SpriteBatch
//uniform sampler2D u_mask; // GL_TEXTURE1

// the time (seconds)
uniform float time;
// how intense should the blobs look
uniform float intensity;
// how fast should it scroll
uniform float speed;
// how zoomed out should it look, the higher the more zoomed OUT
uniform float zoom;
// outline colour
uniform vec4 outlinecolor;

vec3 mod289(vec3 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec2 mod289(vec2 x) {
  return x - floor(x * (1.0 / 289.0)) * 289.0;
}

vec3 permute(vec3 x) {
  return mod289(((x*34.0)+1.0)*x);
}

float snoise(vec2 v)
  {
  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
                     -0.577350269189626,  // -1.0 + 2.0 * C.x
                      0.024390243902439); // 1.0 / 41.0
// First corner
  vec2 i  = floor(v + dot(v, C.yy) );
  vec2 x0 = v -   i + dot(i, C.xx);

// Other corners
  vec2 i1;
  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0
  //i1.y = 1.0 - i1.x;
  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  // x0 = x0 - 0.0 + 0.0 * C.xx ;
  // x1 = x0 - i1 + 1.0 * C.xx ;
  // x2 = x0 - 1.0 + 2.0 * C.xx ;
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;

// Permutations
  i = mod289(i); // Avoid truncation effects in permutation
  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))
        + i.x + vec3(0.0, i1.x, 1.0 ));

  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);
  m = m*m ;
  m = m*m ;

// Gradients: 41 points uniformly over a line, mapped onto a diamond.
// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)

  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;

// Normalise gradients implicitly by scaling m
// Approximation of: m *= inversesqrt( a0*a0 + h*h );
  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );

// Compute final noise value at P
  vec3 g;
  g.x  = a0.x  * x0.x  + h.x  * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

void main(void) {
    vec4 sentIn = texture2D(u_texture, vTexCoord);

    // helpful coefficients:
    // multiplying time makes it faster (dividing is inverse)
    // multiplying texCoord makes it smaller (zoom out)
    // multiplying noise makes it more intense (less smoothness) or makes it fade away
    // adding to noise makes it bleed out (occupies more space)
    
    // an intensity of at least 2.5 with zoom of 2.0 occupies the entire texture
    
    // interestingly, a static time with a speed > 0 makes it more pixelated
    float noiseTime = (time * speed / 3.0);
    
    vec2 manipulatedTexCoord = vec2(vTexCoord + vec2(noiseTime, noiseTime)) * zoom;
    
    float noise = snoise(manipulatedTexCoord) * pow(intensity, 2.0);
    noise = noise + pow(intensity, 2.0);
    vec4 toLerpTo = vec4(sentIn.rgb, 0.0);
    
    // outline below
    
    // intensify mixing
    float mixAmount = sentIn.a * noise * 3.0;
    
    // cutoff point to make it look neater
    float cutoff = 0.75;
    
    if(sentIn.a * noise >= cutoff){
       mixAmount = 0.0;
    }
    
    toLerpTo.rgb = mix(toLerpTo.rgb, outlinecolor.rgb, mixAmount * outlinecolor.a * (1.0 + cutoff));
    
    gl_FragColor = vColor * mix(sentIn, toLerpTo, noise);
}

