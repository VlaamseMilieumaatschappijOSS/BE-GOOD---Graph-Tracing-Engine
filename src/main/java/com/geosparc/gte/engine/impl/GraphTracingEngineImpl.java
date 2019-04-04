/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.engine.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.util.logging.Logging;
import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.FeatureGraphAggregator;
import com.geosparc.graph.geo.FeatureGraphBuilder;
import com.geosparc.graph.geo.FeatureGraphRetyper;
import com.geosparc.graph.geo.FeatureGraphTracer;
import com.geosparc.graph.geo.GlobalId;
import com.geosparc.gte.config.AggregateConfig;
import com.geosparc.gte.config.AreasConfig;
import com.geosparc.gte.config.ConnectionConfig;
import com.geosparc.gte.config.GteConfig;
import com.geosparc.gte.config.NetworkConfig;
import com.geosparc.gte.config.NetworkConfig.NodeType;
import com.geosparc.gte.engine.GraphTracingEngine;
import com.geosparc.gte.engine.GraphTracingResult;

/**
 * 
 * Graph Tracing Enginge. Service that manages the graph, rebuilding and tracing.
 * 
 * @author Niels Charlier
 *
 */
@Service
public class GraphTracingEngineImpl implements GraphTracingEngine {
	
	private static final Logger LOGGER = Logging.getLogger(GraphTracingEngineImpl.class);
	
	private static final FilterFactory2 fac = CommonFactoryFinder.getFilterFactory2();
	
	@Autowired
	private GteConfig config;
	
	private DGraph<GlobalId, SimpleFeature, SimpleFeature> graph;
	
	private List<String> networks;
	
	@EventListener(ApplicationReadyEvent.class) 
	public void start() {
		config.validate();
		load();
	}

	@Scheduled(cron = "${frequency}")
	public void reload() {
		synchronized(graph) {
			load();
		}
	}
	
	/**
	 * Load the graph from the database
	 */
	protected void load() {
		LOGGER.info("Building network...");
		FeatureGraphBuilder graphBuilder = new FeatureGraphBuilder();
		networks = new ArrayList<String>();
		for (NetworkConfig networkConfig : config.getNetworks()) {
			networks.add(networkConfig.getName());
			addNetwork(graphBuilder, networkConfig);
		}
		for (ConnectionConfig connConfig : config.getConnections()) {
			addConnection(graphBuilder, connConfig);
		}
		graph = graphBuilder.get();
		LOGGER.info("Successfully built network from data sources, " +
						graph.vertexSet().size() + " nodes, " +
						graph.edgeSet().size() + " edges.");
	}
	
	/**
	 * Add a network to the graph that is being built.
	 * 
	 * @param graphBuilder the current graph builder
	 * @param networkConfig the network config to add
	 */
	protected void addNetwork(FeatureGraphBuilder graphBuilder, NetworkConfig networkConfig) {		
		if (networkConfig.getNodeType() == NodeType.LOGICAL) {
			try {
				DataStore edgeStore = DataStoreFinder.getDataStore(networkConfig.getEdgeDataStore());
				DataStore nodeStore = networkConfig.getNodeDataStore() == null ? edgeStore :
						DataStoreFinder.getDataStore(networkConfig.getNodeDataStore());
				
				if (edgeStore == null) {
					throw new IllegalStateException("Failed to find edge datastore for network " + networkConfig.getName());
				}
				if (nodeStore == null) {
					throw new IllegalStateException("Failed to find node datastore for network " + networkConfig.getName());
				}
				
				graphBuilder.addVertices(networkConfig.getName(),
						nodeStore.getFeatureSource(networkConfig.getNodeFeature().getName()), 
						fac.property(networkConfig.getNodeFeature().getIdAttribute()), 
						networkConfig.getNodeFeature().getAllAttributes());
				
				graphBuilder.addLogicalEdges(networkConfig.getName(),
						edgeStore.getFeatureSource(networkConfig.getEdgeFeature().getName()), 
						fac.property(networkConfig.getEdgeFeature().getIdAttribute()),
						fac.property(networkConfig.getStartAttribute()), 
						fac.property(networkConfig.getEndAttribute()), 
						networkConfig.getEdgeFeature().getAllAttributes());
				
				edgeStore.dispose();
				nodeStore.dispose();
				
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to add network " + networkConfig.getName(), e);
			}
		} else {
			try {
				DataStore edgeStore = DataStoreFinder.getDataStore(networkConfig.getEdgeDataStore());
				
				if (edgeStore == null) {
					throw new IllegalStateException("Failed to find edge datastore for network " + networkConfig.getName());
				}
				
				graphBuilder.addGeographicalEdges(networkConfig.getName(),
						edgeStore.getFeatureSource(networkConfig.getEdgeFeature().getName()), 
						fac.property(networkConfig.getEdgeFeature().getIdAttribute()),
						CQL.toExpression(networkConfig.getStartAttribute()), 
						CQL.toExpression(networkConfig.getEndAttribute()), 
						networkConfig.getEdgeFeature().getAllAttributes(), 
						networkConfig.getTolerance());
				
				edgeStore.dispose();
				
			} catch (IOException | CQLException e) {
				LOGGER.log(Level.SEVERE, "Failed to add network " + networkConfig.getName(), e);
			}
		}
	}
	
	/**
	 * Add a connection to the graph that is being built.
	 * 
	 * @param graphBuilder the current graph builder
	 * @param connConfig the connection configuration to be added
	 */
	protected void addConnection(FeatureGraphBuilder graphBuilder, ConnectionConfig connConfig) {		
		try {
			if (connConfig.getReferenceAttribute() == null) {
				graphBuilder.addConnection(connConfig.getSourceNetwork(), connConfig.getTargetNetwork());
			} else {
				graphBuilder.addConnection(connConfig.getSourceNetwork(), connConfig.getTargetNetwork(),
					CQL.toExpression(connConfig.getReferenceAttribute()), 
					connConfig.getConnectionType());
			}
		} catch (CQLException | IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to add connection " + connConfig.getSourceNetwork() + " to "
					+ connConfig.getTargetNetwork(), e);
		}
	}
	

	@Override
	public DGraph<GlobalId, SimpleFeature, SimpleFeature> getGraph() {
		synchronized (graph) {
			return graph;
		}
	}

	
	@Override
	public GraphTracingResult trace(GlobalId startNode, Double maxDistance,
			List<String> networks, List<String> nodeFilters, List<String> edgeFilters,
					List<Double> maxDistances, List<List<String>> edgeAggregatedAtts,
					boolean upstream, boolean includeOverlappingAreas,
					Long limit) 
					throws CQLException {
		synchronized (graph) {
			FeatureGraphTracer tracer = new FeatureGraphTracer(graph, startNode, upstream, limit);
			
			if (networks.size() != nodeFilters.size()) {
				throw new IllegalArgumentException();
			}
			
			if (networks.size() != edgeFilters.size()) {
				throw new IllegalArgumentException();
			}
	
			if (networks.size() != maxDistances.size()) {
				throw new IllegalArgumentException();
			}
	
			if (networks.size() != edgeAggregatedAtts.size()) {
				throw new IllegalArgumentException();
			}
			
			if (maxDistance != null) {
				tracer.setMaximumDistance(maxDistance);
			}
			
			for (int i = 0; i < networks.size(); i++) {
				tracer.addNetwork(networks.get(i));
				
				if (nodeFilters.get(i) != null) {
					tracer.setVertexFilter(networks.get(i), 
							CQL.toFilter(nodeFilters.get(i)));
				}
	
				if (edgeFilters.get(i) != null) {
					tracer.setEdgeFilter(networks.get(i), 
							CQL.toFilter(edgeFilters.get(i)));
				}
				
				if (maxDistances.get(i) != null) {
					tracer.setMaximumDistance(networks.get(i), maxDistances.get(i));
				}
	
			}
			
			Graph<Idp<GlobalId, SimpleFeature>, 
				Idp<GlobalId, SimpleFeature>> trace = tracer.trace();
												
			Map<String, Map<GlobalId, Object>> aggregates = new HashMap<>();
			for (int i = 0; i < edgeAggregatedAtts.size(); i++) {
				NetworkConfig networkConfig = config.findNetworkByName(networks.get(i));
				if (networkConfig != null && networkConfig.getEdgeFeature().getAggregatedAttributes() != null) {
					for (AggregateConfig agg : networkConfig.getEdgeFeature().getAggregatedAttributes()) {
						if (edgeAggregatedAtts.get(i) == null || edgeAggregatedAtts.get(i).contains(agg.getTarget())) {
							aggregates.put(agg.getTarget(),
									new FeatureGraphAggregator(trace, tracer.orderEdges(trace),
											networkConfig.getEdgeFeature().getName(),
											agg.getSource(), FeatureGraphAggregator.Method.valueOf(agg.getMethod()))
													.aggregate());
						}
					}
				}
			}
			
			// filter out connecting edges at the end of paths
			filterLooseEnds(trace);
			
			FeatureGraphRetyper retyper = new FeatureGraphRetyper(trace);
			for (NetworkConfig networkConfig : config.getNetworks()) {
				retyper.setEdgeProperties(networkConfig.getName(), 
						networkConfig.getEdgeFeature().getUserAttributes());
				if (networkConfig.getNodeFeature() != null) {
					retyper.setVertexProperties(networkConfig.getName(), 
						networkConfig.getNodeFeature().getUserAttributes());
				}
			}		
			trace = retyper.process();
			
			Map<GlobalId, Double> distances = new HashMap<>();
			for(Idp<GlobalId, SimpleFeature> vertex : trace.vertexSet()) {
				if (!vertex.getId().equals(startNode)) {
					distances.put(vertex.getId(), tracer.getDistance(vertex));
				}
			}
			
			return new GraphTracingResultImpl(trace, distances, aggregates,
					includeOverlappingAreas ? findOverlappingAreas(trace) : null,
					tracer.orderEdges(trace),
					tracer.orderVertices(trace),
					tracer.isLimitReached());
		}
	}
	
	/**
	 * Filter out connection edges at the ends of a trace result
	 * 
	 * @param trace the result of a trace
	 */
	protected void filterLooseEnds(Graph<Idp<GlobalId, SimpleFeature>, 
				Idp<GlobalId, SimpleFeature>> trace) {
		List<Idp<GlobalId, SimpleFeature>> edgesToBeRemoved = new ArrayList<>();
		List<Idp<GlobalId, SimpleFeature>> verticesToBeRemoved = new ArrayList<>();
		
		for (Idp<GlobalId, SimpleFeature> edge : trace.edgeSet()) {
			if (edge.getData() != null
				&& edge.getData().getFeatureType().getName().getLocalPart().equals(
						FeatureGraphBuilder.CONNECTION_EDGE)) {
				Idp<GlobalId, SimpleFeature> targetVertex =
						trace.getEdgeTarget(edge);
				if (trace.outgoingEdgesOf(targetVertex).isEmpty()) {
					edgesToBeRemoved.add(edge);
					verticesToBeRemoved.add(targetVertex);

					// we don't want to end with a connection vertex either
					Idp<GlobalId, SimpleFeature> sourceVertex =
							trace.getEdgeSource(edge);
					if (sourceVertex.getData() != null
							&& sourceVertex.getData().getFeatureType().getName().getLocalPart().equals(
									FeatureGraphBuilder.CONNECTION_VERTEX)) {
						verticesToBeRemoved.add(sourceVertex);
						edgesToBeRemoved.addAll(trace.incomingEdgesOf(sourceVertex));
					}
				}
			}
		}
		
		trace.removeAllEdges(edgesToBeRemoved);
		trace.removeAllVertices(verticesToBeRemoved);
	}
	
	/**
	 * Find overlapping areas for a trace
	 * 
	 * @param trace the result of a trace
	 * @return the overlapping areas mapped per network
	 */
	protected Map<String, List<SimpleFeature>> 
		findOverlappingAreas(Graph<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>> trace) {
		
		Map<String, List<SimpleFeature>> result = new HashMap<>();
		
		for (AreasConfig areasConfig : config.getAreas()) {
			DataStore store;
			try {
				store = DataStoreFinder.getDataStore(areasConfig.getDataStore());
				
				if (store == null) {
					throw new IllegalStateException("Failed to find datastore for areas " + 
							areasConfig.getName());
				}
				
				SimpleFeatureSource source = store.getFeatureSource(
						areasConfig.getNativeName() == null ? areasConfig.getName() :
								areasConfig.getNativeName());
				
				List<Filter> filters = new ArrayList<>();				
				for (Idp<GlobalId, SimpleFeature> edge : trace.edgeSet()) {
					filters.add(fac.intersects(
							fac.property(source.getSchema().getGeometryDescriptor().getLocalName()),
							fac.literal(edge.getData().getDefaultGeometry())));
				}

				Query query = new Query();
				query.setPropertyNames(areasConfig.getAttributes());
				query.setFilter(fac.or(filters));
				SimpleFeatureIterator it
					= source.getFeatures(query).features();

				List<SimpleFeature> list = new ArrayList<>();
				while (it.hasNext()) {
					list.add(it.next());
				}
				result.put(areasConfig.getName(), list);
				
				it.close();
				store.dispose();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		
		return result;
	}

	@Override
	public List<String> getNetworks() {
		return networks;
	}
	

}
