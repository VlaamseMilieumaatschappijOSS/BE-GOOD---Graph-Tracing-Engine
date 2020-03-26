/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.geosparc.gte.TestUtilities;
import com.geosparc.gte.VmmGteApplication;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {VmmGteApplication.class},
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) 
@ActiveProfiles("test")
public class TraceControllerTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();
    
	@LocalServerPort
	private int port;
	
	private HttpHeaders headers = new HttpHeaders();
	
	private TestRestTemplate restTemplate = new TestRestTemplate();

	@BeforeClass
	public static void beforeClass() throws IOException {
		TestUtilities.unzip("shape/riool.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/vha.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/areas.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/areas2.zip", testFolder.getRoot());
		System.setProperty("temp-directory", testFolder.getRoot().getAbsolutePath());
	}
	
	@Before
	public void before() {
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setAcceptLanguageAsLocales(Collections.singletonList(Locale.ENGLISH));
	}
	
	private ResponseEntity<String> trace(String requestFileName) throws IOException {
		
		HttpEntity<String> entity = new HttpEntity<String>(
				IOUtils.toString(getClass().getResourceAsStream(requestFileName), "utf-8"),
				headers);

		return restTemplate.exchange(
				"http://localhost:" + port + "/trace",
				HttpMethod.POST, entity, String.class);
		
	}
		
	@Test
	public void testBasicRequest() throws JSONException, IOException {
		
		ResponseEntity<String> response = trace("request-1.json");
				
		JSONObject o = new JSONObject(response.getBody());

		assertEquals(0, o.getJSONArray("warnings").length());
		
		JSONObject g = o.getJSONObject("graph");
		JSONArray vs = g.getJSONArray("vertices");
		assertEquals(18, vs.length());
		assertEquals("riool:ZG018_291505", 
				vs.getJSONObject(0).getString("id"));
		JSONObject v = vs.getJSONObject(1);
		assertEquals("riool:ZG018_179130", v.getString("id"));
		assertEquals(69.98546, v.getDouble("distance"), 0.00001);
		JSONArray in = v.getJSONArray("in");
		assertEquals(1, in.length());
		assertEquals("riool:20031046", in.get(0));
		JSONArray out = v.getJSONArray("out");
		assertEquals(1, out.length());
		assertEquals("riool:20031048", out.get(0));
		JSONObject vf = v.getJSONObject("data");
		assertNotNull(vf.getJSONObject("geometry"));
		JSONObject vp = vf.getJSONObject("properties");
		assertNotNull(vp);
		assertEquals(2, vp.length());
		assertNotNull(vp.getString("status"));
		assertNotNull(vp.getString("peil"));
		
		JSONArray es = g.getJSONArray("edges");
		assertEquals(17, es.length());
		JSONObject e = es.getJSONObject(0);
		assertEquals("riool:20031046", e.getString("id"));
		assertEquals("riool:ZG018_291505", e.getString("from"));
		assertEquals("riool:ZG018_179130", e.getString("to"));
		assertEquals(69.985464, e.getDouble("weight"), 0.00001);
		JSONObject ef = e.getJSONObject("data");
		assertNotNull(ef.getJSONObject("geometry"));
		JSONObject ep = ef.getJSONObject("properties");
		assertNotNull(ep);
		assertEquals(2, ep.length());
		assertNotNull(ep.getString("str_type"));
		assertNotNull(ep.getString("zuiverings"));	
	}
	
	@Test
	public void testFilteredRequest() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-2.json");

		JSONObject o = new JSONObject(response.getBody());

		JSONObject g = o.getJSONObject("graph");
		JSONArray vs = g.getJSONArray("vertices");
		assertEquals(9, vs.length());

		JSONArray es = g.getJSONArray("edges");
		assertEquals(8, es.length());

	}
	
	@Test
	public void testUpstreamRequest() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-3.json");

		JSONObject o = new JSONObject(response.getBody());

		JSONObject g = o.getJSONObject("graph");
		JSONArray vs = g.getJSONArray("vertices");
		assertEquals(3, vs.length());

		JSONArray es = g.getJSONArray("edges");
		assertEquals(2, es.length());

	}
	
	@Test
	public void testRequestByEdge() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-4.json");

		JSONObject o = new JSONObject(response.getBody());
		JSONObject g = o.getJSONObject("graph");
		JSONArray vs = g.getJSONArray("vertices");
		assertEquals(34, vs.length());

		JSONArray es = g.getJSONArray("edges");
		assertEquals(33, es.length());

	}
	

	@Test
	public void testRequestWithAreas() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-areas.json");

		JSONObject o = new JSONObject(response.getBody());
		System.out.println(o);
		
		JSONObject areas = o.getJSONObject("overlappingAreas");
		assertEquals(1, areas.length());
		
		JSONArray areasRisico = areas.getJSONArray("Risicogebieden");
		assertEquals(2, areasRisico.length());
		JSONObject f = areasRisico.getJSONObject(0);
		assertNotNull(f.getJSONObject("geometry"));
		JSONObject fp = f.getJSONObject("properties");
		assertNotNull(fp);
		assertEquals(3, fp.length());
		assertNotNull(fp.getString("Zone"));
	}

	@Test
	public void testRequestWithAreas2() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-areas2.json");

		JSONObject o = new JSONObject(response.getBody());
		
		JSONObject areas = o.getJSONObject("overlappingAreas");
		assertEquals(2, areas.length());
		
		JSONArray areasRisico = areas.getJSONArray("Risicogebieden2");
		assertEquals(2, areasRisico.length());
		JSONObject f = areasRisico.getJSONObject(0);
		assertNotNull(f.getJSONObject("geometry"));
		JSONObject fp = f.getJSONObject("properties");
		assertNotNull(fp);
		assertEquals(3, fp.length());
		assertNotNull(fp.getString("Zone"));
	}
	
	
	@Test
	public void testZipfile() throws IOException {
		HttpEntity<String> entity = new HttpEntity<String>(
				IOUtils.toString(getClass().getResourceAsStream("request-6.json"), "utf-8"),
				headers);

		ResponseEntity<byte[]> response =  restTemplate.exchange(
				"http://localhost:" + port + "/trace-shape",
				HttpMethod.POST, entity, byte[].class);

		Set<String> fileNames = new HashSet<String>();
		try (ZipInputStream zis = new ZipInputStream(
				new ByteArrayInputStream(response.getBody()))) {
			ZipEntry entry;
			while ((entry = zis.getNextEntry()) != null) {
				fileNames.add(entry.getName());
			}
		}

		assertEquals(37, fileNames.size());
		assertTrue(fileNames.contains("trace_info.txt"));
		assertTrue(fileNames.contains("riool-nodes.dbf"));
		assertTrue(fileNames.contains("riool-nodes.prj"));
		assertTrue(fileNames.contains("riool-nodes.cst"));
		assertTrue(fileNames.contains("riool-nodes.shx"));
		assertTrue(fileNames.contains("riool-nodes.fix"));
		
		assertTrue(fileNames.contains("riool-edges.shp"));
		assertTrue(fileNames.contains("riool-edges.prj"));
		assertTrue(fileNames.contains("riool-edges.dbf"));
		assertTrue(fileNames.contains("riool-edges.shx"));
		assertTrue(fileNames.contains("riool-edges.cst"));
		assertTrue(fileNames.contains("riool-edges.fix"));
		assertTrue(fileNames.contains("riool-edges.shp"));
		
		assertTrue(fileNames.contains("vha-edges.prj"));
		assertTrue(fileNames.contains("vha-edges.dbf"));
		assertTrue(fileNames.contains("vha-edges.shx"));
		assertTrue(fileNames.contains("vha-edges.cst"));
		assertTrue(fileNames.contains("vha-edges.fix"));
		assertTrue(fileNames.contains("vha-edges.shp"));
		assertTrue(fileNames.contains("vha-nodes.prj"));
		assertTrue(fileNames.contains("vha-nodes.dbf"));
		assertTrue(fileNames.contains("vha-nodes.shx"));
		assertTrue(fileNames.contains("vha-nodes.cst"));
		assertTrue(fileNames.contains("vha-nodes.fix"));
		assertTrue(fileNames.contains("vha-nodes.shp"));

		assertTrue(fileNames.contains("connections-edges.prj"));
		assertTrue(fileNames.contains("connections-edges.dbf"));
		assertTrue(fileNames.contains("connections-edges.shx"));
		assertTrue(fileNames.contains("connections-edges.cst"));
		assertTrue(fileNames.contains("connections-edges.fix"));
		assertTrue(fileNames.contains("connections-edges.shp"));

		assertTrue(fileNames.contains("connections-nodes.prj"));
		assertTrue(fileNames.contains("connections-nodes.dbf"));
		assertTrue(fileNames.contains("connections-nodes.shx"));
		assertTrue(fileNames.contains("connections-nodes.cst"));
		assertTrue(fileNames.contains("connections-nodes.fix"));
		assertTrue(fileNames.contains("connections-nodes.shp"));
	}

	@Test
	public void testAggregated() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-5.json");

		JSONObject o = new JSONObject(response.getBody());
		
		System.out.println(o);

		assertEquals(381, o.getJSONObject("graph")
				.getJSONArray("edges")
				.getJSONObject(0).getInt("ie_agg"));
	}

	@Test
	public void testEmptyResult() throws JSONException, IOException {

		ResponseEntity<String> response = trace("request-empty.json");

		JSONObject o = new JSONObject(response.getBody());

		assertEquals(0, o.getJSONObject("graph")
				.getJSONArray("edges").length());
		assertEquals(0, o.getJSONObject("graph")
				.getJSONArray("vertices").length());
	}
	
	@Test
	public void testLimitRequest() throws JSONException, IOException {
		
		ResponseEntity<String> response = trace("request-limit.json");
				
		JSONObject o = new JSONObject(response.getBody());		

		assertEquals(1, o.getJSONArray("warnings").length());
		assertEquals("The maximum size of the search query was reached. The returned result might be incomplete.", 
				o.getJSONArray("warnings").get(0));
	}
	
	@Test
	public void testOverlaptypes() throws IOException, JSONException {
		String response =  restTemplate.getForObject(
				"http://localhost:" + port + "/overlapTypes", 
				String.class);
		JSONArray a = new JSONArray(response);
		System.out.println(a);
	}

}
