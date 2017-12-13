REST Client uses clientcertificate to authenticate to Spring Boot Server
=============================
[![Build Status](https://travis-ci.org/jonashackt/spring-boot-rest-clientcertificates-docker-compose.svg?branch=master)](https://travis-ci.org/jonashackt/spring-boot-rest-clientcertificates-docker-compose)

This repository basically forks all the ground work that was done in https://github.com/jonashackt/spring-boot-rest-clientcertificate. This is a basic example, where the client certificate secured server is a Spring Boot Application and the client is just a Testcase that uses Spring´s RestTemplate which is configured to use the client certificate.

In contrast the present project focusses on the configuration of more than one client certificates and how to access REST endpoints from multiple servers that are secured by different client certificates with Spring´s RestTemplate.

Therefore we use several Spring Boot based microservices that provide different client certificate secured REST endpoint and a separate microservice that accesses these services:

```
                                   ================
                                   =              =
                                   = server-alice =
==============                     =              =
=            = ------------------> ================
= client-bob =                     
=            = ------------------> ================
==============                     =              =
                                   =  server-tom  =
                                   =              =
                                   ================
```


For a general approach on how to generate private keys and certificates and create Java Keystores, have a look into https://github.com/jonashackt/spring-boot-rest-clientcertificate#generate-the-usual-key-and-crt---and-import-them-into-needed-keystore-jks-files

# HowTo Use

```
mvn clean install
docker-compose up
```

Open your Browser with [http:localhost:8080/swagger-ui.html] and fire up a GET-Request to /secretservers with Swagger :)


# TlDR: How to create multiple keys & certificates for multiple servers - and add these into one truststore / keystore


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


#### 2. Java Keystore, that inherits Public and Private Keys (keypair): client-keystore.p12

We need a way to import multiple private keys and certificates into the same `client-keystore.jks`, so that our implementation could call multiple secured endpoints. This seems to be a harder task then one could think beforehand. But luckily there´s a simple way: Just copy both `alice-keystore.p12` and `tom-keystore.p12` into __client-bob/src/main/resources__ and use keytool as follows:

```
keytool -importkeystore -srckeystore alice-keystore.p12 -srcstoretype pkcs12 -destkeystore client-keystore.jks -deststoretype JKS
keytool -importkeystore -srckeystore tom-keystore.p12 -srcstoretype pkcs12 -destkeystore client-keystore.jks -deststoretype JKS
```

The result should look like this:

![client-keystore](https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose/blob/master/client-keystore.png)




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
