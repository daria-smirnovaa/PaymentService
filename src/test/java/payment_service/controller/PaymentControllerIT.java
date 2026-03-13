package payment_service.controller;

import payment_service.dto.DateRange;
import payment_service.dto.PaymentRequest;
import payment_service.dto.Status;
import payment_service.entity.Payment;
import payment_service.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class PaymentControllerIT extends BaseIT {

    @Autowired
    private PaymentRepository paymentRepository;

    public static final Long ORDER_ID = 1L;
    public static final Long INVALID_ORDER_ID = -1L;
    public static final Long USER_ID = 100L;

    private Payment payment1;
    private Payment payment2;
    private Payment payment3;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
        paymentRequest = PaymentRequest.builder()
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(new BigDecimal("150.50"))
                .build();

        payment1 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(new BigDecimal("100.00"))
                .status(Status.SUCCESS)
                .timestamp(LocalDateTime.now().minusDays(2))
                .build();

        payment2 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(new BigDecimal("200.00"))
                .status(Status.SUCCESS)
                .timestamp(LocalDateTime.now().minusDays(1))
                .build();

        payment3 = Payment.builder()
                .id(UUID.randomUUID())
                .orderId(ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(new BigDecimal("50.00"))
                .status(Status.FAILED)
                .timestamp(LocalDateTime.now().plusDays(1))
                .build();
    }

    @Test
    void createPayment_ShouldReturnCreatedPayment() throws Exception {
        ResultActions result = mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(paymentRequest)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.userId").value(100))
                .andExpect(jsonPath("$.paymentAmount").value(150.50));

        List<Payment> payments = paymentRepository.findAll();
        assertThat(payments).hasSize(1);
        assertThat(payments.getFirst().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(payments.getFirst().getPaymentAmount()).isEqualTo(new BigDecimal("150.50"));
    }

    @Test
    void getPaymentsByOrderId_ShouldReturnPayments() throws Exception {
        paymentRepository.saveAll(List.of(payment1, payment2));

        ResultActions result = mockMvc.perform(get("/payments/order/{orderId}", ORDER_ID));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[1].orderId").value(1));
    }

    @Test
    void getPaymentsByUserId_ShouldReturnPayments() throws Exception {
        paymentRepository.saveAll(List.of(payment1, payment2));

        ResultActions result = mockMvc.perform(get("/payments/user/{userId}", USER_ID));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].userId").value(100))
                .andExpect(jsonPath("$[1].userId").value(100));
    }

    @Test
    void getPaymentsByStatus_ShouldReturnPayments() throws Exception {
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        ResultActions result = mockMvc.perform(get("/payments/status/{status}", Status.SUCCESS));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"))
                .andExpect(jsonPath("$[1].status").value("SUCCESS"));
    }

    @Test
    void getTotalSumOfPaymentsForDatePeriod_ShouldReturnCorrectSum() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        paymentRepository.saveAll(List.of(payment1, payment2, payment3));
        DateRange dateRange = DateRange.builder()
                .startDate(now.minusDays(3))
                .endDate(now)
                .build();

        ResultActions result = mockMvc.perform(post("/payments/total")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dateRange)));

        result.andExpect(status().isOk())
                .andExpect(content().string("300.00"));
    }

    @Test
    void createPayment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        PaymentRequest invalidRequest = PaymentRequest.builder()
                .orderId(INVALID_ORDER_ID)
                .userId(USER_ID)
                .paymentAmount(new BigDecimal("-50.00"))
                .build();

        ResultActions result = mockMvc.perform(post("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        result.andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsByOrderId_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/payments/order/{orderId}", INVALID_ORDER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsByUserId_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/payments/user/{userId}", INVALID_ORDER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTotalSumOfPaymentsForDatePeriod_WithInvalidDateRange_ShouldReturnBadRequest() throws Exception {
        DateRange invalidDateRange = DateRange.builder()
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/payments/total")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDateRange)))
                .andExpect(status().isBadRequest());
    }
}