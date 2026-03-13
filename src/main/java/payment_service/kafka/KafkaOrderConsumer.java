package payment_service.kafka;

import event.OrderEvent;
import payment_service.dto.PaymentRequest;
import payment_service.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOrderConsumer {
    private final PaymentService paymentService;

    @KafkaListener(topics = "${kafka.topic.order.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenCreateOrder(OrderEvent orderEvent) {
        log.info("Accepting event from Kafka with total amount: {}", orderEvent.paymentAmount());
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .orderId(orderEvent.id())
                .userId(orderEvent.userId())
                .paymentAmount(orderEvent.paymentAmount())
                .build();
        paymentService.createPayment(paymentRequest);
    }
}
