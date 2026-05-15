package com.airtribe.jobexecutor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.kafka")
@Component
@Data
public class AivenKafkaProperties {

    private String bootstrapServers;
    private Ssl ssl = new Ssl();
    private Consumer consumer = new Consumer();

    @Data
    public static class Ssl {
        private String caPemLocation;
        private String svcPemLocation;
        private String truststorePassword;
        private String keystorePassword;
    }
    @Data
    public static class Consumer {
        private String groupId;
        private String topic;
        private String autoOffsetReset = "earliest";
    }
}