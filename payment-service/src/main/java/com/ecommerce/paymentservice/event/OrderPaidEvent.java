package com.ecommerce.paymentservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPaidEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String transactionId;
    private LocalDateTime paidAt;
}
