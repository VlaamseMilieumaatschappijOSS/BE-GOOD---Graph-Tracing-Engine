package com.geosparc.gte.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureConfig {
	
	private String name;
	
	private String idAttribute;
		
	private List<String> filterAttributes;

	private List<String> userAttributes;
	
	private List<AggregateConfig> aggregatedAttributes;
		
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdAttribute() {
		return idAttribute;
	}

	public void setIdAttribute(String idAttribute) {
		this.idAttribute = idAttribute;
	}

	public List<String> getFilterAttributes() {
		return filterAttributes;
	}

	public void setFilterAttributes(List<String> filterAttributes) {
		this.filterAttributes = filterAttributes;
	}

	public List<String> getUserAttributes() {
		return userAttributes;
	}

	public void setUserAttributes(List<String> userAttributes) {
		this.userAttributes = userAttributes;
	}
		
	public List<AggregateConfig> getAggregatedAttributes() {
		return aggregatedAttributes;
	}

	public void setAggregatedAttributes(List<AggregateConfig> aggregatedAttributes) {
		this.aggregatedAttributes = aggregatedAttributes;
	}

	public List<String> getAllAttributes() {
		Set<String> properties = new HashSet<String>();
		if (!"@id".equalsIgnoreCase(getIdAttribute())) {
			properties.add(getIdAttribute());			
		}
		properties.addAll(getUserAttributes());
		properties.addAll(getFilterAttributes());
		if (getAggregatedAttributes() != null) {
			for (AggregateConfig agg : getAggregatedAttributes()) {
				properties.add(agg.getSource());
			}
		}
		return new ArrayList<String>(properties);
	}
}
