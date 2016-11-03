package ionium.util.noise;

/**
 * 
 * A wrapper for OpenSimplexNoise made by Ecumene on java-gaming.net that loops! Thanks Ecumene!
 * 
 * @author Ecumene on java-gaming.net
 * 
 *
 */
public class LoopingSimplexNoise {

	private SimplexNoise noise;
	private int sizex, sizey;

	public LoopingSimplexNoise(SimplexNoise noise, int sizex, int sizey) {
		this.noise = noise;
		this.sizex = sizex;
		this.sizey = sizey;
	}

	public double loopedNoise(double radx, double rady) {
		double s = (radx) / sizex;
		double t = (rady) / sizey;

		double x1 = -10, x2 = 10;
		double y1 = -10, y2 = 10;

		double dx = x2 - x1;
		double dy = y2 - y1;

		double x = x1 + Math.cos(s * 2 * Math.PI) * dx / (2 * Math.PI);
		double y = y1 + Math.cos(t * 2 * Math.PI) * dy / (2 * Math.PI);
		double z = x1 + Math.sin(s * 2 * Math.PI) * dx / (2 * Math.PI);
		double w = y1 + Math.sin(t * 2 * Math.PI) * dy / (2 * Math.PI);

		return noise.eval(x, y, z, w);
	}
}