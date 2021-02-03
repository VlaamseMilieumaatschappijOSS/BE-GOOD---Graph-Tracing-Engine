package com.geosparc.graph.base;

import java.util.HashMap;
import java.util.Map;

import org.jgrapht.graph.DirectedMultigraph;


/**
 * Graph that attaches data to edge and vertex identifiers and can
 * manage edges based on vertex identifiers
 * 
 * @author niels
 *
 * @param <I>
 * @param <V>
 * @param <E>
 */
public class DGraph <I, V, E> 
	extends DirectedMultigraph<Idp<I, V>, Idp<I, E>> {
	
	private static final long serialVersionUID = -2482245233022976918L;
	
	private Map<I, Idp<I, V>> vertices
		= new HashMap<>();
	
	private Map<I, Idp<I, E>> edges
		= new HashMap<>();
	
	public DGraph(boolean weighted) {
		super(null, null, weighted);
	}
	
	@Override
    public boolean addVertex(Idp<I, V> v) {
		if (super.addVertex(v)) {	
			vertices.put(v.getId(), v);
			return true;
		} else {
			return false;
		}
	}
	
	public Idp<I, V> addVertexById(I vertexId) {
		Idp<I, V> e = new Idp<>(vertexId);
    	if (!addVertex(e)) {
    		throw new IllegalArgumentException("Vertex already added: " + vertexId);
    	}
    	return e;
	}
	
	@Override
    public Idp<I, V> addVertex() {
		Idp<I, V> v = super.addVertex(); 
		vertices.put(v.getId(), v);
		return v;
	}
	
	@Override
    public boolean addEdge(Idp<I, V> from, Idp<I, V> to, 
    		Idp<I, E> edge) {
		if (super.addEdge(from, to, edge)) {
			edges.put(edge.getId(), edge);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
    public Idp<I, E> addEdge(Idp<I, V> from, Idp<I, V> to) {
		Idp<I, E> e = super.addEdge(from, to); 
		edges.put(e.getId(), e);
		return e;
	}
    	
    public Idp<I, E> addEdgeById(I sourceId, I targetId, I edgeId) {
    	Idp<I, E> e = new Idp<>(edgeId);
    	Idp<I, V> sourceV = vertices.get(sourceId);
    	Idp<I, V> targetV = vertices.get(targetId);
    	if (sourceV == null) {
    		throw new IllegalArgumentException("No such vertex: " + sourceId);
    	} else if (targetV == null) {
    		throw new IllegalArgumentException("No such vertex: " + targetId);
    	}
    	if (!addEdge(sourceV, targetV, e)) {
    		throw new IllegalArgumentException("Edge already added: " + edgeId);
    	}
    	return e;
    }
    
    public Idp<I, E> getEdgeByIds(I sourceId, I targetId) {
    	return getEdge(vertices.get(sourceId), vertices.get(targetId));
    }
	
    public Idp<I, V> getVertexById(I vertexId) {
    	return vertices.get(vertexId);
    }
    
    public Idp<I, E> getEdgeById(I edgeId) {
    	return edges.get(edgeId);
    }

	public void removeVertexById(I vertexId) {
		removeVertex(vertices.get(vertexId));
	}
	
	public void removeEdgeById(I edgeId) {
		removeEdge(edges.get(edgeId));
	}
	
	public void removeEdgeByIds(I sourceId, I targetId) {
		removeEdge(vertices.get(sourceId), vertices.get(targetId));
	}
}
