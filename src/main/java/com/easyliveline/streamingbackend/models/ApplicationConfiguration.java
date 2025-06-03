package com.easyliveline.streamingbackend.models;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "streaming")
public class ApplicationConfiguration {

    private Map<String, WebsiteConfig> website;
    private AppSettings appSettings;

    @PostConstruct
    public void print() {
        System.out.println("Loaded website config: " + website);
        System.out.println("Loaded app settings: " + appSettings);
    }
}
