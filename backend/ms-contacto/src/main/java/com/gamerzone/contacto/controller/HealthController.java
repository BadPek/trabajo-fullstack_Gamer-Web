package com.gamerzone.contacto.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    @GetMapping
    @Operation(summary = "Comprobar el estado del servicio")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El servicio está disponible.")
    })
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
