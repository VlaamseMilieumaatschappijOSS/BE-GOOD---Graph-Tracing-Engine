/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.alg;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.GraphTests;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.GraphWalk;

/**
 * The generic tracing algorithm.
 * 
 * @author Niels Charlier
 *
 * @param <V> vertex class
 * @param <E> edge class
 */
public class Tracing<V, E>
{
    private final Graph<V, E> graph;
    
    private boolean limitReached;

    /**
     * Create a new instance
     *
     * @param graph the input graph
     * @throws IllegalArgumentException if the graph is not directed
     */
    public Tracing(Graph<V, E> graph)
    {
        this.graph = GraphTests.requireDirected(graph);
    }
    
    /**
     * Calculate (and return) all paths from the source vertex, limited by a maximum distance.
     *
     * @param sourceVertex the source vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * @param maxPathWeight maximum weight to allow in a path 
     *
     * @return list of all paths  
     */
    public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, 
    		double maxPathWeight) {
    	
        if (maxPathWeight <= 0) {
            throw new IllegalArgumentException("maxPathWeight must be positive");
        }
        
        return getAllPaths(sourceVertex, null, simplePathsOnly,
        		path -> path.getWeight() < maxPathWeight, Long.MAX_VALUE);        		
    }
    
    /**
     * Calculate (and return) all paths from the source vertex, limited by a maximum distance
     * and a predicate.
     *
     * @param sourceVertex the source vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * @param maxPathWeight maximum weight to allow in a path 
     * @param predicate defines, together with max weight, when to continue (true) or finalize (false) path
     *
     * @return list of all paths  
     */
    public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, 
    		double maxPathWeight, Predicate<GraphPath<V, E>> predicate) {
    	
        if (maxPathWeight <= 0) {
            throw new IllegalArgumentException("maxPathWeight must be positive");
        }
        
        return getAllPaths(sourceVertex, null, simplePathsOnly,
        		predicate.and(path -> path.getWeight() < maxPathWeight), Long.MAX_VALUE);        		
    }
    
    /**
     * Calculate (and return) all paths from the source vertex, limited by a maximum distance
     * and a predicate.
     *
     * @param sourceVertex the source vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * @param maxPathWeight maximum weight to allow in a path 
     * @param predicate defines, together with max weight, when to continue (true) or finalize (false) path
     * @param maxEdge maximum number of edges
     *
     * @return list of all paths  
     */
    public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, 
    		double maxPathWeight, Predicate<GraphPath<V, E>> predicate, long maxEdges) {
    	
        if (maxPathWeight <= 0) {
            throw new IllegalArgumentException("maxPathWeight must be positive");
        }
        
        return getAllPaths(sourceVertex, null, simplePathsOnly,
        		predicate.and(path -> path.getWeight() < maxPathWeight), maxEdges);        		
    }
    

    /**
     * Calculate (and return) all paths from the source vertex, limited by a custom predicate.
     *
     * @param sourceVertex the source vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * 		if this is false, the predicate must eventually end every trace or we will
     * 		get an infinite loop!
     * @param predicate defines when to continue (true) or finalize (false) path
     *
     * @return list of all paths  
     */
	public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, boolean simplePathsOnly, 
    		Predicate<GraphPath<V, E>> predicate) {
        return getAllPaths(sourceVertex, null, simplePathsOnly, predicate, Long.MAX_VALUE);     
		
	}
	

    /**
     * Calculate (and return) all paths from the source vertex, limited by a custom predicate.
     *
     * @param sourceVertex the source vertex
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * 		if this is false, the predicate must eventually end every trace or we will
     * 		get an infinite loop!
     * @param predicate defines when to continue (true) or finalize (false) path
     * @param maxEdge maximum number of edges
     *
     * @return list of all paths  
     */
	public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, 
			boolean simplePathsOnly, 
    		Predicate<GraphPath<V, E>> predicate, long maxEdges) {
		return getAllPaths(sourceVertex, null, simplePathsOnly, predicate, maxEdges);
	}


    /**
     * Calculate (and return) all paths from the source vertex, limited by a custom predicate.
     *
     * @param sourceVertex the source vertex, if starting from a vertex
     * @param sourceEdge the source edge, if starting from an edge
     * @param simplePathsOnly if true, only search simple (non-self-intersecting) paths
     * 		if this is false, the predicate must eventually end every trace or we will
     * 		get an infinite loop!
     * @param predicate defines when to continue (true) or finalize (false) path
     * @param maxEdge maximum number of edges
     *
     * @return list of all paths  
     */
	public final List<GraphPath<V, E>> getAllPaths(V sourceVertex, 
			E sourceEdge,
			boolean simplePathsOnly, 
    		Predicate<GraphPath<V, E>> predicate, long maxEdges) {


        if (sourceVertex == null && sourceEdge == null) {
        	throw new IllegalArgumentException("no source vertex or edge");
        }
        
        if (sourceVertex != null && sourceEdge != null && !graph.getEdgeSource(sourceEdge).equals(sourceVertex)) {
        	throw new IllegalArgumentException("provided both source edge and vertex that don't match");
        }

        // Generate all the paths

        /*
         * We walk forwards through the network from the source vertices, exploring all outgoing
         * edges whose minimum distances is small enough.
         */
        List<GraphPath<V, E>> completePaths = new ArrayList<>();
        Deque<GraphPath<V, E>> incompletePaths = new LinkedList<>();

        // Bootstrap the search with the source vertices
        if (sourceEdge == null) {
    		for (E edge : graph.outgoingEdgesOf(sourceVertex)) {
    			assert graph.getEdgeSource(edge).equals(sourceVertex);
    			incompletePaths.add(newPath(edge));
    		}	
        } else {
        	incompletePaths.add(newPath(sourceEdge));
        }
		
		limitReached = false;
		
		long edgeCounter = 0;

        // Walk through the queue of incomplete paths
        for (GraphPath<V, E> incompletePath; (incompletePath = incompletePaths.poll()) != null;) {
        	
        	if (edgeCounter >= maxEdges || 
        			simplePathsOnly && hasReturned(incompletePath) ||
        			!predicate.test(incompletePath)) {
        		
        		if (edgeCounter >= maxEdges) {
        			limitReached = true;
        		}
        		
				completePaths.add(incompletePath);
				
        	} else { // look further
	            
	            boolean noValidEdges = true;
	
				for (E outEdge : graph.outgoingEdgesOf(
						incompletePath.getEndVertex())) {

					edgeCounter++;
					
					if (edgeCounter >= maxEdges) {
	        			limitReached = true;
						break;
					}
					
					noValidEdges = false;
	
					incompletePaths.addFirst(
							addToPath(incompletePath, outEdge)); 
						// We use incompletePaths in FIFO mode to avoid memory blowup
					
				}
				
				if (noValidEdges) {
					//if there were no valid outgoing edges, we still want this path
					completePaths.add(incompletePath);
				}
        	}
        }

        return completePaths;
    }
    
    private boolean hasReturned(GraphPath<V, E> incompletePath) {
    	List<V> vertexList = incompletePath.getVertexList();
		for (int i = 0; i < vertexList.size() - 1; i++) {
			if (vertexList.get(i).equals(vertexList.get(vertexList.size() - 1))) {
				return true;
			}
		}
		return false;
	}
    
    /**
     * Returns if limit was reached in last tracing call
     * 
     * @return if limit was reached
     */
    public boolean isLimitReached() {
    	return limitReached;
    }

	/**
     * Return list of paths as a single graph (a tree in case simplePathsOnly was specified)
     * 
     * @return tree graph
     */
    public Graph<V, E> asGraph(List<GraphPath<V, E>> paths) {
    	Graph<V, E> result = new DirectedMultigraph<V, E>(null, 
    			null, 
    			true);
		for (GraphPath<V, E> path : paths) {
			List<V> vertexList = path.getVertexList();
			List<E> edgeList = path.getEdgeList();
			result.addVertex(vertexList.get(0));
			for (int i = 0; i < edgeList.size(); i++) {
				E edge = edgeList.get(i);
				result.addVertex(vertexList.get(i + 1));
				result.addEdge(vertexList.get(i),
						vertexList.get(i + 1),
						edge);
				result.setEdgeWeight(edge,
						graph.getEdgeWeight(edge));
			}
		}
    	return result;
    }
    
    /**
     * Creates a map of weights per end vertex, if multiple paths end in the same vertex
     * the minimum is taken.
     * 
     * @return map of vertex/weight pairs
     */
    public Map<V, Double> getMimimumWeights(List<GraphPath<V, E>> paths) {
    	Map<V, Double> map = new HashMap<>();
    	for (GraphPath<V, E> path : paths) {
    		double weight = 0;
    		for (E edge : path.getEdgeList()) {
    			V vertex = graph.getEdgeTarget(edge);
	    		Double current = map.get(vertex);
	    		weight += graph.getEdgeWeight(edge);
	    		if (current == null || current > path.getWeight()) {
	    			map.put(vertex, weight);
	    		}
    		}
    	}
    	return map;
    }

    
    /**
     * Adds one edge to a GraphPath
     *
     * @param path the old graph path
     * @param edge the new edge
     *
     * @return the new GraphPath
     */
    private GraphPath<V, E> addToPath(GraphPath<V, E> path, E edge)
    {
    	List<E> edges = new ArrayList<>(path.getEdgeList());
    	List<V> vertices = new ArrayList<>(path.getVertexList());
    	edges.add(edge);
    	V endVertex = graph.getEdgeTarget(edge);
    	vertices.add(endVertex);
    	return new GraphWalk<>(graph, path.getStartVertex(), 
    			endVertex, vertices,
        		edges, path.getWeight() + graph.getEdgeWeight(edge));
    }
    
    /**
    * Creates new GraphPath from a single edge
    *
    * @param edge the edge
    *
    * @return the new GraphPath
    */
    private GraphPath<V, E> newPath(E edge)
    {
    	List<E> edges = new ArrayList<>();
    	List<V> vertices = new ArrayList<>();
    	edges.add(edge);
    	vertices.add(graph.getEdgeSource(edge));
    	V endVertex = graph.getEdgeTarget(edge);
    	vertices.add(endVertex);
    	return new GraphWalk<>(graph, graph.getEdgeSource(edge), 
    			endVertex, vertices, 
        		edges, graph.getEdgeWeight(edge));
    }
}