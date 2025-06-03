package com.easyliveline.streamingbackend.dto;

public record RefundHistoryResponse(long totalRefunds, long lastWeekRefunds, long lastMonthRefunds,
                                    long lastYearRefunds, long totalRefunded, long totalRejected, long totalPending) {
}
