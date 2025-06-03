package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.MasterWithParentUsername;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.exceptions.InsufficientPointsException;
import com.easyliveline.streamingbackend.exceptions.PermissionDeniedDataAccessExceptionWithRole;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.DateUtil;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class MasterService {

    private final MasterRepository masterRepository;
    private final CommonQueryService commonQueryService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final OwnerRepository ownerRepository;
    private final SuperMasterRepository superMasterRepository;
    private final TransactionService transactionService;

    @Autowired
    public MasterService(MasterRepository masterRepository, CommonQueryService commonQueryService, PasswordEncoder passwordEncoder, UserRepository userRepository, OwnerRepository ownerRepository,SuperMasterRepository superMasterRepository, TransactionService transactionService) {
        this.masterRepository = masterRepository;
        this.commonQueryService = commonQueryService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.ownerRepository = ownerRepository;
        this.superMasterRepository = superMasterRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public Master createMaster(SuperMasterCreateRequest request) {
        return ExceptionWrapper.handle(() -> {
            log.info("Creating new Master with username: {}", request.getUsername());

            final Long parentId = commonQueryService.resolveParent();
            final RoleType parentType = RoleType.valueOf(JwtUtil.getRoleFromJWT());

            final int requestedPoints = request.getPoints();
            Integer parentPoints = fetchParentPoints(parentId, parentType);

            log.debug("Parent ID: {}, Role: {}, Parent Points: {}", parentId, parentType, parentPoints);

            if (parentPoints == null || parentPoints < requestedPoints) {
                log.error("Insufficient points. Parent ID: {}, Required: {}, Available: {}",
                        parentId, requestedPoints, parentPoints);
                throw new InsufficientPointsException("Parent does not have enough points to create a Master.");
            }

            verifyRequiredAndDeductPoints(requestedPoints, parentId, parentType, parentPoints);
            log.debug("Points deduction successful for {} points", requestedPoints);

            final User parentUser = userRepository.getReferenceById(parentId);

            final Master newMaster = new Master();
            newMaster.setUsername(request.getUsername());
            newMaster.setPassword(passwordEncoder.encode(request.getPassword()));
            newMaster.setName(request.getName());
            newMaster.setRole(RoleType.MASTER);
            newMaster.setParentType(parentType);
            newMaster.setParent(parentUser);
            newMaster.setPoints(requestedPoints);
            newMaster.setEnabled(true);
            newMaster.setCreatedAt(DateUtil.getTodayAsInt());

            log.debug("Saving new Master to database...");
            final Master saved = masterRepository.save(newMaster);
            log.debug("Master created with ID: {}", saved.getId());

            transactionService.createTransactionBilling(
                    parentId,
                    saved.getId(),
                    requestedPoints,
                    "Created New Master: " + saved.getUsername(),
                    "Recharge",
                    parentPoints,
                    0L
            );

            log.info("Master creation successful: {} (ID: {})", saved.getUsername(), saved.getId());
            return saved;
        });
    }


    @Transactional
    public void deleteMasterById(Long masterId) {
        log.info("Deleting master with ID: {}", masterId);
        try {
            masterRepository.deleteById(masterId);
            log.info("Successfully deleted master with ID: {}", masterId);
        } catch (Exception e) {
            log.error("Failed to delete master with ID: {}", masterId, e);
            throw e;
        }
    }

    @Transactional
    public void reverseTransaction(Long masterId, int points) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Starting reversal of {} points from Master ID: {}", points, masterId);

            final Long ownerId = commonQueryService.resolveParent();
            final long masterCurrentPoints = masterRepository.findPointsById(masterId);
            final int ownerCurrentPoints = ownerRepository.findPointsByOwnerId(ownerId);

            log.debug("Fetched Master Points: {}, Owner ID: {}, Owner Points: {}",
                    masterCurrentPoints, ownerId, ownerCurrentPoints);

            if (masterCurrentPoints < points) {
                log.error("Reversal failed: Insufficient points in Master. Master ID: {}, Required: {}, Available: {}",
                        masterId, points, masterCurrentPoints);
                throw new InsufficientPointsException("Master does not have enough points for reversal.");
            }

            final long masterPointsAfter = masterCurrentPoints - points;
            final int ownerPointsAfter = ownerCurrentPoints + points;

            log.debug("Updating points: Master ({} → {}), Owner ({} → {})",
                    masterCurrentPoints, masterPointsAfter, ownerCurrentPoints, ownerPointsAfter);

            masterRepository.updateMasterPoints(masterId, masterPointsAfter);
            ownerRepository.updateOwnerPoints(ownerId, ownerPointsAfter);
            log.debug("Points updated in database.");

            transactionService.createTransactionBilling(
                    masterId,
                    ownerId,
                    points,
                    "Reverse Transaction",
                    "Reversed Transaction from Master: " + masterId,
                    masterCurrentPoints,
                    ownerCurrentPoints
            );

            log.info("Reversal successful. {} points moved from Master ID: {} to Owner ID: {}",
                    points, masterId, ownerId);
        });
    }


    @Async
    @Transactional
    public CompletableFuture<Void> reverseTransactionAsync(Long masterId, int points) {
        log.info("Starting async transaction reversal of {} points from master ID: {}", points, masterId);
        return CompletableFuture.runAsync(() -> {
            ExceptionWrapper.handleVoid(() -> {
                log.debug("Executing async reversal transaction for master ID: {}", masterId);
                final Long ownerId = commonQueryService.resolveParent();
                final long masterCurrentPoints = masterRepository.findPointsById(masterId);
                log.debug("Async - Master current points: {}, Owner ID: {}", masterCurrentPoints, ownerId);

                if (masterCurrentPoints < points) {
                    log.error("Async - Insufficient points for reversal. Master ID: {}, Required: {}, Available: {}", 
                            masterId, points, masterCurrentPoints);
                    throw new InsufficientPointsException("Master does not have enough points for reversal.");
                }

                final Integer ownerCurrentPoints = ownerRepository.findPointsByOwnerId(ownerId);
                final long masterPointsAfterReversal = masterCurrentPoints - points;
                final long ownerPointsAfterReversal = ownerCurrentPoints + points;
                log.debug("Async - Owner current points: {}, Master points after reversal: {}, Owner points after reversal: {}", 
                        ownerCurrentPoints, masterPointsAfterReversal, ownerPointsAfterReversal);

                masterRepository.updateMasterPoints(masterId, masterPointsAfterReversal);
                ownerRepository.updateOwnerPoints(ownerId, ownerPointsAfterReversal);
                log.debug("Async - Updated points in database");

                transactionService.createTransactionBilling(
                        masterId,
                        ownerId,
                        points,
                        "Reverse Transaction",
                        "Reversed Transaction from Master: " + masterId,
                        masterCurrentPoints,
                        ownerCurrentPoints
                );
                log.info("Async - Successfully reversed transaction of {} points from master ID: {} to owner ID: {}", 
                        points, masterId, ownerId);
            });
        }).exceptionally(ex -> {
            log.error("Async transaction reversal failed for master ID: {}", masterId, ex);
            throw new RuntimeException("Async transaction reversal failed", ex);
        });
    }


    @Transactional
    public void recharge(Long masterId, int points) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Initiating recharge of {} points to Master ID: {}", points, masterId);

            Master master = masterRepository.getReferenceById(masterId);
            log.debug("Fetched Master: {}, Current points: {}", master.getUsername(), master.getPoints());

            Long parentId = master.getParent().getId();
            RoleType parentType = master.getParentType();
            Integer parentPoints = fetchParentPoints(parentId, parentType);

            log.debug("Fetched Parent ID: {}, Type: {}, Available Points: {}", parentId, parentType, parentPoints);

            verifyRequiredAndDeductPoints(points, parentId, parentType, parentPoints);

            log.debug("Points deducted from Parent. Proceeding to credit Master.");

            transactionService.createTransactionBilling(
                    parentId,
                    masterId,
                    points,
                    "Recharge to Master: " + master.getUsername(),
                    "Recharge",
                    parentPoints,
                    master.getPoints()
            );
            log.debug("Transaction billing entry created successfully.");

            int updatedPoints = master.getPoints() + points;
            master.setPoints(updatedPoints);
            master.setLastRecharge(DateUtil.getTodayAsInt());
            log.debug("Master updated. New points: {}, Last recharge date: {}", updatedPoints, master.getLastRecharge());

            log.info("Recharge successful. {} points added to Master: {} (ID: {}). Final balance: {}",
                    points, master.getUsername(), masterId, updatedPoints);
        });
    }

    private Integer fetchParentPoints(Long parentId, RoleType parentType) {
        return switch (parentType) {
            case OWNER -> ownerRepository.findPointsByOwnerId(parentId);
            case SUPER_MASTER -> superMasterRepository.findPointsById(parentId);
            default -> {
                log.error("Recharge failed: Invalid parent type: {}", parentType);
                throw new PermissionDeniedDataAccessExceptionWithRole("Permission denied: Cannot recharge from this parent type.");
            }
        };
    }



    private void verifyRequiredAndDeductPoints(Integer requiredPoints, Long ownerId, RoleType parentType, Integer parentPoints) {
        ExceptionWrapper.handleVoid(() -> {
            log.debug("Verifying points. Required: {}, Owner ID: {}, Available: {}", requiredPoints, ownerId, parentPoints);

            if (parentPoints == null || parentPoints < requiredPoints) {
                log.error("Insufficient points. Owner ID: {}, Required: {}, Available: {}", ownerId, requiredPoints, parentPoints);
                throw new InsufficientPointsException("Parent does not have enough points.");
            }

            int remainingPoints = parentPoints - requiredPoints;
            log.debug("Sufficient points. Deducting {} points. Remaining: {}", requiredPoints, remainingPoints);

            deductParentPoints(ownerId, parentType, remainingPoints);
        });
    }

    private void deductParentPoints(Long parentId, RoleType parentType, int newPoints) {
        ExceptionWrapper.handleVoid(() -> {
            log.debug("Updating points for Parent ID: {} (Role: {}). New points: {}", parentId, parentType, newPoints);

            switch (parentType) {
                case SUPER_MASTER -> superMasterRepository.updateSuperMasterPoints(parentId, newPoints);
                case OWNER -> ownerRepository.updateOwnerPoints(parentId, newPoints);
                default -> {
                    log.error("Invalid parent type for point deduction: {}", parentType);
                    throw new PermissionDeniedDataAccessExceptionWithRole("Permission denied for point deduction.");
                }
            }

            log.debug("Successfully updated points for Parent ID: {}", parentId);
        });
    }

    public Page<MasterWithParentUsername> getAllMasters(FilterRequest sortFilterBody) {
        return ExceptionWrapper.handle(() -> {
            log.info("Fetching all masters with filters: {}", sortFilterBody);
            Map<String, String> columnAliasMap = Map.of(
                    "name", "e.name",
                    "hostCount", "SIZE(e.hosts)"
            );

            Long parentId = commonQueryService.resolveParent();
            log.debug("Parent ID for master query: {}", parentId);
            Map<String, Object> dynamicParams = new HashMap<>();

            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, parentId);
            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.MasterWithParentUsername(e, SIZE(e.subscribers))";
            String fromClause   = "FROM Master e";
            log.debug("Query clauses - Select: {}, From: {}, Where: {}", selectClause, fromClause, whereClause);

            Page<MasterWithParentUsername> result = commonQueryService.fetchWithCustomFilters(
                    MasterWithParentUsername.class,
                    Master.class,
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.empty(),
                    Optional.of(whereClause),
                    Optional.empty(),
                    dynamicParams
            );

            log.info("Fetched {} masters (page {} of {}, size {})", 
                    result.getNumberOfElements(), 
                    result.getNumber() + 1, 
                    result.getTotalPages(),
                    result.getSize());

            return result;
        });
    }


    private static String getStringBuilder(FilterRequest sortFilterBody, Map<String, Object> dynamicParams, Long parentId) {
        return ExceptionWrapper.handle(() -> {
            log.debug("Building where clause for parent ID: {}", parentId);
            StringBuilder whereClause = new StringBuilder("WHERE e.parent.id = :parentId");
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

// No need to explicitly call save() due to JPA dirty checking
// as long as this method runs within a @Transactional context.
// masterRepository.save(master);
// If master is a JPA managed entity, you don't need the explicit save() unless:
// You're using a detached entity (which you're not, since getReferenceById() returns a proxy).
// You want to ensure save() triggers certain repository logic (like pre-save hooks).
// Suggestion: You can omit masterRepository.save(master); if @Transactional ensures flush on commit.

