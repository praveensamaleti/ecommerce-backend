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
import java.util.*;

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

        // Create Initial Products (100+ products across categories)
        if (productRepository.count() < 100) {
            String[] electronics = {"Smart TV", "Gaming Laptop", "Wireless Mouse", "Mechanical Keyboard", "Noise Cancelling Headphones", "Bluetooth Speaker", "Tablet", "Smartwatch", "Camera", "External SSD"};
            String[] electronicsImgs = {
                "https://images.unsplash.com/photo-1593305841991-05c297ba4575?w=500&q=80",
                "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500&q=80",
                "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&q=80",
                "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=500&q=80",
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80",
                "https://images.unsplash.com/photo-1608156639585-b3a032ef9689?w=500&q=80",
                "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&q=80",
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&q=80",
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500&q=80",
                "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500&q=80"
            };

            String[] clothing = {"Cotton T-shirt", "Denim Jeans", "Hooded Sweatshirt", "Running Shoes", "Formal Shirt", "Summer Dress", "Woolen Sweater", "Leather Jacket", "Sports Shorts", "Sneakers"};
            String[] clothingImgs = {
                "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=500&q=80",
                "https://images.unsplash.com/photo-1542272604-787c3835535d?w=500&q=80",
                "https://images.unsplash.com/photo-1556821840-3a63f95609a7?w=500&q=80",
                "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=500&q=80",
                "https://images.unsplash.com/photo-1596755094514-f87034a26cc1?w=500&q=80",
                "https://images.unsplash.com/photo-1515347663391-0505528130ab?w=500&q=80",
                "https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=500&q=80",
                "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=500&q=80",
                "https://images.unsplash.com/photo-1591195853828-11db59a44f6b?w=500&q=80",
                "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=500&q=80"
            };

            String[] home = {"Ergonomic Chair", "Standing Desk", "Table Lamp", "Scented Candle", "Throw Blanket", "Wall Clock", "Kitchen Blender", "Coffee Maker", "Air Purifier", "Bookshelf"};
            String[] homeImgs = {
                "https://images.unsplash.com/photo-1524758631624-e2822e304c36?w=500&q=80",
                "https://images.unsplash.com/photo-1530018607912-eff2df114fbe?w=500&q=80",
                "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=500&q=80",
                "https://images.unsplash.com/photo-1602872030219-3fd6380393c0?w=500&q=80",
                "https://images.unsplash.com/photo-1580301762395-21ce84d00bc6?w=500&q=80",
                "https://images.unsplash.com/photo-1563861826100-9cb868fdbe1c?w=500&q=80",
                "https://images.unsplash.com/photo-1585238341267-1cfec2046a55?w=500&q=80",
                "https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?w=500&q=80",
                "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=500&q=80",
                "https://images.unsplash.com/photo-1594620302200-9a762244a156?w=500&q=80"
            };

            String[] books = {"Mystery Novel", "Science Fiction", "Historical Biography", "Cooking Book", "Self-Help Guide", "Programming Guide", "Fantasy Epic", "Business Strategy", "Travel Guide", "Children's Story"};
            String[] booksImgs = {
                "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500&q=80",
                "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=500&q=80",
                "https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=500&q=80",
                "https://images.unsplash.com/photo-1506880018603-83d5b814b5a6?w=500&q=80",
                "https://images.unsplash.com/photo-1589998059171-988d887df646?w=500&q=80",
                "https://images.unsplash.com/photo-1516116216624-53e697fedbea?w=500&q=80",
                "https://images.unsplash.com/photo-1543005124-8198f5acbb5e?w=500&q=80",
                "https://images.unsplash.com/photo-1553729459-efe14ef6055d?w=500&q=80",
                "https://images.unsplash.com/photo-1527176930608-09cb256ab504?w=500&q=80",
                "https://images.unsplash.com/photo-1512585933035-45cbb6b6227b?w=500&q=80"
            };

            String[] sports = {"Yoga Mat", "Dumbbell Set", "Basketball", "Tennis Racket", "Soccer Ball", "Cycling Helmet", "Swimming Goggles", "Skipping Rope", "Resistance Bands", "Punching Bag"};
            String[] sportsImgs = {
                "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=500&q=80",
                "https://images.unsplash.com/photo-1583454110551-21f2fa2afe61?w=500&q=80",
                "https://images.unsplash.com/photo-1519861531473-9200262188bf?w=500&q=80",
                "https://images.unsplash.com/photo-1595435064222-449248675ba1?w=500&q=80",
                "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=500&q=80",
                "https://images.unsplash.com/photo-1596733430284-f7437764b1a9?w=500&q=80",
                "https://images.unsplash.com/photo-1557935728-e6d1eaabe558?w=500&q=80",
                "https://images.unsplash.com/photo-1434596922112-19c563067271?w=500&q=80",
                "https://images.unsplash.com/photo-1598289431512-b97b0917abbc?w=500&q=80",
                "https://images.unsplash.com/photo-1591117207239-788cd8594840?w=500&q=80"
            };

            List<Product> products = new ArrayList<>();
            Random random = new Random();

            for (int i = 0; i < 21; i++) {
                // Generate 105 products total (21 loops * 5 products per loop)
                products.add(createProduct(electronics[i % 10] + " " + UUID.randomUUID().toString().substring(0, 4), Category.Electronics, electronicsImgs[i % 10], 50 + random.nextInt(1950), random));
                products.add(createProduct(clothing[i % 10] + " " + UUID.randomUUID().toString().substring(0, 4), Category.Clothing, clothingImgs[i % 10], 15 + random.nextInt(85), random));
                products.add(createProduct(home[i % 10] + " " + UUID.randomUUID().toString().substring(0, 4), Category.Home, homeImgs[i % 10], 20 + random.nextInt(480), random));
                products.add(createProduct(books[i % 10] + " " + UUID.randomUUID().toString().substring(0, 4), Category.Books, booksImgs[i % 10], 10 + random.nextInt(40), random));
                products.add(createProduct(sports[i % 10] + " " + UUID.randomUUID().toString().substring(0, 4), Category.Sports, sportsImgs[i % 10], 10 + random.nextInt(290), random));
            }

            productRepository.saveAll(products);
        }
    }

    private Product createProduct(String name, Category category, String img, int basePrice, Random random) {
        Map<String, String> specs = new HashMap<>();
        specs.put("Manufacturer", "GlobalBrand " + (random.nextInt(10) + 1));
        specs.put("Warranty", (random.nextInt(3) + 1) + " years");

        return Product.builder()
                .name(name)
                .price(new BigDecimal(basePrice + ".99"))
                .images(Arrays.asList(img))
                .category(category)
                .stock(10 + random.nextInt(90))
                .rating(3.5 + (random.nextDouble() * 1.5))
                .ratingCount(5 + random.nextInt(495))
                .description("High-quality " + name + " in the " + category + " category. Designed for comfort and durability.")
                .specs(specs)
                .featured(random.nextBoolean())
                .build();
    }
}
