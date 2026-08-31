package com.samadhanx.module.organization.controller;

import com.samadhanx.common.response.ApiResponse;
import com.samadhanx.module.organization.dto.CreateDomainRequest;
import com.samadhanx.module.organization.dto.DomainResponse;
import com.samadhanx.module.organization.service.DomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/domains")
@RequiredArgsConstructor
@Tag(name = "Societal Domains / Sectors", description = "Taxonomy of problem and innovation sectors (Water, Agri-Tech, Energy, Health, etc.)")
public class DomainController {

    private final DomainService domainService;

    @GetMapping
    @SecurityRequirements // Public endpoint
    @Operation(summary = "List all active societal domains", description = "Retrieves active domain focus sectors for challenges and organizations")
    public ResponseEntity<ApiResponse<List<DomainResponse>>> getAllDomains() {
        List<DomainResponse> domains = domainService.getAllActiveDomains();
        return ResponseEntity.ok(ApiResponse.ok("Societal domains retrieved successfully", domains));
    }

    @GetMapping("/{id}")
    @SecurityRequirements
    @Operation(summary = "Get domain by ID")
    public ResponseEntity<ApiResponse<DomainResponse>> getDomainById(@PathVariable UUID id) {
        DomainResponse domain = domainService.getDomainById(id);
        return ResponseEntity.ok(ApiResponse.ok(domain));
    }

    @GetMapping("/code/{code}")
    @SecurityRequirements
    @Operation(summary = "Get domain by unique code")
    public ResponseEntity<ApiResponse<DomainResponse>> getDomainByCode(@PathVariable String code) {
        DomainResponse domain = domainService.getDomainByCode(code);
        return ResponseEntity.ok(ApiResponse.ok(domain));
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create new societal domain", description = "Restricted to Super Admin")
    public ResponseEntity<ApiResponse<DomainResponse>> createDomain(@Valid @RequestBody CreateDomainRequest request) {
        DomainResponse response = domainService.createDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Societal domain created successfully", response));
    }
}
