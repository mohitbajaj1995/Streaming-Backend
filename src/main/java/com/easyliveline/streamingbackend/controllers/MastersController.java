package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.MasterWithParentUsername;
import com.easyliveline.streamingbackend.exceptions.PermissionDeniedDataAccessExceptionWithRole;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.MasterService;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/masters")
public class MastersController {

    private final MasterService masterService;
    private final SecurityService securityService;

    @Autowired
    public MastersController(MasterService masterService, SecurityService securityService) {
        this.masterService = masterService;
        this.securityService = securityService;
    }

    @PreAuthorize("hasAnyRole('OWNER','MANAGER') && hasAuthority('PERMISSION_CREATE_MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Master>> createMaster(@RequestBody SuperMasterCreateRequest requestBody) {
        Master createdMaster = masterService.createMaster(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_MASTER", createdMaster));
    }

    @PreAuthorize("hasRole('OWNER') && hasAuthority('PERMISSION_RECHARGE_MASTER')")
    @PutMapping("/recharge")
    public ResponseEntity<ApiResponse<String>> recharge(@RequestParam Long masterId, @RequestParam int points) {
        if(!securityService.hasPermissionForTask("RECHARGE_POINTS", masterId)){
            throw new PermissionDeniedDataAccessExceptionWithRole("You do not have permission to recharge this master");
        }
        masterService.recharge(masterId, points);
        return ResponseEntity.ok(ApiResponseBuilder.success("RECHARGE_SUCCESSFULLY", null));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/reverse-transaction/{masterId}/{points}")
    public ResponseEntity<ApiResponse<String>> reverseTransaction(@PathVariable Long masterId, @PathVariable int points) {
        if(!securityService.hasPermissionForTask("REVERSE_POINTS_TRANSACTION", masterId)){
            throw new PermissionDeniedDataAccessExceptionWithRole("You do not have permission to recharge this master");
        }
        masterService.reverseTransaction(masterId, points);
        return ResponseEntity.ok(ApiResponseBuilder.success("REVERSED_TRANSACTION", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable Long id) {
//        securityService.isMasterDeletableAndHasPermissionToDelete(id);
        masterService.deleteMasterById(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("MASTER_DELETED", null));
    }

    @PreAuthorize("hasAnyRole('OWNER','MANAGER') && hasAuthority('PERMISSION_READ_MASTERS')")
    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<MasterWithParentUsername>>> getFilteredUsers(@RequestBody FilterRequest filterRequest) {
        Page<MasterWithParentUsername> masters = masterService.getAllMasters(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("FILTERED_MASTERS", masters));
    }
}
