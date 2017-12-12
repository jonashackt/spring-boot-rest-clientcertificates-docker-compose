package de.jonashackt.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ServerClientImpl implements ServerClient {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String callServerAlice() {
        return restTemplate.getForObject("https://server-alice:8443/hello", String.class);
    }

    @Override
    public String callServerTom() {
        return restTemplate.getForObject("https://server-tom:8443/hello", String.class);
    }
}
