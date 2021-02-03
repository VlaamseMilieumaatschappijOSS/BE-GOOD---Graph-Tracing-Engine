package com.geosparc.graph.geo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.geosparc.graph.base.DGraphOperation;
import com.geosparc.graph.base.Idp;

/**
 * Helper class for retyping feature graphs
 *  (used for removing filter attributes and keeping only user attributes).
 * 
 * @author Niels Charlier
 *
 */
public class FeatureGraphRetyper extends DGraphOperation<GlobalId, SimpleFeature, SimpleFeature> {
	
	private Map<String, List<String>> vertexProperties = new HashMap<>();
	private Map<String, List<String>> edgeProperties = new HashMap<>();
	private Map<String, SimpleFeatureBuilder> vertexTypes = new HashMap<>();
	private Map<String, SimpleFeatureBuilder> edgeTypes = new HashMap<>();

	public FeatureGraphRetyper(Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph) {
		super(graph, true);
	}
	
	public void setVertexProperties(String networkName, List<String> properties) {
		vertexProperties.put(networkName, properties);
	}
	
	public void setEdgeProperties(String networkName, List<String> properties) {
		edgeProperties.put(networkName, properties);
	}
	
	protected SimpleFeatureBuilder getVertexType(String networkName, SimpleFeatureType type) {
		SimpleFeatureBuilder vertexType = vertexTypes.get(networkName);
		if (vertexType == null) {
			vertexType = new SimpleFeatureBuilder(
					SimpleFeatureTypeBuilder.retype(type, 
					vertexProperties.get(networkName)));
			vertexTypes.put(networkName, vertexType);		
		}
		return vertexType;
	}
	
	protected SimpleFeatureBuilder getEdgeType(String networkName, SimpleFeatureType type) {
		SimpleFeatureBuilder vertexType = edgeTypes.get(networkName);
		if (vertexType == null) {
			vertexType = new SimpleFeatureBuilder(
					SimpleFeatureTypeBuilder.retype(type, 
					edgeProperties.get(networkName)));
			edgeTypes.put(networkName, vertexType);		
		}
		return vertexType;
	}
	
	@Override
	protected SimpleFeature processEdge(GlobalId id, SimpleFeature edge) {
		if (edge == null) {
			return null;
		}
		if (edge.getFeatureType().getName().getLocalPart().equals(FeatureGraphBuilder.CONNECTION_EDGE)) {
			return edge;
		}
		return SimpleFeatureBuilder.retype(edge, 
				getEdgeType(id.getNetwork(), edge.getFeatureType()));
	}
	
	@Override
	protected SimpleFeature processVertex(GlobalId id, SimpleFeature vertex) {
		if (vertex == null) {
			return null;
		}
		if (vertex.getFeatureType().getName().getLocalPart().endsWith(FeatureGraphBuilder.GENERATED_VERTEX)) {
			return vertex;
		}
		return SimpleFeatureBuilder.retype(vertex, 
				getVertexType(id.getNetwork(), vertex.getFeatureType()));
	}
	

}
