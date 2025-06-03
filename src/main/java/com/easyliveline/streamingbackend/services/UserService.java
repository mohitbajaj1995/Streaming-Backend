package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.util.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MasterRepository masterRepository;
    private final CommonQueryService commonQueryService;
    private final TransactionalUserService transactionalUserService;

    public Map<String, List<OwnerFilterSuperMasterAndMasterMeta>> getFilterMetaData() {
        log.debug("Fetching filter metadata");
        try {
            Long userId = commonQueryService.resolveParent();
            RoleType userRole = userRepository.findRoleById(userId);
            log.debug("Fetching masters for user ID: {} with role: {}", userId, userRole);

            Map<String, List<OwnerFilterSuperMasterAndMasterMeta>> result = Map.of(
                    "masters", masterRepository.findMastersByOwnerId(userId)
            );
            log.debug("Found {} masters for user ID: {}", result.get("masters").size(), userId);
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch filter metadata: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    public void updateUserNameAndPassword(Long id, String name, String password) {
        log.info("Updating name and password for user ID: {}", id);
        try {
            userRepository.updateNameAndPasswordById(id, name, password);
            log.info("Successfully updated name and password for user ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to update name and password for user ID: {}", id, e);
            throw e;
        }
    }

    @Async("virtualThreadExecutor")
    public void updateLastSeenAsync(Long userId, long epochMillis, String tenantId) {
        TenantContext.setCurrentTenant(tenantId);
        try {
            transactionalUserService.updateLastSeen(userId, epochMillis);
        } catch (Exception e) {
            log.error("Failed to update last seen", e);
        } finally {
            TenantContext.clear();
        }
    }

    @Transactional
    public void updateUserName(Long id, String name) {
        log.info("Updating name for user ID: {} to '{}'", id, name);
        try {
            userRepository.updateNameById(id, name);
            log.info("Successfully updated name for user ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to update name for user ID: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void toggleUserEnabled(Long id) {
        log.info("Toggling enabled status for user ID: {}", id);
        try {
            userRepository.toggleUserEnabled(id);
            log.info("Successfully toggled enabled status for user ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to toggle enabled status for user ID: {}", id, e);
            throw e;
        }
    }
}
