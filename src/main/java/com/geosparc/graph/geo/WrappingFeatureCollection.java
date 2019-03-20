/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.BaseSimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;

/**
 * 
 * Wraps a list of feature IDP's into a feature collection
 * used for export to shape file.
 * 
 * @author Niels Charlier
 *
 */
public class WrappingFeatureCollection extends BaseSimpleFeatureCollection {
	
	private Collection<Idp<GlobalId, SimpleFeature>> delegate;
	
	protected WrappingFeatureCollection(Collection<Idp<GlobalId, SimpleFeature>> delegate) {
		super(delegate.iterator().next().getData().getType());
		this.delegate = delegate;
	}

	@Override
	public SimpleFeatureIterator features() {
		
		Iterator<Idp<GlobalId, SimpleFeature>> iterator = delegate.iterator();
		
		return new SimpleFeatureIterator() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public SimpleFeature next() throws NoSuchElementException {
				return iterator.next().getData();
			}

			@Override
			public void close() {
				
			}
			
		};
	}

}
