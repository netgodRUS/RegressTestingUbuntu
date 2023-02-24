package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StressTestTrust {

    public static void main(String[] args) throws Exception {
        int numThreads = 100;
        int numRequestsPerThread = 1000;

        // Load SSL certificate
        KeyStore keyStore  = KeyStore.getInstance("PKCS12");
        keyStore.load(StressTestTrust.class.getResourceAsStream("/home/dexter/Serts/Internship program Infotecs 2023.pfx"), "gf6i7S*G#*dlsi@&*".toCharArray());
        KeyStore trustStore  = KeyStore.getInstance("JKS");
        trustStore.load(StressTestTrust.class.getResourceAsStream("/home/dexter/Serts/rootTestRsa2048.crt"), "gf6i7S*G#*dlsi@&*".toCharArray());
// Load the certificate
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new FileInputStream("/home/dexter/Serts/rootTestRsa2048.crt");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(is);
        is.close();

// Create a trust strategy that trusts only the specified certificate
        String thumbprint = getThumbprint(cert);
        TrustStrategy trustStrategy = (TrustStrategy) new ThumbprintTrustStrategy(thumbprint);

// Create SSL context with trust strategy
        SSLContext sslContext = SSLContextBuilder.create()
                .loadTrustMaterial(trustStrategy)
                .build();

// Create SSL socket factory with SSL context
        SSLSocketFactory sslSocketFactory = new SSLSocketFactory() {
            @Override
            public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
                return null;
            }

            @Override
            public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
                return null;
            }

            @Override
            public Socket createSocket(InetAddress host, int port) throws IOException {
                return null;
            }

            @Override
            public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
                return null;
            }

            @Override
            public String[] getDefaultCipherSuites() {
                return new String[0];
            }

            @Override
            public String[] getSupportedCipherSuites() {
                return new String[0];
            }

            @Override
            public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
                return null;
            }
        };

        // Create trust strategy
//        TrustStrategy trustStrategy = new TrustSelfSignedStrategy();
//
//        // Create SSL context with trust strategy
//        SSLContext sslContext = SSLContextBuilder.create()
//                .loadKeyMaterial(keyStore, "gf6i7S*G#*dlsi@&*".toCharArray())
//                .loadTrustMaterial(trustStore, trustStrategy)
//                .build();
//
//        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

        // Create HttpClient with SSL socket factory
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory((LayeredConnectionSocketFactory) sslSocketFactory)
                .build();

        // Create output file
        Writer writer = new OutputStreamWriter(new FileOutputStream("/home/dexter/results.txt"));

        // Create latch for threads to wait on
        CountDownLatch latch = new CountDownLatch(numThreads);

        // Create thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);

        // Create and execute requests
        for (int i = 0; i < numThreads; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < numRequestsPerThread; j++) {
                    try {
                        HttpGet httpget = new HttpGet("https://91.244.183.36:30012/");
                        HttpResponse response = httpclient.execute(httpget);
                        HttpEntity entity = response.getEntity();
                        if (entity != null) {
                            entity.getContent().close();
                        }
                        writer.write("Thread " + Thread.currentThread().getId() + " - Request " + j + " - Status " + response.getStatusLine().getStatusCode() + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            });
        }

        // Wait for threads to finish
        latch.await();

        // Close HttpClient and output file
        httpclient.close();
        writer.close();
    }

    private static String getThumbprint(X509Certificate cert) {
        return null;
    }
}


