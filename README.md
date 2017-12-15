Spring RestTemplate calls multiple servers that are secured with multiple client certificate - setup with Docker Compose & Tested with docker-compose-rule
=============================
[![Build Status](https://travis-ci.org/jonashackt/spring-boot-rest-clientcertificates-docker-compose.svg?branch=master)](https://travis-ci.org/jonashackt/spring-boot-rest-clientcertificates-docker-compose)

This repository basically forks all the ground work that was done in https://github.com/jonashackt/spring-boot-rest-clientcertificate. This is a basic example, where the client certificate secured server is a Spring Boot Application and the client is just a Testcase that uses Spring´s RestTemplate which is configured to use the client certificate.

In contrast the present project focusses on the configuration of more than one client certificates and how to access REST endpoints from multiple servers that are secured by different client certificates with Spring´s RestTemplate.

Therefore we use several Spring Boot based microservices that provide different client certificate secured REST endpoint and a separate microservice that accesses these services:

```
                  -------------------------------------------    
                 | Docker Network scope                 __   |  
                 |                         ============|o¬|  |  
                 |                         =            ¯¯=  |   
                 |                         = server-alice =  |
 ============    |   ==============   ssl  =              =  |
 =  docker- =    |   =            = -----> ================  |
 = network- = -----> = client-bob =                     __   |
 =  client  =    |   =   __   __  = -----> ============|o¬|  |
 ============    |   ===|o¬|=|o¬|==   ssl  =            ¯¯=  |
                 |       ¯¯   ¯¯           =  server-tom  =  |
                 |                         =              =  |
                 |                         ================  |
                  -------------------------------------------
                 
```


For a general approach on how to generate private keys and certificates and create Java Keystores, have a look into https://github.com/jonashackt/spring-boot-rest-clientcertificate#generate-the-usual-key-and-crt---and-import-them-into-needed-keystore-jks-files

# HowTo Use

Everything you need to run a full build and __complete__ test (incl. Integrationtest of docker-network-client firing up all three microservices that´ll call each other with client certificate support) is this:

```
mvn clean install
```

Only, if you want to check manually, you can do a `docker-compose up -d` and open your Browser with [http:localhost:8080/swagger-ui.html] and fire up a GET-Request to /secretservers with Swagger :)


# Integrationtesting with [docker-compose-rule](https://github.com/palantir/docker-compose-rule)

As client-bob only has access to the DNS aliases `server-alice` and `server-tom`, if it itself is part of the Docker (Compose) network and these aliases are used to access both client certificate secured endpoints, we need another way to run an Integration test inside the Docker network scope.

Therefore we use the [docker-compose-rule](https://github.com/palantir/docker-compose-rule) and the __docker-network-client__ that just calls __client-bob__ inside the Docker network.

docker-compose-rule needs a special Maven repository to be added in `docker-network-client`, because it is only served on Bintray.

```
		<dependency>
			<groupId>com.palantir.docker.compose</groupId>
			<artifactId>docker-compose-rule-junit4</artifactId>
			<version>${docker-compose-rule-junit4.version}</version>
			<scope>test</scope>
		</dependency>

	</dependencies>

	<repositories>
		<repository>
			<id>bintray</id>
			<name>Bintray Maven Repository - as docker-compose-rule-junit4 is only available there</name>
			<url>https://dl.bintray.com/palantir/releases</url>
			<layout>default</layout>
		</repository>
	</repositories>
```

And the code you need, to fire up all Docker Compose services / Docker Containers is really simple:

```
package de.jonashackt;

import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@ContextConfiguration()
public class ClientTest {

	@ClassRule
	public static DockerComposeRule docker = DockerComposeRule.builder()
			.file("../docker-compose.yml")
			.waitingForService("server-alice", HealthChecks.toHaveAllPortsOpen())
			.waitingForService("server-tom", HealthChecks.toHaveAllPortsOpen())
			.waitingForService("client-bob",  HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat("http://$HOST:$EXTERNAL_PORT/swagger-ui.html")))
			.build();

	@Test
	public void is_client_bob_able_to_call_all_servers_with_client_certs() {

		when()
			.get("http://localhost:8080/secretservers")
		.then()
			.statusCode(HttpStatus.SC_OK)
			.assertThat()
				.body(containsString("Both Servers called - Alice said 'Alice answering!' & Tom replied 'Tom answering!'."));
	}
}

```

# TlDR: How to create multiple keys & certificates for multiple servers - and add these into appropriate truststores / keystores


## server-alice keys and client certificate, truststore & keystore (see /server-alice/src/main/resources)

#### 1. Private Key: aliceprivate.key

```
openssl genrsa -des3 -out aliceprivate.key 1024
```

- passphrase `alicepassword`


#### 2. Certificate Signing Request (CSR): alice.csr

```
openssl req -new -key aliceprivate.key -out alice.csr -config alice-csr.conf
```

__Common Name__: `server-alice`, which will later be a DNS alias inside the Docker network 


#### 3. self-signed Certificate: alice.crt

```
openssl x509 -req -days 3650 -in alice.csr -signkey aliceprivate.key -out alice.crt -extfile alice-csr.conf -extensions v3_req
```


#### 4. Java Truststore Keystore, that inherits the generated self-signed Certificate: alice-truststore.jks

```
keytool -import -file alice.crt -alias alicesCA -keystore alice-truststore.jks
```

__the same password__ `alicepassword`


#### 5. Java Keystore, that inherits Public and Private Keys (keypair): alice-keystore.jks

```
openssl pkcs12 -export -in alice.crt -inkey aliceprivate.key -certfile alice.crt -name "alicecert" -out alice-keystore.p12
```

__the same password__ `alicepassword`

To read in KeyStore Explorer

```
keytool -importkeystore -srckeystore alice-keystore.p12 -srcstoretype pkcs12 -destkeystore alice-keystore.jks -deststoretype JKS
```



## server-tom keys and client certificate, truststore & keystore (see /server-tom/src/main/resources)

#### 1. Private Key: tomprivate.key

```
openssl genrsa -des3 -out tomprivate.key 1024
```

- passphrase `tompassword`


#### 2. Certificate Signing Request (CSR): tom.csr

```
openssl req -new -key tomprivate.key -out tom.csr -config tom-csr.conf
```

__Common Name__: `server-tom`, which will later be a DNS alias inside the Docker network 


#### 3. self-signed Certificate: tom.crt

```
openssl x509 -req -days 3650 -in tom.csr -signkey tomprivate.key -out tom.crt -extfile tom-csr.conf -extensions v3_req
```


#### 4. Java Truststore Keystore, that inherits the generated self-signed Certificate: tom-truststore.jks

```
keytool -import -file tom.crt -alias tomsCA -keystore tom-truststore.jks
```

__the same password__ `tompassword`


#### 5. Java Keystore, that inherits Public and Private Keys (keypair): tom-keystore.p12

```
openssl pkcs12 -export -in tom.crt -inkey tomprivate.key -certfile tom.crt -name "tomcert" -out tom-keystore.p12
```

__the same password__ `tompassword`



## client-bob truststore & keystore (see /server-alice/src/main/resources)

#### 1. Java Truststore Keystore, that inherits the generated self-signed Certificate: client-truststore.jks

```
keytool -import -file alice.crt -alias alicesCA -keystore client-truststore.jks
keytool -import -file tom.crt -alias tomsCA -keystore client-truststore.jks
```

__password__ `bobpassword`

In KeyStore Explorer this should look like this:

![client-truststore](https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose/blob/master/client-truststore.png)


#### 2. Java Keystores, that inherit Public and Private Keys (keypair): copy alice-keystore.p12 & tom-keystore.p12

As Apache HttpClient isn´t able to handle [more than one client certificate for the same SSLContext](http://mail-archives.apache.org/mod_mbox/hc-httpclient-users/201109.mbox/%3C1315998630.3176.17.camel@ubuntu%3E), we need to provide two of them. Therefore we don´t need to add two private keys and certificates to one Keystore - we can just use both Keystores we already assembled before. So we copy `alice-keystore.p12` & `tom-keystore.p12` to clien-bob/src/main/resources and use them in the [RestClientCertConfiguration](https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose/blob/master/client-bob/src/main/java/de/jonashackt/configuration/RestClientCertConfiguration.java) like this:

```
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
```

Now we´re able to insert individual SSLContexts into Spring´s RestTemplate. Therefore see [ServerClientImpl](https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose/blob/master/client-bob/src/main/java/de/jonashackt/client/ServerClientImpl.java):

```
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServerClientImpl implements ServerClient {

    @Autowired
    private HttpComponentsClientHttpRequestFactory serverAliceClientHttpRequestFactory;

    @Autowired
    private HttpComponentsClientHttpRequestFactory serverTomClientHttpRequestFactory;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public String callServerAlice() {
        restTemplate.setRequestFactory(serverAliceClientHttpRequestFactory);

        return restTemplate.getForObject("https://server-alice:8443/hello", String.class);
    }

    @Override
    public String callServerTom() {
        restTemplate.setRequestFactory(serverTomClientHttpRequestFactory);

        return restTemplate.getForObject("https://server-tom:8443/hello", String.class);
    }
}
```



# Links

https://stackoverflow.com/questions/25869428/classpath-resource-not-found-when-running-as-jar

https://www.thomas-krenn.com/de/wiki/Openssl_Multi-Domain_CSR_erstellen

https://stackoverflow.com/questions/30977264/subject-alternative-name-not-present-in-certificate

https://stackoverflow.com/questions/21488845/how-can-i-generate-a-self-signed-certificate-with-subjectaltname-using-openssl

--> this is not the only solution, see `-extfile` and `-extensions` CLI paramters!

https://serverfault.com/questions/779475/openssl-add-subject-alternate-name-san-when-signing-with-ca

#### Multiple certificates handling in Java Keystores: 

Look into the documentation of Tomcat in section `keyAlias`: http://tomcat.apache.org/tomcat-6.0-doc/config/http.html#SSL_Support

https://stackoverflow.com/questions/5292074/how-to-specify-outbound-certificate-alias-for-https-calls

https://stackoverflow.com/questions/6370745/can-we-load-multiple-certificates-keys-in-a-key-store
