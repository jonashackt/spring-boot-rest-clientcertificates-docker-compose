package de.jonashackt.configuration;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;

@Configuration
public class RestClientCertConfiguration {

    private char[] bobPassword = "bobpassword".toCharArray();
    private char[] tomPassword = "tompassword".toCharArray();

    @Value("classpath:alice-keystore.p12")
    private Resource aliceKeystoreResource;

    @Value("classpath:tom-keystore.p12")
    private Resource tomKeystoreResource;

    @Value("classpath:client-truststore.jks")
    private Resource truststoreResource;
    private char[] alicePassword = "alicepassword".toCharArray();

    @Bean
    public HttpComponentsClientHttpRequestFactory serverTomClientHttpRequestFactory() throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(tomKeystoreResource), tomPassword, tomPassword)
                .loadTrustMaterial(inStream2File(truststoreResource), bobPassword)
                .build();

        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory serverAliceClientHttpRequestFactory() throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(aliceKeystoreResource), alicePassword, alicePassword)
                .loadTrustMaterial(inStream2File(truststoreResource), bobPassword)
                .build();

        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }

    private File inStream2File(Resource resource) {
        try {
            File tempFile = File.createTempFile("file", ".tmp");
            FileUtils.copyInputStreamToFile(resource.getInputStream(), tempFile);
            return tempFile;
        } catch (IOException e) {
            throw new RuntimeException("Problems loading Keystores", e);
        }
    }
}
