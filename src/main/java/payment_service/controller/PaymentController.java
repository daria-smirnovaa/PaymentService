package payment_service.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import payment_service.dto.DateRange;
import payment_service.dto.PaymentRequest;
import payment_service.dto.PaymentResponse;
import payment_service.dto.Status;
import payment_service.service.PaymentService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {
    public final PaymentService paymentService;

    @PostMapping
    ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest paymentRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPayment(paymentRequest));
    }

    @GetMapping("/order/{orderId}")
    ResponseEntity<List<PaymentResponse>> getPaymentsByOrderId(@PathVariable @NotNull @Positive Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByOrderId(orderId));
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable @NotNull @Positive Long userId) {
        return ResponseEntity.ok(paymentService.getPaymentsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable Status status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @PostMapping("/total")
    ResponseEntity<BigDecimal> getTotalSumOfPaymentsForDatePeriod(@RequestBody @Valid DateRange dateRange) {
        dateRange.validate();
        return ResponseEntity.ok(paymentService.getTotalSumOfPaymentsForDatePeriod(
                dateRange.getStartDate(),
                dateRange.getEndDate()));
    }
}
