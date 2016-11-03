package ionium.util.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import ionium.templates.Main;
import ionium.util.MathHelper;

public final class ElectricityFX {

	private ElectricityFX() {
	}
	
	public static final float DEFAULT_THICKNESS = 3f;
	public static final float DEFAULT_OFFSET_OF_POINTS = 16f;
	public static final float DEFAULT_DISTANCE_BETWEEN_POINTS = 24f;
	
	private static Texture lineTexture = null;

	/**
	 * 
	 * @param batch
	 * @param startX
	 * @param startY
	 * @param endX
	 * @param endY
	 * @param maxOffset how much "jagged" is in a point on the line
	 * @param lineThickness thickness
	 * @param distanceBetweenPoints 
	 */
	public static void drawElectricity(SpriteBatch batch, float startX, float startY, float endX,
			float endY, float maxOffset, float lineThickness, float distanceBetweenPoints, float color) {
		float currentX = startX;
		float currentY = startY;
		float lastX = startX;
		float lastY = startY;
		
		float oldColor = batch.getPackedColor();
		int segments = (int) (MathHelper.calcDistance(startX, startY, endX, endY) / distanceBetweenPoints) + 1;

		batch.setColor(color);
		
		for (int i = 0; i < segments; i++) {
			currentX = ((endX - startX) / segments) * i + startX;
			currentY = ((endY - startY) / segments) * i + startY;
			
			// offsetting
			currentX += maxOffset * MathUtils.random(1f) * MathUtils.randomSign();
			currentY += maxOffset * MathUtils.random(1f) * MathUtils.randomSign();
			
			drawLine(batch, lastX, lastY, currentX, currentY, lineThickness);
			
			lastX = currentX;
			lastY = currentY;
		}

		drawLine(batch, currentX, currentY, endX, endY, lineThickness);
		
		batch.setColor(oldColor);
	}
	
	public static void drawElectricity(SpriteBatch batch, float startX, float startY, float endX, float endY, float color){
		drawElectricity(batch, startX, startY, endX, endY, DEFAULT_OFFSET_OF_POINTS, DEFAULT_THICKNESS, DEFAULT_DISTANCE_BETWEEN_POINTS, color);
	}

	/**
	 * gets a random colour that looks lightningy
	 * @param alpha
	 * @return
	 */
	public static float getDefaultColor(float alpha) {
		return Color.toFloatBits(MathUtils.random(14f, 54f) / 255f,
				MathUtils.random(100f, 210f) / 255f, MathUtils.random(200f, 239f) / 255f, alpha);
	}

	private static void drawLine(SpriteBatch batch, float _x1, float _y1, float _x2, float _y2,
			float thickness) {
		if(lineTexture == null){
			setLineTexture(Main.filltex);
		}
		
		float length = (float) MathHelper.calcDistance(_x1, _y1, _x2, _y2);
		float dx = _x1;
		float dy = _y1;
		dx = dx - _x2;
		dy = dy - _y2;
		float angle = MathUtils.radiansToDegrees * MathUtils.atan2(dy, dx);
		angle = angle - 180;
		batch.draw(lineTexture, _x1, _y1, 0f, thickness * 0.5f, length, thickness, 1f, 1f, angle,
				0, 0, 1, 1, false, false);
	}
	
	public static void setLineTexture(Texture t){
		lineTexture = t;
	}

}
