package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.Subscriber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HostWithPlanName {

    private Long id;
    private String name;
    private String username;
    private long startAt;
    private long endAt;
    private long lastRecharge;
    private long lastSeen;
    private Boolean canRefund;
    private int refundableMonths;
    private Boolean enabled;
    private String plan;

    public HostWithPlanName(Subscriber subscriber, String planName) {
        this.id = subscriber.getId();
        this.name = subscriber.getName();
        this.username = subscriber.getUsername();
        this.startAt = subscriber.getStartAt();
        this.endAt = subscriber.getEndAt();
        this.lastRecharge = subscriber.getLastRecharge();
        this.refundableMonths = subscriber.getRefundableMonths();
        this.lastSeen = subscriber.getLastSeen();
        this.enabled = subscriber.isEnabled();
        this.canRefund = subscriber.isCanRefund();
        if (subscriber.getPlan() != null) {
            this.plan = planName;
        }
    }
}
