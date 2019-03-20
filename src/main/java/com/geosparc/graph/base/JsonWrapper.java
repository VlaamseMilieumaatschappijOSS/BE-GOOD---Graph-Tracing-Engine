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

import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.json.simple.JSONStreamAware;

/**
 * Convenience class for adding a graph into a bigger json object
 * 
 * @author Niels Charlier
 *
 * @param <I> identifier class
 * @param <V> vertex data class
 * @param <E> edge data class
 */
public class JsonWrapper<I, V, E> implements JSONStreamAware {
	
	private Graph<Idp<I,V>, Idp<I,E>> graph;
	private JsonExporter<I, V, E> exporter;
	
	public JsonWrapper(Graph<Idp<I,V>, Idp<I,E>> graph,
			JsonExporter<I, V, E> exporter) {
		this.graph = graph;
		this.exporter = exporter;
	}

	@Override
	public void writeJSONString(Writer out) throws IOException {
		try {
			exporter.exportGraph(graph, out);
		} catch (ExportException e) {
			throw new IOException(e);
		}
	}

}
