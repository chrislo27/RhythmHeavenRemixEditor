package ionium.util;

public class MemoryUtils {

	private MemoryUtils() {

	}

	public static int getUsedMemory() {
		return (int) (Runtime.getRuntime().totalMemory() / 1024);
	}

	public static int getMaxMemory() {
		return (int) (Runtime.getRuntime().maxMemory() / 1024);
	}

	public static int getFreeMemory() {
		return (int) (Runtime.getRuntime().freeMemory() / 1024);
	}

	public static int getCores() {
		return Runtime.getRuntime().availableProcessors();
	}
}
