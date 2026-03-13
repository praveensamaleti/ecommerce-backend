package com.ecommerce.backend.entity;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {
    private String fullName;
    private String email;
    private String phone;
    private String address1;
    private String city;
    private String state;
    private String zip;
    private String country;
}
