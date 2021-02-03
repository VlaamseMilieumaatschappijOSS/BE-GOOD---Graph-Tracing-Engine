package com.geosparc.gte.config;

import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private String wmsLayerUrl;
	private String wmsLayerName;
	private String wmsStyleNaam;

	private String detailName;
	private String propertyForDetail;
	private String propertyForFilter;

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
		return parseDataStore(dataStore);
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

	public String getWmsLayerUrl() {
		return wmsLayerUrl;
	}

	public void setWmsLayerUrl(String wmsLayerUrl) {
		this.wmsLayerUrl = wmsLayerUrl;
	}

	public String getWmsLayerName() {
		return wmsLayerName;
	}

	public void setWmsLayerName(String wmsLayerName) {
		this.wmsLayerName = wmsLayerName;
	}

	public String getWmsStyleNaam() {
		return wmsStyleNaam;
	}

	public void setWmsStyleNaam(String wmsStyleNaam) {
		this.wmsStyleNaam = wmsStyleNaam;
	}

	public String getDetailName() {
		return detailName;
	}

	public void setDetailName(String detailName) {
		this.detailName = detailName;
	}

	public String getPropertyForDetail() {
		return propertyForDetail;
	}

	public void setPropertyForDetail(String propertyForDetail) {
		this.propertyForDetail = propertyForDetail;
	}

	public String getPropertyForFilter() {
		return propertyForFilter;
	}

	public void setPropertyForFilter(String propertyForFilter) {
		this.propertyForFilter = propertyForFilter;
	}

	public void validate() {
		if (Strings.isEmpty(getName())) {
			throw new IllegalStateException("Areas must have name.");
		}
	}

	// ----------------------------------------------------

	private Map<String, String> parseDataStore(Map<String, String> dataStore) {
		if (dataStore != null) {
			// -- add default Geotools DataStore Parameters
			dataStore.putIfAbsent("Expose primary keys", "true");
		}
		return dataStore;
	}
}
