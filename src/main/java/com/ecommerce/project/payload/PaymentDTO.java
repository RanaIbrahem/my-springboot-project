package com.ecommerce.project.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private long paymentId;
    private String paymentMethod;
    private String pgPayementId;
    private String pgStatus;
    private String pgResponseMessage;
    private String pgName;

}
