/*
 * Graph Tracing Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */

package com.geosparc.graph.geo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.geotools.data.shapefile.ShapefileDumper;
import org.jgrapht.Graph;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;
import com.google.common.base.Predicates;

/**
 * 
 * Exports feature graph to shapefile.
 * 
 * @author Niels Charlier
 *
 */
public class ShapeExporter implements GraphExporter<Idp<GlobalId, SimpleFeature>, 
	Idp<GlobalId, SimpleFeature>> {
	
	private List<String> networks;
	
	public ShapeExporter(List<String> networks) {
		this.networks = networks;
	}
	
	private void dumpSubgraph(ShapefileDumper dumper, String network,
			Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph, 
			Predicate<Idp<GlobalId, SimpleFeature>> predicate) throws IOException {
		dumpSubgraph(dumper, network, graph, predicate, predicate);
	}
	
	private void dumpSubgraph(ShapefileDumper dumper, String network, 
			Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph, 
			Predicate<Idp<GlobalId, SimpleFeature>> predicateEdges,
			Predicate<Idp<GlobalId, SimpleFeature>> predicateVertices) throws IOException {
		
		MaskSubgraph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> maskGraph = 
				new MaskSubgraph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>>(
				graph, predicateVertices, Predicates.alwaysFalse());
		
		if (maskGraph.vertexSet().size() > 0) {
			dumper.dump(network + "-nodes", new WrappingFeatureCollection(maskGraph.vertexSet()));
		}	
		
		maskGraph = 
				new MaskSubgraph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>>(
				graph, Predicates.alwaysFalse(), predicateEdges);
		
		if (maskGraph.edgeSet().size() > 0) {
			dumper.dump(network + "-edges", new WrappingFeatureCollection(maskGraph.edgeSet()));
		}
	}

	@Override
	public void exportGraph(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> graph, OutputStream os)
			throws ExportException {
		try {
			File tempDir = Files.createTempDirectory("vmm.shape").toFile();
			ShapefileDumper dumper = new ShapefileDumper(tempDir);

			for (String network : networks) {
				dumpSubgraph(dumper, network, graph, 
						o -> {
							return !network.equals(o.getId().getNetwork()) ||
									FeatureGraphBuilder.CONNECTION_EDGE.equals(
											o.getData().getType().getName().getLocalPart());
						});
			}

			dumpSubgraph(dumper, "connections", graph,
					o -> {
						return !o.getData().getType().getName().getLocalPart()
								.equals(FeatureGraphBuilder.CONNECTION_EDGE);
					}, o -> {return !o.getData().getType().getName().getLocalPart()
							.equals(FeatureGraphBuilder.CONNECTION_VERTEX);
					});
			
			customize(tempDir);
			
			try (ZipOutputStream zipStream = new ZipOutputStream(os)) {
				for (File file : tempDir.listFiles()) {
					zipStream.putNextEntry(new ZipEntry(file.getName()));
					try(InputStream is = new FileInputStream(file)) {
						IOUtils.copy(is, zipStream);
					}
				}
			}
			
			//cleanup
			FileUtils.deleteDirectory(tempDir);
		} catch (IOException e) {
			throw new ExportException(e);
		}
	}
	
	public void customize(File directory) throws IOException {
		//hook
	}

	@Override
	public void exportGraph(Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> g, Writer writer)
			throws ExportException {
		exportGraph(g, new WriterOutputStream(writer, StandardCharsets.UTF_8));
		
	}
	
	

}
