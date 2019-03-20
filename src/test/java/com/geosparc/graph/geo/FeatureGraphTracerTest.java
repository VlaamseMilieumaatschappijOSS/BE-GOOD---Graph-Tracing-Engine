/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geotools.factory.CommonFactoryFinder;
import org.jgrapht.Graph;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId.Type;
import com.geosparc.gte.TestData;
import com.geosparc.gte.config.ConnectionConfig.ConnectionType;

public class FeatureGraphTracerTest {
	

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
	public void testFeatureGraphTracer() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
		
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
				new GlobalId("testL", "2"), false);
		
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
			
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
				
		assertEquals(3, trace.edgeSet().size());
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	@Test
	public void testFeatureGraphTracerByEdge() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
		
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
				new GlobalId("testL", "23"), false);
		
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
			
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
						
		assertEquals(3, trace.edgeSet().size());
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	@Test
	public void testFeatureGraphTracerDistance() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
		
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
				new GlobalId("testL", "1"), false);

		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setMaximumDistance("testL", 4.0);	
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertEquals(2, trace.edgeSet().size());
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","12"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","13"))));
		
		assertEquals(4.242640687119285,
				tracer.getDistance(new GlobalId("testL", "2")), 0.0001);
		assertEquals(7.0710678118654755,
				tracer.getDistance(new GlobalId("testL", "3")), 0.0001);
	}
	
	@Test
	public void testFeatureGraphTracerReverse() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
		
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
				new GlobalId("testL", "3"), true);

		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertEquals(3, trace.edgeSet().size());
		Idp<GlobalId, SimpleFeature> e = graph.getEdgeById(new GlobalId("testL","12"));
		assertTrue(trace.containsEdge(e));
		assertEquals(graph.getVertexById(new GlobalId("testL", "1")), trace.getEdgeTarget(e));
		assertEquals(graph.getVertexById(new GlobalId("testL", "2")), trace.getEdgeSource(e));
		
	}
	
	@Test
	public void testFeatureGraphTraceNodeFilter() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "1"), false);

		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setVertexFilter("testL", fac.notEqual(
				fac.property("code"), fac.literal("x")));
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertFalse(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","12"))));
		assertFalse(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));

		
	}
	
	@Test
	public void testFeatureGraphTraceNodeFilter2() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "2"), false);

		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setVertexFilter("testL", 
				fac.or(fac.equals(
						fac.property("code"), fac.literal("x")),
					fac.equals(
						fac.property("code"), fac.literal("y"))));		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));

		
	}
	
	
	@Test
	public void testFeatureGraphTraceEdgeFilter() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "1"), false);
	
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setEdgeFilter("testL", fac.notEqual(
				fac.property("code"), fac.literal("x")));
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","12"))));
		assertFalse(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	@Test
	public void testFeatureGraphTraceEdgeFilter2() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "2"), false);
	
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setEdgeFilter("testL", fac.equals(
				fac.property("code"), fac.literal("x")));
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	@Test
	public void testFeatureGraphTraceEdgeFilter3() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "1"), false);
	
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setEdgeFilter("testG", fac.notEqual(
				fac.property("code"), fac.literal("x")));
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","12"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertFalse(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	@Test
	public void testFeatureGraphTraceEdgeFilter4() throws Exception {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph
			= createGraph();
	
		FeatureGraphTracer tracer = new FeatureGraphTracer(graph, 
			new GlobalId("testL", "1"), false);
	
		tracer.addNetwork("testL");
		tracer.addNetwork("testG");
		tracer.setEdgeFilter("testG", fac.equals(
				fac.property("code"), fac.literal("x")));
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
		
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","12"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testL","23"))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c", Type.GENERATED))));
		assertTrue(trace.containsEdge(graph.getEdgeById(new GlobalId("testG","testL.3-c+", Type.GENERATED))));
		
	}
	
	
	
}
