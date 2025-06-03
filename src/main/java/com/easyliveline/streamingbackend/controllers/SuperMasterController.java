package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.SuperMasterWithOwnerUsername;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.SuperMasterService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/super-masters")
public class SuperMasterController {

    private final SuperMasterService superMasterService;

    @Autowired
    public SuperMasterController(SuperMasterService superMasterService) {
        this.superMasterService = superMasterService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SuperMaster>> createSuperMaster(@RequestBody SuperMasterCreateRequest requestBody) {
        SuperMaster createdSuperMaster = superMasterService.createSuperMaster(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_SUPER_MASTER", createdSuperMaster));
    }

    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<SuperMasterWithOwnerUsername>>> getFilteredSuperMasters(@RequestBody FilterRequest filterRequest) {
        Page<SuperMasterWithOwnerUsername> users = superMasterService.getAllSuperMasters(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("Filtered Super Masters", users));
    }
}
