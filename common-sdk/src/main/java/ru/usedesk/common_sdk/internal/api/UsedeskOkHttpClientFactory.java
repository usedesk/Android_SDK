package ru.usedesk.common_sdk.internal.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.security.ProviderInstaller;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.inject.Inject;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public class UsedeskOkHttpClientFactory {

    private final Context appContext;

    @Inject
    public UsedeskOkHttpClientFactory(@NonNull Context appContext) {
        this.appContext = appContext;
    }

    @NonNull
    public OkHttpClient createInstance() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            try {
                ProviderInstaller.installIfNeeded(appContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                applyProtocol(builder, TlsVersion.TLS_1_2.javaName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return builder.build();
    }

    private void applyProtocol(@NonNull OkHttpClient.Builder builder,
                               @NonNull String protocolName) throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager allTrustManager = new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {

            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SSLContext sslContext = SSLContext.getInstance(protocolName);
        sslContext.init(null, new X509TrustManager[]{allTrustManager}, new SecureRandom());

        SSLSocketFactory sslSocketFactory = new ProtocolSocketFactory(sslContext.getSocketFactory(), protocolName);

        builder.sslSocketFactory(sslSocketFactory, allTrustManager);
        builder.hostnameVerifier((hostname, session) -> true);
    }

    private class ProtocolSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory sslSocketFactory;
        private final String[] protocols;

        public ProtocolSocketFactory(@NonNull SSLSocketFactory sslSocketFactory,
                                     @NonNull String protocol) {
            this.sslSocketFactory = sslSocketFactory;
            this.protocols = new String[]{protocol};
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return sslSocketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return sslSocketFactory.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return enableProtocolOnSocket(sslSocketFactory.createSocket(s, host, port, autoClose));
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return enableProtocolOnSocket(sslSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return enableProtocolOnSocket(sslSocketFactory.createSocket(host, port, localHost, localPort));
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return enableProtocolOnSocket(sslSocketFactory.createSocket(host, port));
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return enableProtocolOnSocket(sslSocketFactory.createSocket(address, port, localAddress, localPort));
        }

        private Socket enableProtocolOnSocket(@Nullable Socket socket) {
            if (socket instanceof SSLSocket) {
                ((SSLSocket) socket).setEnabledProtocols(protocols);
            }
            return socket;
        }
    }
}
