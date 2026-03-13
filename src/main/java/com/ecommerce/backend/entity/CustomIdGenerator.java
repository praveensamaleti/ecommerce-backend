package com.ecommerce.backend.entity;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.UUID;

public class CustomIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        String prefix = "";
        if (object instanceof User) {
            prefix = "u";
        } else if (object instanceof Product) {
            prefix = "p";
        } else if (object instanceof Order) {
            prefix = "o";
        }
        
        // Generate IDs like "u1234", "p1001", "o987654"
        // For simplicity, using a random number here
        long random = (long) (Math.random() * 9000L) + 1000L;
        if (object instanceof Order) {
            random = (long) (Math.random() * 900000L) + 100000L; // 6 digits for order
        }
        return prefix + random;
    }
}
