package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.enums.RoleType;

public record OwnerFilterSuperMasterAndMasterMeta(Long id, String username, RoleType userType) {
}
