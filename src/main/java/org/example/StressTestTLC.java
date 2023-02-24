package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore;

public class StressTestTLC {

        private static final String URL = "https://91.244.183.36:30012/";
        private static final int MAX_CONNECTIONS = 100;
        private static final String OUTPUT_FILE = "output.txt";

        public static void main(String[] args) throws Exception {
            SSLContext sslContextBuilder = SSLContexts.custom()
                    .loadTrustMaterial((KeyStore) null, (TrustStrategy) new TrustSelfSignedStrategy())
                    .build();

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.getSocketFactory(),
                    new String[]{"TLSv1.2", "TLSv1.3"},
                    null,
                    null);

            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(5000)
                    .build();

            HttpClient client = HttpClientBuilder.create()
                    .setMaxConnTotal(MAX_CONNECTIONS)
                    .setSSLSocketFactory(sslSocketFactory)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

            HttpPost post = new HttpPost(URL);
            post.setEntity(new StringEntity("test data"));

            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();

            // save response status code and body to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE))) {
                writer.write("Response Status: " + response.getStatusLine().getStatusCode() + "\n");
                if (entity != null) {
                    writer.write("Response Body: " + entity.getContent() + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


