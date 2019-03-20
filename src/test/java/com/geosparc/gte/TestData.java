/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.gte;

import org.geotools.data.collection.CollectionFeatureSource;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.opengis.feature.simple.SimpleFeatureType;

public class TestData {
	
	private static final GeometryFactory geomFac = new GeometryFactory();
	
	protected static SimpleFeatureType buildLogicalEdgeType() throws Exception {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.add("id", String.class);
		typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
		typeBuilder.add("from", String.class);
		typeBuilder.add("to", String.class);
		typeBuilder.add("code", String.class);
		typeBuilder.setName("logicalEdge");
		return typeBuilder.buildFeatureType();
	}
	
	protected static SimpleFeatureType buildLogicalNodeType() throws Exception {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.add("id", String.class);
		typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
		typeBuilder.add("ref", String.class);
		typeBuilder.add("code", String.class);
		typeBuilder.setName("logicalNode");
		return typeBuilder.buildFeatureType();
	}
	
	protected static SimpleFeatureType buildGeoEdgeType() throws Exception {
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.add("id", String.class);
		typeBuilder.add("geom", Geometry.class, CRS.decode("EPSG:4326"));
		typeBuilder.add("code", String.class);
		typeBuilder.setName("geoEdge");
		return typeBuilder.buildFeatureType();
	}
	
	public static SimpleFeatureSource getLogicalNodes() throws Exception {
		SimpleFeatureType type = buildLogicalNodeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first node
		builder.add("1");
		builder.add(geomFac.createPoint(new Coordinate(0,0)));
		collection.add(builder.buildFeature("1"));
		//second node
		builder.add("2");
		builder.add(geomFac.createPoint(new Coordinate(3,3)));
		builder.add(null);
		builder.add("x");
		collection.add(builder.buildFeature("2"));
		//third node
		builder.add("3");
		builder.add(geomFac.createPoint(new Coordinate(2,6)));
		builder.add("c");
		builder.add("y");
		collection.add(builder.buildFeature("3"));
		return new CollectionFeatureSource(collection);
	}
	
	public static SimpleFeatureSource getLogicalNodes2() throws Exception {
		SimpleFeatureType type = buildLogicalNodeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first node
		builder.add("a");
		builder.add(geomFac.createPoint(new Coordinate(0,0)));
		collection.add(builder.buildFeature("1"));
		//second node
		builder.add("b");
		builder.add(geomFac.createPoint(new Coordinate(2,2)));
		collection.add(builder.buildFeature("2"));
		//third node
		builder.add("c");
		builder.add(geomFac.createPoint(new Coordinate(3,3)));
		collection.add(builder.buildFeature("2"));
		return new CollectionFeatureSource(collection);
	}
	
	public static SimpleFeatureSource getLogicalEdges() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first edge
		builder.add("12");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(0,0), new Coordinate(3,3) }));
		builder.add("1");
		builder.add("2");
		collection.add(builder.buildFeature("12"));
		//second edge
		builder.add("23");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(3,3), new Coordinate(5,5) }));
		builder.add("2");
		builder.add("3");
		builder.add("x");
		collection.add(builder.buildFeature("23"));	
		//third edge
		builder.add("13");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(0,0), new Coordinate(5,5) }));
		builder.add("1");
		builder.add("3");	
		collection.add(builder.buildFeature("13"));		

		return new CollectionFeatureSource(collection);
	}
	
	public static SimpleFeatureSource getGeoEdges() throws Exception {
		SimpleFeatureType type = buildLogicalEdgeType();
		MemoryFeatureCollection collection = new MemoryFeatureCollection(type);
		SimpleFeatureBuilder builder = new SimpleFeatureBuilder(type);
		//first edge
		builder.add("a");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(0,0), new Coordinate(3,3) }));
		collection.add(builder.buildFeature("a"));
		//second edge
		builder.add("b");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(3.0001,2.9999), new Coordinate(4,4) }));
		collection.add(builder.buildFeature("b"));	
		//third edge
		builder.add("c");
		builder.add(geomFac.createLineString(new Coordinate[] {
				new Coordinate(3.001, 2.999), new Coordinate(5,5) }));
		builder.add(null);
		builder.add(null);
		builder.add("x");
		collection.add(builder.buildFeature("c"));

		return new CollectionFeatureSource(collection);
	}

}
