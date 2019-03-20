/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.util.Strings;

public class AreasConfig {

	/**
	 * name for this set of areas
	 */
	private String name;	
	
	/**
	 * Data store 
	 */
	private Map<String, String> dataStore = new HashMap<>();
			
	/**
	 * native name (such as table name) if not the same as name
	 */
	private String nativeName;

	/**
	 * attributes 
	 */
	private List<String> attributes;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getDataStore() {
		return dataStore;
	}

	public void setDataStore(Map<String, String> dataStore) {
		this.dataStore = dataStore;
	}

	public String getNativeName() {
		return nativeName;
	}

	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}

	public List<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<String> attributes) {
		this.attributes = attributes;
	}

	public void validate() {
		if (Strings.isEmpty(getName())) {
			throw new IllegalStateException("Areas must have name.");
		}
		
	}
	
	

}
