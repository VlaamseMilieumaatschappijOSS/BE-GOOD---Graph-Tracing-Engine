/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;

public class NetworkConfig {
	
	public enum NodeType {
		/* Nodes are generated from references (usually foreign keys). */
		LOGICAL, 
		/* Nodes are generated purely based on geographical location */
		GEOGRAPHICAL
	}

	/**
	 * Name of the network
	 */
	private String name;
	
	/**
	 * The node type
	 */
	private NodeType nodeType;
	
	/**
	 * Data store for the edges
	 */
	private Map<String, String> edgeDataStore = new HashMap<>();
	
	/**
	 * Feature configuration for edge
	 */
	private FeatureConfig edgeFeature;
	
	/**
	 * Attribute that marks beginning of an edge (in case of logical, a key, in case of geographical, a geography)
	 */
	private String startAttribute;
	
	/**
	 * Attribute that marks end of an edge (in case of logical, a key, in case of geographical, a geography)
	 */
	private String endAttribute;
	
	/**
	 * Only applicable for NodeType LOGICAL, table with node attributes.
	 * If NULL, the same datastore as edgeDataStore is used.
	 */
	private Map<String, String> nodeDataStore = new HashMap<>();
	
	/**
	 * Only applicable for NodeType LOGICAL, feature configuration for node
	 */
	private FeatureConfig nodeFeature;
	
	/**
	 * Only applicable for NodeType GEOGRAPHICAL, the tolerance difference to snap edges to a node
	 */
	private double tolerance;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getEdgeDataStore() {
		return parseClassPathFromDataStore(edgeDataStore);
	}

	public void setEdgeDataStore(Map<String, String> edgeDataStore) {
		this.edgeDataStore = edgeDataStore;
	}

	public Map<String, String> getNodeDataStore() {
		return nodeDataStore;
	}

	public void setNodeDataStore(Map<String, String> nodeDataStore) {
		this.nodeDataStore = nodeDataStore;
	}

	public String getStartAttribute() {
		return startAttribute;
	}

	public void setStartAttribute(String startAttribute) {
		this.startAttribute = startAttribute;
	}

	public String getEndAttribute() {
		return endAttribute;
	}

	public void setEndAttribute(String endAttribute) {
		this.endAttribute = endAttribute;
	}

	public FeatureConfig getEdgeFeature() {
		return edgeFeature;
	}

	public void setEdgeFeature(FeatureConfig edgeFeature) {
		this.edgeFeature = edgeFeature;
	}

	public FeatureConfig getNodeFeature() {
		return nodeFeature;
	}

	public void setNodeFeature(FeatureConfig nodeFeature) {
		this.nodeFeature = nodeFeature;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public double getTolerance() {
		return tolerance;
	}

	public void setTolerance(double tolerance) {
		this.tolerance = tolerance;
	}

	private Map<String, String> parseClassPathFromDataStore(Map<String, String> dataStore) {
		if (dataStore.containsKey("url") && dataStore.get("url").contains("classpath:")) {
			String url = dataStore.get("url");
			String fileName = url.split("classpath:", 2)[1];
			URL file = getClass().getClassLoader().getResource(fileName);
			dataStore.put("url", url.replace("classpath:"+fileName, file.getPath()));
		}
		dataStore.put("Expose primary keys", "true");
		return dataStore;
	}
	
	public void validate() {
		if (Strings.isEmpty(getName())) {
			throw new IllegalStateException("Network without name found in configuration.");
		}
		if (getNodeType() == null) {
			throw new IllegalStateException("Missing node type for network " + getName());
		}
		if (getEdgeDataStore() == null) {
			throw new IllegalStateException("Missing edge datastore for network " + getName());
		}
		if (getEdgeFeature() == null) {
			throw new IllegalStateException("Missing edge feature for network " + getName());
		}
		if (Strings.isEmpty(getEdgeFeature().getName())) {
			throw new IllegalStateException("Missing edge feature name for network " + getName());
		}
		if (Strings.isEmpty(getStartAttribute())) {
			throw new IllegalStateException("Missing start attribute for network " + getName());
		}
		if (Strings.isEmpty(getEndAttribute())) {
			throw new IllegalStateException("Missing end attribute for network " + getName());
		}
		if (Strings.isEmpty(getEdgeFeature().getIdAttribute())) {
			throw new IllegalStateException("Missing edge id attribute for logical network " + getName());
		}
		if (getNodeFeature() != null &&
				getNodeFeature().getAggregatedAttributes() != null &&
				!getNodeFeature().getAggregatedAttributes().isEmpty()) {
			throw new IllegalStateException("Aggregated node attributes not (yet) supported");			
		}
		if (getNodeType() == NodeType.LOGICAL) {
			if (getNodeFeature() == null) {
				throw new IllegalStateException("Missing node feature for logical network " + getName());
			}
			if (Strings.isEmpty(getNodeFeature().getIdAttribute())) {
				throw new IllegalStateException("Missing node id attribute for logical network " + getName());
			}
			if (Strings.isEmpty(getNodeFeature().getName())) {
				throw new IllegalStateException("Missing node feature name for logical network " + getName());
			}
		} else {
			if (tolerance <= 0) {
				throw new IllegalStateException("Tolerance for geographical network " + getName() + " is not greater than 0.");
			}
		}
	}		

}
