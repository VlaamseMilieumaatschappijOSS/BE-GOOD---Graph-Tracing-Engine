/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.engine;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.geo.GlobalId;
import com.geosparc.gte.TestUtilities;
import com.geosparc.gte.config.GteConfig;
import com.geosparc.gte.engine.impl.GraphTracingEngineImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {GteConfig.class, GraphTracingEngineImpl.class})
@EnableScheduling
@ActiveProfiles("test")
public class GraphTracingEngineTest {
	
    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();
	
	@Autowired
	private GraphTracingEngine engine;	
	
		
	@BeforeClass
	public static void beforeClass() throws IOException {
		TestUtilities.unzip("shape/riool.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/vha.zip", testFolder.getRoot());
		System.setProperty("temp-directory", testFolder.getRoot().getAbsolutePath());
	}
	
	@Test
	public void testConfigurationLoaded() {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> g = engine.getGraph();
		assertEquals(84, g.edgeSet().size());
		assertEquals(83, g.vertexSet().size());
	}
	
}
