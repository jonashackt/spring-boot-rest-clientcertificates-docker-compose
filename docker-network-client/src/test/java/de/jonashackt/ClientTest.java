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
			.waitingForService("client-bob",  HealthChecks.toRespondOverHttp(8080, (port) -> port.inFormat("http://localhost:8080/swagger-ui.html")))
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
