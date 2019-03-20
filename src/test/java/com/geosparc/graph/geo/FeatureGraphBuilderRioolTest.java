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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.locationtech.jts.geom.MultiLineString;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.base.DGraph;
import com.geosparc.gte.TestUtilities;
import com.geosparc.gte.config.ConnectionConfig.ConnectionType;

public class FeatureGraphBuilderRioolTest {
	
    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();
    
	private FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2();
    
	@BeforeClass
	public static void beforeClass() throws IOException {
		TestUtilities.unzip("shape/riool.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/vha.zip", testFolder.getRoot());
	}
	
	@Test
	public void testLogicalNetwork() throws Exception {
    	//System.gc();
    	//long beginMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    	
    	FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
    	
    	// read points and create vertices
        ShapefileDataStore dataStore = new ShapefileDataStore(
        		new File(testFolder.getRoot(), "riool/Punt.shp").toURI().toURL());
    	try {
    		graphBuilder.addVertices("riool", dataStore.getFeatureSource(), 
    				fac.property("punt_code_"),
    				null);
    		
    	} finally {    
    		dataStore.dispose();
        }

        // read lines and create edges
        dataStore = new ShapefileDataStore(
        		new File(testFolder.getRoot(), "riool/Streng.shp").toURI().toURL());
        try {
    		graphBuilder.addLogicalEdges("riool", dataStore.getFeatureSource(), 
    				fac.property("streng_cod"),
    				fac.property("begin_p"),
    				fac.property("eind_p"),
    				null);
    		
    	} finally {
    		dataStore.dispose();
        }
        
        DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
        
        Idp<GlobalId, SimpleFeature> edge = graph.getEdgeById(new GlobalId("riool", "20024343"));
        assertNotNull(edge);
        Idp<GlobalId, SimpleFeature> source = graph.getEdgeSource(edge);
        assertEquals(graph.getVertexById(new GlobalId("riool", "ZG018_344701")), source);
        Set<Idp<GlobalId, SimpleFeature>> set = graph.incomingEdgesOf(source);
        assertEquals(2, set.size());
        assertTrue(set.contains(graph.getEdgeById(new GlobalId("riool", "20024347"))));
        assertTrue(set.contains(graph.getEdgeById(new GlobalId("riool", "20024346"))));
        
        //System.gc();
        //System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - beginMemory) / 1024/ 1024);
                
        //TestUtilities.export(graphBuilder.get(), new File("/home/niels/Documents/riool.dot"));

	}
	
	@Test
	public void testGeographicalNetwork() throws Exception {
    	//System.gc();
    	//long beginMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    	
    	FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
    	

        // read lines and create edges
    	ShapefileDataStore dataStore = new ShapefileDataStore(
        		new File(testFolder.getRoot(), "vha/vha.shp").toURI().toURL());
        try {
    		graphBuilder.addGeographicalEdges("vha", dataStore.getFeatureSource(), 
    				fac.property("VHAS_ID"),
    				fac.function("startPoint", fac.property("the_geom")),
    				fac.function("endPoint", fac.property("the_geom")),
    				null, 0.00001);
    	} finally {
    		dataStore.dispose();
        }
        
        DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
        
        Idp<GlobalId, SimpleFeature> edge = graph.getEdgeById(new GlobalId("vha", "63259"));
        assertNotNull(edge);
        Idp<GlobalId, SimpleFeature> target = graph.getEdgeTarget(edge);
        Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(target);
        assertEquals(1, set.size());
        assertTrue(set.contains(graph.getEdgeById(new GlobalId("vha", "48464"))));
        set = graph.incomingEdgesOf(target);
        assertEquals(2, set.size());
        assertTrue(set.contains(edge));
        assertTrue(set.contains(graph.getEdgeById(new GlobalId("vha", "65257"))));
        
        //System.gc();
        //System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - beginMemory) / 1024/ 1024);
        
        /*final int MAX_VERTICES = 10000;        
        Iterator<Identifiable<FeatureIdentifier, SimpleFeature>> it = 
        		graph.get().vertexSet().iterator();
        Set<Identifiable<FeatureIdentifier, SimpleFeature>> subSet = new HashSet<>();
        for (int i = 0; i < MAX_VERTICES; i++) {
        	subSet.add(it.next());
        }       
		TestUtilities.export(new AsSubgraph<>(graph.get(), subSet), 
        		new File("/home/niels/Documents/vha.dot"));*/

	}
	
	@Test
	public void testConnection() throws Exception {
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
    	
        ShapefileDataStore dataStore = new ShapefileDataStore(
        		new File(testFolder.getRoot(), "riool/Punt.shp").toURI().toURL());
    	try {
    		graphBuilder.addVertices("riool", dataStore.getFeatureSource(), 
    				fac.property("punt_code_"),
    				null);
    		
    	} finally {    
    		dataStore.dispose();
        }
    	dataStore = new ShapefileDataStore(
        		new File(testFolder.getRoot(), "vha/vha.shp").toURI().toURL());
        try {
    		graphBuilder.addGeographicalEdges("vha", dataStore.getFeatureSource(), 
    				fac.property("CODE"),
    				fac.function("startPoint", fac.property("the_geom")),
    				fac.function("endPoint", fac.property("the_geom")),
    				null, 0.00001);
    	} finally {
    		dataStore.dispose();
        }
        
        graphBuilder.addConnection("riool", "vha", 
        		fac.property("vhas_code"), 
        		ConnectionType.PROJECTED);
        
        DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
        
        Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("riool", "ZG018_11659"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edge = set.iterator().next();
		assertEquals(220.09214, graph.getEdgeWeight(edge), 0.00001);
		vertex = graph.getEdgeTarget(edge);
		set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		edge = set.iterator().next();
		vertex = graph.getEdgeTarget(edge);
		assertEquals(48.68928, graph.getEdgeWeight(edge), 0.00001);
		MultiLineString geometry = (MultiLineString) edge.getData().getDefaultGeometry();
		assertEquals(3, geometry.getCoordinates().length);
		assertEquals(48.68928, graph.getEdgeWeight(edge), 0.00001);
		set = graph.incomingEdgesOf(vertex);
		assertTrue(set.contains(graph.getEdgeById(new GlobalId("vha", "6038244"))));
	}

}
