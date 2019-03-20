/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

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
