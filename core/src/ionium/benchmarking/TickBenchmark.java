package ionium.benchmarking;

public class TickBenchmark extends Benchmark {

	private static TickBenchmark instance;

	private TickBenchmark() {
	}

	public static TickBenchmark instance() {
		if (instance == null) {
			instance = new TickBenchmark();
			instance.loadResources();
		}
		return instance;
	}

	private void loadResources() {

	}

}
