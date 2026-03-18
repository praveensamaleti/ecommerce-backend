package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderDto;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product sampleProduct;
    private CreateOrderRequest createOrderRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("p1")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest();
        itemReq.setProductId("p1");
        itemReq.setQty(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setItems(Arrays.asList(itemReq));
    }

    @Test
    void placeOrder_ShouldSucceed() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        
        Order savedOrder = new Order();
        savedOrder.setId("o1");
        savedOrder.setUserId("u1");
        savedOrder.setSubtotal(new BigDecimal("200.00"));
        savedOrder.setTax(new BigDecimal("16.00"));
        savedOrder.setTotal(new BigDecimal("216.00"));
        savedOrder.setItems(new ArrayList<>());
        
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = orderService.placeOrder(createOrderRequest, "u1");

        assertNotNull(result);
        assertEquals("o1", result.getId());
        verify(productRepository, times(1)).save(any(Product.class));
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_WithInsufficientStock_ShouldThrowException() {
        sampleProduct.setStock(1);
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));

        assertThrows(RuntimeException.class, () -> orderService.placeOrder(createOrderRequest, "u1"));
    }

    @Test
    void getUserOrders_ShouldReturnList() {
        Order order = new Order();
        order.setUserId("u1");
        order.setItems(new ArrayList<>());
        when(orderRepository.findByUserId("u1")).thenReturn(Arrays.asList(order));

        List<OrderDto> result = orderService.getUserOrders("u1");

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void updateOrderStatus_ShouldUpdate() {
        Order order = new Order();
        order.setId("o1");
        order.setItems(new ArrayList<>());
        when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDto result = orderService.updateOrderStatus("o1", OrderStatus.delivered);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }
}
