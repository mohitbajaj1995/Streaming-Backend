package com.easyliveline.streamingbackend.dto;

public record TransactionDTO(Long id, Long points, String description, Long createdAt, boolean isCredit, long now, long after) {
}
