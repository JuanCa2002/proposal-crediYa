package co.com.pragma.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapter.sqs")
public record SQSSenderProperties(
     String region,
     String queueSendNotificationUrl,
     String queueAutomaticValidationUrl,
     String queueAutomaticEvaluationResponsesUrl,
     String queueReportMetrics,
     String endpoint){
}
