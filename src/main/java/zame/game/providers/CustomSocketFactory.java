package zame.game.providers;

import android.annotation.SuppressLint;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class CustomSocketFactory implements LayeredSocketFactory {
    protected SSLContext sslContext = null;

    public CustomSocketFactory() {
    }

    @SuppressLint("TrulyRandom")
    protected SSLContext getSslContext() throws IOException {
        if (sslContext == null) {
            try {
                sslContext = SSLContext.getInstance(SSLSocketFactory.TLS);
                sslContext.init(null, new TrustManager[] { new CustomTrustManager() }, new SecureRandom());
            } catch (Exception ex) {
                throw new IOException();
            }
        }

        return sslContext;
    }

    @Override
    public Socket connectSocket(Socket socket, String host, int port, InetAddress localAddress, int localPort, HttpParams params) throws
        IOException,
        UnknownHostException,
        ConnectTimeoutException
    {
        SSLSocket sslSocket = (SSLSocket)(socket != null ? socket : createSocket());
        InetSocketAddress remoteAddress = new InetSocketAddress(host, port);

        if (localAddress != null || localPort > 0) {
            InetSocketAddress bindAddress = new InetSocketAddress(localAddress, localPort < 0 ? 0 : localPort);
            sslSocket.bind(bindAddress);
        }

        sslSocket.connect(remoteAddress, HttpConnectionParams.getConnectionTimeout(params));
        sslSocket.setSoTimeout(HttpConnectionParams.getSoTimeout(params));

        return sslSocket;
    }

    @Override
    public Socket createSocket() throws IOException {
        return getSslContext().getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
        return getSslContext().getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public boolean isSecure(Socket socket) throws IllegalArgumentException {
        return true;
    }
}
