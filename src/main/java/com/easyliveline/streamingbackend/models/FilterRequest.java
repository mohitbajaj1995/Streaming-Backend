package com.easyliveline.streamingbackend.models;

import lombok.Data;

import java.util.List;

@Data
public class FilterRequest {
    private List<ColumnFilter> columnFilters;
    private String globalFilter;
    private List<Sorting> sorting;
    private Pagination pagination;

    @Data
    public static class ColumnFilter {
        private String id;
        private String value;
    }

    @Data
    public static class Sorting {
        private String id;
        private boolean desc;
    }

    @Data
    public static class Pagination {
        private int pageIndex;
        private int pageSize;
    }
}