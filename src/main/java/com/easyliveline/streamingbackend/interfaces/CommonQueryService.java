package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.FilterRequest;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.Optional;

public interface CommonQueryService {
    <T> Page<T> fetchWithCustomFilters(
            Class<T> projectionClass,
            Class<?> entityClass,
            FilterRequest filterRequest,
            Map<String, String> columnAliasMap,
            Optional<String> customSelectClause,
            Optional<String> customFromClause,
            Optional<String> customJoinClause,
            Optional<String> customWhereClause,
            Optional<String> groupByClause,
            Map<String, Object> dynamicParams);
    Long resolveParent();
}
