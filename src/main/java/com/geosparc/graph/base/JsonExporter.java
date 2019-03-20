/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.base;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

/**
 * Generic JSON exporter for IDP Graphs
 * 
 * @author Niels Charlier
 *
 * @param <I> identifier class
 * @param <V> vertex data class
 * @param <E> edge data class
 */
public class JsonExporter<I, V, E> implements GraphExporter<Idp<I,V>, Idp<I,E>> {
	
	private class JSONObjectWrapper implements JSONStreamAware {
		
		private Object o;
		
		public JSONObjectWrapper(Object o) {
			this.o = o;			
		}

		@Override
		public void writeJSONString(Writer out) throws IOException {
			out.append('"')
				.append(o.toString())
				.append('"');
		}
		
	}
	
	@Override
	public void exportGraph(Graph<Idp<I,V>, Idp<I,E>> g, Writer writer) throws ExportException {		
		try {
			convertGraph(g).writeJSONString(writer);
		} catch (IOException e) {
			throw new ExportException(e);
		}
	}
		
	//hooks
	
	@SuppressWarnings("unchecked")	
	public JSONStreamAware convertGraph(Graph<Idp<I,V>, Idp<I,E>> g) {
		JSONArray vertices = new JSONArray();
		for (Idp<I,V> vertex : vertices(g)) {
			JSONObject vertexWrapper = new JSONObject();	
			vertexWrapper.put("id", convertId(vertex.getId()));
			if (vertex.getData() != null) {
				vertexWrapper.put("data", convertVertex(vertex.getData()));
			}
			JSONArray outArray = new JSONArray();
			for (Idp<I, E> e : g.outgoingEdgesOf(vertex)) {
				outArray.add(convertId(e.getId()));
			}
			vertexWrapper.put("out", outArray);
			JSONArray inArray = new JSONArray();
			for (Idp<I, E> e : g.incomingEdgesOf(vertex)) {
				inArray.add(convertId(e.getId()));
			}
			vertexWrapper.put("in", inArray);
			ppVertex(vertex.getId(), vertexWrapper);
			vertices.add(vertexWrapper);
		}
		
		JSONArray edges = new JSONArray();
		for (Idp<I,E> edge : edges(g)) {
			JSONObject edgeWrapper = new JSONObject();	
			edgeWrapper.put("id", convertId(edge.getId()));
			if (edge.getData() != null) {
				edgeWrapper.put("data", convertEdge(edge.getData()));
			}
			edgeWrapper.put("from", convertId(g.getEdgeSource(edge).getId()));
			edgeWrapper.put("to", convertId(g.getEdgeTarget(edge).getId()));
			edgeWrapper.put("weight", g.getEdgeWeight(edge));
			ppEdge(edge.getId(), edgeWrapper);
			edges.add(edgeWrapper);
		}
		
		JSONObject graph = new JSONObject();		
		graph.put("vertices", vertices);
		graph.put("edges", edges);
		
		return graph;
	}
	
	public JSONStreamAware convertId(I id) {
		return id instanceof JSONStreamAware ? 
				(JSONStreamAware) id : 
			new JSONObjectWrapper(id);
	}
	
	public JSONStreamAware convertEdge(E edgeData) {
		return edgeData instanceof JSONStreamAware ? 
				(JSONStreamAware) edgeData : 
			new JSONObjectWrapper(edgeData);
	}
	
	public JSONStreamAware convertVertex(V vertexData) {
		return vertexData instanceof JSONStreamAware ? 
				(JSONStreamAware) vertexData : 
			new JSONObjectWrapper(vertexData);
	}
	
	/**
	 * post process a vertex hook
	 * 
	 * @param id id of vertex
	 * @param vertexWrapper the json object of vertex
	 */
	public void ppVertex(I id, JSONObject vertexWrapper) {
		
	}
	
	/**
	 * post process an edge hook
	 * 
	 * @param id id of edge
	 * @param vertexWrapper the json object of edge
	 */
	public void ppEdge(I id, JSONObject edgeWrapper) {
		
	}
	
	/**
	 * hook for edges collection, can be used to determine order
	 * 
	 * @param g the graph
	 * @return the (ordered) collection of edges
	 */
	public Collection<Idp<I,E>> edges(Graph<Idp<I,V>, Idp<I,E>> g) {
		return g.edgeSet();
	}
	
	/**
	 * hook for vertex collection, can be used to determine order
	 * 
	 * @param g the graph
	 * @return the (ordered) collection of vertices
	 */
	public Collection<Idp<I,V>> vertices(Graph<Idp<I,V>, Idp<I,E>> g) {
		return g.vertexSet();
	}

}
