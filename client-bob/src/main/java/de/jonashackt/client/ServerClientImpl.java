package de.jonashackt.client;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;

@Component
public class ServerClientImpl implements ServerClient {

    @Autowired
    private SSLContext serverTomSSLContext;

    @Autowired
    private SSLContext serverAliceSSLContext;

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public String callServerAlice() {
        restTemplate.setRequestFactory(createHttpComponentsClientHttpRequestFactory(serverAliceSSLContext));

        return restTemplate.getForObject("https://server-alice:8443/hello", String.class);
    }

    @Override
    public String callServerTom() {
        restTemplate.setRequestFactory(createHttpComponentsClientHttpRequestFactory(serverTomSSLContext));

        return restTemplate.getForObject("https://server-tom:8443/hello", String.class);
    }

    private HttpComponentsClientHttpRequestFactory createHttpComponentsClientHttpRequestFactory(SSLContext sslContext) {
        HttpClient client = HttpClients.custom()
                .setSSLContext(sslContext)
                .build();

        return new HttpComponentsClientHttpRequestFactory(client);
    }
}
