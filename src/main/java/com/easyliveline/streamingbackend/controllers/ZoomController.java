package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.ZoomWithMeetingSize;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.RedisService;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.services.ZoomService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/zooms")
public class ZoomController {

    private final ZoomService zoomService;
    private final CommonQueryService commonQueryService;
    private final SecurityService securityService;
    private final RedisService redisService;

    @Autowired
    public ZoomController(ZoomService zoomService, CommonQueryService commonQueryService, SecurityService securityService, RedisService redisService) {
        this.zoomService = zoomService;
        this.commonQueryService = commonQueryService;
        this.securityService = securityService;
        this.redisService = redisService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Zoom>> createZoom(@RequestBody ZoomCreateRequest requestBody) {
        Zoom createdZoom = zoomService.createZoom(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_ZOOM", createdZoom));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ZoomWithMeetingSize>>> getFilteredUsers() {
        List<ZoomWithMeetingSize> users = zoomService.getAllZooms();
        return ResponseEntity.ok(ApiResponseBuilder.success("Filtered Zooms", users));
    }

//    @GetMapping("/all")
//    public ResponseEntity<ApiResponse<List<Zoom>>> getAllZoomsByParentId() {
//        Long parentId = commonQueryService.resolveParent();
//        List<Zoom> zoom = zoomService.getAllZoomsByParentId(parentId);
//        return ResponseEntity.ok(ApiResponseBuilder.success("ZOOM_WITH_ID", zoom));
//    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteById(@PathVariable Long id) {
//        securityService.isZoomAccountDeletableAndHasPermissionToDelete(id);
        zoomService.deleteZoomById(id);
        redisService.deleteKeysContaining("_zoom_" + id);
        return ResponseEntity.ok(ApiResponseBuilder.success("ZOOM_DELETED", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Zoom>> updateZoom(@PathVariable Long id, @RequestBody ZoomUpdateRequest requestBody) {
        System.out.println("Updating Zoom with ID: " + id);
        Zoom updatedZoom = zoomService.updateZoom(id, requestBody);
        redisService.deleteKeysContaining("_zoom_" + id);
        return ResponseEntity.ok(ApiResponseBuilder.success("UPDATED_ZOOM", updatedZoom));
    }

    @PreAuthorize("hasAnyRole('SUB_HOST','PARTICIPANT')")
    @GetMapping("/get-zoom-token/{meetingId}")
    public String getZoomToken(@PathVariable Long meetingId) {
        System.out.println("Generating Zoom token...");
        return zoomService.generateSignature(meetingId);
    }
}
