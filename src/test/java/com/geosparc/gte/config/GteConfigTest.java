/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.geosparc.gte.config.ConnectionConfig.ConnectionType;
import com.geosparc.gte.config.NetworkConfig.NodeType;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=GteConfig.class)
@ActiveProfiles("test")
public class GteConfigTest {

	@Autowired 
	private GteConfig configuration;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		System.setProperty("temp-directory", "/my/dir");
	}
	
	@Test
	public void testConfiguration() {
		configuration.validate();
		
		assertEquals(2, configuration.getNetworks().size());	
		
		NetworkConfig config = configuration.getNetworks().get(0);
		assertEquals("riool", config.getName());
		assertEquals(NodeType.LOGICAL, config.getNodeType());
		assertEquals("file:/my/dir/riool/Streng.shp", config.getEdgeDataStore().get("url"));
		assertNotNull(config.getEdgeFeature());
		assertEquals("Streng", config.getEdgeFeature().getName());
		assertEquals("streng_cod", config.getEdgeFeature().getIdAttribute());
		assertEquals(3, config.getEdgeFeature().getUserAttributes().size());
		assertEquals("str_type", config.getEdgeFeature().getUserAttributes().get(0));
		assertEquals("zuiverings", config.getEdgeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getEdgeFeature().getUserAttributes().get(2));
		assertEquals(4, config.getEdgeFeature().getFilterAttributes().size());
		assertEquals("str_type", config.getEdgeFeature().getFilterAttributes().get(0));
		assertEquals("afgeleidwa", config.getEdgeFeature().getFilterAttributes().get(1));
		assertEquals("begin_p", config.getEdgeFeature().getFilterAttributes().get(2));
		assertEquals("eind_p", config.getEdgeFeature().getFilterAttributes().get(3));
		assertEquals("begin_p", config.getStartAttribute());
		assertEquals("eind_p", config.getEndAttribute());
		assertEquals("file:/my/dir/riool/Punt.shp", config.getNodeDataStore().get("url"));
		assertNotNull(config.getNodeFeature());
		assertEquals("Punt", config.getNodeFeature().getName());
		assertEquals("punt_code_", config.getNodeFeature().getIdAttribute());
		assertEquals(3, config.getNodeFeature().getUserAttributes().size());
		assertEquals("status", config.getNodeFeature().getUserAttributes().get(0));
		assertEquals("peil", config.getNodeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getNodeFeature().getUserAttributes().get(2));
		assertEquals(3, config.getNodeFeature().getFilterAttributes().size());
		assertEquals("status", config.getNodeFeature().getFilterAttributes().get(0));
		assertEquals("pnt_type", config.getNodeFeature().getFilterAttributes().get(1));
		assertEquals("vhas_code", config.getNodeFeature().getFilterAttributes().get(2));
		assertEquals(0, config.getTolerance(), 0.0);

		config = configuration.getNetworks().get(1);
		assertEquals("vha", config.getName());
		assertEquals(NodeType.GEOGRAPHICAL, config.getNodeType());
		assertEquals("file:/my/dir/vha/vha.shp", config.getEdgeDataStore().get("url"));
		assertNotNull(config.getEdgeFeature());
		assertEquals("CODE", config.getEdgeFeature().getIdAttribute());
		assertEquals(3, config.getEdgeFeature().getUserAttributes().size());
		assertEquals("CATEGORIE", config.getEdgeFeature().getUserAttributes().get(0));
		assertEquals("CODE", config.getEdgeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getEdgeFeature().getUserAttributes().get(2));
		assertEquals(2, config.getEdgeFeature().getFilterAttributes().size());
		assertEquals("CATEGORIE", config.getEdgeFeature().getFilterAttributes().get(0));
		assertEquals("WATERLOOP", config.getEdgeFeature().getFilterAttributes().get(1));
		assertEquals("startPoint(the_geom)", config.getStartAttribute());
		assertEquals("endPoint(the_geom)", config.getEndAttribute());
		assertTrue(config.getNodeDataStore().isEmpty());
		assertNull(config.getNodeFeature());
		assertEquals(0.00001, config.getTolerance(), 0.0000001);
		
		assertEquals(1, configuration.getConnections().size());	
		ConnectionConfig conn = configuration.getConnections().get(0);
		assertEquals("riool", conn.getSourceNetwork());
		assertEquals("vha", conn.getTargetNetwork());
		assertEquals("vhas_code", conn.getReferenceAttribute());
		assertEquals(ConnectionType.PROJECTED, conn.getConnectionType());
		
		assertEquals(2, configuration.getAreas().size());	
		AreasConfig areas = configuration.getAreas().get(0);
		assertEquals("Risicogebieden", areas.getName());
		assertEquals("Risicogebied", areas.getNativeName());
		assertEquals("file:/my/dir/areas/Risicogebied.shp", 
				areas.getDataStore().get("url"));
		assertEquals(4, areas.getAttributes().size());
		assertEquals("Zone", areas.getAttributes().get(0));
		assertEquals("Oppervlakt", areas.getAttributes().get(1));
		assertEquals("EXTRA", areas.getAttributes().get(2));
		assertEquals("the_geom", areas.getAttributes().get(3));
	}
	
}
