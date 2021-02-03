package com.geosparc.graph.geo;

import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.collection.BaseSimpleFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * 
 *
 * 
 * @author Jan Venstermans
 *
 */
public class OverlapAreaFeatureCollection extends BaseSimpleFeatureCollection {

	private Collection<SimpleFeature> features;

	protected OverlapAreaFeatureCollection(Collection<SimpleFeature> features) {
		super(features.iterator().next().getType());
		this.features = features;
	}

	@Override
	public SimpleFeatureIterator features() {
		
		Iterator<SimpleFeature> iterator = features.iterator();
		
		return new SimpleFeatureIterator() {

			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public SimpleFeature next() throws NoSuchElementException {
				return iterator.next();
			}

			@Override
			public void close() {
				
			}
			
		};
	}

}
