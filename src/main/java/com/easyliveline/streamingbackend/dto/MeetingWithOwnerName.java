package com.easyliveline.streamingbackend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MeetingWithOwnerName {
    private Long id;
    private String name;
    private String email;
    private String password;
    private String meetingNumber;
    private String meetingPassword;
    private Integer createdAt;
    private long zoomAccountId;


    public MeetingWithOwnerName(
            Long id,
            String name,
            String email,
            String password,
            String meetingNumber,
            String meetingPassword,
            Integer createdAt,
            Long zoomAccountId
    ) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.meetingNumber = meetingNumber;
        this.meetingPassword = meetingPassword;
        this.createdAt = createdAt != null ? createdAt : 0;
        this.zoomAccountId = zoomAccountId != null ? zoomAccountId : 0L;
    }
}


//    public MeetingWithOwnerName(Meeting meeting) {
//        this.name = meeting.getName();
//        this.email = meeting.getEmail();
//        this.activated = meeting.isActivated();
//        this.meetingNumber = meeting.getMeetingNumber();
//        this.meetingPassword = meeting.getMeetingPassword();
//        this.slot = meeting.getSlot();
//        this.hostParentType = meeting.getHostParentType();
//        this.hostParent = meeting.getHostParent();
//        this.zoom = meeting.getZoom();
//    }