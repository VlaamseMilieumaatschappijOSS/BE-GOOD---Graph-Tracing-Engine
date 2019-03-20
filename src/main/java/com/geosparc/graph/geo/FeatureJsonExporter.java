/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.io.IOException;
import java.io.Writer;

import org.geotools.geojson.feature.FeatureJSON;
import org.jgrapht.Graph;
import org.json.simple.JSONArray;
import org.json.simple.JSONStreamAware;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.base.JsonExporter;

/**
 * 
 * Export Feature Graph to JSON
 * 
 * @author Niels Charlier
 *
 */
public class FeatureJsonExporter extends JsonExporter<GlobalId, SimpleFeature, SimpleFeature> {
	
	private boolean flat;
	
	public FeatureJsonExporter() {
		this(false);
	}
	
	public FeatureJsonExporter(boolean flat) {
		this.flat = flat;
	}
	
	private class JSONFeatureWrapper implements JSONStreamAware {
		
		private SimpleFeature sf;
		
		public JSONFeatureWrapper(SimpleFeature sf) {
			this.sf = sf;			
		}

		@Override
		public void writeJSONString(Writer out) throws IOException {
			new FeatureJSON().writeFeature(sf, out);
		}
		
	}
	
	@Override
	public JSONStreamAware convertEdge(SimpleFeature edgeData) {
		return new JSONFeatureWrapper(edgeData);
	}
	
	@Override
	public JSONStreamAware convertVertex(SimpleFeature vertexData) {
		return new JSONFeatureWrapper(vertexData);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONStreamAware convertGraph(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> g) {
		if (flat) {			
			
			JSONArray features = new JSONArray();
			
			for (Idp<GlobalId, SimpleFeature> vertex : g.vertexSet()) {
				if (vertex.getData() != null) {
					features.add(new JSONFeatureWrapper(vertex.getData()));
				}
			}
			for (Idp<GlobalId, SimpleFeature> edge : g.edgeSet()) {
				if (edge.getData() != null) {
					features.add(new JSONFeatureWrapper(edge.getData()));
				}		
			}
			
			return features;
			
		} else {
			return super.convertGraph(g);
		}
		
	}

}
