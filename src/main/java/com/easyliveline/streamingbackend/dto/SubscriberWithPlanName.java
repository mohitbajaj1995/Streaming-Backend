package com.easyliveline.streamingbackend.dto;

import com.easyliveline.streamingbackend.models.Subscriber;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SubscriberWithPlanName {

    private Long id;
    private String name;
    private String username;
    private long startAt;
    private long endAt;
    private long lastRecharge;
    private long lastSeen;
    private int participantsCount;
    private Boolean canRefund;
    private int refundableMonths;
    private int subHostCount;
    private Boolean enabled;
    private String plan;

    public SubscriberWithPlanName(Subscriber subscriber, String planName, int participantsCount, int subHostCount) {
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
        this.participantsCount = participantsCount;
        this.subHostCount = subHostCount;
        if (subscriber.getPlan() != null) {
            this.plan = planName;
        }
    }
}
