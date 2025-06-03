package com.easyliveline.streamingbackend.controllers;


import com.easyliveline.streamingbackend.dto.OwnerWithAdminUsername;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.OwnerService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    @PreAuthorize("hasRole('ADMIN') && hasAuthority('PERMISSION_CREATE_OWNER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Owner>> createOwner(@RequestBody OwnerCreateRequest requestBody) {
        Owner createdOwner = ownerService.createOwner(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_OWNER", createdOwner));
    }

    @PreAuthorize("hasRole('ADMIN') && hasAuthority('PERMISSION_READ_OWNERS')")
    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<OwnerWithAdminUsername>>> getFilteredOwners(@RequestBody FilterRequest filterRequest) {
        Page<OwnerWithAdminUsername> users = ownerService.getAllOwners(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("Filtered Owners", users));
    }
}
