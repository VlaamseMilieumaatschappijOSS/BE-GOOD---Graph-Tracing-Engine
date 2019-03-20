/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */
package com.geosparc.gte.rest;

import java.util.List;

/**
 * The Trace Request 
 * 
 * @author Niels Charlier
 *
 */
public class TraceRequest {
	
	/**
	 * Request parameters per network
	 *
	 */
	public static class TraceRequestNetwork {
		
		/**
		 * Network name
		 */
		private String name;
		
		/**
		 * Maximum distance
		 */
		private Double maxDistance;
		
		/**
		 * Node filter
		 */
		private String nodeFilter;
		
		/**
		 * Edge filter
		 */
		private String edgeFilter;
		
		/**
		 * Edge aggregates
		 */
		private List<String> edgeAggregates;

		public String getName() {
			return name;
		}

		public void setName(String networkName) {
			this.name = networkName;
		}

		public Double getMaxDistance() {
			return maxDistance;
		}

		public void setMaxDistance(Double maxDistance) {
			this.maxDistance = maxDistance;
		}

		public String getNodeFilter() {
			return nodeFilter;
		}

		public void setNodeFilter(String nodeFilter) {
			this.nodeFilter = nodeFilter;
		}

		public String getEdgeFilter() {
			return edgeFilter;
		}

		public void setEdgeFilter(String edgeFilter) {
			this.edgeFilter = edgeFilter;
		}

		public List<String> getEdgeAggregates() {
			return edgeAggregates;
		}

		public void setEdgeAggregates(List<String> edgeAggregates) {
			this.edgeAggregates = edgeAggregates;
		}
		
	}
	
	/**
	 * Start network.
	 */
	private String startNetwork;
	
	/**
	 * Start node/edge id
	 */
	private String startId;
	
	/**
	 * Maximum total distance
	 */
	private Double maxDistance;
	
	/**
	 * List of included networks and their parameters
	 */
	private List<TraceRequestNetwork> networks;
	
	/**
	 * true if upstream, false if downstream
	 */
	private boolean upstream;

	/**
	 * true if overlapping areas should be calculated
	 */
	private boolean includeOverlappingAreas;
	
	/**
	 * maximum amount of edges per single path
	 */
	private Long limit;

	public String getStartNetwork() {
		return startNetwork;
	}

	public void setStartNetwork(String startNetwork) {
		this.startNetwork = startNetwork;
	}

	public String getStartId() {
		return startId;
	}

	public void setStartId(String startId) {
		this.startId = startId;
	}

	public Double getMaxDistance() {
		return maxDistance;
	}

	public void setMaxDistance(Double maxDistance) {
		this.maxDistance = maxDistance;
	}

	public List<TraceRequestNetwork> getNetworks() {
		return networks;
	}

	public void setNetworks(List<TraceRequestNetwork> networks) {
		this.networks = networks;
	}

	public boolean isUpstream() {
		return upstream;
	}

	public void setUpstream(boolean upstream) {
		this.upstream = upstream;
	}

	public boolean isIncludeOverlappingAreas() {
		return includeOverlappingAreas;
	}

	public void setIncludeOverlappingAreas(boolean includeOverlappingAreas) {
		this.includeOverlappingAreas = includeOverlappingAreas;
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}
	
}
