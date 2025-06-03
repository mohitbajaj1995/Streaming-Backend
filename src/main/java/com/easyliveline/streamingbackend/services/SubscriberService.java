package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.HostWithPlanName;
import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.exceptions.InsufficientPointsException;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.DateUtil;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class SubscriberService {

    private final SubscriberRepository subscriberRepository;
    private final CommonQueryService commonQueryService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlanRepository planRepository;
    private final OwnerRepository ownerRepository;
        private final SuperMasterRepository superMasterRepository;
    private final MasterRepository masterRepository;
    private final TransactionService transactionService;

    @Autowired
    public SubscriberService(SubscriberRepository subscriberRepository, CommonQueryService commonQueryService, UserRepository userRepository, PasswordEncoder passwordEncoder, PlanRepository planRepository,
                             OwnerRepository ownerRepository,SuperMasterRepository superMasterRepository, MasterRepository masterRepository, TransactionService transactionService) {
        this.subscriberRepository = subscriberRepository;
        this.commonQueryService = commonQueryService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.planRepository = planRepository;
        this.ownerRepository = ownerRepository;
        this.superMasterRepository = superMasterRepository;
        this.masterRepository = masterRepository;
        this.transactionService = transactionService;
    }

    @Transactional
    public void createHost(SubscriberCreateRequest requestBody) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Creating new host with username: {}", requestBody.getUsername());

            Long parentId = commonQueryService.resolveParent();
            User parent = userRepository.getReferenceById(parentId);
            RoleType parentType = userRepository.findRoleById(parentId);
            log.debug("Resolved parent ID: {}, parent type: {}", parentId, parentType);

            Plan plan = planRepository.findById(requestBody.getPlan())
                    .orElseThrow(() -> {
                        log.error("Plan not found with ID: {}", requestBody.getPlan());
                        return new RuntimeException("Plan not found");
                    });
            log.debug("Found plan: {}, required points: {}", plan.getName(), plan.getRequiredPoints());

            long parentCurrentPoints = verifyRequiredPointsAndReturnParentPointsIdPass(
                    plan.getRequiredPoints(), parentId, parentType
            );
            log.debug("Verified parent has sufficient points. Current points: {}", parentCurrentPoints);

            Subscriber subscriber = new Subscriber();
            subscriber.setName(requestBody.getName());
            subscriber.setUsername(requestBody.getUsername());
            subscriber.setPassword(passwordEncoder.encode(requestBody.getPassword()));
            subscriber.setRole(RoleType.SUBSCRIBER);
            subscriber.setParent(parent);
            subscriber.setParentType(parentType);
            subscriber.setPlan(plan);
            subscriber.setEnabled(true);
            subscriber.setCanRefund(true);
            subscriber.setRefundableMonths(plan.getDurationInMonths());

            int today = DateUtil.getTodayAsInt();
            subscriber.setStartAt(today);
            subscriber.setLastRecharge(today);
            subscriber.setEndAt(DateUtil.getDateAfterMonthsAndDays(
                    plan.getDurationInMonths(), plan.getDurationInDays()
            ));

            Subscriber savedHost = subscriberRepository.save(subscriber);
            log.debug("Saved host with ID: {}", savedHost.getId());

            transactionService.createTransactionBilling(
                    parentId,
                    savedHost.getId(),
                    plan.getRequiredPoints(),
                    "Created Host With Recharge: " + savedHost.getUsername() + " / " + plan.getName(),
                    "Recharge " + plan.getName() + " Plan",
                    parentCurrentPoints,
                    0
            );
            log.info("Successfully created host: {} with ID: {}", savedHost.getUsername(), savedHost.getId());
        });
    }


    @Transactional
    public void deleteHost(Long subscriberId) {
        log.info("Deleting host with ID: {}", subscriberId);
        try {
            subscriberRepository.deleteById(subscriberId);
            log.info("Successfully deleted host with ID: {}", subscriberId);
        } catch (Exception e) {
            log.error("Failed to delete host with ID: {}", subscriberId, e);
            throw e;
        }
    }

    @Transactional
    public void adjustSubscription(Long subscriberId, String startDate, String endDate) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Adjusting subscription for host ID: {} with start date: {} and end date: {}", subscriberId, startDate, endDate);

            Subscriber subscriber = subscriberRepository.findById(subscriberId)
                    .orElseThrow(() -> {
                        log.error("Host not found with ID: {}", subscriberId);
                        return new RuntimeException("Host not found: ID = " + subscriberId);
                    });
            log.debug("Found host: {}", subscriber.getUsername());

            LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.debug("Parsed dates - start: {}, end: {}", start, end);

            subscriber.setStartAt(Long.parseLong(start.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            subscriber.setEndAt(Long.parseLong(end.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            log.debug("Set subscription period - start: {}, end: {}", subscriber.getStartAt(), subscriber.getEndAt());

            subscriberRepository.save(subscriber);
            log.debug("Saved host with updated subscription period");

            Long parentID = subscriber.getParent().getId();
            log.debug("Getting parent points for parent ID: {}, type: {}", parentID, subscriber.getParentType());
            long parentPoints = switch (subscriber.getParentType()) {
                case MASTER -> masterRepository.findPointsById(parentID);
                case SUPER_MASTER -> superMasterRepository.findPointsById(parentID);
                case OWNER -> ownerRepository.findPointsByOwnerId(parentID);
                default -> {
                    log.error("Unsupported parent type: {} for host ID: {}", subscriber.getParentType(), subscriberId);
                    throw new RuntimeException("Unsupported parent type: " + subscriber.getParentType());
                }
            };

            transactionService.createTransactionBilling(
                    parentID,
                    subscriberId,
                    0,
                    "Adjustment: "+ subscriber.getUsername() + " new Dates " + start + "-" + end,
                    "Adjustment: "+ subscriber.getUsername() + " new Dates " + start + "-" + end,
                    parentPoints,
                    0
            );
        });
    }


    @Transactional
    public void rechargeSubscription(Long userId, Long planId) {
        ExceptionWrapper.handleVoid(() -> {
            log.info("Recharging subscription for host ID: {} with plan ID: {}", userId, planId);
            Long parentId = commonQueryService.resolveParent();
            log.debug("Resolved parent ID: {}", parentId);

            Subscriber subscriber = subscriberRepository.findById(userId)
                    .orElseThrow(() -> {
                        log.error("Host not found with ID: {}", userId);
                        return new RuntimeException("Host not found: ID = " + userId);
                    });
            log.debug("Found host: {}", subscriber.getUsername());

            Plan plan = planRepository.findById(planId)
                    .orElseThrow(() -> {
                        log.error("Plan not found with ID: {}", planId);
                        return new RuntimeException("Plan not found: ID = " + planId);
                    });
            log.debug("Found plan: {}, required points: {}", plan.getName(), plan.getRequiredPoints());

            long parentCurrentPoints;
            try {
                parentCurrentPoints = verifyRequiredPointsAndReturnParentPointsIdPass(plan.getRequiredPoints(), parentId, subscriber.getParentType());
                log.debug("Verified parent has sufficient points. Current points: {}", parentCurrentPoints);
            } catch (InsufficientPointsException e) {
                log.error("Insufficient points for recharging subscription. Host ID: {}, Plan ID: {}, Error: {}", 
                        userId, planId, e.getMessage());
                throw new InsufficientPointsException("Error verifying points -> " + e.getMessage());
            } catch (RuntimeException e) {
                log.error("Error verifying points for recharging subscription. Host ID: {}, Plan ID: {}, Error: {}", 
                        userId, planId, e.getMessage(), e);
                throw new RuntimeException("Error verifying points: " + e.getMessage());
            }

            transactionService.createTransactionBilling(
                    parentId,
                    userId,
                    plan.getRequiredPoints(),
                    "Recharge Subscription: " + subscriber.getName() + " / " + plan.getName(),
                    "Recharge " + plan.getName() + " Plan",
                    parentCurrentPoints,
                    0
            );
            log.debug("Created transaction billing for recharge");

            LocalDate now = LocalDate.now();
            LocalDate newEndAt;
            log.debug("Current date: {}, current end date: {}", now, subscriber.getEndAt());

            if (subscriber.getEndAt() != 0 && DateUtil.fromInt((int) subscriber.getEndAt()).isAfter(now)) {
                newEndAt = DateUtil.fromInt((int) subscriber.getEndAt())
                        .plusMonths(plan.getDurationInMonths())
                        .plusDays(plan.getDurationInDays());
                log.debug("Current end date is in the future, extending from current end date: {}", newEndAt);
            } else {
                subscriber.setStartAt(Long.parseLong(now.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
                newEndAt = now.plusMonths(plan.getDurationInMonths())
                        .plusDays(plan.getDurationInDays());
                log.debug("Current end date is not in the future, setting new start date to today and new end date: {}", newEndAt);
            }

            subscriber.setPlan(plan);
            subscriber.setEndAt(Long.parseLong(newEndAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"))));
            subscriber.setCanRefund(true);
            subscriber.setRefundableMonths(plan.getDurationInMonths());
            subscriber.setLastRecharge(DateUtil.getTodayAsInt());
            log.info("Successfully recharged subscription for host: {} with plan: {}. New end date: {}",
                    subscriber.getUsername(), plan.getName(), newEndAt);

            subscriberRepository.save(subscriber);
        });
    }


    private long verifyRequiredPointsAndReturnParentPointsIdPass(long requiredPoints, Long parentID, RoleType parentType) throws RuntimeException {
        log.debug("Verifying required points: {}, parent ID: {}, parent type: {}", requiredPoints, parentID, parentType);

        long parentPoints = switch (parentType) {
                case OWNER -> ownerRepository.findPointsByOwnerId(parentID);
                case MASTER -> masterRepository.findPointsById(parentID);
                case SUPER_MASTER -> superMasterRepository.findPointsById(parentID);
                default -> {
                    log.error("Unsupported parent type: {} for parent ID: {}", parentType, parentID);
                    throw new RuntimeException("Unsupported parent type: " + parentType);
                }
            };
        log.debug("Parent points: {}", parentPoints);

        if (parentPoints >= requiredPoints) {
            deductParentPoints(parentID, (parentPoints - requiredPoints), parentType);
            log.debug("Points verified and deducted. Remaining points: {}", parentPoints - requiredPoints);
        } else {
            log.error("Insufficient points. Parent ID: {}, Required: {}, Available: {}", 
                    parentID, requiredPoints, parentPoints);
            throw new InsufficientPointsException("Parent does not have enough points.");
        }
        return parentPoints;
    }

    private void deductParentPoints(Long parentID, long points, RoleType parentType) {
        ExceptionWrapper.handleVoid(() -> {
            log.debug("Deducting points from parent ID: {}, setting new points: {}, parent type: {}", parentID, points, parentType);
            if (parentType == RoleType.OWNER) {
                ownerRepository.updateOwnerPoints(parentID, points);
                log.debug("Updated owner points for ID: {}", parentID);
            } else if (parentType == RoleType.SUPER_MASTER) {
                superMasterRepository.updateSuperMasterPoints(parentID, points);
                log.debug("Updated master points for ID: {}", parentID);
            } else if (parentType == RoleType.MASTER) {
                masterRepository.updateMasterPoints(parentID, points);
                log.debug("Updated master points for ID: {}", parentID);
            } else {
                log.error("Unsupported parent type: {} for parent ID: {}", parentType, parentID);
                throw new RuntimeException("Unsupported parent type: " + parentType);
            }
            log.info("Successfully deducted points from parent ID: {}, new points: {}", parentID, points);
        });
    }

    public List<OwnerFilterSuperMasterAndMasterMeta> getParentHost(Long parentId) {
        log.debug("Fetching hosts for parent ID: {}", parentId);
        return ExceptionWrapper.handle(() -> {
            List<OwnerFilterSuperMasterAndMasterMeta> hosts = subscriberRepository.findHostsByParentId(parentId);
            log.debug("Found {} hosts for parent ID: {}", hosts.size(), parentId);
            return hosts;
        });
    }

    public Page<HostWithPlanName> getAllHosts(FilterRequest sortFilterBody, Long userId) {
        log.debug("Fetching all hosts with filters: {}, user ID: {}", sortFilterBody, userId);
        return ExceptionWrapper.handle(() -> {
            Map<String, String> columnAliasMap = Map.of(
                                    "plan", "p.name",
                                    "participantsCount", "SIZE(e.participants)",
                                    "subHostCount", "SIZE(e.subHosts)"
            );
            log.debug("Column alias map created");

            Map<String, Object> dynamicParams = new HashMap<>();
            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, userId);
            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.HostWithPlanName(e, p.name)";
            String fromClause = "FROM Subscriber e";
            String joinClause = "JOIN e.plan p";
            log.debug("Query clauses - Select: {}, From: {}, Join: {}, Where: {}", 
                    selectClause, fromClause, joinClause, whereClause);

            Page<HostWithPlanName> result = commonQueryService.fetchWithCustomFilters(
                    HostWithPlanName.class,
                    Subscriber.class,
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.of(joinClause),
                    Optional.of(whereClause),
                    Optional.empty(),
                    dynamicParams
            );

            log.info("Fetched {} hosts (page {} of {}, size {})", 
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

            if (sortFilterBody.getGlobalFilter() != null && !sortFilterBody.getGlobalFilter().isEmpty()) {
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
