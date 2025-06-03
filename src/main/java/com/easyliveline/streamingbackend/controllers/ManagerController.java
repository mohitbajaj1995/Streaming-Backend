package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.ManagerDTO;
import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.ManagerCreateRequest;
import com.easyliveline.streamingbackend.services.ManagerService;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import com.easyliveline.streamingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/managers")
public class ManagerController {

    private final ManagerService managerService;
    private final SecurityService securityService;

    @Autowired
    public ManagerController(ManagerService managerService, SecurityService securityService) {
        this.managerService = managerService;
        this.securityService = securityService;
    }

    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createManager(@RequestBody ManagerCreateRequest requestBody) {
        managerService.createManager(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_MANAGER", null));
    }

    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ManagerDTO>>> getManagersByParentId() {
        Long parentId = JwtUtil.getUserIdFromJWT();
        List<ManagerDTO> managers = managerService.getManagersByParentId(parentId);
        return ResponseEntity.ok(ApiResponseBuilder.success("MANAGERS_BY_PARENT_ID", managers));
    }

    @PreAuthorize("hasAnyRole('OWNER','MASTER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable Long id) {
        if (!securityService.hasPermissionForTask("DELETE_MANAGER", id)) {
            throw new RuntimeException("You do not have permission to delete this manager");
        }
        managerService.deleteById(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("MANAGER_DELETED", null));
    }
}
