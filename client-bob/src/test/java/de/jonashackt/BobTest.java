package de.jonashackt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(
		classes = BobApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
public class BobTest {

	@LocalServerPort
	private int port;

    private RestTemplate restTemplate = new RestTemplate();

	@Disabled("no localhost support, because without Docker we would need to bind the same Port 8443 of server-alice and server-tom two times, which isn´t possible")
	@Test
	public void is_hello_resource_callable_with_client_cert() {
		String response = restTemplate.getForObject("https://localhost:" + port + "/secretservers", String.class);
	    
	    assertThat(response, containsString("Both Servers called - Alice said 'Alice answering!' & Tom replied 'Tom answering!'."));
	}
}
