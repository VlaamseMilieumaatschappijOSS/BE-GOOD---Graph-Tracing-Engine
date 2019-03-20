/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.EdgeReversedGraph;
import org.jgrapht.graph.MaskSubgraph;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;

import com.geosparc.graph.alg.Tracing;
import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.base.Idp;

/**
 * Helper class for tracing.
 * 
 * @author Niels Charlier
 *
 */
public class FeatureGraphTracer {
	
	private DGraph<GlobalId, SimpleFeature, SimpleFeature> graph;
	
	private Set<String> networks = new HashSet<>();
	
	private Map<String, Double> maxDistances = new HashMap<>();
	
	private Double maxDistance;
	
	private Map<String, Filter> vertexFilters = new HashMap<>();
	
	private Map<String, Filter> edgeFilters = new HashMap<>();
	
	private GlobalId source;
	
	private boolean upstream;
	
	private long limit;
	
	private boolean limitReached;
		
	private final Predicate<Idp<GlobalId, SimpleFeature>> edgePredicate =
			e -> {
				if (!networks.contains(e.getId().getNetwork())) {
					return true;
				}
				if (e.getData().getType().getName().getLocalPart().equals(
						FeatureGraphBuilder.CONNECTION_EDGE)) {
					return false;
				}
				Filter filter = edgeFilters.get(e.getId().getNetwork());
				return filter != null && !filter.evaluate(e.getData());
			};
			
	private final Predicate<Idp<GlobalId, SimpleFeature>> vertexPredicate =
			v -> {
				if (!networks.contains(v.getId().getNetwork())) {
					return true;
				}
				if (v.getData().getType().getName().getLocalPart().endsWith(
						FeatureGraphBuilder.GENERATED_VERTEX)) {
					return false;
				}
				Filter filter = vertexFilters.get(v.getId().getNetwork());
				return filter != null && !filter.evaluate(v.getData());
			};
			
	private final Predicate<GraphPath<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>>> weightPredicate =
			p -> {
				if (maxDistance != null && p.getWeight() > maxDistance) {
					return false;
				}
				for(Entry<String, Double> entry : maxDistances.entrySet()) {
					if (getNetworkWeight(p, entry.getKey()) > entry.getValue()) {
						return false;
					}
				}
				return true;
			};

	private Map<Idp<GlobalId, SimpleFeature>, Double> distances;
	
	public FeatureGraphTracer(DGraph<GlobalId, SimpleFeature, SimpleFeature> graph,
			GlobalId source, boolean upstream) {
		this(graph, source, upstream, null);
	}
					
	public FeatureGraphTracer(DGraph<GlobalId, SimpleFeature, SimpleFeature> graph,
			GlobalId source, boolean upstream, Long limit) {
		this.graph = graph;
		this.source = source;
		this.upstream = upstream;
		this.limit = limit == null ? Long.MAX_VALUE : limit;
	}
	
	public void addNetwork(String network) {
		networks.add(network);
	}
	
	public void setMaximumDistance(String network, double distance) {
		maxDistances.put(network, distance);
	}
	
	public void setMaximumDistance(double distance) {
		maxDistance = distance;
	}
	
	public void setEdgeFilter(String network, Filter edgeFilter) {
		edgeFilters.put(network, edgeFilter);
	}

	public void setVertexFilter(String network, Filter edgeFilter) {
		vertexFilters.put(network, edgeFilter);
	}
	
	public Graph<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>> trace() {
		
		Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> workingGraph = graph;
		
		if (upstream) {
			workingGraph = new EdgeReversedGraph<>(workingGraph);
		}
		
		workingGraph = new MaskSubgraph<Idp<GlobalId, SimpleFeature>, 
				Idp<GlobalId, SimpleFeature>>(
				workingGraph, 
				vertexPredicate,
				edgePredicate);

		Tracing<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> tracing = 
				new Tracing<>(workingGraph);
		
		Idp<GlobalId, SimpleFeature> sourceVertex = 
				getSourceVertex(workingGraph);
		Idp<GlobalId, SimpleFeature> sourceEdge = 
				getSourceEdge(workingGraph);
		
		List<GraphPath<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>>> paths
				= tracing.getAllPaths(
						sourceVertex, 
						sourceEdge,
						true, 
						weightPredicate,
						limit);
		distances = tracing.getMimimumWeights(paths);
		
		limitReached = tracing.isLimitReached();
		
		return tracing.asGraph(paths);
	}
	
	public boolean isLimitReached() {
		return limitReached;
	}
	
	public Double getDistance(GlobalId leaf) {
		return distances.get(graph.getVertexById(leaf));
	}

	public Double getDistance(Idp<GlobalId, SimpleFeature> leaf) {
		return distances.get(leaf);
	}

	private Double getNetworkWeight(GraphPath<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> p,
			String network) {
		double result = 0;
		for (Idp<GlobalId, SimpleFeature> e : p.getEdgeList()) {
			if (e.getId().getNetwork().equals(network)) {
				result += graph.getEdgeWeight(e);
			}
		}
		return result;
	}
	
	protected Idp<GlobalId, SimpleFeature> getSourceVertex(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> workingGraph) {
		Idp<GlobalId, SimpleFeature> sourceVertex = 
				graph.getVertexById(source);
		
		if (sourceVertex == null) {
			//try edge
			Idp<GlobalId, SimpleFeature> sourceEdge = 
					graph.getEdgeById(source);
			if (sourceEdge == null) {
				throw new IllegalArgumentException("start_node_or_edge_not_found");
			} else {
				sourceVertex = workingGraph.getEdgeSource(sourceEdge);
			}
		}
		
		return sourceVertex;
	}
	
	protected Idp<GlobalId, SimpleFeature> getSourceEdge(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> workingGraph) {
		Idp<GlobalId, SimpleFeature> sourceVertex = 
				graph.getVertexById(source);
		
		if (sourceVertex == null) {
			//try edge
			Idp<GlobalId, SimpleFeature> sourceEdge = 
					graph.getEdgeById(source);
			return sourceEdge;
		}
		
		return null;
	}
	
	/**
	 * Orders the vertices of a trace
	 * 
	 * @param trace trace result
	 * @return ordered vertices
	 */
	public List<Idp<GlobalId, SimpleFeature>> orderVertices(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace) {		
		List<Idp<GlobalId, SimpleFeature>> result 
			= new ArrayList<>();		
		if (trace.vertexSet().size() > 0) {
			Idp<GlobalId, SimpleFeature> sourceVertex = 
				getSourceVertex(trace);
			addNodeAndChildrenIfNotYetInThere(trace, result, sourceVertex);		
		}
		assert result.size() == trace.vertexSet().size();
		return result;
	}
	
	private void addNodeAndChildrenIfNotYetInThere(Graph<Idp<GlobalId, SimpleFeature>, 
				Idp<GlobalId, SimpleFeature>> trace, 
				List<Idp<GlobalId, SimpleFeature>> result, 
				Idp<GlobalId, SimpleFeature> node) {		
		if (!result.contains(node)) {
			result.add(node);			
			for (Idp<GlobalId, SimpleFeature> edge : trace.outgoingEdgesOf(node)) {
				addNodeAndChildrenIfNotYetInThere(trace, result, trace.getEdgeTarget(edge));
			}
		}
	}
	
	/**
	 * Orders the edges of a trace
	 * 
	 * @param trace trace result
	 * @return ordered edges
	 */
	public List<Idp<GlobalId, SimpleFeature>> orderEdges(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> trace) {		
		List<Idp<GlobalId, SimpleFeature>> result 
			= new ArrayList<>();
		if (trace.edgeSet().size() > 0) {
			Idp<GlobalId, SimpleFeature> sourceVertex = 
					getSourceVertex(trace);
			for (Idp<GlobalId, SimpleFeature> edge : trace.outgoingEdgesOf(sourceVertex)) {
				addEdgeAndChildrenIfNotYetInThere(trace, result, edge);
			}
		}
		assert result.size() == trace.edgeSet().size();
		return result;
	}
	
	private void addEdgeAndChildrenIfNotYetInThere(Graph<Idp<GlobalId, SimpleFeature>, 
				Idp<GlobalId, SimpleFeature>> trace, 
				List<Idp<GlobalId, SimpleFeature>> result, 
				Idp<GlobalId, SimpleFeature> edge) {		
		if (!result.contains(edge)) {
			result.add(edge);			
			for (Idp<GlobalId, SimpleFeature> child : trace.outgoingEdgesOf(
					trace.getEdgeTarget(edge))) {
				addEdgeAndChildrenIfNotYetInThere(trace, result, child);
			}
		}
	}
	

}
