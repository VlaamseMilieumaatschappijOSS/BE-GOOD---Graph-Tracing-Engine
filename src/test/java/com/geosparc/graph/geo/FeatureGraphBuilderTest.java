package com.geosparc.graph.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.base.Idp;
import com.geosparc.gte.TestData;
import com.geosparc.gte.config.ConnectionConfig.ConnectionType;

public class FeatureGraphBuilderTest {
	
	private FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2();
	
	@Test
	public void testLogicalNetwork() throws Exception {
		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		
		graphBuilder.addVertices("test", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		

		graphBuilder.addLogicalEdges("test", 
				TestData.getLogicalEdges(), 
				fac.property("id"),
				fac.property("from"),
				fac.property("to"),
				null);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		assertEquals(3, graph.vertexSet().size());
		assertEquals(3, graph.edgeSet().size());
		
		Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("test", "1"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(2, set.size());
		Idp<GlobalId, SimpleFeature> edge12 = graph.getEdgeById(new GlobalId("test", "12"));
		Idp<GlobalId, SimpleFeature> edge13 = graph.getEdgeById(new GlobalId("test", "13"));
		assertTrue(set.contains(edge12));
		assertTrue(set.contains(edge13));
		assertEquals(4.2426, graph.getEdgeWeight(edge12), 0.0001);
		assertEquals(7.0711, graph.getEdgeWeight(edge13), 0.0001);
	}
	
	@Test
	public void testGeographicalNetwork() throws Exception {
		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		
		graphBuilder.addGeographicalEdges("test", 
				TestData.getGeoEdges(), 
				fac.property("id"),
				fac.function("startPoint", fac.property("geom")),
				fac.function("endPoint", fac.property("geom")),
				null, 0.001);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		Idp<GlobalId, SimpleFeature> edgeA = graph.getEdgeById(new GlobalId("test", "a"));
		Idp<GlobalId, SimpleFeature> vertex = graph.getEdgeTarget(edgeA);
		assertNotNull(vertex.getData());
		assertEquals("test-generated-vertex", vertex.getData().getType().getName().getLocalPart());
		assertNotNull(vertex.getData().getDefaultGeometry());
		assertEquals(new Coordinate(3.0, 3.0),
				((Geometry) vertex.getData().getDefaultGeometry()).getCoordinate());
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edgeB = graph.getEdgeById(new GlobalId("test", "b"));
		assertTrue(set.contains(edgeB));
		assertEquals(4.2426, graph.getEdgeWeight(edgeA), 0.0001);
	}
	
	@Test
	public void testConnectionLogicalToLogical() throws Exception {		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		graphBuilder.addVertices("test1", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		
		graphBuilder.addVertices("test2", 
				TestData.getLogicalNodes2(), 
				fac.property("id"),
				null);		
		graphBuilder.addConnection("test1", "test2", 
				fac.property("ref"), null);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("test1", "3"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edge = set.iterator().next();
		vertex = graph.getEdgeTarget(edge);
		assertEquals(graph.getVertexById(new GlobalId("test2", "c")), vertex);
		assertEquals(1.0, graph.getEdgeWeight(edge), 0.00001);
	}
	
	@Test
	public void testConnectionLogicalToGeoEnd() throws Exception {		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		graphBuilder.addVertices("testL", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		
		graphBuilder.addGeographicalEdges("testG", 
				TestData.getGeoEdges(), 
				fac.property("id"),
				fac.function("startPoint", fac.property("geom")),
				fac.function("endPoint", fac.property("geom")),
				null, 0.001);
		graphBuilder.addConnection("testL", "testG", 
				fac.property("ref"), ConnectionType.END);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("testL", "3"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edge = set.iterator().next();
		vertex = graph.getEdgeTarget(edge);
		set = graph.incomingEdgesOf(vertex);
		assertEquals(2, set.size());
		assertTrue(set.contains(graph.getEdgeById(new GlobalId("testG", "c"))));
		assertEquals(3.16227, graph.getEdgeWeight(edge), 0.00001);
	}
	
	@Test
	public void testConnectionLogicalToGeoStart() throws Exception {		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		graphBuilder.addVertices("testL", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		
		graphBuilder.addGeographicalEdges("testG", 
				TestData.getGeoEdges(), 
				fac.property("id"),
				fac.function("startPoint", fac.property("geom")),
				fac.function("endPoint", fac.property("geom")),
				null, 0.001);
		graphBuilder.addConnection("testL", "testG", 
				fac.property("ref"), ConnectionType.START);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("testL", "3"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edge = set.iterator().next();
		vertex = graph.getEdgeTarget(edge);
		set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		assertTrue(set.contains(graph.getEdgeById(new GlobalId("testG", "c"))));
		assertEquals(3.16354, graph.getEdgeWeight(edge), 0.00001);
	}
	
	@Test
	public void testConnectionLogicalToGeoProjected() throws Exception {		
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		graphBuilder.addVertices("testL", 
				TestData.getLogicalNodes(), 
				fac.property("id"),
				null);		
		graphBuilder.addGeographicalEdges("testG", 
				TestData.getGeoEdges(), 
				fac.property("id"),
				fac.function("startPoint", fac.property("geom")),
				fac.function("endPoint", fac.property("geom")),
				null, 0.001);
		graphBuilder.addConnection("testL", "testG", 
				fac.property("ref"), ConnectionType.PROJECTED);
		
		DGraph<GlobalId, SimpleFeature, SimpleFeature> graph = graphBuilder.get();
		
		Idp<GlobalId, SimpleFeature> vertex = graph.getVertexById(new GlobalId("testL", "3"));
		Set<Idp<GlobalId, SimpleFeature>> set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		Idp<GlobalId, SimpleFeature> edge = set.iterator().next();
		assertEquals("testL.3-c", edge.getId().getIdentifier());
		assertEquals(new Coordinate(2.0, 6.0),
				((Geometry) edge.getData().getDefaultGeometry()).getCoordinate());
		assertEquals(2.82913, graph.getEdgeWeight(edge), 0.00001);
		vertex = graph.getEdgeTarget(edge);
		set = graph.outgoingEdgesOf(vertex);
		assertEquals(1, set.size());
		edge = set.iterator().next();
		assertEquals("testL.3-c+", edge.getId().getIdentifier());
		assertEquals(4.00,
				((Geometry) edge.getData().getDefaultGeometry()).getCoordinate().getX(),
				0.01);
		assertEquals(4.00,
				((Geometry) edge.getData().getDefaultGeometry()).getCoordinate().getY(),
				0.01);
		vertex = graph.getEdgeTarget(edge);
		assertNotNull(vertex.getData());
		assertEquals("testG-generated-vertex", vertex.getData().getType().getName().getLocalPart());
		assertNotNull(vertex.getData().getDefaultGeometry());
		assertEquals(new Coordinate(5.0, 5.0),
				((Geometry) vertex.getData().getDefaultGeometry()).getCoordinate());
		assertEquals(1.41279, graph.getEdgeWeight(edge), 0.00001);
		set = graph.incomingEdgesOf(vertex);
		assertEquals(2, set.size());
		assertTrue(set.contains(graph.getEdgeById(new GlobalId("testG", "c"))));
	}

}
