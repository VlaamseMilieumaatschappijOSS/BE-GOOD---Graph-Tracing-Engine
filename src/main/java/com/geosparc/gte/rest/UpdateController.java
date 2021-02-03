package com.geosparc.gte.rest;

import com.geosparc.gte.engine.GraphStatus;
import com.geosparc.gte.engine.GraphTracingEngine;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jgrapht.io.ExportException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Oliver May
 */
@RestController
@Api(tags = "Update Tools")
@RequestMapping("/graph")
public class UpdateController {

    @Autowired
    private GraphTracingEngine engine;

    @ApiOperation("Trigger an update of the graph from the datasource.")
    @PostMapping(value = "/update", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void updateGraph(HttpServletResponse response) throws ExportException, IOException {
        if (engine.isUpdating()) {
            response.sendError(HttpStatus.SERVICE_UNAVAILABLE.value(), "Graph is already updating");
            return;
        }
        engine.reload();
    }

    @ApiOperation("Get the status of the graph.")
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public GraphStatus getGraphStatus() {
        return engine.getStatus();
    }
}

