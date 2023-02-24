package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.KeyStore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StreeTestGood {


        public static void main(String[] args) throws Exception {
            int numThreads = 100;
            int numRequestsPerThread = 1000;

            // Load SSL certificate
            KeyStore keyStore  = KeyStore.getInstance("PKCS12");
            keyStore.load(StreeTestGood.class.getResourceAsStream("/home/dexter/Serts/Internship program Infotecs 2023.pfx"), "gf6i7S*G#*dlsi@&*".toCharArray());
            KeyStore trustStore  = KeyStore.getInstance("JKS");
            trustStore.load(StreeTestGood.class.getResourceAsStream("/home/dexter/Serts/ca3.cer.der"), "gf6i7S*G#*dlsi@&*".toCharArray());
            SSLContext sslContext = new SSLContextBuilder()
                    .loadKeyMaterial(keyStore, "24O5zbogtpe".toCharArray())
                    .loadTrustMaterial(trustStore, null)
                    .build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext);

            // Create HttpClient with SSL socket factory
            CloseableHttpClient httpclient = HttpClients.custom()
                    .setSSLSocketFactory(sslSocketFactory)
                    .build();

            // Create output file
            Writer writer = new OutputStreamWriter(new FileOutputStream("results.txt"));

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
    }



