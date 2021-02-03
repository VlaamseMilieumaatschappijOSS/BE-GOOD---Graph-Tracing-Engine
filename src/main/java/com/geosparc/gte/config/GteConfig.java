package com.geosparc.gte.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
	
	public AreasConfig findAreasByName(String name) {
		for (AreasConfig areasConfig : areas) {
			if (areasConfig.getName().equals(name)) {
				return areasConfig;
			}
		}
		return null;
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