package com.geosparc.gte.engine;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.geo.GlobalId;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;

import java.util.List;

public interface GraphTracingEngine {

	/**
	 * Start the engine, initialize the graph.
	 */
	void start();

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
	 * @param ignorePaths If true, do not calculate all possible paths, but stop when an already visited vertex is met.
	 *      This means that predicates and limits will not be calculated correctly.
	 * @return the tracing result
	 * @throws CQLException malformed filter
	 */
	GraphTracingResult trace(List<GlobalId> startNodes, Double maxDistance, 
			List<String> networks, List<String> nodeFilters, List<String> edgeFilters,
			List<Double> maxDistances, List<List<String>> edgeAggregatedAtts, boolean upstream,
			boolean includeOverlappingAreas, List<String> overlapTypes, Long limit, boolean ignorePaths,
			Double bufferSize) throws CQLException;

	/**
	 * Return a list of all networks.
	 */
	List<String> getNetworks();

	/**
	 * Return the status of the graph.
	 */
	GraphStatus getStatus();

	/**
	 * Reload the entire graph.
	 */
	void reload();

	/**
	 * Check if the graph is updating.
	 */
	boolean isUpdating();

}
