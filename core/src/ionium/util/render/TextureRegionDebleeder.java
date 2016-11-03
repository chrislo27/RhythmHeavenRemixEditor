package ionium.util.render;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureRegionDebleeder {

	public static float fixAmountPx = 0.25f;

	public static void fixBleeding(TextureRegion region) {
		float x = region.getRegionX();
		float y = region.getRegionY();
		float width = region.getRegionWidth();
		float height = region.getRegionHeight();

		region.setRegion((x + fixAmountPx) / region.getTexture().getWidth(),
				(y + fixAmountPx) / region.getTexture().getHeight(),
				(x + width - fixAmountPx) / region.getTexture().getWidth(),
				(y + height - fixAmountPx) / region.getTexture().getHeight());
	}

}
