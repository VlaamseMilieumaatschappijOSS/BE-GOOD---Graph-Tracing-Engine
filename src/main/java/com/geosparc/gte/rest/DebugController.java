package com.geosparc.gte.rest;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.jgrapht.io.ExportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.geosparc.graph.geo.FeatureJsonExporter;
import com.geosparc.graph.geo.ShapeExporter;
import com.geosparc.gte.engine.GraphTracingEngine;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Debug Tools
 * 
 * @author niels
 *
 */
@RestController
@Api(tags = "Debug Tools")
@RequestMapping("/debug")
public class DebugController {

	@Autowired
	private GraphTracingEngine engine;

	@ApiOperation("Get the entire network graph as JSON.")
	@GetMapping(value = "/graph", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public void getGraph(
			@ApiParam("True if you want only features, and no graph structure.")
			@RequestParam(required = false, defaultValue = "false")
			boolean flat,
			HttpServletResponse response) throws ExportException, IOException {
		new FeatureJsonExporter().exportGraph(engine.getGraph(),
				response.getOutputStream());
	}

	@ApiOperation("Write the entire graph to a shapefile in the working directory.")
	@GetMapping(value = "/graph-shape", produces = "application/zip")
	public void getGraphAsShape(
			HttpServletResponse response) throws IOException {
		try {
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=graph.zip");
			new ShapeExporter(engine.getNetworks())
					.exportGraph(engine.getGraph(), response.getOutputStream());
		} catch (ExportException e) {
			throw new IOException(e);
		}
	}

}
