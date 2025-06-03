package com.easyliveline.streamingbackend.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WebsiteConfig {
    private boolean isVideo;
    private String website;
    private Settings settings;
}