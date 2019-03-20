/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.config;

import org.apache.logging.log4j.util.Strings;

public class ConnectionConfig {
	
	public enum ConnectionType {
		/* connect to start node */
		START,
		/* connect to end node */
		END,
		/* project and create new node */
		PROJECTED
	}
	
	/**
	 * Name of first network, this must be a logical network
	 * (so we have nodes)
	 */
	private String sourceNetwork;
	
	/**
	 * Name of second network
	 */
	private String targetNetwork;
	
	/**
	 * Attribute in the nodes of network sourceNetwork that references network targetNetwork
	 */
	private String referenceAttribute;
	
	/**
	 * If the second network is geographical instead of logical,
	 * we need to know how to connect to it
	 */
	private ConnectionType connectionType;

	public String getSourceNetwork() {
		return sourceNetwork;
	}

	public void setSourceNetwork(String sourceNetwork) {
		this.sourceNetwork = sourceNetwork;
	}

	public String getTargetNetwork() {
		return targetNetwork;
	}

	public void setTargetNetwork(String targetNetwork) {
		this.targetNetwork = targetNetwork;
	}

	public String getReferenceAttribute() {
		return referenceAttribute;
	}

	public void setReferenceAttribute(String referenceAttribute) {
		this.referenceAttribute = referenceAttribute;
	}

	public ConnectionType getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(ConnectionType connectionType) {
		this.connectionType = connectionType;
	}
	
	public void validate() {
		if (Strings.isEmpty(getSourceNetwork())) {
			throw new IllegalStateException("Connection found with missing first network.");
		}
		if (Strings.isEmpty(getTargetNetwork())) {
			throw new IllegalStateException("Connection found with missing second network.");	
		}
		//if (Strings.isEmpty(getReferenceAttribute())) {
		//	throw new IllegalStateException("Reference missing in connection " + sourceNetwork + " -> " + targetNetwork);
		//}
	}
	

}
