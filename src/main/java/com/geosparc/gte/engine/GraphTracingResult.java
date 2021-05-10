package com.geosparc.gte.engine;

import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId;

/**
 * The Tracing Result
 * 
 * @author Niels Charlier
 *
 */
public interface GraphTracingResult {
	
	/**
	 * 
	 * @return the trace as graph
	 * 
	 */
	Graph<Idp<GlobalId, SimpleFeature>, 
		Idp<GlobalId, SimpleFeature>> getGraph();
	
	/**
	 * 
	 * @return for each start node and node, (minimal) distance
	 * 
	 */
	Map<GlobalId, Map<GlobalId, Double>> getDistances();
	
	/**
	 * 
	 * @return the overlapping areas, if requested
	 * 
	 */
	Map<String, List<SimpleFeature>> getAreas();
	
	/**
	 * 
	 * @return the edges of the trace, ordered from start -> end
	 * 
	 */
	List<Idp<GlobalId, SimpleFeature>> edges();
	
	/**
	 * 
	 * @return the vertices of the traces, order from start -> end
	 * 
	 */
	List<Idp<GlobalId, SimpleFeature>> vertices();

	/**
	 * 
	 * @return the requested aggrates
	 * 
	 */
	Map<String, Map<GlobalId, Object>> getAggregates();
	
	/**
	 * 
	 * @return true if the edge limit was reached
	 * 
	 */
	boolean isLimitReached();

}
