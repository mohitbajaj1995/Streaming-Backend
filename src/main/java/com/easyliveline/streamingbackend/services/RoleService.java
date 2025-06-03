package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.models.Role;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class RoleService {

    private final Map<RoleType, Role> rolesMap = new EnumMap<>(RoleType.class);

    @PostConstruct
    public void initializeRoles() {
        log.info("Initializing roles and permissions");
        rolesMap.put(RoleType.ADMIN, new Role(RoleType.ADMIN, Arrays.asList("CREATE_OWNER", "READ_OWNERS")));
        rolesMap.put(RoleType.OWNER, new Role(RoleType.OWNER, Arrays.asList("READ_OWNER_SLOTS","CREATE_MASTER","READ_MASTERS","RECHARGE_MASTER")));
        rolesMap.put(RoleType.MANAGER, new Role(RoleType.MANAGER, Arrays.asList("READ_MASTERS", "CREATE_MASTER")));
        rolesMap.put(RoleType.SUPER_MASTER, new Role(RoleType.SUPER_MASTER, Arrays.asList()));
        rolesMap.put(RoleType.MASTER, new Role(RoleType.MASTER, Arrays.asList()));
        rolesMap.put(RoleType.SUBSCRIBER, new Role(RoleType.SUBSCRIBER, Arrays.asList()));
        log.info("Roles and permissions initialized successfully");
    }

    public Role getRoleDetails(RoleType roleType) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Getting role details for role type: {}", roleType);
            Role role = rolesMap.get(roleType);
            if (role == null) {
                log.error("Role not found for role type: {}", roleType);
                throw new IllegalArgumentException("Role not found for role type: " + roleType);
            }
            return role;
        });
    }

    public List<String> getPermissionsByRole(RoleType roleType) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Getting permissions for role type: {}", roleType);
            Role role = getRoleDetails(roleType);
            List<String> permissions = role.getPermissions();
            log.debug("Found {} permissions for role type: {}", permissions.size(), roleType);
            return permissions;
        });
    }
}
