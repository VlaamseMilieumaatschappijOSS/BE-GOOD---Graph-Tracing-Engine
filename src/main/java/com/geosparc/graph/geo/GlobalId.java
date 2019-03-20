/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

/**
 * Global ID is unique over all networks because it includes the network as part of its identity.
 * 
 * @author Niels Charlier
 *
 */
public class GlobalId {
	
	public enum Type {DATA, GENERATED};
	
	private String network;
	
	private String identifier;
	
	private Type type;
	
	public GlobalId(String network, String identifier) {
		this(network, identifier, Type.DATA);
	}
	
	public GlobalId(String network, String identifier, Type type) {
		assert(network != null);
		assert(identifier != null);
		this.network = network;
		this.identifier = identifier;
		this.type = type;
	}

	public String getNetwork() {
		return network;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return network + ":" + identifier;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof GlobalId
				&& ((GlobalId) other).network.equals(network)
				&& ((GlobalId) other).identifier.equals(identifier)
				&& ((GlobalId) other).type.equals(type);
	}
	
	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

}
