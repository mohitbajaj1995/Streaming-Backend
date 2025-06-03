package com.easyliveline.streamingbackend.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AppSettings {
    private boolean needToUpdateTables;
    private boolean refreshScheduleOnStartup;
}