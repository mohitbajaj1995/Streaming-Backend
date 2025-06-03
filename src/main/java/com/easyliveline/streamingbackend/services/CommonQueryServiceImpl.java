package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.interfaces.CommonQueryService;
import com.easyliveline.streamingbackend.interfaces.ManagerRepository;
import com.easyliveline.streamingbackend.models.FilterRequest;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import com.easyliveline.streamingbackend.util.JwtUtil;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommonQueryServiceImpl implements CommonQueryService {
    private final EntityManager entityManager;
    private final ManagerRepository managerRepository;

    public CommonQueryServiceImpl(EntityManager entityManager, ManagerRepository managerRepository) {
        this.entityManager = entityManager;
        this.managerRepository = managerRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public <T> Page<T> fetchWithCustomFilters(
            Class<T> projectionClass,
            Class<?> entityClass,
            FilterRequest filterRequest,
            Map<String, String> columnAliasMap,
            Optional<String> customSelectClause,
            Optional<String> customFromClause,
            Optional<String> customJoinClause,
            Optional<String> customWhereClause,
            Optional<String> groupByClause,
            Map<String, Object> dynamicParams) {

        return ExceptionWrapper.handle(() -> {
            Session session = entityManager.unwrap(Session.class);

            Sort sort = buildSort(filterRequest);
            Pageable pageable = buildPageRequest(filterRequest, sort);

            // Build main HQL query
            StringBuilder hql = new StringBuilder();
            hql.append(customSelectClause.orElse("SELECT e")).append(" ");
            hql.append(customFromClause.orElse("FROM ").trim()).append(" ");
            customJoinClause.ifPresent(join -> hql.append(join).append(" "));
            customWhereClause.ifPresent(where -> hql.append(where).append(" "));
            groupByClause.ifPresent(group -> hql.append(group).append(" "));
            hql.append(buildOrderClause(sort, columnAliasMap));

            // Log for debug
            System.out.println("Generated HQL: " + hql);
            System.out.println("With Params: " + dynamicParams);

            // Run query
            Query<T> query = session.createQuery(hql.toString().trim(), projectionClass);
            System.out.println("Query: " + query);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
            dynamicParams.forEach(query::setParameter);

            List<T> results = query.getResultList();

            System.out.println("Printing Result: ");
            results.forEach(System.out::println);

            // Run group-aware count
            long total = getGroupedTotalCount(session, entityClass, customFromClause, customJoinClause, customWhereClause, groupByClause, dynamicParams);

            return new PageImpl<>(results, pageable, total);
        });
    }



    private Sort buildSort(FilterRequest request) {
        if (request.getSorting() != null && !request.getSorting().isEmpty()) {
            List<Sort.Order> orders = request.getSorting().stream()
                    .map(s -> new Sort.Order(s.isDesc() ? Sort.Direction.DESC : Sort.Direction.ASC, s.getId()))
                    .collect(Collectors.toList());
            return Sort.by(orders);
        }
        return Sort.by(Sort.Order.desc("createdAt"));
    }

    private Pageable buildPageRequest(FilterRequest request, Sort sort) {
        int page = request.getPagination() != null ? request.getPagination().getPageIndex() : 0;
        int size = request.getPagination() != null ? request.getPagination().getPageSize() : 10;
        return PageRequest.of(page, size, sort);
    }

    private String buildOrderClause(Sort sort, Map<String, String> columnAliasMap) {
        if (sort.isSorted()) {
            return sort.stream()
                    .map(order -> {
                        String column = columnAliasMap.getOrDefault(order.getProperty(), "e." + order.getProperty());
                        return column + " " + (order.isAscending() ? "ASC" : "DESC");
                    })
                    .collect(Collectors.joining(", ", " ORDER BY ", ""));
        }
        return "ORDER BY e.name ASC";
    }

    private <T> long getGroupedTotalCount(Session session,
                                          Class<T> entityClass,
                                          Optional<String> fromClause,
                                          Optional<String> joinClause,
                                          Optional<String> whereClause,
                                          Optional<String> groupByClause,
                                          Map<String, Object> dynamicParams) {

        // If no groupBy, use standard count
        if (groupByClause.isEmpty()) {
            StringBuilder countHql = new StringBuilder("SELECT COUNT(e) ");
            countHql.append(fromClause.orElse("FROM " + entityClass.getSimpleName() + " e")).append(" ");
            joinClause.ifPresent(jc -> countHql.append(jc).append(" "));
            whereClause.ifPresent(wc -> countHql.append(wc).append(" "));

            Query<Long> countQuery = session.createQuery(countHql.toString(), Long.class);
            dynamicParams.forEach(countQuery::setParameter);
            return countQuery.uniqueResult();
        }

        // With groupBy, fetch only group keys and count them
        StringBuilder groupHql = new StringBuilder("SELECT 1 ");
        groupHql.append(fromClause.orElse("FROM " + entityClass.getSimpleName() + " e")).append(" ");
        joinClause.ifPresent(j -> groupHql.append(j).append(" "));
        whereClause.ifPresent(w -> groupHql.append(w).append(" "));
        groupByClause.ifPresent(g -> groupHql.append(g).append(" "));

        Query<?> groupedQuery = session.createQuery(groupHql.toString().trim());
        dynamicParams.forEach(groupedQuery::setParameter);
        List<?> groupedResults = groupedQuery.getResultList();

        return groupedResults.size();
    }


    @Override
    public Long resolveParent() {
        return ExceptionWrapper.handle(() -> {
            Long userId = JwtUtil.getUserIdFromJWT();

            RoleType roleType = RoleType.valueOf(JwtUtil.getRoleFromJWT());

            if (roleType.equals(RoleType.MANAGER)) {
                return managerRepository.findParentIdByManagerId(userId);
            }

            return userId;
        });
    }
}


//    private <T> long getTotalCount(Session session, Class<T> entityClass,
//                                   Optional<String> fromClause,
//                                   Optional<String> joinClause,
//                                   Optional<String> customWhereClause,
//                                   Map<String, Object> dynamicParams) { // 👈 Add param
//        StringBuilder countHql = new StringBuilder("SELECT COUNT(e) ");
//        countHql.append(fromClause.orElse("FROM " + entityClass.getSimpleName() + " e"));
//        joinClause.ifPresent(jc -> countHql.append(" ").append(jc.trim()));
//        customWhereClause.ifPresent(wc -> countHql.append(" ").append(wc));
//
//        Query<Long> countQuery = session.createQuery(countHql.toString(), Long.class);
//        dynamicParams.forEach(countQuery::setParameter); // 👈 Set dynamic params
//        return countQuery.uniqueResult();
//    }


//    @Override
//    @Transactional(readOnly = true)
//    public <T> Page<T> fetchWithCustomFilters(
//            Class<T> projectionClass,
//            Class<?> entityClass,
//            FilterRequest filterRequest,
//            Map<String, String> columnAliasMap,
//            Optional<String> customSelectClause,
//            Optional<String> customFromClause,
//            Optional<String> customJoinClause,
//            Optional<String> customWhereClause,
//            Optional<String> groupByClause,
//            Map<String, Object> dynamicParams) {
//
//        return ExceptionWrapper.handle(() -> {
//            Session session = entityManager.unwrap(Session.class);
//
//            Sort sort = buildSort(filterRequest);
//            Pageable pageable = buildPageRequest(filterRequest, sort);
//
//            StringBuilder hql = new StringBuilder();
//            hql.append(customSelectClause.orElse("SELECT e"))
//                    .append(" ")
//                    .append(customFromClause.orElse("FROM " + entityClass.getSimpleName() + " e"));
//            customJoinClause.ifPresent(join -> hql.append(" ").append(join));
//            customWhereClause.ifPresent(where -> hql.append(" ").append(where));
//            hql.append(buildOrderClause(sort, columnAliasMap));
//
//            System.out.println("Generated HQL: " + hql);
//            System.out.println("With Params: " + dynamicParams);
//
//            Query<T> query = session.createQuery(hql.toString(), projectionClass);
//            query.setFirstResult((int) pageable.getOffset());
//            query.setMaxResults(pageable.getPageSize());
//            dynamicParams.forEach(query::setParameter);
//
//            List<T> results = query.getResultList();
//            long total = getTotalCount(session, entityClass, customFromClause, customJoinClause, customWhereClause, dynamicParams);
//
//            return new PageImpl<>(results, pageable, total);
//        });
//    }