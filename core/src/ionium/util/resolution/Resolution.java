package ionium.util.resolution;

public class Resolution implements Comparable<Resolution>{

	private static final Resolution[] resolutions43 = new Resolution[] { new Resolution(640, 480),
			new Resolution(800, 600), new Resolution(1024, 768), new Resolution(1152, 864),
			new Resolution(1280, 960) };
	private static final Resolution[] resolutions169 = new Resolution[] {
			new Resolution(1176, 664), new Resolution(1280, 720), new Resolution(1360, 768),
			new Resolution(1366, 768), new Resolution(1600, 900), new Resolution(1768, 992),
			new Resolution(1920, 1080) };
	private static final Resolution[] resolutions1610 = new Resolution[] {
			new Resolution(1280, 800), new Resolution(1440, 900), new Resolution(1600, 1024),
			new Resolution(1680, 750) };

	public final int width;
	public final int height;

	public Resolution(int w, int h) {
		width = w;
		height = h;
	}
	
	public int getArea(){
		return width * height;
	}

	@Override
	public String toString() {
		return width + "x" + height;
	}

	public static Resolution[] get43ResolutionsList() {
		return resolutions43;
	}

	public static Resolution[] get169ResolutionsList() {
		return resolutions169;
	}

	public static Resolution[] get1610ResolutionsList() {
		return resolutions1610;
	}

	public static String[] get43ResolutionsStrings() {
		String[] list = new String[get43ResolutionsList().length];
		for (int i = 0; i < list.length; i++) {
			list[i] = get43ResolutionsList()[i].toString();
		}

		return list;
	}

	public static String[] get169ResolutionsStrings() {
		String[] list = new String[get169ResolutionsList().length];
		for (int i = 0; i < list.length; i++) {
			list[i] = get169ResolutionsList()[i].toString();
		}

		return list;
	}

	public static String[] get1610ResolutionsStrings() {
		String[] list = new String[get1610ResolutionsList().length];
		for (int i = 0; i < list.length; i++) {
			list[i] = get1610ResolutionsList()[i].toString();
		}

		return list;
	}

	@Override
	public int compareTo(Resolution other) {
		int area = this.getArea();
		
		return (int) Math.signum(area - other.getArea());
	}

}
