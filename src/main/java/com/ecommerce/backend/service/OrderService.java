package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderDto;
import com.ecommerce.backend.dto.OrderItemDto;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public OrderDto placeOrder(CreateOrderRequest request, String userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(OrderStatus.pending);
        order.setShipping(request.getShipping());
        order.setBilling(request.getBilling());
        order.setPayment(request.getPayment());

        List<OrderItem> items = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found  : " + itemReq.getProductId()));
            
            OrderItem item = OrderItem.builder()
                    .productId(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .qty(itemReq.getQty())
                    .order(order)
                    .build();
            
            items.add(item);
            subtotal = subtotal.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQty())));
            
            // Update stock
            if (product.getStock() < itemReq.getQty()) {
                throw new RuntimeException("Insufficient stock for product:   " + product.getName());
            }
            product.setStock(product.getStock() - itemReq.getQty());
            productRepository.save(product);
        }

        order.setItems(items);
        order.setSubtotal(subtotal);
        order.setDiscount(BigDecimal.ZERO); // Simplified
        order.setTax(subtotal.multiply(new BigDecimal("0.08"))); // 8% tax
        order.setTotal(order.getSubtotal().subtract(order.getDiscount()).add(order.getTax()));

        Order savedOrder = orderRepository.save(order);
        return convertToDto(savedOrder);
    }

    public List<OrderDto> getUserOrders(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDto updateOrderStatus(String id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        order.setStatus(status);
        return convertToDto(orderRepository.save(order));
    }

    private OrderDto convertToDto(Order order) {
        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .shipping(order.getShipping())
                .billing(order.getBilling())
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .total(order.getTotal())
                .items(order.getItems().stream()
                        .map(item -> OrderItemDto.builder()
                                .productId(item.getProductId())
                                .name(item.getName())
                                .price(item.getPrice())
                                .qty(item.getQty())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
