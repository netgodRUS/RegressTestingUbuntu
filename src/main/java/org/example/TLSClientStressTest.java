package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyStore;

public class TLSClientStressTest {

        private static final String URL = "https://91.244.183.36:30012/";
        private static final int MAX_CONNECTIONS = 100;
        private static final String OUTPUT_FILE = "/home/dexter/Documents/output.odt";
        private static final String KEYSTORE_PASSWORD = "gf6i7S*G#*dlsi@&*";
        private static final String CERTIFICATE_ALIAS = "roottestrsa2048";
        private static final String KEYSTORE_FILE = "/home/dexter/Serts/rootTestRsa2048.crl";
        private static final String CERTIFICATE_FILE = "/home/dexter/Serts/rootTestRsa2048.crl";

        public static void main(String[] args) throws Exception {
            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadKeyMaterial(loadKeystore(), KEYSTORE_PASSWORD.toCharArray(), (aliases, socket) -> CERTIFICATE_ALIAS);

            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContextBuilder.build(),
                    new String[]{"TLS","TLSv1.2", "TLSv1.3"}, // specify supported protocols
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

        private static KeyStore loadKeystore() throws Exception {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(new FileInputStream(KEYSTORE_FILE), KEYSTORE_PASSWORD.toCharArray());
            return keystore;
        }
    }

