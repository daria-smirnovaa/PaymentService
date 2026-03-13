package payment_service.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${kafka.topic.payment.name}")
    private String paymentTopic;

    @Value(value = "${kafka.topic.payment.partitions}")
    private int paymentTopicNumPartitions;

    @Value(value = "${kafka.topic.payment.replicas}")
    private short paymentReplicationFactor;


    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configs.put(AdminClientConfig.RETRIES_CONFIG, 5);
        configs.put(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic paymentTopic() {
        return new NewTopic(paymentTopic, paymentTopicNumPartitions, paymentReplicationFactor);
    }
}
