package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.SuperMasterWithOwnerUsername;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.interfaces.OwnerRepository;
import com.easyliveline.streamingbackend.interfaces.SuperMasterRepository;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SuperMasterService {

    private final SuperMasterRepository superMasterRepository;
    private final OwnerRepository ownerRepository;
    private final CommonQueryService commonQueryService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SuperMasterService(SuperMasterRepository superMasterRepository,OwnerRepository ownerRepository, CommonQueryService commonQueryService, PasswordEncoder passwordEncoder) {
        this.superMasterRepository = superMasterRepository;
        this.ownerRepository = ownerRepository;
        this.commonQueryService = commonQueryService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SuperMaster createSuperMaster(SuperMasterCreateRequest requestBody) {
        // Security check: Ensure the user is an Owner
        SuperMaster superMaster = new SuperMaster();
        superMaster.setName(requestBody.getName());
        superMaster.setRole(RoleType.SUPER_MASTER);
        Long ownerId = commonQueryService.resolveParent();
        Owner owner = ownerRepository.getReferenceById(ownerId);
        superMaster.setOwner(owner);
        superMaster.setEnabled(true);
        superMaster.setUsername(requestBody.getUsername());
        superMaster.setPassword(passwordEncoder.encode(requestBody.getPassword()));
        verifyRequiredPoints(requestBody.getPoints(), ownerId);
        superMaster.setPoints(requestBody.getPoints());
        return superMasterRepository.save(superMaster);
    }

    private void verifyRequiredPoints(Integer requiredPoints, Long ownerID){
        Integer ownerPoints = ownerRepository.findPointsByOwnerId(ownerID);
        if(ownerPoints != null && ownerPoints >= requiredPoints) {
            deductOwnerPoints(ownerID, ownerPoints - requiredPoints);
        } else {
            throw new RuntimeException("Not enough points");
        }
    }

    private void deductOwnerPoints(Long ownerID, int points) {
        ownerRepository.updateOwnerPoints(ownerID, points);
    }

    public Page<SuperMasterWithOwnerUsername> getAllSuperMasters(FilterRequest sortFilterBody) {
        return ExceptionWrapper.handle(() -> {
            Map<String, String> columnAliasMap = Map.of(
                    "name", "e.name"
//                    "hostCount", "SIZE(e.hosts)"
            );

            Long parentId = commonQueryService.resolveParent();
            log.debug("Parent ID for master query: {}", parentId);
            Map<String, Object> dynamicParams = new HashMap<>();

            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, parentId);
            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.SuperMasterWithOwnerUsername(e, SIZE(e.masters), SIZE(e.subscribers))";
            String fromClause   = "FROM SuperMaster e";
            log.debug("Query clauses - Select: {}, From: {}, Where: {}", selectClause, fromClause, whereClause);

            return commonQueryService.fetchWithCustomFilters(
                    SuperMasterWithOwnerUsername.class,
                    SuperMaster.class,
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.empty(),
                    Optional.of(whereClause),
                    Optional.empty(),
                    dynamicParams
            );
        });
    }


    private static String getStringBuilder(FilterRequest sortFilterBody, Map<String, Object> dynamicParams, Long parentId) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Building where clause for parent ID: {}", parentId);
            StringBuilder whereClause = new StringBuilder("WHERE e.owner.id = :parentId");
            dynamicParams.put("parentId", parentId);

            if(sortFilterBody.getGlobalFilter() != null && !sortFilterBody.getGlobalFilter().isEmpty()){
                log.debug("Adding global filter: {}", sortFilterBody.getGlobalFilter());
                whereClause.append(" AND (e.name ILIKE :globalFilter OR e.username ILIKE :globalFilter)");
                dynamicParams.put("globalFilter", "%" + sortFilterBody.getGlobalFilter() + "%");
            }

            if (!sortFilterBody.getColumnFilters().isEmpty()) {
                log.debug("Processing {} column filters", sortFilterBody.getColumnFilters().size());
            }

            for (FilterRequest.ColumnFilter filter : sortFilterBody.getColumnFilters()) {
                log.debug("Processing column filter - ID: {}, Value: {}", filter.getId(), filter.getValue());
                switch (filter.getId()) {
                    case "name" -> {
                        whereClause.append(" AND e.name = :name");
                        dynamicParams.put("name", filter.getValue());
                    }
                    case "username" -> {
                        whereClause.append(" AND e.username = :username");
                        dynamicParams.put("username", filter.getValue());
                    }
                    case "enabled" -> {
                        whereClause.append(" AND e.enabled = :enabled");
                        dynamicParams.put("enabled", Boolean.parseBoolean(filter.getValue()));
                    }
                }
            }

            String result = whereClause.toString();
            log.debug("Final where clause: {}", result);
            return result;
        });
    }
}

