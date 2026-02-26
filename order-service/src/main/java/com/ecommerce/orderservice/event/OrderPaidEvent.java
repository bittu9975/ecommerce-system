// NEW FILE: order-service/.../event/OrderPaidEvent.java

package com.ecommerce.orderservice.event;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderPaidEvent implements Serializable {
    private Long orderId;
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
}