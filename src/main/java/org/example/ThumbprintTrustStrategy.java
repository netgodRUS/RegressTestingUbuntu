package org.example;


import org.apache.http.conn.ssl.TrustStrategy;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

    public class ThumbprintTrustStrategy implements TrustStrategy {

        private final String thumbprint;

        public ThumbprintTrustStrategy(String thumbprint) {
            this.thumbprint = thumbprint;
        }

        @Override
        public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            X509TrustManager tm = getTrustManager();
            for (X509Certificate cert : chain) {
                if (thumbprint.equalsIgnoreCase(getThumbprint(cert))) {
                    return true;
                }
            }
            tm.checkServerTrusted(chain, authType);
            return true;
        }

        private X509TrustManager getTrustManager() throws CertificateException {
            try {
                TrustManager[] trustManagers = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()).getTrustManagers();
                for (TrustManager trustManager : trustManagers) {
                    if (trustManager instanceof X509TrustManager) {
                        return (X509TrustManager) trustManager;
                    }
                }
                throw new CertificateException("No X509TrustManager found");
            } catch (Exception e) {
                throw new CertificateException("Failed to get X509TrustManager", e);
            }
        }

        private String getThumbprint(X509Certificate cert) throws CertificateException {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] thumbprint = digest.digest(cert.getEncoded());
                StringBuilder sb = new StringBuilder();
                for (byte b : thumbprint) {
                    sb.append(String.format("%02x", b));
                }
                return sb.toString();
            } catch (Exception e) {
                throw new CertificateException("Failed to get certificate thumbprint", e);
            }
        }
    }

