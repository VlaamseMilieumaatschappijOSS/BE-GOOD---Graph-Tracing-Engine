/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;

/**
 * 
 * Helper class for calculating aggregates in feauture graphs.
 * 
 * @author Niels Charlier
 *
 */
public class FeatureGraphAggregator {
	
	public enum Method {
		/**
		 * Aggregates by addition
		 */
		ADD {
			@Override
			public Object compute(Object one, Object two) {
				return (one == null ? 0 : ((Number) one).longValue()) + 
					(two == null ? 0 : ((Number) two).longValue());
			}
		};
		
		public abstract Object compute(Object one, Object two);
	}
	
	private Graph<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>> graph;
	
	private String typeName;
	
	private String sourceProperty;
	
	private Method method;

	private List<Idp<GlobalId, SimpleFeature>> orderedEdges;
			
	public FeatureGraphAggregator(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> graph, 
			List<Idp<GlobalId, SimpleFeature>> orderedEdges,
			String typeName,
			String sourceProperty,
			Method method) {
		this.graph = graph;
		this.sourceProperty = sourceProperty;
		this.typeName = typeName;
		this.method = method;
		this.orderedEdges = orderedEdges;
	}

	public Map<GlobalId, Object> aggregate() {
		Map<GlobalId, Object> result = new HashMap<>();
		for (Idp<GlobalId, SimpleFeature> e : orderedEdges) {
			if (e.getData().getType().getName().getLocalPart().equals(typeName)) {
				calculateEdge(e, result);
			}
		}
		return result;
	}
	
	protected Object calculateEdge(Idp<GlobalId, SimpleFeature> edge,
			Map<GlobalId, Object> result) {
		Object value = result.get(edge.getId());
		if (value == null) {
			// this avoids infinite recursion if there is a loop
			// therefore, it is important to follow the right order 
			// starting with the source
			result.put(edge.getId(), 0);
			
			value = edge.getData().getAttribute(sourceProperty);
			for (Idp<GlobalId, SimpleFeature> e : graph.outgoingEdgesOf(
					graph.getEdgeTarget(edge))) {
				if (e.getData().getType().getName().getLocalPart().equals(typeName)) {
					value = method.compute(value, calculateEdge(e, result));
				}
			}
			result.put(edge.getId(), value);
		}
		return value;
	}

}
