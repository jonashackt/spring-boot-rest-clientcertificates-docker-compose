package de.jonashackt.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "REST Client uses clientcertificate to authenticate to Spring Boot Server",
                version = "2.0",
                description = "See https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose for more information",
                license = @License(
                        name = "Apache License Version 2.0",
                        url = "https://github.com/jonashackt/spring-boot-rest-clientcertificates-docker-compose/blob/master/LICENSE"
                )
        ),
        servers = @Server(url = "http://client-bob:8080")
)
public class OpenAPIConfig {
}