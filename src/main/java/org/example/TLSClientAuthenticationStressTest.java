package org.example;


import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;


public class TLSClientAuthenticationStressTest {
    public static void main(String[] args) throws Exception {
        int numThreads = 100;
        int numRequestsPerThread = 1000;
        String url = "https://91.244.183.36:30012/files";

        KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
        FileInputStream clientKeyStoreFile = new FileInputStream("/home/dexter/Serts/Internship program Infotecs 2023.pfx");
        clientKeyStore.load(clientKeyStoreFile, "gf6i7S*G#*dlsi@&*".toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(clientKeyStore, "gf6i7S*G#*dlsi@&*".toCharArray());

        InputStream is = new FileInputStream("/home/dexter/Serts/ca3.cer.der");
// You could get a resource as a stream instead.

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null); // You don't need the KeyStore instance to come from a file.
        ks.setCertificateEntry("caCert", caCert);

        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);


//        CloseableHttpClient httpClient = HttpClients.custom()
//                .setSslcontext(sslContext)
//                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSslcontext(sslContext)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setSocketTimeout(10000)
                        .setConnectTimeout(10000)
                        .build())
                .build();

        Thread[] threads = new Thread[numThreads];
        for (int i = 0; i < numThreads; i++) {

            threads[i] = new Thread(() -> {
                for (int j = 0; j < numRequestsPerThread; j++) {
                    try {
                        HttpGet httpGet = new HttpGet(url);
                        HttpResponse response = httpClient.execute(httpGet);
                        int statusCode = response.getStatusLine().getStatusCode();
                        System.out.println("Thread " + Thread.currentThread().getId() +

                                        " completed " + numRequestsPerThread + " requests."+

                        " request " + j + " response status code: " + statusCode+ "  All threads completed."  ) ;
                    } catch (Exception e) {
                        System.out.println("Thread " + Thread.currentThread().getId() +
                                " request " + j + " failed: " + e.getMessage() + "  All threads completed." );

                    }
                }
            });
            threads[i].start();
        }

        for (int i = 0; i < numThreads; i++) {


            threads[i].join();

        }

        threads.wait();

        httpClient.close();



    }
}


