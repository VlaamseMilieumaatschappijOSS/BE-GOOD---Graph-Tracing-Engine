/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

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
	 * @return for each node, (minimal) distance from start node
	 * 
	 */
	Map<GlobalId, Double> getDistances();
	
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
