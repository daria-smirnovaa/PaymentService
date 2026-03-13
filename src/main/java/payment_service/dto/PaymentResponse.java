package payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private UUID id;
    private Long orderId;
    private Long userId;
    private Status status;
    private LocalDateTime timestamp;
    private BigDecimal paymentAmount;
}
