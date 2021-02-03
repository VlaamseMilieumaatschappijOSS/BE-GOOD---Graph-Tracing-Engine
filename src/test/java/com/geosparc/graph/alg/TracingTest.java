package com.geosparc.graph.alg;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.EdgeReversedGraph;
import org.junit.Test;

public class TracingTest {
	
	Graph<Integer, String> graph 
		= new DirectedMultigraph<Integer, String>(null, null, true);
	
	public TracingTest() {
		graph.addVertex(1);
		graph.addVertex(2);
		graph.addVertex(3);
		graph.addVertex(4);
		graph.addVertex(5);
		graph.addVertex(6);
		graph.addVertex(7);
		graph.addEdge(1, 2, "1->2");
		graph.setEdgeWeight("1->2", 0.25);
		graph.addEdge(2, 3, "2->3");
		graph.setEdgeWeight("2->3", 0.35);
		graph.addEdge(2, 4, "2->4");
		graph.setEdgeWeight("2->4", 0.45);
		graph.addEdge(3, 5, "3->5");
		graph.setEdgeWeight("3->5", 0.25);
		graph.addEdge(4, 5, "4->5");
		graph.setEdgeWeight("4->5", 0.35);
		graph.addEdge(5, 6, "5->6");
		graph.setEdgeWeight("5->6", 0.45);
		graph.addEdge(4, 2, "4->2");	
		graph.setEdgeWeight("4->2", 0.55);	
		graph.addEdge(4, 7, "4->7");
		graph.setEdgeWeight("4->7", 0.25);
		graph.addEdge(7, 1, "7->1");
		graph.setEdgeWeight("7->1", 0.05);
	}
	
	@Test
	public void testTracingWeight() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(graph);
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(1, true, 2);
		assertFalse(tracing.isLimitReached());
		assertEquals(4, paths.size());
		assertTrue(containsPath(paths, "1->2", "2->4", "4->7", "7->1"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->2"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->5", "5->6"));
		assertTrue(containsPath(paths, "1->2", "2->3", "3->5", "5->6"));
		Graph<Integer, String> g = 
				tracing.asGraph(paths);
		assertEquals(9, g.edgeSet().size());
		assertTrue(g.containsEdge("1->2"));
		assertTrue(g.containsEdge("2->3"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("5->6"));
		assertTrue(g.containsEdge("7->1"));
		assertTrue(g.containsEdge("4->2"));
		
		Map<Integer, Double> weights = tracing.getMimimumWeights(tracing.asGraph(paths), 1, null);
		assertEquals(0.25, weights.get(2), 0.0001);
		assertEquals(0.6, weights.get(3), 0.0001);
		assertEquals(0.7, weights.get(4), 0.0001);
		assertEquals(0.85, weights.get(5), 0.0001);
		assertEquals(1.3, weights.get(6), 0.0001);
		assertEquals(0.95, weights.get(7), 0.0001);
		
		
		paths = tracing.getAllPaths(2, true, 2);
		assertTrue(containsPath(paths, "2->4", "4->7", "7->1", "1->2"));
		assertTrue(containsPath(paths, "2->4", "4->2"));
		assertTrue(containsPath(paths, "2->4", "4->5", "5->6"));
		assertTrue(containsPath(paths, "2->3", "3->5", "5->6"));
		g = tracing.asGraph(paths);
		assertEquals(9, g.edgeSet().size());
		assertTrue(g.containsEdge("2->3"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("5->6"));
		assertTrue(g.containsEdge("7->1"));
		
		weights = tracing.getMimimumWeights(tracing.asGraph(paths), 2, null);
		assertEquals(0.75, weights.get(1), 0.0001);
		assertEquals(0.35, weights.get(3), 0.0001);
		assertEquals(0.45, weights.get(4), 0.0001);
		assertEquals(0.6, weights.get(5), 0.0001);
		assertEquals(1.05, weights.get(6), 0.0001);
		assertEquals(0.7, weights.get(7), 0.0001);
	}
	
	@Test
	public void testTracingMaxEdges() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(graph);
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(1, true, o -> true, 5);
		Graph<Integer, String> g = tracing.asGraph(paths);
		assertEquals(5, g.edgeSet().size());
		
		assertTrue(tracing.isLimitReached());

		paths = tracing.getAllPaths(1, true, o -> true, 4);
		g = tracing.asGraph(paths);
		assertEquals(4, g.edgeSet().size());
		
		assertTrue(tracing.isLimitReached());

		paths = tracing.getAllPaths(1, true, o -> true, 3);
		g = tracing.asGraph(paths);
		assertEquals(3, g.edgeSet().size());

		assertTrue(tracing.isLimitReached());
	}
	
	@Test
	public void testTracingWeightCyclesAllowed() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(graph);
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(1, false, 1.1);
		assertEquals(4, paths.size());
		assertTrue(containsPath(paths, "1->2", "2->4", "4->5", "5->6"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->2"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->7", "7->1", "1->2"));
		assertTrue(containsPath(paths, "1->2", "2->3", "3->5", "5->6"));
		Graph<Integer, String> g = 
				tracing.asGraph(paths);
		assertEquals(9, g.edgeSet().size());
		assertTrue(g.containsEdge("1->2"));
		assertTrue(g.containsEdge("2->3"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("5->6"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("7->1"));
	}
	
	@Test
	public void testTracingWeightCyclesNotAllowed() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(graph);
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(1, true, 1.1);
		assertEquals(4, paths.size());
		assertTrue(containsPath(paths, "1->2", "2->4", "4->5", "5->6"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->2"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->7", "7->1"));
		assertTrue(containsPath(paths, "1->2", "2->3", "3->5", "5->6"));
		Graph<Integer, String> g = 
				tracing.asGraph(paths);
		assertEquals(9, g.edgeSet().size());
		assertTrue(g.containsEdge("1->2"));
		assertTrue(g.containsEdge("2->3"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("5->6"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("7->1"));
	}
	
	@Test
	public void testTracingWeightReversed() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(
						new EdgeReversedGraph<>(graph));
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(6, true, 1);
		assertEquals(2, paths.size());
		assertTrue(containsPath(paths, "5->6", "4->5", "2->4"));
		assertTrue(containsPath(paths, "5->6", "3->5", "2->3"));
		Graph<Integer, String> g = 
				tracing.asGraph(paths);
		assertEquals(5, g.edgeSet().size());
		assertTrue(g.containsEdge("5->6"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("2->3"));		
	}
	
	@Test
	public void testTracingPredicate() {
		Tracing<Integer, String> tracing = 
				new Tracing<>(graph);
		List<GraphPath<Integer, String>> paths = 
				tracing.getAllPaths(1, true, 
				p -> !p.getVertexList().contains(5));
		assertFalse(tracing.isLimitReached());
		assertEquals(4, paths.size());
		assertTrue(containsPath(paths, "1->2", "2->4", "4->7", "7->1"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->5"));
		assertTrue(containsPath(paths, "1->2", "2->3", "3->5"));
		assertTrue(containsPath(paths, "1->2", "2->4", "4->2"));
		Graph<Integer, String> g = 
				tracing.asGraph(paths);
		assertEquals(8, g.edgeSet().size());
		assertTrue(g.containsEdge("1->2"));
		assertTrue(g.containsEdge("2->3"));
		assertTrue(g.containsEdge("2->4"));
		assertTrue(g.containsEdge("4->7"));
		assertTrue(g.containsEdge("3->5"));
		assertTrue(g.containsEdge("4->5"));
		assertTrue(g.containsEdge("4->2"));
		assertTrue(g.containsEdge("7->1"));

	}
	
	private boolean containsPath(Collection<GraphPath<Integer, String>> paths,
			String... edges) {
		
		for (GraphPath<Integer, String> path : paths) {
			if (path.getEdgeList().size() == edges.length) {
				boolean isTheSame = true;
				for (int i = 0; i < edges.length; i++) {
					if (!edges[i].equals(path.getEdgeList().get(i))) {
						isTheSame = false;
						break;
					}
				}
				if (isTheSame) {
					return true;
				}				
			}
		}		
		return false;
		
	}

}
