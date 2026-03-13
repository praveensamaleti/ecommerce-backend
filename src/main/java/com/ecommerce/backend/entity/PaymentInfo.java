package com.ecommerce.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo {
    private String cardName;
    private String cardNumber;
    private String exp;
    private String cvc;
}
