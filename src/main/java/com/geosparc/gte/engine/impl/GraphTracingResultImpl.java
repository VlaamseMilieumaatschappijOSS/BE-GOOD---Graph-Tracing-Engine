package com.geosparc.gte.engine.impl;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId;
import com.geosparc.gte.engine.GraphTracingResult;

/**
 * 
 * Graph Tracing Result.
 * 
 * @author Niels Charlier
 *
 */
public class GraphTracingResultImpl implements GraphTracingResult {
	
	private Graph<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>> graph;
	
	private Map<GlobalId, Map<GlobalId, Double>> distances;

	private Map<String, List<SimpleFeature>> areas;

	private List<Idp<GlobalId, SimpleFeature>> edges;

	private List<Idp<GlobalId, SimpleFeature>> vertices;

	private Map<String, Map<GlobalId, Object>> aggregates;

	private boolean limitReached;
	
	private Geometry buffer;
		
	public GraphTracingResultImpl(Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph,
			Map<GlobalId, Map<GlobalId, Double>> distances,
			Map<String, Map<GlobalId, Object>> aggregates,
			Map<String, List<SimpleFeature>> areas,
			List<Idp<GlobalId, SimpleFeature>> edges,
			List<Idp<GlobalId, SimpleFeature>> vertices,
			boolean limitReached,
			Geometry buffer) {
		this.graph = graph;
		this.distances = distances;
		this.areas = areas;
		this.edges = edges;
		this.vertices = vertices;
		this.aggregates = aggregates;
		this.limitReached = limitReached;
		this.buffer = buffer;
	}

	@Override
	public Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> getGraph() {
		return graph;
	}
	@Override
	public Map<GlobalId, Map<GlobalId, Double>> getDistances() {
		return distances;
	}

	@Override
	public Map<String, List<SimpleFeature>> getAreas() {
		return areas;
	}

	@Override
	public List<Idp<GlobalId, SimpleFeature>> edges() {
		return edges;
	}

	@Override
	public List<Idp<GlobalId, SimpleFeature>> vertices() {
		return vertices;
	}

	@Override
	public Map<String, Map<GlobalId, Object>> getAggregates() {
		return aggregates;
	}

	@Override
	public boolean isLimitReached() {
		return limitReached;
	}

	@Override
	public Geometry getBuffer() {
		return buffer;
	}
	
}
