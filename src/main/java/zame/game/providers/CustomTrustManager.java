package zame.game.providers;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;

public class CustomTrustManager implements X509TrustManager {
	protected static final X509Certificate[] ACCEPTED_ISSUERS = new X509Certificate[0];

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	}

	public boolean isClientTrusted(X509Certificate[] chain) {
		return true;
	}

	public boolean isServerTrusted(X509Certificate[] chain) {
		return true;
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return ACCEPTED_ISSUERS;
	}
}
