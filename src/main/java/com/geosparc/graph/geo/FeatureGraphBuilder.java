/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.util.Converters;
import org.geotools.util.logging.Logging;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.geosparc.graph.base.DGraph;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId.Type;
import com.geosparc.gte.config.ConnectionConfig.ConnectionType;

import org.locationtech.jts.index.quadtree.Quadtree;
import org.locationtech.jts.linearref.LinearLocation;
import org.locationtech.jts.linearref.LocationIndexedLine;

/**
 * 
 * Helper class for building the feature graph.
 * 
 * @author Niels Charlier
 *
 */
public class FeatureGraphBuilder {
	
	public static final String GENERATED_VERTEX = "-generated-vertex";
	
	public static final String CONNECTION_EDGE = "connection-edge";
	
	public static final String CONNECTION_VERTEX = "connection" + GENERATED_VERTEX;

	private static final Logger LOGGER = Logging.getLogger(FeatureGraphBuilder.class);

	private static final GeometryFactory geomFac = new GeometryFactory();
	
	private static final FilterFactory2 filterFac = CommonFactoryFinder.getFilterFactory2();
		
	private DGraph<GlobalId, SimpleFeature, SimpleFeature> graph =
			new DGraph<>(true);

	private SimpleFeatureType simpleEdgeType;
	
	private Map<String, SimpleFeatureType> simpleVertexTypes =
			new HashMap<>();
	
	private CoordinateReferenceSystem crs;
	
	public DGraph<GlobalId, SimpleFeature, SimpleFeature> get() {
		return graph;
	}
	
	protected void setOrCheckCRS(CoordinateReferenceSystem newCrs) {
		if (crs == null) {
			crs = newCrs;
		}
		//TODO fails with vha/riool :/
		/*else {
			try {
				if (CRS.isTransformationRequired(crs, newCrs)) {
					throw new IllegalArgumentException("All coordinate reference systems must match!");
				}
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
	}
		
	/**
	 * Add vertices for logical network
	 * 
	 * @param network network name
	 * @param source the data source
	 * @param idExpression the identifier expression
	 * @param propNames the property names to include in result
	 * @throws IOException
	 */
	public void addVertices(String network, SimpleFeatureSource source, 
			Expression idExpression, List<String> propNames) throws IOException {
		setOrCheckCRS(source.getSchema().getCoordinateReferenceSystem());
		Query query = new Query();
		query.setPropertyNames(propNames);
        try (SimpleFeatureIterator iterator = source.getFeatures(query).features()) {        	
        	while (iterator.hasNext()) {
        		SimpleFeature sf = iterator.next();        		
        		String id = Converters.convert(idExpression.evaluate(sf), 
        				String.class);   
        		if (id == null) {
        			throw new IllegalArgumentException("id for node should not return null");
        		}         		
        		Idp<GlobalId, SimpleFeature> v = graph.addVertexById(new GlobalId(network, id));
        		if (v == null) {
        			throw new IllegalArgumentException("vertex id " + id + " is not unique.");
        		}
        		v.setData(sf);  
        	}
        }
	}
		
	/**
	 * Add edges for logical network 
	 * 
	 * @param network the network name
	 * @param source the data source
	 * @param idExpression the identifier expression
	 * @param fromExpression the "from" expression (reference to node)
	 * @param toExpression the "to" expression (reference to node)
	 * @param propNames the property names to include in result
	 * @throws IOException
	 */
	public void addLogicalEdges(String network, SimpleFeatureSource source, Expression idExpression,
			Expression fromExpression, Expression toExpression, 
			List<String> propNames) throws IOException {
		setOrCheckCRS(source.getSchema().getCoordinateReferenceSystem());		
		Query query = new Query();
		query.setPropertyNames(propNames);
        try (SimpleFeatureIterator iterator = source.getFeatures(query).features()) {        	
        	while (iterator.hasNext()) {
        		SimpleFeature sf = iterator.next();        		
        		String from = Converters.convert(fromExpression.evaluate(sf), 
        				String.class);        		      		
        		String to = Converters.convert(toExpression.evaluate(sf), 
        				String.class);  
        		String id = Converters.convert(idExpression.evaluate(sf), 
        				String.class);  
        		if (id == null) {
        			throw new IllegalArgumentException("id for edge should not return null");
        		}
        		if (from == null) {
        			throw new IllegalArgumentException("start attribute should not return null");
        		}
        		if (to == null) {
        			throw new IllegalArgumentException("end attribute should not return null");
        		}
        		if (from.equals(to)) {
    				LOGGER.log(Level.WARNING, "Found from node = to node for edge " + id,
    						". Skipping...");
    			} else {
	    			try {
		        		Idp<GlobalId, SimpleFeature> edge = graph.addEdgeById(
		        				new GlobalId(network, from), 
		        				new GlobalId(network, to), 
		        				new GlobalId(network, id));
		        		edge.setData(sf);
		        		graph.setEdgeWeight(edge, ((Geometry) sf.getDefaultGeometry()).getLength());
	    			} catch (IllegalArgumentException | NullPointerException e) {
	    				LOGGER.log(Level.SEVERE, "Failed to create edge " + id, e);
	    			}
    			}
        	}
        }
	}
	
	/**
	 * Add edges for geographical network (nodes are automatically generated)
	 * 
	 * @param network the network name
	 * @param source the data source
	 * @param idExpression the identifier expression
	 * @param fromExpression the "from" expression (geometry)
	 * @param toExpression the "to" expression (geometry)
	 * @param propNames the property names
	 * @param tolerance the tolerance (max distance between nodes that can be merged to one)
	 * @throws IOException
	 */
	public void addGeographicalEdges(String network, SimpleFeatureSource source, 
			Expression idExpression, Expression fromExpression, Expression toExpression, 
			List<String> propNames, double tolerance) throws IOException {
		setOrCheckCRS(source.getSchema().getCoordinateReferenceSystem());
		Map<GlobalId, SimpleFeature> edges = new HashMap<>();
		Map<GlobalId, GlobalId> fromNodes = new HashMap<>();
		Map<GlobalId, GlobalId> toNodes = new HashMap<>();
		Multimap<GlobalId, GlobalId> fromNodesRev = LinkedListMultimap.create();
		Multimap<GlobalId, GlobalId> toNodesRev = LinkedListMultimap.create();
		Quadtree quadTree = new Quadtree();
		
		Query query = new Query();
		query.setPropertyNames(propNames);
        try (SimpleFeatureIterator iterator = source.getFeatures(query).features()) {        	
        	while (iterator.hasNext()) {        			
        		SimpleFeature sf = iterator.next();
        		GlobalId edgeId = new GlobalId(network,
        				Converters.convert(idExpression.evaluate(sf), 
                				String.class));		
        		edges.put(edgeId, sf);
        		
        		Geometry fromBuffered = Converters.convert(fromExpression.evaluate(sf), 
    					Geometry.class).buffer(tolerance);
        		quadTree.insert(fromBuffered.getEnvelopeInternal(), edgeId);
        	}
        }
        
        for (Entry<GlobalId, SimpleFeature> edgeEntry : edges.entrySet()) {
        	GlobalId id = edgeEntry.getKey();
        	SimpleFeature feature = edgeEntry.getValue();
        	Geometry toBuffered = Converters.convert(
        			toExpression.evaluate(feature), 
					Geometry.class).buffer(tolerance);        	
        	for (Object o : quadTree.query(toBuffered.getEnvelopeInternal())) {
        		GlobalId otherId = (GlobalId) o;
        		if (!otherId.equals(id)) {
        			SimpleFeature otherFeature = edges.get(otherId);
        			Geometry from = Converters.convert(
                			fromExpression.evaluate(otherFeature), 
        					Geometry.class);
        			if (from.within(toBuffered)) {
        				//we have a match
        				GlobalId toNodeId = toNodes.get(id);
        				GlobalId fromNodeId = fromNodes.get(otherId);
        				if (toNodeId == null && fromNodeId == null) {
        					GlobalId nodeId = 
        							new GlobalId(network,
        	        				UUID.randomUUID().toString(),
    								Type.GENERATED);
        					graph.addVertexById(nodeId)
        						.setData(generateNode(network,
        								nodeId.getIdentifier(),
        								(Geometry) feature.getDefaultGeometry(), true));
        					toNodes.put(id, nodeId);
        					toNodesRev.put(nodeId, id);
        					fromNodes.put(otherId, nodeId);
        					fromNodesRev.put(nodeId, otherId);
        				} else if (toNodeId == null) {
        					toNodes.put(id, fromNodeId);
        					toNodesRev.put(fromNodeId, id);
        				} else if (fromNodeId == null) {
        					fromNodes.put(otherId, toNodeId);
        					fromNodesRev.put(toNodeId, otherId);
        				} else if (!fromNodeId.equals(toNodeId)) {
        					graph.removeVertexById(fromNodeId);
        					for (GlobalId someId : fromNodesRev.get(fromNodeId)) {
            					fromNodes.put(someId, toNodeId);
            					fromNodesRev.put(toNodeId, someId);
        					}
        					fromNodesRev.removeAll(fromNodeId);
        					for (GlobalId someId : toNodesRev.get(fromNodeId)) {
            					toNodes.put(someId, toNodeId);
            					toNodesRev.put(toNodeId, someId);
        					}
        					toNodesRev.removeAll(fromNodeId);
        				}
        			} 
        		}
        	}
        }
        
        for (Entry<GlobalId, SimpleFeature> edgeEntry : edges.entrySet()) { 
        	GlobalId id = edgeEntry.getKey();
        	SimpleFeature feature = edgeEntry.getValue();
        	GlobalId toNodeId = toNodes.get(id);
			if (toNodeId == null) {
				toNodeId = new GlobalId(network,
        				UUID.randomUUID().toString(),
						Type.GENERATED);
				graph.addVertexById(toNodeId)
					.setData(generateNode(network, toNodeId.getIdentifier(),
						(Geometry) feature.getDefaultGeometry(), true));
			} 
        	GlobalId fromNodeId = fromNodes.get(id);
			if (fromNodeId == null) {
				fromNodeId = new GlobalId(network,
        				UUID.randomUUID().toString(),
						Type.GENERATED);
				graph.addVertexById(fromNodeId)
					.setData(generateNode(network, fromNodeId.getIdentifier(),
						(Geometry) feature.getDefaultGeometry(), false));
			} 
			if (fromNodeId.equals(toNodeId)) {
				LOGGER.log(Level.WARNING, "Found from node = to node for edge " + id.getIdentifier(),
						", is your tolerance to high? Skipping...");
			} else {
				try {
					Idp<GlobalId, SimpleFeature> edge = graph.addEdgeById(fromNodeId, toNodeId, id);
					edge.setData(feature);
	        		graph.setEdgeWeight(edge, 
	        				((Geometry) edgeEntry.getValue().getDefaultGeometry()).getLength());
				} catch (IllegalArgumentException | NullPointerException e) {
    				LOGGER.log(Level.SEVERE, "Failed to create edge " + id, e);
    			}
			}
        }
	}
	
	protected SimpleFeature generateNode(String network,
			String id, Geometry edgeGeometry, boolean end) {
		LocationIndexedLine lil = new LocationIndexedLine(edgeGeometry);
		return generateNode(network + GENERATED_VERTEX, id, geomFac.createPoint(
				lil.extractPoint(
						end? lil.getEndIndex() : lil.getStartIndex())));
	}
	
	protected SimpleFeature generateNode(String typeName, String id, Geometry nodeGeometry) {		
		SimpleFeatureType simpleVertexType = simpleVertexTypes.get(typeName);
		if (simpleVertexType  == null) {
			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
			typeBuilder.add("geom", Point.class, crs);
			typeBuilder.setName(typeName);
			simpleVertexType = typeBuilder.buildFeatureType();
			simpleVertexTypes.put(typeName, simpleVertexType);
		}
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(simpleVertexType);
		builder.add(nodeGeometry);
		return builder.buildFeature(id);
	}
	
	/**
	 * Add a connection between to networks, using reference
	 * 
	 * @param sourceNetwork source network
	 * @param targetNetwork target network
	 * @param refExpression the reference expression
	 * @param connectionType the connection type
	 * @throws IOException
	 */
	public void addConnection(String sourceNetwork, String targetNetwork, Expression refExpression,
			ConnectionType connectionType) throws IOException {
		
		List<Idp<GlobalId, SimpleFeature>> vertices
			= new ArrayList<>(graph.vertexSet());
		for (Idp<GlobalId, SimpleFeature> vertex : vertices) {
			if (vertex.getId().getNetwork().equals(sourceNetwork)) {
				String ref = Converters.convert(refExpression.evaluate(vertex.getData()), 
						String.class);
				String edgeId = 
						vertex.getId().getNetwork() + "." +
						vertex.getId().getIdentifier() + "-" + 
						ref;
				if (ref == null) {
					// do nothing
					// don't have to log, this is okay.
				} else if (connectionType == null) { //node to node
					Idp<GlobalId, SimpleFeature> otherVertex
						= graph.getVertexById(new GlobalId(targetNetwork, ref));
					if (otherVertex == null) {
						LOGGER.log(Level.WARNING, "Reference " + ref + " in node " 
						 + vertex.toString() + " missing in network " + targetNetwork
						 + ", skipping...");
					} else {
						graph.addEdgeById(vertex.getId(), otherVertex.getId(), 
								new GlobalId(sourceNetwork,
								UUID.randomUUID().toString(),
								Type.GENERATED));
					}
				} else { //node to edge
					Idp<GlobalId, SimpleFeature> edge =
							graph.getEdgeById(new GlobalId(targetNetwork, ref));
					if (edge == null) {
						LOGGER.log(Level.WARNING, "Reference " + ref + " in node " 
						 + vertex.toString() + " missing in network " + targetNetwork
						 + ", skipping...");
					} else {
						Idp<GlobalId, SimpleFeature> otherVertex;
						Point destination;
						
						Geometry linear = (Geometry) edge.getData().getDefaultGeometry();
						if (!(linear instanceof LineString || linear instanceof MultiLineString)) {
							LOGGER.log(Level.SEVERE, "Geometry for edge " + edge.toString() + 
									" is not linear.");
							continue;
						}
						LocationIndexedLine lil = new LocationIndexedLine(linear);
						Geometry point = (Geometry) vertex.getData().getDefaultGeometry();
						if (!(point instanceof Point)) {
							LOGGER.log(Level.SEVERE, "Geometry for node " + vertex.toString() + 
									" is not a point.");
							continue;
						}
						
						switch (connectionType) {
						case START:
							otherVertex = graph.getEdgeSource(edge);
							destination = geomFac.createPoint(lil.extractPoint(
									lil.getStartIndex()));
							break;	
						case END:
							otherVertex = graph.getEdgeTarget(edge);
							destination = geomFac.createPoint(lil.extractPoint(
									lil.getEndIndex()));
							break;
						default:
							LinearLocation destinationIndex = 
									lil.project(point.getCoordinate());
							destination = geomFac.createPoint(
									lil.extractPoint(destinationIndex));
							Geometry connectingEdgeGeometry =
									lil.extractLine(destinationIndex, lil.getEndIndex());
							GlobalId nodeId = new GlobalId(sourceNetwork,
									UUID.randomUUID().toString(),
									Type.GENERATED);
							otherVertex = graph.addVertexById(nodeId);
							otherVertex.setData(generateNode(CONNECTION_VERTEX,
											nodeId.getIdentifier(), 
											destination));
							try {
								String secondEdgeId = edgeId + "+";
								Idp<GlobalId, SimpleFeature> secondConnectingEdge = 
										graph.addEdgeById(otherVertex.getId(), graph.getEdgeTarget(edge).getId(),
										new GlobalId(targetNetwork, secondEdgeId,
												Type.GENERATED));
								secondConnectingEdge.setData(createPartialEdge(secondEdgeId, 
										connectingEdgeGeometry, edge.getData()));
								graph.setEdgeWeight(secondConnectingEdge, 
										connectingEdgeGeometry.getLength());
							} catch (IllegalArgumentException e) {
			    				LOGGER.log(Level.SEVERE, "Failed to create additional connecting edge for " + vertex.getId(), e);
			    				continue;
							}
							break;
						}
						try {
							Idp<GlobalId, SimpleFeature> connectingEdge = 
									graph.addEdgeById(vertex.getId(), otherVertex.getId(), 
									new GlobalId(targetNetwork, edgeId,
											Type.GENERATED));
							connectingEdge.setData(createConnectingEdge(edgeId, 
									 point.getCoordinate(), destination.getCoordinate()));
							double length = ((Geometry) vertex.getData().getDefaultGeometry())
									.distance(destination);
							graph.setEdgeWeight(connectingEdge, length);
						} catch (IllegalArgumentException e) {
		    				LOGGER.log(Level.SEVERE, "Failed to create connecting edge for " + vertex.getId(), e);
		    			}
					}
				} 
			}
		}
		
	}
	
	/**
	 * Add a connection between networks, without reference
	 * (type is always PROJECTED)
	 * 
	 * @param sourceNetwork the source network
	 * @param targetNetwork the target network
	 * @throws IOException
	 */
	public void addConnection(String sourceNetwork, String targetNetwork) throws IOException {
		List<Idp<GlobalId, SimpleFeature>> vertices = new ArrayList<>(graph.vertexSet());
		List<Idp<GlobalId, SimpleFeature>> edges = new ArrayList<>(graph.edgeSet());
		for (Idp<GlobalId, SimpleFeature> vertex : vertices) {
			if (vertex.getId().getNetwork().equals(sourceNetwork) 
					&& graph.outgoingEdgesOf(vertex).isEmpty()) {
				double shortestDistance = Double.MAX_VALUE;
				Idp<GlobalId, SimpleFeature> closestEdge = null;
				for (Idp<GlobalId, SimpleFeature> edge : edges) {
					if (edge.getId().getNetwork().equals(targetNetwork)) {
						Expression exprDistance = filterFac.function("distance", 
								filterFac.property(edge.getData().getType().getGeometryDescriptor().getLocalName()),
								filterFac.literal(vertex.getData().getDefaultGeometry()));						
						Double distance = (Double) exprDistance.evaluate(edge.getData());
						if (distance < shortestDistance) {
							shortestDistance = distance;
							closestEdge = edge;
						}
					}
				}

				String edgeId = vertex.getId().getNetwork() + "." + vertex.getId().getIdentifier() + "-" + 
						closestEdge.getId().getIdentifier();

				Idp<GlobalId, SimpleFeature> otherVertex;
				Point destination;

				Geometry linear = (Geometry) closestEdge.getData().getDefaultGeometry();
				if (!(linear instanceof LineString || linear instanceof MultiLineString)) {
					LOGGER.log(Level.SEVERE, "Geometry for edge " + closestEdge.toString() + " is not linear.");
					continue;
				}
				LocationIndexedLine lil = new LocationIndexedLine(linear);
				Geometry point = (Geometry) vertex.getData().getDefaultGeometry();
				if (!(point instanceof Point)) {
					LOGGER.log(Level.SEVERE, "Geometry for node " + vertex.toString() + " is not a point.");
					continue;
				}

				LinearLocation destinationIndex = lil.project(point.getCoordinate());
				destination = geomFac.createPoint(lil.extractPoint(destinationIndex));
				Geometry connectingEdgeGeometry = lil.extractLine(destinationIndex, lil.getEndIndex());
				GlobalId nodeId = new GlobalId(sourceNetwork, UUID.randomUUID().toString(), Type.GENERATED);
				otherVertex = graph.addVertexById(nodeId);
				otherVertex.setData(generateNode(CONNECTION_VERTEX, nodeId.getIdentifier(), destination));
				try {
					String secondEdgeId = edgeId + "+";
					Idp<GlobalId, SimpleFeature> secondConnectingEdge = graph.addEdgeById(otherVertex.getId(),
							graph.getEdgeTarget(closestEdge).getId(),
							new GlobalId(targetNetwork, secondEdgeId, Type.GENERATED));
					secondConnectingEdge
							.setData(createPartialEdge(secondEdgeId, connectingEdgeGeometry, closestEdge.getData()));
					graph.setEdgeWeight(secondConnectingEdge, connectingEdgeGeometry.getLength());
				} catch (IllegalArgumentException e) {
					LOGGER.log(Level.SEVERE, "Failed to create additional connecting edge for " + vertex.getId(), e);
					continue;
				}
				try {
					Idp<GlobalId, SimpleFeature> connectingEdge = graph.addEdgeById(vertex.getId(), otherVertex.getId(),
							new GlobalId(targetNetwork, edgeId, Type.GENERATED));
					connectingEdge
							.setData(createConnectingEdge(edgeId, point.getCoordinate(), destination.getCoordinate()));
					double length = ((Geometry) vertex.getData().getDefaultGeometry()).distance(destination);
					graph.setEdgeWeight(connectingEdge, length);
				} catch (IllegalArgumentException e) {
					LOGGER.log(Level.SEVERE, "Failed to create connecting edge for " + vertex.getId(), e);
				}
			}
		}

	}
	
	protected SimpleFeature createConnectingEdge(String id, Coordinate source,
			Coordinate destination) {
		return createConnectingEdge(id, 
				geomFac.createLineString(
						new Coordinate[] {source, destination}));
	}
	
	protected SimpleFeature createConnectingEdge(String id, Geometry connectingGeometry) {
		if (simpleEdgeType == null) {
			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
			typeBuilder.add("geom", LineString.class, crs);
			typeBuilder.setName(CONNECTION_EDGE);
			simpleEdgeType = typeBuilder.buildFeatureType();
		}
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(simpleEdgeType);
		builder.add(connectingGeometry);
		return builder.buildFeature(id);
	}
	
	protected SimpleFeature createPartialEdge(String id, Geometry connectingGeometry,
			SimpleFeature original) {
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(original.getFeatureType());
		for (Property prop : original.getProperties()) {
			if (prop.getDescriptor().equals(
					original.getType().getGeometryDescriptor())) {
				builder.add(connectingGeometry);
			} else {
				builder.add(prop.getValue());
			}
		}
		return builder.buildFeature(id);
	}

}
