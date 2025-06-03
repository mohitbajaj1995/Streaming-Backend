package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.RefundHistoryResponse;
import com.easyliveline.streamingbackend.dto.RefundDTO;
import com.easyliveline.streamingbackend.enums.RefundStatus;
import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.exceptions.RefundNotFoundException;
import com.easyliveline.streamingbackend.exceptions.UserNotFoundException;
import com.easyliveline.streamingbackend.interfaces.*;
import com.easyliveline.streamingbackend.models.*;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class RefundService {

    private final RefundRepository refundRepository;
    private final TransactionService transactionService;
    private final SubscriberRepository subscriberRepository;
    private final MasterRepository masterRepository;
    private final OwnerRepository ownerRepository;
    private final CommonQueryService commonQueryService;
    private final PlanRepository planRepository;

    @Autowired
    public RefundService(RefundRepository refundRepository, TransactionService transactionService, SubscriberRepository subscriberRepository, MasterRepository masterRepository, OwnerRepository ownerRepository, CommonQueryService commonQueryService, PlanRepository planRepository) {
        this.refundRepository = refundRepository;
        this.transactionService = transactionService;
        this.subscriberRepository = subscriberRepository;
        this.masterRepository = masterRepository;
        this.ownerRepository = ownerRepository;
        this.commonQueryService = commonQueryService;
        this.planRepository = planRepository;
    }

    @Transactional
    public void createRefund(long userId, RefundRequest refundRequest) throws IllegalArgumentException {
        log.info("Creating refund request for user ID: {} with duration: {} months", userId, refundRequest.getDurationInMonths());

        if(refundRequest.getDurationInMonths() <= 0) {
            log.error("Invalid refund duration: {}", refundRequest.getDurationInMonths());
            throw new IllegalArgumentException("Duration should be greater than 0");
        }

        Subscriber refundingUser = subscriberRepository.findById(userId)
            .orElseThrow(() -> {
                log.error("Refunding user not found with ID: {}", userId);
                return new IllegalArgumentException("Refunding user not found");
            });
        log.debug("Found refunding user: {}", refundingUser.getUsername());

        if (!refundingUser.isCanRefund()) {
            log.error("User {} cannot request refund, canRefund flag is false", refundingUser.getUsername());
            throw new IllegalArgumentException("User cannot request for refund");
        }

        if (refundingUser.getRefundableMonths() < refundRequest.getDurationInMonths()) {
            log.error("Insufficient refundable months. Available: {}, Requested: {}", 
                    refundingUser.getRefundableMonths(), refundRequest.getDurationInMonths());
            throw new IllegalArgumentException("Insufficient refundable months");
        }

        long lastRechargeLong = refundingUser.getLastRecharge(); // e.g., 20250507
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate lastRechargeDate = LocalDate.parse(String.valueOf(lastRechargeLong), formatter);
        log.debug("Last recharge date: {}, current date: {}", lastRechargeDate, LocalDate.now());

        if (lastRechargeDate.plusDays(7).isBefore(LocalDate.now())) {
            log.error("Refund request period has expired. Last recharge: {}, Current date: {}", 
                    lastRechargeDate, LocalDate.now());
            throw new IllegalArgumentException("Refund request period has expired");
        }

        Plan plan = refundingUser.getPlan();
        log.debug("User plan: {}, duration: {} months", plan.getName(), plan.getDurationInMonths());

        if(refundRequest.getDurationInMonths() > plan.getDurationInMonths()) {
            log.error("Requested duration ({} months) exceeds plan duration ({} months)", 
                    refundRequest.getDurationInMonths(), plan.getDurationInMonths());
            throw new IllegalArgumentException("Duration should be less than plan months");
        }

        String refundType = plan.getDurationInMonths() != refundRequest.getDurationInMonths() ? "Partial" : "Full";
        log.debug("Creating {} refund for {} months", refundType, refundRequest.getDurationInMonths());

        Refund refund = new Refund(
                refundRequest.getDurationInMonths(), 
                refundingUser.getUsername(), 
                refundingUser.getId(), 
                refundingUser.getParent().getId(), 
                commonQueryService.resolveParent(), 
                plan.getName(), 
                refundingUser.getLastRecharge(), 
                refundType, 
                refundRequest.getDurationInMonths(), 
                refundRequest.getReason(), 
                RefundStatus.PENDING
        );

        refundingUser.setCanRefund(false);
        subscriberRepository.save(refundingUser);
        refundRepository.save(refund);
        log.info("Refund request created successfully for user: {}, refund ID: {}", 
                refundingUser.getUsername(), refund.getId());
    }


    @Transactional
    public void acceptRefund(Long refundId) throws RefundNotFoundException, UserNotFoundException, IllegalArgumentException {
        log.info("Processing refund acceptance for refund ID: {}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.error("No pending refund found with ID: {}", refundId);
                    return new RefundNotFoundException("No Pending Refund found with id: " + refundId);
                });
        log.debug("Found refund with ID: {}, status: {}, refunding months: {}", 
                refundId, refund.getStatus(), refund.getRefundingMonths());

        Subscriber refundingUser = subscriberRepository.findById(refund.getUserId())
                .orElseThrow(() -> {
                    log.error("Refunding user not found with ID: {}", refund.getUserId());
                    return new UserNotFoundException("Refunding user not found");
                });
        log.debug("Found refunding user: {}, refundable months: {}", 
                refundingUser.getUsername(), refundingUser.getRefundableMonths());

        if(refundingUser.getRefundableMonths() < refund.getRefundingMonths()) {
            log.error("Insufficient refundable months. Available: {}, Required: {}", 
                    refundingUser.getRefundableMonths(), refund.getRefundingMonths());
            throw new IllegalArgumentException("Insufficient refundable months");
        }

        RoleType role = refundingUser.getParentType();
        Long parentId = refundingUser.getParent().getId();
        log.debug("Getting refund user parent with role: {}, ID: {}", role, parentId);
        User refundingParent = getRefundUserParent(role, parentId);
        log.debug("Found refunding parent: {}", refundingParent.getUsername());

        // Update refund status
        refund.setStatus(RefundStatus.REFUNDED);
        refundingUser.setCanRefund(true);
        log.debug("Updated refund status to REFUNDED and set canRefund flag to true");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate endAtDate = LocalDate.parse(String.valueOf(refundingUser.getEndAt()), formatter);
        LocalDate updatedEndAt = endAtDate.minusMonths(refund.getRefundingMonths()).minusDays(1);
        log.debug("Calculating new end date. Current: {}, New: {}", endAtDate, updatedEndAt);

        // Convert back to long (yyyyMMdd)
        long updatedEndAtLong = Long.parseLong(updatedEndAt.format(formatter));
        refundingUser.setEndAt(updatedEndAtLong);
        refundingUser.setRefundableMonths(refundingUser.getRefundableMonths() - refund.getRefundingMonths());
        log.debug("Updated user end date to: {} and reduced refundable months to: {}", 
                updatedEndAtLong, refundingUser.getRefundableMonths());

        long parentPointsBeforeRefund;
        if(refundingParent instanceof Master master) {
            parentPointsBeforeRefund = master.getPoints();
            master.setPoints(master.getPoints() + refund.getPoints());
            log.debug("Updated Master points. Before: {}, After: {}", 
                    parentPointsBeforeRefund, master.getPoints());
        } else if(refundingParent instanceof Owner owner) {
            parentPointsBeforeRefund = owner.getPoints();
            owner.setPoints(owner.getPoints() + refund.getPoints());
            log.debug("Updated Owner points. Before: {}, After: {}", 
                    parentPointsBeforeRefund, owner.getPoints());
        } else {
            log.error("Refunding parent has invalid type: {}", refundingParent.getClass().getName());
            throw new IllegalArgumentException("Refunding parent not found");
        }

        log.debug("Creating transaction billing for refund");
        transactionService.createTransactionBilling(
                refund.getUserId(),
                parentId,
                refund.getRefundingMonths(),
                "Refunding Months: " + refund.getRefundingMonths(),
                "Refund Received for " + refund.getRefundingMonths() + " from " + refundingUser.getUsername(),
                0,
                parentPointsBeforeRefund
        );

        // Save updated entities
        refundRepository.save(refund);
        subscriberRepository.save(refundingUser);
        log.debug("Saved updated refund and host entities");

        if(refundingParent instanceof Master) {
            masterRepository.save((Master) refundingParent);
            log.debug("Saved updated Master entity");
        } else {
            ownerRepository.save((Owner) refundingParent);
            log.debug("Saved updated Owner entity");
        }

        log.info("Successfully processed refund acceptance for refund ID: {}, user: {}", 
                refundId, refundingUser.getUsername());
    }

    private User getRefundUserParent(RoleType role, Long parentId) {
        log.debug("Getting refund user parent with role: {} and ID: {}", role, parentId);
        User parent = switch (role) {
            case MASTER -> masterRepository.findById(parentId)
                    .orElseThrow(() -> {
                        log.error("Master not found with ID: {}", parentId);
                        return new IllegalArgumentException("Master not found");
                    });
            case OWNER  -> ownerRepository.findById(parentId)
                    .orElseThrow(() -> {
                        log.error("Owner not found with ID: {}", parentId);
                        return new IllegalArgumentException("Owner not found");
                    });
            default     -> {
                log.error("Unsupported role for refund: {}", role);
                throw new IllegalArgumentException("Unsupported role for refund");
            }
        };
        log.debug("Found parent user: {}", parent.getUsername());
        return parent;
    }

    @Transactional
    public void rejectRefund(Long refundId) throws RefundNotFoundException, UserNotFoundException {
        log.info("Processing refund rejection for refund ID: {}", refundId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> {
                    log.error("No pending refund found with ID: {}", refundId);
                    return new RefundNotFoundException("No Pending Refund not found with id: " + refundId);
                });
        log.debug("Found refund with ID: {}, status: {}", refundId, refund.getStatus());

        Subscriber refundingUser = subscriberRepository.findById(refund.getUserId())
                .orElseThrow(() -> {
                    log.error("Refunding user not found with ID: {}", refund.getUserId());
                    return new UserNotFoundException("Refunding user not found");
                });
        log.debug("Found refunding user: {}", refundingUser.getUsername());

        refundingUser.setCanRefund(true);
        refund.setStatus(RefundStatus.REJECTED);
        log.debug("Updated refund status to REJECTED and set canRefund flag to true");

        refundRepository.save(refund);
        subscriberRepository.save(refundingUser);
        log.info("Successfully processed refund rejection for refund ID: {}, user: {}", 
                refundId, refundingUser.getUsername());
    }

    public RefundHistoryResponse getRefundHistory(Long parentId) {
        log.info("Retrieving refund history for parent ID: {}", parentId);
        LocalDateTime now = LocalDateTime.now();

        log.debug("Calculating time periods for refund statistics");
        long oneWeekAgo = toYYYYMMDD(now.minusWeeks(1));
        long oneMonthAgo = toYYYYMMDD(now.minusMonths(1));
        long oneYearAgo = toYYYYMMDD(now.minusYears(1));

        log.debug("Querying refund statistics with time periods: 1w={}, 1m={}, 1y={}", 
                oneWeekAgo, oneMonthAgo, oneYearAgo);

        Object[] result = refundRepository.getRefundStatistics(
                parentId,
                oneWeekAgo,
                oneMonthAgo,
                oneYearAgo
        );

        // Check if result is non-null and contains at least one item
        if (result == null || result.length == 0) {
            log.error("Unexpected result from query for parent ID: {}. No data returned.", parentId);
            throw new RuntimeException("Unexpected result from query. No data returned.");
        }

        // Log the inner content for debugging
        Object[] statistics = (Object[]) result[0];  // Unwrap the inner array
        log.debug("Retrieved raw statistics array with {} elements", statistics.length);

        // Safely cast the inner array elements to Long
        long totalRefunds = castToLong(statistics[0]);
        long lastWeekRefunds = castToLong(statistics[1]);
        long lastMonthRefunds = castToLong(statistics[2]);
        long lastYearRefunds = castToLong(statistics[3]);
        long totalRefunded = castToLong(statistics[4]);
        long totalRejected = castToLong(statistics[5]);
        long totalPending = castToLong(statistics[6]);

        log.debug("Parsed statistics: total={}, 1w={}, 1m={}, 1y={}, refunded={}, rejected={}, pending={}", 
                totalRefunds, lastWeekRefunds, lastMonthRefunds, lastYearRefunds, 
                totalRefunded, totalRejected, totalPending);

        return new RefundHistoryResponse(
                totalRefunds,
                lastWeekRefunds,
                lastMonthRefunds,
                lastYearRefunds,
                totalRefunded,
                totalRejected,
                totalPending
        );
    }

    private long toYYYYMMDD(LocalDateTime dateTime) {
        long result = Long.parseLong(dateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        log.trace("Converted LocalDateTime {} to YYYYMMDD format: {}", dateTime, result);
        return result;
    }

    private long castToLong(Object obj) {
        if (obj instanceof Number) {
            long value = ((Number) obj).longValue();
            log.trace("Cast object of type {} to long: {}", obj.getClass().getSimpleName(), value);
            return value;
        }
        log.warn("Failed to cast object to long, returning 0. Object type: {}", 
                (obj != null) ? obj.getClass().getSimpleName() : "null");
        return 0;
    }

    public Page<RefundDTO> getAllRefunds(FilterRequest sortFilterBody) {
        log.info("Fetching all refunds with filters");
        return ExceptionWrapper.handle(() -> {
            Map<String, String> columnAliasMap = Map.of(
            );
            log.debug("Column alias map created");

            Map<String, Object> dynamicParams = new HashMap<>();
            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, null);
            log.debug("Where clause generated: {}", whereClause);

            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.RefundDTO(e)";
            String fromClause = "FROM Refund e";
//            String joinClause = "";
            log.debug("Query clauses - Select: {}, From: {}", selectClause, fromClause);

            return commonQueryService.fetchWithCustomFilters(
                    RefundDTO.class,         // projection
                    Refund.class,            // entity
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.empty(),
                    Optional.of(whereClause),
                    Optional.empty(), // group by
                    dynamicParams
            );
        });
    }


    private static String getStringBuilder(FilterRequest sortFilterBody, Map<String, Object> dynamicParams, Long userId) {
        log.debug("Building where clause for refund query");
        StringBuilder whereClause = new StringBuilder();
        boolean hasCondition = false;

        if (sortFilterBody.getGlobalFilter() != null && !sortFilterBody.getGlobalFilter().isEmpty()) {
            log.debug("Adding global filter: {}", sortFilterBody.getGlobalFilter());
            if (!hasCondition) {
                whereClause.append(" WHERE ");
                hasCondition = true;
            } else {
                whereClause.append(" AND ");
            }
            whereClause.append("(e.reason ILIKE :globalFilter OR e.username ILIKE :globalFilter)");
            dynamicParams.put("globalFilter", "%" + sortFilterBody.getGlobalFilter() + "%");
        }

        if (!sortFilterBody.getColumnFilters().isEmpty()) {
            log.debug("Processing {} column filters", sortFilterBody.getColumnFilters().size());
        }

        for (FilterRequest.ColumnFilter filter : sortFilterBody.getColumnFilters()) {
            log.debug("Processing column filter - ID: {}, Value: {}", filter.getId(), filter.getValue());
            switch (filter.getId()) {
                case "type" -> {
                    if (!hasCondition) {
                        whereClause.append(" WHERE ");
                        hasCondition = true;
                    } else {
                        whereClause.append(" AND ");
                    }
                    whereClause.append("e.type = :type");
                    dynamicParams.put("type", filter.getValue());
                    log.debug("Added type filter: {}", filter.getValue());
                }
                case "status" -> {
                    if (!hasCondition) {
                        whereClause.append(" WHERE ");
                        hasCondition = true;
                    } else {
                        whereClause.append(" AND ");
                    }
                    whereClause.append("e.status = :status");
                    dynamicParams.put("status", RefundStatus.valueOf(filter.getValue()));
                    log.debug("Added status filter: {}", filter.getValue());
                }
                case "username" -> {
                    if (!hasCondition) {
                        whereClause.append(" WHERE ");
                        hasCondition = true;
                    } else {
                        whereClause.append(" AND ");
                    }
                    whereClause.append("e.username LIKE :username");
                    dynamicParams.put("username", "%" + filter.getValue() + "%");
                    log.debug("Added username filter: {}", filter.getValue());
                }
            }
        }

        String result = whereClause.toString();
        log.debug("Final where clause: {}", result);
        return result;
    }
}
