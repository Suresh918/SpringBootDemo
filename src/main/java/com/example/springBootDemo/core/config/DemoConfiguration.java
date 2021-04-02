package com.example.springBootDemo.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "project.libraries.framework.core")
@Data
public class DemoConfiguration {
    private Boolean entityCacheEnabled = false;
    private String property1 = "";
    private String property2 = "";
}
