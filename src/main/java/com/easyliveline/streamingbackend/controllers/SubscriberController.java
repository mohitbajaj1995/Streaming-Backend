package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.HostWithPlanName;
import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.exceptions.PermissionDeniedDataAccessExceptionWithRole;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.services.SubscriberService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import com.easyliveline.streamingbackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscribers")
public class SubscriberController {

    private final SubscriberService subscriberService;
    private final CommonQueryService commonQueryService;
    private final SecurityService securityService;

    @Autowired
    public SubscriberController(SubscriberService subscriberService, CommonQueryService commonQueryService, SecurityService securityService) {
        this.subscriberService = subscriberService;
        this.commonQueryService = commonQueryService;
        this.securityService = securityService;
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createHost(@RequestBody SubscriberCreateRequest requestBody) {
        subscriberService.createHost(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_HOST", null));
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @GetMapping("/recharge/{hostId}/{planId}")
    public ResponseEntity<ApiResponse<Subscriber>> rechargeHost(@PathVariable Long hostId, @PathVariable Long planId) {
        if(!securityService.hasPermissionForTask("RECHARGE_HOST", hostId)){
            throw new PermissionDeniedDataAccessExceptionWithRole("You do not have permission to recharge this host");
        }
        subscriberService.rechargeSubscription(hostId, planId);
        return ResponseEntity.ok(ApiResponseBuilder.success("RECHARGED_HOST", null));
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/adjustment/{hostId}/{startDate}/{endDate}")
    public ResponseEntity<ApiResponse<String>> adjustHost(@PathVariable Long hostId, @PathVariable String startDate, @PathVariable String endDate) {
        subscriberService.adjustSubscription(hostId, startDate, endDate);
        return ResponseEntity.ok(ApiResponseBuilder.success("ADJUSTED_HOST", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteHost(@PathVariable Long id) {
//        securityService.isHostDeletableAndHasPermissionToDelete(id);
        subscriberService.deleteHost(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("DELETED_HOST", null));
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<HostWithPlanName>>> getFilteredHosts(@RequestBody FilterRequest filterRequest) {
        Long userId = commonQueryService.resolveParent();
        Page<HostWithPlanName> masters = subscriberService.getAllHosts(filterRequest,userId);
        return ResponseEntity.ok(ApiResponseBuilder.success("FILTERED_HOST", masters));
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @GetMapping("/all-visible-slots/{id}")
    public ResponseEntity<ApiResponse<List<OwnerFilterSuperMasterAndMasterMeta>>> getAllHosts(@PathVariable Long id) {
        if(!commonQueryService.resolveParent().equals(id) && JwtUtil.getRoleFromJWT().equals("MASTER")){
            throw new PermissionDeniedDataAccessExceptionWithRole("You do not have permission to view other hosts");
        }
        List<OwnerFilterSuperMasterAndMasterMeta> hosts = subscriberService.getParentHost(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("ALL_HOSTS", hosts));
    }
}
