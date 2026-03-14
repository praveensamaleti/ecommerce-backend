package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.enums.UserRole;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");

        // Create Admin
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.admin)
                    .build();
            userRepository.save(admin);
            log.info("Admin user created.");
        }

        // Create User
        if (userRepository.findByEmail("john@example.com").isEmpty()) {
            User user = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .password(passwordEncoder.encode("securepassword123"))
                    .role(UserRole.user)
                    .build();
            userRepository.save(user);
            log.info("Sample user created.");
        }

        // Create Initial Products (Only 2 products)
        if (productRepository.count() == 0) {
            log.info("Generating 2 sample products...");
            
            Map<String, String> specs1 = new HashMap<>();
            specs1.put("Bluetooth", "5.0");
            specs1.put("Battery", "20h");

            Product p1 = Product.builder()
                    .name("Wireless Headphones")
                    .price(new BigDecimal("99.99"))
                    .images(Arrays.asList("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80"))
                    .category(Category.Electronics)
                    .stock(50)
                    .rating(4.5)
                    .ratingCount(120)
                    .description("High-quality wireless headphones with noise cancellation.")
                    .specs(specs1)
                    .featured(true)
                    .build();

            Map<String, String> specs2 = new HashMap<>();
            specs2.put("Cotton", "100%");
            specs2.put("Size", "M");

            Product p2 = Product.builder()
                    .name("Classic White T-Shirt")
                    .price(new BigDecimal("19.99"))
                    .images(Arrays.asList("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&q=80"))
                    .category(Category.Clothing)
                    .stock(100)
                    .rating(4.0)
                    .ratingCount(85)
                    .description("A comfortable 100% cotton classic white t-shirt.")
                    .specs(specs2)
                    .featured(false)
                    .build();

            productRepository.saveAll(Arrays.asList(p1, p2));
            log.info("Finished generating 2 sample products.");
        }
        log.info("Data initialization completed.");
    }
}
