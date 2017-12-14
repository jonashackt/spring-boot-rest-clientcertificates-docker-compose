package de.jonashackt.client;

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
