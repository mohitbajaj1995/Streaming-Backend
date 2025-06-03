package com.easyliveline.streamingbackend.models;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MeetingUpdateRequest {
    private String name;
    private String email;
    private boolean activated;
    private String password;
    private String meetingNumber;
    private String meetingPassword;
    private Long zoomId;
}