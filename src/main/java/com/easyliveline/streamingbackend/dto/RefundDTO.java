package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.enums.RefundStatus;
import com.easyliveline.streamingbackend.models.Refund;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundDTO {

    private Long id;
    private String username;
    private int points;
    private Long parent;
    private long requester;
    private String subscriptionName;
    private String reason;
    private long subscriptionStartedAt;
    private int refundingMonths;
    private String type;
    private RefundStatus status;

    public RefundDTO(Refund refund) {
        this.id = refund.getId();
        this.username = refund.getUsername();
        this.points = refund.getPoints();
        this.parent = refund.getParentId();
        this.requester = refund.getRequester();
        this.subscriptionName = refund.getSubscriptionName();
        this.subscriptionStartedAt = refund.getSubscriptionStartedAt();
        this.type = refund.getType();
        this.refundingMonths = refund.getRefundingMonths();
        this.reason = refund.getReason();
        this.status = refund.getStatus();
    }
}
