package com.ecommerce.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Health Check", description = "Simple health check endpoint for AWS ALB")
public class HealthCheckController {

    @GetMapping("/")
    @Operation(summary = "Health check", description = "Returns a simple 200 OK to indicate the service is running")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is running");
    }
}
