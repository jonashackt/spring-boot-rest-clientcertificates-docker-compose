package de.jonashackt.configuration;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;

@Configuration
public class RestClientCertConfiguration {

    private char[] bobPassword = "bobpassword".toCharArray();

    @Value("classpath:alice-keystore.p12")
    private Resource aliceKeystoreResource;

    @Value("classpath:alice-truststore.jks")
    private Resource aliceTruststoreResource;

    @Value("classpath:tom-keystore.p12")
    private Resource tomKeystoreResource;

    @Value("classpath:tom-truststore.jks")
    private Resource tomTruststoreResource;

    @Value("classpath:client-truststore.jks")
    private Resource truststoreResource;

/*    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) throws Exception {



        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(aliceKeystoreResource), "alicepassword".toCharArray(), "alicepassword".toCharArray())
                .loadKeyMaterial(inStream2File(tomKeystoreResource), "tompassword".toCharArray(), "tompassword".toCharArray())
                .loadTrustMaterial(inStream2File(truststoreResource), bobPassword)
                .build();

        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        return builder
                .requestFactory(new HttpComponentsClientHttpRequestFactory(client))
                .build();
    }*/

    @Bean
    public HttpComponentsClientHttpRequestFactory serverTomClientHttpRequestFactory() throws Exception {
        SSLContext sslContext = SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(tomKeystoreResource), "tompassword".toCharArray(), "tompassword".toCharArray())
                .loadTrustMaterial(inStream2File(tomTruststoreResource), "tompassword".toCharArray())
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
                .loadKeyMaterial(inStream2File(aliceKeystoreResource), "alicepassword".toCharArray(), "alicepassword".toCharArray())
                .loadTrustMaterial(inStream2File(aliceTruststoreResource), "alicepassword".toCharArray())
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
