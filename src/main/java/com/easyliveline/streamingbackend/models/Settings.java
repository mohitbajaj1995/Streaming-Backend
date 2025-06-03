package com.easyliveline.streamingbackend.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Settings {
    private boolean isRefresh;
    private boolean isCacheOn;
}