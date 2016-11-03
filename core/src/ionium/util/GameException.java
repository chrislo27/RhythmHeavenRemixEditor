package ionium.util;

public class GameException extends RuntimeException {

	public GameException(String reason) {
		super(reason);
	}

	public GameException(String reason, Throwable throwable) {
		super(reason, throwable);
	}

	public GameException(Throwable t) {
		super(t);
	}

	public GameException(String s, Throwable t, boolean suppress, boolean writablestacktrace) {
		super(s, t, suppress, writablestacktrace);
	}
}
