package com.ecommerce.notificationservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResultEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long orderId;
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String status;
    private String transactionId;
    private String failureReason;
}
