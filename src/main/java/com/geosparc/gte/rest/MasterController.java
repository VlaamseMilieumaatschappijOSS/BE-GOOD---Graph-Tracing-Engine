package com.geosparc.gte.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class MasterController {	

	@GetMapping
	public ResponseEntity<?> index() {
//		return new RedirectView("actuator/health");
		return ResponseEntity.ok(null);
	}

}
