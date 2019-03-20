/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.engine;

import java.util.List;

import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.geo.GlobalId;

public interface GraphTracingEngine {
	
	/**
	 * Get the entire graph
	 */
	DGraph<GlobalId, SimpleFeature, SimpleFeature> getGraph();
	
	/**
	 * Perform a trace.
	 * 
	 * @param startNode the start node or edge id
	 * @param maxDistance the maximum total distance
	 * @param networks the list of networks
	 * @param nodeFilters the list of node filters per network (same order as networks)
	 * @param edgeFilters the list of edge filters per network (same order as networks)
	 * @param maxDistances the list of maximum distance per network (same order as networks)
	 * @param edgeAggregatedAtts the lists of aggregated attributes (same order as networks), null = all
	 * @param upstream true if upstream, false if downstream
	 * @param includeOverlappingAreas calculate overlapping areas
	 * @param limit the max amount of edges per single path
	 * @return the tracing result
	 * @throws CQLException malformed filter
	 */
	GraphTracingResult trace(GlobalId startNode, Double maxDistance, 
			List<String> networks, List<String> nodeFilters, List<String> edgeFilters,
			List<Double> maxDistances, List<List<String>> edgeAggregatedAtts, boolean upstream,
			boolean includeOverlappingAreas, Long limit) throws CQLException;

	/**
	 * Return a list of all networks.
	 */
	List<String> getNetworks();

}
