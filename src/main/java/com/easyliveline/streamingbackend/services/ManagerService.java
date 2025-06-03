package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.ManagerDTO;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.models.Manager;
import com.easyliveline.streamingbackend.models.ManagerCreateRequest;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final CommonQueryService commonQueryService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MasterRepository masterRepository;
    private final OwnerRepository ownerRepository;

    @Autowired
    public ManagerService(ManagerRepository managerRepository, CommonQueryService commonQueryService,
                         UserRepository userRepository, PasswordEncoder passwordEncoder,
                          MasterRepository masterRepository, OwnerRepository ownerRepository) {
        this.managerRepository = managerRepository;
        this.commonQueryService = commonQueryService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.masterRepository = masterRepository;
        this.ownerRepository = ownerRepository;
    }

    @Transactional
    public void createManager(ManagerCreateRequest requestBody) {
        log.info("Creating new manager with username: {}", requestBody.getUsername());
        ExceptionWrapper.handleVoid(() -> {
            Long parentId = JwtUtil.getUserIdFromJWT();
            log.debug("Parent ID from JWT: {}", parentId);

            Manager manager = new Manager();
            RoleType parentRole = userRepository.findRoleById(parentId);
            log.debug("Parent role: {}", parentRole);

            if (RoleType.MASTER.equals(parentRole)) {
                log.debug("Setting parent as Master with ID: {}", parentId);
                manager.setParent(masterRepository.getReferenceById(parentId));
            } else {
                log.debug("Setting parent as Owner with ID: {}", parentId);
                manager.setParent(ownerRepository.getReferenceById(parentId));
            }

            manager.setName(requestBody.getName());
            manager.setUsername(requestBody.getUsername());
            manager.setPassword(passwordEncoder.encode(requestBody.getPassword()));
            manager.setRole(RoleType.MANAGER);
            manager.setParentType(parentRole);
            manager.setEnabled(true);
            log.debug("Manager object prepared with name: {}, username: {}, role: {}", 
                    manager.getName(), manager.getUsername(), manager.getRole());

            managerRepository.save(manager);
            log.info("Successfully created manager with username: {}", manager.getUsername());
        });
    }


    public List<ManagerDTO> getManagersByParentId(Long parentId) {
        log.info("Fetching managers for parent ID: {}", parentId);
        return ExceptionWrapper.handle(() -> {
            List<ManagerDTO> managers = managerRepository.findManagersByParentId(parentId);
            log.debug("Found {} managers for parent ID: {}", managers.size(), parentId);
            return managers;
        });
    }

    @Transactional
    public void deleteById(Long id) {
        log.info("Deleting manager with ID: {}", id);
        ExceptionWrapper.handleVoid(() -> {
            try {
                managerRepository.deleteManagerById(id);
                log.info("Successfully deleted manager with ID: {}", id);
            } catch (Exception e) {
                log.error("Failed to delete manager with ID: {}", id, e);
                throw e;
            }
        });
    }
}
