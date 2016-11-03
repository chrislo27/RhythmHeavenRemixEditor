package ionium.util.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class Shaders {

	private Shaders() {
	}

	public static final String VERTDEFAULT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE
			+ ";\n" + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

			"uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n" +

			"void main() {\n" + "	vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "	vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
			+ "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";
	
	public static final String FRAGBAKE = Gdx.files.internal("shaders/mask.frag").readString();
	
	public static final String FRAGBAKENOISE = Gdx.files.internal("shaders/maskNoise.frag").readString();

	public static final String VERTBLUEPRINT = "attribute vec4 a_position;\r\n"
			+ "attribute vec4 a_color;\r\n" + "attribute vec2 a_texCoord0;\r\n" + "\r\n"
			+ "uniform mat4 u_projTrans;\r\n" + "\r\n" + "varying vec4 v_color;\r\n"
			+ "varying vec2 v_texCoords;\r\n" + "\r\n" + "void main() {\r\n"
			+ "    v_color = a_color;\r\n" + "    v_texCoords = a_texCoord0;\r\n"
			+ "    gl_Position = u_projTrans * a_position;\r\n" + "}";

	public static final String FRAGBLUEPRINT = "#ifdef GL_ES\r\n"
			+ "    precision mediump float;\r\n" + "#endif\r\n" + "\r\n"
			+ "varying vec4 v_color;\r\n" + "varying vec2 v_texCoords;\r\n"
			+ "uniform sampler2D u_texture;\r\n" + "uniform mat4 u_projTrans;\r\n" + "\r\n"
			+ "void main() {\r\n"
			+ "        vec3 color = texture2D(u_texture, v_texCoords).rgb;\r\n"
			+ "        float gray = (color.r + color.g + color.b) / 3.0;\r\n"
			+ "        vec3 grayscale = vec3(gray);\r\n"
			+ "		 grayscale.b = grayscale.b + 0.25;\r\n"
			+ "        gl_FragColor = vec4(grayscale, texture2D(u_texture, v_texCoords).a);\r\n"
			+ "}";

	public static final String FRAGBLUEPRINT2 =
	// GL ES specific stuff
	"#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n#version 120"
			+ //
			"varying LOWP vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "void main() {\n"
			+ "       vec4 texColor = texture2D(u_texture, vTexCoord);\n"
			+ "       \n"
			+ "if(texColor.r < 0.019608 || texColor.g < 0.019608 || texColor.b < 0.019608){ \n"
			+ "       gl_FragColor = vec4(0.0, 0.0, 0.0, texColor.a);}\n"
			+ "       gl_FragColor = vec4(0.019607, 0.015686, 0.564705, vColor.a);\n" + "}";

	public static final String VERTTOON = "#ifdef GL_ES\r\n" + "#define MED mediump\r\n"
			+ "#else\r\n" + "#define MED\r\n" + "#endif\r\n" + " \r\n"
			+ "attribute vec4 a_position;\r\n" + "attribute vec2 a_texCoord0;\r\n"
			+ "varying MED vec2 v_texCoord0;\r\n" + " \r\n" + "void main(){\r\n"
			+ "    v_texCoord0 = a_texCoord0;\r\n" + "    gl_Position = a_position;\r\n" + "}";

	public static final String FRAGTOON = "#ifdef GL_ES\r\n" + "#define LOWP lowp\r\n"
			+ "#define MED mediump\r\n" + "precision lowp float;\r\n" + "#else\r\n"
			+ "#define LOWP\r\n" + "#define MED\r\n" + "#endif\r\n" + " \r\n"
			+ "uniform sampler2D u_texture;\r\n" + "varying MED vec2 v_texCoord0;\r\n" + " \r\n"
			+ "float toonify(in float intensity) {\r\n" + "    if (intensity > 0.8)\r\n"
			+ "        return 1.0;\r\n" + "    else if (intensity > 0.5)\r\n"
			+ "        return 0.8;\r\n" + "    else if (intensity > 0.25)\r\n"
			+ "        return 0.3;\r\n" + "    else\r\n" + "        return 0.1;\r\n" + "}\r\n"
			+ " \r\n" + "void main(){\r\n"
			+ "    vec4 color = texture2D(u_texture, v_texCoord0);\r\n"
			+ "    float factor = toonify(max(color.r, max(color.g, color.b)));\r\n"
			+ "    gl_FragColor = vec4(factor*color.rgb, color.a);\r\n" + "}";

	public static final String VERTGREY = "attribute vec4 a_position;\r\n"
			+ "attribute vec4 a_color;\r\n" + "attribute vec2 a_texCoord0;\r\n" + "\r\n"
			+ "uniform mat4 u_projTrans;\r\n" + "\r\n" + "varying vec4 v_color;\r\n"
			+ "varying vec2 v_texCoords;\r\n" + "\r\n" + "void main() {\r\n"
			+ "    v_color = a_color;\r\n" + "    v_texCoords = a_texCoord0;\r\n"
			+ "    gl_Position = u_projTrans * a_position;\r\n" + "}";

	public static final String FRAGGREY = "#ifdef GL_ES\r\n" + "    precision mediump float;\r\n"
			+ "#endif\r\n" + "\r\n" + "varying vec4 v_color;\r\n" + "varying vec2 v_texCoords;\r\n"
			+ "uniform sampler2D u_texture;\r\n"
			+ "uniform mat4 u_projTrans;\r\n uniform float intensity;" + "\r\n"
			+ "void main() {\r\n" + "        vec4 color = texture2D(u_texture, v_texCoords);\r\n"
			+ "        float gray = (color.r + color.g + color.b) / 3.0;\r\n"
			+ "        vec3 grayscale = vec3(gray, gray, gray);\r\n"
			+ "		 vec3 finalcolor = mix(v_color.rgb, grayscale, intensity);\r\n"
			+ "        gl_FragColor = vec4(color.rgb, intensity);\r\n" + "}";

	public static final String VERTBLUR = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE
			+ ";\n" + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

			"uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n" +

			"void main() {\n" + "	vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "	vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
			+ "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";

	public static final String FRAGBLUR = "#ifdef GL_ES\n"
			+ "#define LOWP lowp\n"
			+ "precision mediump float;\n"
			+ "#else\n"
			+ "#define LOWP \n"
			+ "#endif\n"
			+ "varying LOWP vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n"
			+ "\n"
			+ "uniform sampler2D u_texture;\n"
			+ "uniform float resolution;\n"
			+ "uniform float radius;\n"
			+ "uniform vec2 dir;\n"
			+ "\n"
			+ "void main() {\n"
			+ "	vec4 sum = vec4(0.0);\n"
			+ "	vec2 tc = vTexCoord;\n"
			+ "	float blur = radius/resolution; \n"
			+ "    \n"
			+ "    float hstep = dir.x;\n"
			+ "    float vstep = dir.y;\n"
			+ "    \n"
			+ "	sum += texture2D(u_texture, vec2(tc.x - 4.0*blur*hstep, tc.y - 4.0*blur*vstep)) * 0.05;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x - 3.0*blur*hstep, tc.y - 3.0*blur*vstep)) * 0.09;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x - 2.0*blur*hstep, tc.y - 2.0*blur*vstep)) * 0.12;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x - 1.0*blur*hstep, tc.y - 1.0*blur*vstep)) * 0.15;\n"
			+ "	\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x, tc.y)) * 0.16;\n"
			+ "	\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x + 1.0*blur*hstep, tc.y + 1.0*blur*vstep)) * 0.15;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x + 2.0*blur*hstep, tc.y + 2.0*blur*vstep)) * 0.12;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x + 3.0*blur*hstep, tc.y + 3.0*blur*vstep)) * 0.09;\n"
			+ "	sum += texture2D(u_texture, vec2(tc.x + 4.0*blur*hstep, tc.y + 4.0*blur*vstep)) * 0.05;\n"
			+ "\n" + "	gl_FragColor = vColor * vec4(sum.rgb, 1.0);\n" + "}";

	public static final String VERTINVERT = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE
			+ ";\n" + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" + "attribute vec2 "
			+ ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" +

			"uniform mat4 u_projTrans;\n" + " \n" + "varying vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n" +

			"void main() {\n" + "	vColor = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n"
			+ "	vTexCoord = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n"
			+ "	gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" + "}";
	public static final String FRAGINVERT = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n"
			+ //
			"varying LOWP vec4 vColor;\n"
			+ "varying vec2 vTexCoord;\n"
			+ "uniform sampler2D u_texture;\n"
			+ "void main() {\n"
			+ "	vec4 texColor = texture2D(u_texture, vTexCoord);\n"
			+ "	\n"
			+ "	texColor.rgb = 1.0 - texColor.rgb;\n"
			+ "	\n"
			+ "	gl_FragColor = texColor * vColor;\n" + "}";

	public static final String VERTWARP = VERTDEFAULT;

	public static final String FRAGWARP = Gdx.files.internal("shaders/warp.frag").readString();

	public static final String VERTSWIZZLE = VERTDEFAULT;

	public static final String FRAGSWIZZLE = Gdx.files.internal("shaders/swizzle.frag")
			.readString();

	public static final String VERTDISTANCEFIELD = Gdx.files.internal("shaders/distancefield.vert")
			.readString();

	public static final String FRAGMESH = Gdx.files.internal("shaders/mesh.frag").readString();

	public static final String VERTMESH = Gdx.files.internal("shaders/mesh.vert").readString();

	public static final String FRAGDISTANCEFIELD = Gdx.files.internal("shaders/distancefield.frag")
			.readString();

}
