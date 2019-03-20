/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;

import org.geotools.factory.CommonFactoryFinder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.geosparc.graph.base.DGraph;
import com.geosparc.gte.TestData;
import com.geosparc.gte.config.ConnectionConfig.ConnectionType;

public class FeatureJsonExporterTest {
	
	private FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2();

	protected DGraph<GlobalId, SimpleFeature, SimpleFeature> createGraph() throws Exception {
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		graphBuilder.addVertices("testL", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		
		graphBuilder.addLogicalEdges("testL", 
				TestData.getLogicalEdges(), 
				fac.property("id"),
				fac.property("from"),
				fac.property("to"),
				null);
		graphBuilder.addGeographicalEdges("testG", 
				TestData.getGeoEdges(), 
				fac.property("id"),
				fac.function("startPoint", fac.property("geom")),
				fac.function("endPoint", fac.property("geom")),
				null, 0.001);
		graphBuilder.addConnection("testL", "testG", 
				fac.property("ref"), ConnectionType.PROJECTED);
		
		return graphBuilder.get();
	}
	
	@Test
	public void testJson() throws Exception {
		StringWriter w = new StringWriter();
		new FeatureJsonExporter().exportGraph(createGraph(), w);
		JSONObject o = new JSONObject(w.toString());
		assertEquals("testL:1", o.getJSONArray("vertices")
				.getJSONObject(0).getString("id"));
		assertEquals(0, o.getJSONArray("vertices")
				.getJSONObject(0).getJSONArray("in").length());
		assertEquals(2, o.getJSONArray("vertices")
				.getJSONObject(0).getJSONArray("out").length());
	}
	
	@Test
	public void testFlatJson() throws Exception {
		StringWriter w = new StringWriter();
		new FeatureJsonExporter(true).exportGraph(createGraph(), w);
		JSONArray o = new JSONArray(w.toString());
		assertEquals("1", o.getJSONObject(0).getString("id"));
	}
	
}
