package com.samadhanx.module.demo.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.module.demo.service.DemoDataSeederService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
@Tag(name = "Demo & Presenter Mode", description = "One-command demo reset and ecosystem seeder for live presentation")
public class DemoResetController {

    private final DemoDataSeederService demoDataSeederService;

    @PostMapping("/reset-and-seed")
    @Operation(summary = "Reset & seed complete predictable demo ecosystem", description = "Safe developer utility to seed realistic Varanasi/Chandauli demo data")
    public ResponseEntity<ApiResponse<Map<String, String>>> resetAndSeed() {
        demoDataSeederService.resetAndSeedCompleteEcosystem();
        return ResponseEntity.ok(ApiResponse.ok("Demo ecosystem seeded successfully", Map.of(
                "status", "READY",
                "message", "SamadhanX demo ecosystem reset with verified Varanasi and Chandauli testbeds"
        )));
    }

    @GetMapping("/status")
    @Operation(summary = "Check demo data presence")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkStatus() {
        boolean present = demoDataSeederService.isDemoDataPresent();
        return ResponseEntity.ok(ApiResponse.ok("Demo data status", Map.of("demoDataPresent", present)));
    }
}
