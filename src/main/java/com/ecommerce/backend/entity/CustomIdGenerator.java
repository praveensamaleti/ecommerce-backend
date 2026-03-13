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
        
        // Generate unique IDs like "u-7d3f2", "p-8a1c9", "o-4b2e5"
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 5);
        return prefix + "-" + uniqueSuffix;
    }
}
