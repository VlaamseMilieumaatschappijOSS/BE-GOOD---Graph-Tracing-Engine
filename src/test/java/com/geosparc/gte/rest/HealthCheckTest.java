package com.geosparc.gte.rest;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import com.geosparc.gte.VmmGteApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {VmmGteApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("testhealth")
public class HealthCheckTest {

	@LocalServerPort
	private int port;
	
	private TestRestTemplate restTemplate = new TestRestTemplate();
	
	@Test
	public void testHealthStatusUp() throws RestClientException, JSONException {

		JSONObject o = new JSONObject(restTemplate.getForObject(
				"http://localhost:" + port + "/actuator/health",
				String.class));
		
		assertEquals("UP", o.get("status"));
	}

}
