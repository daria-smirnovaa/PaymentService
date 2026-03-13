package payment_service.service;

import event.PaymentEvent;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.dto.Status;
import payment_service.entity.Payment;
import payment_service.kafka.KafkaPaymentProducer;
import payment_service.mapper.PaymentMapper;
import payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RandomNumberService randomNumberService;

    @Mock
    private KafkaPaymentProducer kafkaPaymentProducer;

    @InjectMocks
    private PaymentService paymentService;

    public static final Long ORDER_ID = 1L;
    public static final Long USER_ID = 100L;
    public static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("99.99");


    private PaymentRequest paymentRequest;
    private Payment payment;
    private Payment savedPayment;
    private PaymentResponse paymentResponse;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentMapper, paymentRepository, randomNumberService, kafkaPaymentProducer);

        paymentRequest = PaymentRequest.builder()
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .build();

        payment = Payment.builder()
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .build();

        savedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .timestamp(LocalDateTime.now())
                .status(Status.SUCCESS)
                .build();

        paymentResponse = PaymentResponse.builder()
                .id(savedPayment.getId())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .timestamp(savedPayment.getTimestamp())
                .status(Status.SUCCESS)
                .build();
    }

    @Test
    void createPayment_WhenRandomNumberIsEven_ShouldReturnSuccessPayment() {
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(randomNumberService.isEvenNumber()).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(result.getOrderId()).isEqualTo(paymentRequest.getOrderId());

        verify(paymentMapper).toEntity(paymentRequest);
        verify(randomNumberService).isEvenNumber();
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentMapper).toDto(savedPayment);
        verify(kafkaPaymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_WhenRandomNumberIsOdd_ShouldReturnFailedPayment() {
        Payment failedPayment = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .timestamp(LocalDateTime.now())
                .status(Status.FAILED)
                .build();

        PaymentResponse failedResponse = PaymentResponse.builder()
                .id(failedPayment.getId())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(PAYMENT_AMOUNT)
                .timestamp(failedPayment.getTimestamp())
                .status(Status.FAILED)
                .build();

        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(randomNumberService.isEvenNumber()).thenReturn(false);
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);
        when(paymentMapper.toDto(failedPayment)).thenReturn(failedResponse);

        PaymentResponse result = paymentService.createPayment(paymentRequest);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Status.FAILED);
        verify(randomNumberService).isEvenNumber();
        verify(kafkaPaymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }

    @Test
    void createPayment_ShouldSetIdAndTimestamp() {
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(randomNumberService.isEvenNumber()).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        paymentService.createPayment(paymentRequest);

        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() {
        List<Payment> payments = List.of(savedPayment);
        List<PaymentResponse> expectedResponses = List.of(paymentResponse);

        when(paymentRepository.findAllByOrderId(ORDER_ID)).thenReturn(payments);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(ORDER_ID);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).isEqualTo(expectedResponses);
        verify(paymentRepository).findAllByOrderId(ORDER_ID);
        verify(paymentMapper).toDto(savedPayment);
    }

    @Test
    void getPaymentsByOrderId_WhenNoPayments_ShouldReturnEmptyList() {
        Long orderId = 999L;
        when(paymentRepository.findAllByOrderId(orderId)).thenReturn(List.of());

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(orderId);

        assertThat(result).isNotNull().isEmpty();
        verify(paymentRepository).findAllByOrderId(orderId);
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() {
        List<Payment> payments = List.of(savedPayment);
        List<PaymentResponse> expectedResponses = List.of(paymentResponse);

        when(paymentRepository.findAllByUserId(USER_ID)).thenReturn(payments);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByUserId(USER_ID);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).isEqualTo(expectedResponses);
        verify(paymentRepository).findAllByUserId(USER_ID);
        verify(paymentMapper).toDto(savedPayment);
    }

    @Test
    void getPaymentsByStatus_ShouldReturnPayments() {
        Status status = Status.SUCCESS;
        List<Payment> payments = List.of(savedPayment);
        List<PaymentResponse> expectedResponses = List.of(paymentResponse);

        when(paymentRepository.findAllByStatus(status)).thenReturn(payments);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByStatus(status);

        assertThat(result).isNotNull().hasSize(1);
        assertThat(result).isEqualTo(expectedResponses);
        verify(paymentRepository).findAllByStatus(status);
        verify(paymentMapper).toDto(savedPayment);
    }

    @Test
    void getTotalSumOfPaymentsForDatePeriod_ShouldReturnCorrectSum() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        Payment payment1 = Payment.builder()
                .paymentAmount(new BigDecimal("50.00"))
                .build();
        Payment payment2 = Payment.builder()
                .paymentAmount(new BigDecimal("75.50"))
                .build();

        List<Payment> payments = List.of(payment1, payment2);
        BigDecimal expectedSum = new BigDecimal("125.50");

        when(paymentRepository.findByTimestampBetween(startDate, endDate)).thenReturn(payments);

        BigDecimal result = paymentService.getTotalSumOfPaymentsForDatePeriod(startDate, endDate);

        assertThat(result).isEqualByComparingTo(expectedSum);
        verify(paymentRepository).findByTimestampBetween(startDate, endDate);
    }

    @Test
    void getTotalSumOfPaymentsForDatePeriod_WhenNoPayments_ShouldReturnZero() {
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(paymentRepository.findByTimestampBetween(startDate, endDate)).thenReturn(List.of());

        BigDecimal result = paymentService.getTotalSumOfPaymentsForDatePeriod(startDate, endDate);

        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        verify(paymentRepository).findByTimestampBetween(startDate, endDate);
    }

    @Test
    void createPayment_ShouldSendCorrectKafkaEvent() {
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(randomNumberService.isEvenNumber()).thenReturn(true);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(paymentMapper.toDto(savedPayment)).thenReturn(paymentResponse);

        paymentService.createPayment(paymentRequest);

        verify(kafkaPaymentProducer).sendPaymentEvent(any(PaymentEvent.class));
    }
}