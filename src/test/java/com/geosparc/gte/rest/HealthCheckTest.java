/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.rest;

import static org.junit.Assert.assertEquals;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;

import com.geosparc.gte.VmmGteApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {VmmGteApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
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
