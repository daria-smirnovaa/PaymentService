package payment_service.kafka;

import event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPaymentProducer {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final NewTopic topic;

    public void sendPaymentEvent(PaymentEvent event) {
        log.info("Sending event {} to Kafka with topic {}", event, topic.name());
        kafkaTemplate.send(topic.name(), event);
    }
}
