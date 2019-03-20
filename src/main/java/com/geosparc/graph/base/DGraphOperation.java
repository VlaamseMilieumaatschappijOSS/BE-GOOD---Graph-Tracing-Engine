/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.base;

import org.jgrapht.Graph;

/**
 * Operation that turns DGraph into other DGraph
 * 
 * @author niels
 *
 * @param <I>
 * @param <V>
 * @param <E>
 */
public class DGraphOperation<I, V, E> {
	
	private Graph<Idp<I, V>, Idp<I, E>> graph;
	
	private boolean weighted;
	
	public DGraphOperation(Graph<Idp<I, V>, Idp<I, E>> graph, boolean weighted) {
		this.graph = graph;
		this.weighted = weighted;
	}
	
	public DGraph<I, V, E> process() {
		
		DGraph<I, V, E> result = new DGraph<I, V, E>(weighted);
		
		for (Idp<I, V> vertex : graph.vertexSet()) {
			result.addVertexById(vertex.getId())
				.setData(processVertex(vertex.getId(), vertex.getData()));
		}
		
		for (Idp<I, E> edge : graph.edgeSet()) {
			Idp<I, E> newEdge = result.addEdgeById(
					graph.getEdgeSource(edge).getId(), 
					graph.getEdgeTarget(edge).getId(),
					edge.getId());
			newEdge.setData(processEdge(edge.getId(), edge.getData()));;
			if (weighted) {
				result.setEdgeWeight(newEdge,
					graph.getEdgeWeight(edge));
			}
		}
		
		return result;
		
	}
	
	protected V processVertex(I id, V vertex) {
		return vertex;
	}
	
	protected E processEdge(I id, E edge) {
		return edge;
	}

}
