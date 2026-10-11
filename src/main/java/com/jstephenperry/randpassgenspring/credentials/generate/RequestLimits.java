package com.jstephenperry.randpassgenspring.credentials.generate;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@ConfigurationProperties("credential.limits")
record RequestLimits(@DefaultValue("50") @Min(1) @Max(10000) int requestsPerSecond,
		@DefaultValue("100") @Min(1) @Max(10000) int burst) {
}
