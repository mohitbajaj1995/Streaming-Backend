package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.OwnerWithAdminUsername;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.AdminRepository;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.interfaces.OwnerRepository;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final AdminRepository adminRepository;
    private final CommonQueryService commonQueryService;
    private final PasswordEncoder passwordEncoder;
    private final TransactionService transactionService;

    @Autowired
    public OwnerService(OwnerRepository ownerRepository,AdminRepository adminRepository, CommonQueryService commonQueryService, PasswordEncoder passwordEncoder, TransactionService transactionService) {
        this.ownerRepository = ownerRepository;
        this.adminRepository = adminRepository;
        this.commonQueryService = commonQueryService;
        this.passwordEncoder = passwordEncoder;
        this.transactionService = transactionService;
    }

    @Transactional
    public Owner createOwner(OwnerCreateRequest requestBody) {
        log.info("Creating new owner with username: {}", requestBody.getUsername());
        return ExceptionWrapper.handle(() -> {
            Owner owner = new Owner();
            owner.setName(requestBody.getName());
            owner.setRole(RoleType.OWNER);

            Long adminId = JwtUtil.getUserIdFromJWT();
            log.debug("Admin ID from JWT: {}", adminId);
            Admin admin = adminRepository.getReferenceById(adminId);

            owner.setAdmin(admin);
            owner.setEnabled(true);
            owner.setUsername(requestBody.getUsername());
            owner.setPassword(passwordEncoder.encode(requestBody.getPassword()));
            owner.setPoints(requestBody.getPoints());
            log.debug("Owner object prepared with name: {}, username: {}, points: {}", 
                    owner.getName(), owner.getUsername(), owner.getPoints());

            Owner savedOwner = ownerRepository.save(owner);
            Long ownerId = savedOwner.getId();
            log.debug("Owner saved with ID: {}", ownerId);

            Long parentId = commonQueryService.resolveParent();
            log.debug("Creating transaction billing with parent ID: {}", parentId);

            transactionService.createTransactionBilling(
                parentId, 
                ownerId, 
                savedOwner.getPoints(), 
                "Created Owner: "+ savedOwner.getUsername(),
                "Recharge from " + parentId,
                admin.getPoints(),
                0L
            );

            log.info("Successfully created owner with ID: {}", ownerId);
            return savedOwner;
        });
    }

    public Page<OwnerWithAdminUsername> getAllOwners(FilterRequest sortFilterBody) {
        log.info("Fetching all owners with filters");
        return ExceptionWrapper.handle(() -> {
            Map<String, String> columnAliasMap = Map.of(
    //                "name", "e.name"
    //                "locked", "e.locked",
    //                "hostUsername", "ho.username"
            );
            log.debug("Column alias map created");

            Long parentId = JwtUtil.getUserIdFromJWT();
            log.debug("Parent ID from JWT: {}", parentId);

            Map<String, Object> dynamicParams = new HashMap<>();
            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, parentId);
            log.debug("Where clause generated: {}", whereClause);

            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.OwnerWithAdminUsername(e, a.username)";
            String fromClause = "FROM Owner e";
            String joinClause = "JOIN e.admin a";
            log.debug("Query clauses - Select: {}, From: {}, Join: {}", selectClause, fromClause, joinClause);

            Page<OwnerWithAdminUsername> result = commonQueryService.fetchWithCustomFilters(
                    OwnerWithAdminUsername.class,         // projection
                    Owner.class,            // entity
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.of(joinClause),
                    Optional.of(whereClause),
                    Optional.empty(),
                    dynamicParams
            );

            log.info("Fetched {} owners (page {} of {}, size {})", 
                    result.getNumberOfElements(), 
                    result.getNumber() + 1, 
                    result.getTotalPages(),
                    result.getSize());

            return result;
        });
    }

//    public void deleteOwner(Long ownerId) {
////        securityService.isOwnerDeletable(ownerId);
//        ExceptionWrapper.handleVoid(() -> ownerRepository.deleteById(ownerId));
//    }


    private static String getStringBuilder(FilterRequest sortFilterBody, Map<String, Object> dynamicParams, Long parentId) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Building where clause for parent ID: {}", parentId);
            StringBuilder whereClause = new StringBuilder("WHERE e.admin.id = :parentId");
            dynamicParams.put("parentId", parentId);
            String result = whereClause.toString();
            log.debug("Final where clause: {}", result);
            return result;
        });
    }
}
