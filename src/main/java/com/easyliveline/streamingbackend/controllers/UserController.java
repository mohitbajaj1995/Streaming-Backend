package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.exceptions.PermissionDeniedDataAccessExceptionWithRole;
import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.services.SecurityService;
import com.easyliveline.streamingbackend.services.UserService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityService securityService;

    @Autowired
    public UserController(UserService userService, PasswordEncoder passwordEncoder, SecurityService securityService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.securityService = securityService;
    }


//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
//        User user = userService.getUserById(id); // will throw if not found
//        return ResponseEntity.ok(ApiResponseBuilder.success("USER_WITH_ID", user));
//    }


    @PreAuthorize("hasAnyRole('SUPER_MASTER','OWNER')")
    @GetMapping("/filter/metadata")
    public ResponseEntity<ApiResponse<Map<String, List<OwnerFilterSuperMasterAndMasterMeta>>>> getFilteredUsers() {
        Map<String, List<OwnerFilterSuperMasterAndMasterMeta>> filterMetaData = userService.getFilterMetaData();
        return ResponseEntity.ok(ApiResponseBuilder.success("FILTERED_USERS", filterMetaData));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<String>> updateUserNameAndPassword(@PathVariable Long id, @RequestBody Map<String, String> requestBody) {
        if (!securityService.hasPermissionForTask("UPDATE_USER", id)) {
            throw new PermissionDeniedDataAccessExceptionWithRole("You do not have permission to update this user");
        }

        String name = requestBody.get("name");
        String rawPassword = requestBody.get("password");

        if (name == null || name.trim().isEmpty()) {
           throw new RuntimeException("Name is required");
        }

        if (rawPassword != null && !rawPassword.trim().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(rawPassword);
            userService.updateUserNameAndPassword(id, name.trim(), encodedPassword);
        } else {
            userService.updateUserName(id, name.trim());
        }
        return ResponseEntity.ok(ApiResponseBuilder.success("USER_UPDATED", "User updated successfully"));
    }

    @PreAuthorize("@securityService.hasPermissionForTask('UPDATE_USER', #id)")
    @PutMapping("/toggle-enabled/{id}")
    public ResponseEntity<ApiResponse<String>> toggleUserEnabled(@PathVariable Long id) {
        userService.toggleUserEnabled(id);
        return ResponseEntity.ok(ApiResponseBuilder.success("USER_TOGGLED", "User enabled status toggled successfully"));
    }
}