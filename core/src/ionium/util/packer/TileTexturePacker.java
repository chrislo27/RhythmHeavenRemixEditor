package ionium.util.packer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.PixmapPacker;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.utils.Array;

public class TileTexturePacker {

	public boolean mustUsePowerOfTwo = true;
	public int maxTextureSize = 2048;
	public String debugOutputFile = null;

	private Array<NamedTexture> texturesToPack = new Array<>();

	private TextureAtlas packedTex = null;

	public TileTexturePacker() {

	}

	public TextureAtlas pack() {
		if (packedTex != null) return getPackedTexture();
		if (texturesToPack.size == 0) throw new IllegalStateException("No textures to pack");

		PixmapPacker packer = new PixmapPacker(maxTextureSize, maxTextureSize, Format.RGBA8888, 2,
				true);

		for (NamedTexture nt : texturesToPack) {
			TextureData td = nt.texture.getTextureData();
			td.prepare();
			Pixmap p = td.consumePixmap();

			packer.pack(nt.name, p);

			if (td.disposePixmap()) {
				p.dispose();
			}

			nt.texture.dispose();
		}

		packedTex = packer.generateTextureAtlas(TextureFilter.Nearest, TextureFilter.Nearest,
				false);

		if (debugOutputFile != null) {
			TextureData td = packedTex.getRegions().first().getTexture().getTextureData();
			if (!td.isPrepared()) {
				td.prepare();
			}
			Pixmap p = td.consumePixmap();
			FileHandle loc = Gdx.files.local(debugOutputFile);
			PixmapIO.writePNG(loc, p);
		}

		packer.dispose();

		return getPackedTexture();
	}

	public TileTexturePacker addTexture(String name, Texture tex) {
		texturesToPack.add(new NamedTexture(tex, name));

		return this;
	}

	public TextureAtlas getPackedTexture() {
		if (packedTex == null) throw new IllegalStateException("Texture was not packed yet");

		return packedTex;
	}

	public static class PackingException extends RuntimeException {

		public PackingException(String reason) {
			super(reason);
		}
	}

	private static class NamedTexture {

		protected final Texture texture;
		protected final String name;

		public NamedTexture(Texture t, String s) {
			texture = t;
			name = s;
		}
	}

}