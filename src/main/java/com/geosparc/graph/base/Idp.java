package com.geosparc.graph.base;

/**
 * ID and DATA pair
 * 
 * @author niels
 *
 * @param <T>
 * @param <S>
 */
public class Idp<T, S> {
	
	private T id;
	
	private S data;
	
	public Idp(T id) {
		this.id = id;
	}

	public Idp(T id, S data) {
		this.id = id;
		this.data = data;
	}
	
	public T getId() {
		return id;
	}
	
	public S getData() {
		return data;
	}

	public void setData(S data) {
		this.data = data;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object other) {
		return other.getClass().equals(getClass())
				&& id.equals(((Idp<?,?>) other).getId());
	}
	
	public String toString() {
		return id.toString();
	}

}
