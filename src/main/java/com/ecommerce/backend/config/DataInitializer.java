package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.User;
import com.ecommerce.backend.enums.Category;
import com.ecommerce.backend.enums.UserRole;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Create Admin
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(UserRole.admin)
                    .build();
            userRepository.save(admin);
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
        }

        // Create Initial Products
        if (productRepository.count() == 0) {
            Map<String, String> specs = new HashMap<>();
            specs.put("Bluetooth", "5.0");
            specs.put("Battery", "20h");

            Product p1 = Product.builder()
                    .name("Wireless Headphones")
                    .price(new BigDecimal("99.99"))
                    .images(Arrays.asList("url1.jpg", "url2.jpg"))
                    .category(Category.Electronics)
                    .stock(50)
                    .rating(4.5)
                    .ratingCount(120)
                    .description("High-quality wireless headphones.")
                    .specs(specs)
                    .featured(true)
                    .build();

            productRepository.save(p1);
        }
    }
}
