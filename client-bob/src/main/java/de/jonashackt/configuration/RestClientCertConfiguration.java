package de.jonashackt.configuration;

import org.apache.commons.io.FileUtils;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;

@Configuration
public class RestClientCertConfiguration {

    private char[] bobPassword = "bobpassword".toCharArray();
    private char[] tomPassword = "tompassword".toCharArray();
    private char[] alicePassword = "alicepassword".toCharArray();

    @Value("classpath:alice-keystore.p12")
    private Resource aliceKeystoreResource;

    @Value("classpath:tom-keystore.p12")
    private Resource tomKeystoreResource;

    @Value("classpath:client-truststore.jks")
    private Resource truststoreResource;

    @Bean
    public SSLContext serverTomSSLContext() throws Exception {
        return SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(tomKeystoreResource), tomPassword, tomPassword)
                .loadTrustMaterial(inStream2File(truststoreResource), bobPassword)
                .build();
    }

    @Bean
    public SSLContext serverAliceSSLContext() throws Exception {
        return SSLContextBuilder
                .create()
                .loadKeyMaterial(inStream2File(aliceKeystoreResource), alicePassword, alicePassword)
                .loadTrustMaterial(inStream2File(truststoreResource), bobPassword)
                .build();
    }


    /**
     * The ugly need to generate a File from a InputStream - because SSLContextBuild.loadKeyMaterial only accepts File,
     * but retrieving Files from within Spring Boot Fatjars is only possible through Resources ->
     * see https://stackoverflow.com/questions/25869428/classpath-resource-not-found-when-running-as-jar
     */
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
