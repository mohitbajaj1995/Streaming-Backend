package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.MeetingWithOwnerName;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.services.MeetingService;
import com.easyliveline.streamingbackend.services.RedisService;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private final SecurityService securityService;
    private final RedisService redisService;

    @Autowired
    public MeetingController(MeetingService meetingService, SecurityService securityService, RedisService redisService) {
        this.meetingService = meetingService;
        this.securityService = securityService;
        this.redisService = redisService;
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<Meeting>> createMeeting(@RequestBody MeetingCreateRequest requestBody) {
        Meeting createdMeeting = meetingService.createMeeting(requestBody);
        return ResponseEntity.ok(ApiResponseBuilder.success("CREATED_MEETING", createdMeeting));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMeeting(@PathVariable Long id) {
//        securityService.isMeetingDeletableAndHasPermissionToDelete(id);
        meetingService.deleteMeeting(id);
        redisService.deleteKeysContaining("_meeting_" + id);
        return ResponseEntity.ok(ApiResponseBuilder.success("DELETED_MEETING", null));
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Meeting>> updateMeeting(@PathVariable Long id, @RequestBody MeetingUpdateRequest requestBody) {
        Meeting updatedMeeting = meetingService.updateMeeting(id, requestBody);
        redisService.deleteKeysContaining("_meeting_" + id);
        return ResponseEntity.ok(ApiResponseBuilder.success("UPDATED_MEETING", updatedMeeting));
    }

    @PreAuthorize("hasAnyRole('MASTER','OWNER','MANAGER')")
    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<MeetingWithOwnerName>>> getFilteredMeetings(@RequestBody FilterRequest filterRequest) {
        Page<MeetingWithOwnerName> meetings = meetingService.getFilteredMeetings(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("Filtered Meetings", meetings));
    }
}
