/*
 * Graph Trace Engine
 * 
 * (c) Copyright 2019 Vlaamse Milieumaatschappij (VMM)
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. 
 * You may obtain may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 
 * 
 */
package com.geosparc.gte.rest;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.base.JsonWrapper;
import com.geosparc.graph.geo.FeatureJsonExporter;
import com.geosparc.graph.geo.GlobalId;
import com.geosparc.graph.geo.ShapeExporter;
import com.geosparc.gte.engine.GraphTracingEngine;
import com.geosparc.gte.engine.GraphTracingResult;
import com.geosparc.gte.rest.TraceRequest.TraceRequestNetwork;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.util.logging.Logging;
import org.jgrapht.Graph;
import org.jgrapht.io.ExportException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Debug Tools
 *
 * @author Niels Charlier
 *
 */
@RestController
@Api(tags = "Tracing Tools")
public class TraceController {

	private static final Logger LOGGER = Logging.getLogger(TraceController.class);

	@Autowired
	private GraphTracingEngine engine;

    @Autowired
    private MessageSource messageSource;

	@ExceptionHandler({IllegalArgumentException.class, CQLException.class,
		HttpMessageNotReadableException.class})
    public void illegalArgument(
    		Exception exception,
    		HttpServletRequest request,
    		HttpServletResponse response)
            throws IOException {
        response.sendError(400, messageSource.getMessage(exception.getMessage(),
        		new Object[] {}, request.getLocale()));
    }

	@ExceptionHandler({IOException.class, ExportException.class})
    public void ioException(
    		Exception exception,
    		HttpServletRequest request,
    		HttpServletResponse response)
            throws IOException {
		LOGGER.log(Level.SEVERE, exception.getMessage(), exception);
        response.sendError(500, exception.getMessage());
    }

	@SuppressWarnings("unchecked")
	@ApiOperation("Perform a trace and retrieve the result in json.")
	@PostMapping(value = "/trace", produces = "application/json")
	public void trace(@RequestBody TraceRequest request,
			HttpServletResponse response,
			Locale locale) throws CQLException, ExportException, IOException {

		GraphTracingResult result = getGraphTracingResult(request);

		JSONObject jsonResult = new JSONObject();
		jsonResult.put("graph", new JsonWrapper<>(result.getGraph(),
				new FeatureJsonExporter() {
			@Override
			public void ppVertex(GlobalId id, JSONObject vertexWrapper) {
				Double distance = result.getDistances().get(id);
				if (distance != null) {
					vertexWrapper.put("distance", distance);
				}
			}

			@Override
			public void ppEdge(GlobalId id, JSONObject edgeWrapper) {
				for(Entry<String, Map<GlobalId, Object>> entry :
					result.getAggregates().entrySet()) {
					Object agg = entry.getValue().get(id);
					if (agg != null) {
						edgeWrapper.put(entry.getKey(), agg);
					}
				}
			}

			@Override
			public Collection<Idp<GlobalId, SimpleFeature>> edges(
					Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> g) {
				return result.edges();
			}

			@Override
			public Collection<Idp<GlobalId, SimpleFeature>> vertices(
					Graph<Idp<GlobalId, SimpleFeature>, Idp<GlobalId, SimpleFeature>> g) {
				return result.vertices();
			}

		}));

		if (request.isIncludeOverlappingAreas()) {
			JSONObject jsonAreas = new JSONObject();
			for (Entry<String, List<SimpleFeature>> entry : result.getAreas().entrySet()) {
				JSONArray array = new JSONArray();
				for (SimpleFeature area : entry.getValue()) {
					array.add(new JSONStreamAware() {
						@Override
						public void writeJSONString(Writer out) throws IOException {
							new FeatureJSON().writeFeature(area, out);
						}
					});
				}
				jsonAreas.put(entry.getKey(), array);
			}
			jsonResult.put("overlappingAreas", jsonAreas);
		}

		jsonResult.put("warnings", getWarnings(result, locale));

		response.setContentType("application/json");
		jsonResult.writeJSONString(response.getWriter());
	}

	public List<String> getWarnings(GraphTracingResult result, Locale locale) {
		List<String> warnings = new ArrayList<>();
		if (result.isLimitReached()) {
			warnings.add(messageSource.getMessage("max_edges_reached",
	        		new Object[] {}, locale));
		}
		return warnings;
	}

	@ApiOperation("Perform a trace and retrieve the result as zip with shapefiles.")
	@PostMapping(value = "/trace-shape", produces = "application/zip")
	public void traceAsZip(@RequestBody TraceRequest request,
			HttpServletResponse response, Locale locale)
					throws CQLException, ExportException, IOException {

		GraphTracingResult result = getGraphTracingResult(request);

		response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=tracing.zip");
        new ShapeExporter(engine.getNetworks()) {
			@Override
			public void customize(File directory) throws IOException {
				try(BufferedWriter writer =
						new BufferedWriter(new FileWriter(
								new File(directory, "trace_info.txt")))) {
					//TODO: use international messages here
					writer.write("Date: " + new Date() + "\r\n");
					writer.write("StartNetwork: " + request.getStartNetwork() + "\r\n");
					writer.write("StartID: " + request.getStartId() + "\r\n");
					writer.write("Upstream: " + request.isUpstream() + "\r\n");
					if (request.getLimit() != null) {
						writer.write("Limit: " + request.getLimit() + "\r\n");
					}
					if (request.getMaxDistance() != null) {
						writer.write("MaxDistance: " + request.getMaxDistance() + "\r\n");
					}
					writer.write("Networks: ");
					for (TraceRequestNetwork network : request.getNetworks()) {
						writer.write(network.getName() + " ");
					}
					writer.write("\r\n");
					for (TraceRequestNetwork network : request.getNetworks()) {
						if (network.getMaxDistance() != null) {
							writer.write(network.getName() + " MaxDistance: " + network.getMaxDistance() + "\r\n");
						}
						if (network.getNodeFilter() != null) {
							writer.write(network.getName() + " NodeFilter: " + network.getNodeFilter() + "\r\n");
						}
						if (network.getEdgeFilter() != null) {
							writer.write(network.getName() + " EdgeFilter: " + network.getEdgeFilter() + "\r\n");
						}
					}
					for (String warning : getWarnings(result, locale)) {
						writer.write("Warning: " + warning);
					}
				}

			}
		}.exportGraph(result.getGraph(),
				response.getOutputStream());
	}

	private GraphTracingResult getGraphTracingResult(@RequestBody TraceRequest request) throws CQLException {
		if (request.getStartNetwork() == null) {
			throw new IllegalArgumentException("missing_start_network");
		}

		if (request.getStartId() == null) {
			throw new IllegalArgumentException("missing_start_id");
		}

		if (request.getNetworks() == null) {
			throw new IllegalArgumentException("missing_networks");
		}

		return engine.trace(
				new GlobalId(request.getStartNetwork(), request.getStartId()),
				request.getMaxDistance(),
				request.getNetworks().stream().map(n -> n.getName()).collect(Collectors.toList()),
				request.getNetworks().stream().map(n -> n.getNodeFilter()).collect(Collectors.toList()),
				request.getNetworks().stream().map(n -> n.getEdgeFilter()).collect(Collectors.toList()),
				request.getNetworks().stream().map(n -> n.getMaxDistance()).collect(Collectors.toList()),
				request.getNetworks().stream().map(n -> n.getEdgeAggregates()).collect(Collectors.toList()),
				request.isUpstream(),
				request.isIncludeOverlappingAreas(),
				request.getLimit());
	}

}
