package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.dto.TransactionDTO;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.interfaces.TransactionRepository;
import com.easyliveline.streamingbackend.models.FilterRequest;
import com.easyliveline.streamingbackend.models.Transaction;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CommonQueryService commonQueryService;

    public TransactionService(TransactionRepository transactionRepository, CommonQueryService commonQueryService) {
        this.transactionRepository = transactionRepository;
        this.commonQueryService = commonQueryService;
    }

    @Transactional
    public void createTransactionBilling(Long fromUserId, Long toUserId, int points,
                                         String fromDescription, String toDescription,
                                         long fromCurrentPoints, long toCurrentPoints) {
        log.info("Creating transaction billing - from user ID: {}, to user ID: {}, points: {}", 
                fromUserId, toUserId, points);
        ExceptionWrapper.handleVoid(() -> {
            long now = System.currentTimeMillis();
            log.debug("Transaction timestamp: {}", now);

            log.debug("Inserting 'from' transaction for user ID: {}, points: {}, current points: {}", 
                    fromUserId, points, fromCurrentPoints);
            boolean fromInserted = insertTransaction(
                    fromUserId, points, fromDescription, false, now, 
                    fromCurrentPoints, fromCurrentPoints - points
            );

            log.debug("Inserting 'to' transaction for user ID: {}, points: {}, current points: {}", 
                    toUserId, points, toCurrentPoints);
            boolean toInserted = insertTransaction(
                    toUserId, points, toDescription, true, now, 
                    toCurrentPoints, toCurrentPoints + points
            );

            if (!fromInserted || !toInserted) {
                String errorMessage = "Transaction insert failed for " +
                        (!fromInserted ? "sender (ID " + fromUserId + ")" : "receiver (ID " + toUserId + ")");
                log.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            log.info("Successfully created transaction billing between users {} and {}", fromUserId, toUserId);
        });
    }

//    private boolean insertTransaction(Long userId, int points, String description,
//                                      boolean isCredit, long timestamp, long beforePoints, long afterPoints) {
//        log.debug("Inserting transaction - user ID: {}, points: {}, isCredit: {}, before: {}, after: {}",
//                userId, points, isCredit, beforePoints, afterPoints);
//        return ExceptionWrapper.handle(() -> {
//            int result = transactionRepository.insertTransaction(
//                    userId, points, description, isCredit, timestamp, beforePoints, afterPoints
//            );
//
//            if (result == 1) {
//                log.debug("Successfully inserted transaction for user ID: {}", userId);
//                return true;
//            } else {
//                log.error("Failed to insert transaction for user ID: {}, result: {}", userId, result);
//                return false;
//            }
//        });
//    }

    private boolean insertTransaction(Long userId, int points, String description,
                                      boolean isCredit, long timestamp, long beforePoints, long afterPoints) {
        log.debug("Inserting transaction - user ID: {}, points: {}, isCredit: {}, before: {}, after: {}",
                userId, points, isCredit, beforePoints, afterPoints);

        return ExceptionWrapper.handle(() -> {
            Transaction transaction = new Transaction();
            transaction.setUserId(userId);
            transaction.setPoints(points);
            transaction.setDescription(description);
            transaction.setCredit(isCredit);
            transaction.setCreatedAt(timestamp);
            transaction.setNow(beforePoints);
            transaction.setAfter(afterPoints);

            Transaction saved = transactionRepository.save(transaction);

            if (saved.getId() != null) {
                log.debug("Successfully inserted transaction for user ID: {}", userId);
                return true;
            } else {
                log.error("Failed to insert transaction for user ID: {}", userId);
                return false;
            }
        });
    }



    public Page<TransactionDTO> getAllTransactions(FilterRequest sortFilterBody) {
        log.info("Fetching all transactions with filters");
        return ExceptionWrapper.handle(() -> {
            Map<String, String> columnAliasMap = Map.of(
    //                "name", "e.name",
    //                "locked", "e.locked",
    //                "hostUsername", "ho.username"
            );
            log.debug("Column alias map created");

            Long parentId = JwtUtil.getUserIdFromJWT();
            log.debug("Parent ID from JWT: {}", parentId);

            Map<String, Object> dynamicParams = new HashMap<>();
            String whereClause = getStringBuilder(sortFilterBody, dynamicParams, parentId);
            log.debug("Where clause generated: {}", whereClause);

            String selectClause = "SELECT new com.easyliveline.streamingbackend.dto.TransactionDTO(e.id, e.points, e.description, e.createdAt, e.isCredit, e.now, e.after)";
            String fromClause = "FROM Transaction e";
            String joinClause = "";
            log.debug("Query clauses - Select: {}, From: {}", selectClause, fromClause);

            Page<TransactionDTO> result = commonQueryService.fetchWithCustomFilters(
                    TransactionDTO.class,         // projection
                    Transaction.class,            // entity
                    sortFilterBody,
                    columnAliasMap,
                    Optional.of(selectClause),
                    Optional.of(fromClause),
                    Optional.of(joinClause),
                    Optional.of(whereClause),
                    Optional.empty(), // group by
                    dynamicParams
            );

            log.info("Fetched {} transactions (page {} of {}, size {})", 
                    result.getNumberOfElements(), 
                    result.getNumber() + 1, 
                    result.getTotalPages(),
                    result.getSize());

            return result;
        });
    }


    private static String getStringBuilder(FilterRequest sortFilterBody,Map<String, Object> dynamicParams, Long userId) {
        StringBuilder whereClause = new StringBuilder("WHERE e.userId = :userId");
        dynamicParams.put("userId", userId);

        if (sortFilterBody.getGlobalFilter() != null && !sortFilterBody.getGlobalFilter().isEmpty()) {
            whereClause.append(" AND e.description ILIKE :globalFilter");
            dynamicParams.put("globalFilter", "%" + sortFilterBody.getGlobalFilter() + "%");
        }

        for (FilterRequest.ColumnFilter filter : sortFilterBody.getColumnFilters()) {
            switch (filter.getId()) {
                case "isCredit" -> {
                    whereClause.append(" AND e.isCredit = :isCredit");
                    dynamicParams.put("isCredit", Boolean.parseBoolean(filter.getValue()));
                }
            }
        }
        return whereClause.toString();
    }
}
