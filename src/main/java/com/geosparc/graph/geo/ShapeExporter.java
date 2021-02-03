package com.geosparc.graph.geo;

import com.geosparc.graph.base.Idp;
import com.geosparc.gte.engine.GraphTracingResult;
import com.google.common.base.Predicates;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.geotools.data.shapefile.ShapefileDumper;
import org.jgrapht.Graph;
import org.jgrapht.graph.MaskSubgraph;
import org.jgrapht.io.ExportException;
import org.jgrapht.io.GraphExporter;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;

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

	private void dumpOverlapAreas(ShapefileDumper dumper, String overlapType,
			List<SimpleFeature> features) throws IOException {
		if (features.size() > 0) {
			dumper.dump(overlapType.replace(" ", "_") + "-areas",
					new OverlapAreaFeatureCollection(features));
		}
	}

	@Override
	public void exportGraph(Graph<Idp<GlobalId, SimpleFeature>, 
			Idp<GlobalId, SimpleFeature>> graph, OutputStream os)
			throws ExportException {
		exportGraph(graph, null, os);
	}

	public void exportGraph(GraphTracingResult result, OutputStream os)
			throws ExportException {
		exportGraph(result.getGraph(), result.getAreas(), os);
	}

	private void exportGraph(
			Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> graph,
			Map<String, List<SimpleFeature>> areas,
			OutputStream os)
			throws ExportException {
		try {
			File tempDir = Files.createTempDirectory("vmm.shape").toFile();
			ShapefileDumper dumper = new ShapefileDumper(tempDir);
			dumper.setCharset(UTF_8);

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

			if (areas != null) {
				for (Map.Entry<String, List<SimpleFeature>> entry : areas.entrySet()) {
					dumpOverlapAreas(dumper, entry.getKey(), entry.getValue());
				}
			}
			
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
		exportGraph(g, new WriterOutputStream(writer, UTF_8));
	}
}
