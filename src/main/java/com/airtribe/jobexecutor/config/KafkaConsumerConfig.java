package com.airtribe.jobexecutor.config;

import com.airtribe.jobexecutor.config.AivenKafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    private final AivenKafkaProperties props;

    public KafkaConsumerConfig(AivenKafkaProperties props) {
        this.props = props;
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, props.getBootstrapServers());
        config.put("security.protocol", "SSL");

        // Truststore — JKS
        config.put("ssl.truststore.type",     "JKS");
        config.put("ssl.truststore.location", props.getSsl().getCaPemLocation());   // truststore.jks
        config.put("ssl.truststore.password", props.getSsl().getTruststorePassword());

        // Keystore — PKCS12
        config.put("ssl.keystore.type",     "PKCS12");
        config.put("ssl.keystore.location", props.getSsl().getSvcPemLocation());    // client.p12
        config.put("ssl.keystore.password", props.getSsl().getKeystorePassword());
        config.put("ssl.key.password",      props.getSsl().getKeystorePassword());
        // NO ssl.keystore.key needed for PKCS12 — key is embedded in the .p12 file

        config.put("auto.register.schemas", "false");
        config.put(ConsumerConfig.GROUP_ID_CONFIG,                 props.getConsumer().getGroupId());
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,        props.getConsumer().getAutoOffsetReset());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return config;
    }
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3);   // equivalent to running 3 consumer threads
        return factory;
    }
}