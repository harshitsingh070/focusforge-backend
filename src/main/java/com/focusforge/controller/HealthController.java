package com.focusforge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    // Deliberately lightweight: returns immediately without touching the database
    // or any service. Liveness probes (Render, the self-ping, load balancers) must
    // never be a source of load or latency themselves, and must stay responsive
    // even if a backing dependency (DB/Redis) is down. Served at /api/health because
    // of the server.servlet.context-path=/api setting.
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
