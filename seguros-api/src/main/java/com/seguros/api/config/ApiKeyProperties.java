package com.seguros.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApiKeyProperties(String apiKey, double ipc) {}
