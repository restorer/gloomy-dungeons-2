package zame.game.engine.data;

public class UnknownSignatureException extends Exception {
	private static final long serialVersionUID = 0L;

	public static final String INVALID_SIGNATURE = "Invalid signature";
	public static final String UNKNOWN_SIGNATURE = "Unknown signature";
	public static final String INVALID_VERSION = "Invalid version";
	public static final String UNSUPPORTED_VERSION = "Unsupported version";
	public static final String INVALID_CHECKSUM = "Invalid checksum";

	public UnknownSignatureException(String detailMessage) {
		super(detailMessage);
	}

	public UnknownSignatureException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
