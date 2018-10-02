package de.jonashackt;

import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@ContextConfiguration()
public class ClientTest {

	@ClassRule
	public static DockerComposeContainer services =
			new DockerComposeContainer(new File("../docker-compose.yml"))
					.withExposedService("server-alice", 8443,Wait.forListeningPort())
					.withExposedService("server-tom", 8443, Wait.forListeningPort())
					.withExposedService("client-bob", 8080, Wait.forHttp("/swagger-ui.html").forStatusCode(200));

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
