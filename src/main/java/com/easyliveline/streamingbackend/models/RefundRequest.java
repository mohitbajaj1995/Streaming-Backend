package com.easyliveline.streamingbackend.models;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RefundRequest {

    @NotNull(message = "Duration is required")
    private int durationInMonths;
    private String reason;
}