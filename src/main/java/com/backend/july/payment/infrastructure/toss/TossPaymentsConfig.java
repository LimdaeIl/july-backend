package com.backend.july.payment.infrastructure.toss;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TossPaymentsProperties.class)
public class TossPaymentsConfig {
}
