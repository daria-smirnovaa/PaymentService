package payment_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import payment_service.dto.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private UUID id;

    @Field("order_id")
    private Long orderId;

    @Field("user_id")
    private Long userId;

    @Field("status")
    private Status status;

    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("payment_amount")
    private BigDecimal paymentAmount;
}
