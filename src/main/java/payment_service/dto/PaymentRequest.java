package payment_service.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    @Positive
    @NotNull(message = "Order ID cant be null")
    private Long orderId;

    @Positive
    @NotNull(message = "User ID cant be null")
    private Long userId;

    @Positive
    @NotNull(message = "Amount cant be null")
    private BigDecimal paymentAmount;
}
