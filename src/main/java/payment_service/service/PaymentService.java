package payment_service.service;

import event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.dto.Status;
import payment_service.entity.Payment;
import payment_service.kafka.KafkaPaymentProducer;
import payment_service.mapper.PaymentMapper;
import payment_service.repository.PaymentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {
    public final PaymentMapper paymentMapper;
    public final PaymentRepository paymentRepository;
    public final RandomNumberService randomNumberService;
    public final KafkaPaymentProducer kafkaPaymentProducer;

    public PaymentResponse createPayment(PaymentRequest paymentRequest) {
        log.info("Creating payment for order: {}", paymentRequest.getOrderId());
        Payment payment = paymentMapper.toEntity(paymentRequest);
        payment.setId(UUID.randomUUID());
        payment.setTimestamp(LocalDateTime.now());

        try {
            Boolean isEven = randomNumberService.isEvenNumber();
            if (isEven != null) {
                payment.setStatus(isEven ? Status.SUCCESS : Status.FAILED);
                log.info("Random number service returned: {}, status: {}", isEven, payment.getStatus());
            } else {
                log.warn("Random number service returned null, setting status to SUCCESS");
                payment.setStatus(Status.SUCCESS);
            }
        } catch (Exception e) {
            log.error("Failed to get random number from external API: {}", e.getMessage());
            payment.setStatus(Status.SUCCESS);
        }

        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment saved with id: {}, status: {}", savedPayment.getId(), savedPayment.getStatus());

        PaymentEvent event = PaymentEvent.builder()
                .id(savedPayment.getId())
                .orderId(savedPayment.getOrderId())
                .status(savedPayment.getStatus().toString())
                .build();
        kafkaPaymentProducer.sendPaymentEvent(event);

        return paymentMapper.toDto(savedPayment);
    }

    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findAllByOrderId(orderId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByUserId(Long userId) {
        return paymentRepository.findAllByUserId(userId).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    public List<PaymentResponse> getPaymentsByStatus(Status status) {
        return paymentRepository.findAllByStatus(status).stream()
                .map(paymentMapper::toDto)
                .toList();
    }

    public BigDecimal getTotalSumOfPaymentsForDatePeriod(LocalDateTime startDate, LocalDateTime endDate) {
        List<Payment> payments = paymentRepository.findByTimestampBetween(startDate, endDate);
        return payments.stream()
                .map(Payment::getPaymentAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}