/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@EnableConfigurationProperties
@ConfigurationProperties
@Component
public class GteConfig {
	
	private List<NetworkConfig> networks = new ArrayList<>();
	
	private List<ConnectionConfig> connections = new ArrayList<>();

	private List<AreasConfig> areas = new ArrayList<>();
	
	private String frequency;

	public List<NetworkConfig> getNetworks() {
		return networks;
	}
	
	public NetworkConfig findNetworkByName(String name) {
		for (NetworkConfig network : networks) {
			if (network.getName().equals(name)) {
				return network;
			}
		}
		return null;
	}

	public void setNetworks(List<NetworkConfig> networks) {
		this.networks = networks;
	}

	public List<ConnectionConfig> getConnections() {
		return connections;
	}

	public void setConnections(List<ConnectionConfig> connections) {
		this.connections = connections;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}	

	public List<AreasConfig> getAreas() {
		return areas;
	}

	public void setAreas(List<AreasConfig> areas) {
		this.areas = areas;
	}
	
	public void validate() {
		for (NetworkConfig networkConfig : getNetworks()) {
			networkConfig.validate();
		}
		for (ConnectionConfig connConfig : getConnections()) {
			connConfig.validate();
		}
		for (AreasConfig areasConfig : getAreas()) {
			areasConfig.validate();
		}
	}
	
	
	
}