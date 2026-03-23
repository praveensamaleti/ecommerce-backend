package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderDto;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.entity.ProductVariant;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.exception.BusinessException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Mock
    private ProductVariantRepository variantRepository;

    @InjectMocks
    private OrderService orderService;

    private Product sampleProduct;
    private ProductVariant sampleVariant;
    private CreateOrderRequest createOrderRequest;
    private CreateOrderRequest variantOrderRequest;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id("p1")
                .name("Test Product")
                .price(new BigDecimal("100.00"))
                .stock(10)
                .build();

        sampleVariant = ProductVariant.builder()
                .id("v1")
                .product(sampleProduct)
                .sku("P1-RED-M")
                .stock(5)
                .price(new BigDecimal("120.00"))
                .attributes(new HashMap<>(Map.of("color", "Red", "size", "M")))
                .build();

        // Order request with no variant
        CreateOrderRequest.OrderItemRequest itemReq = new CreateOrderRequest.OrderItemRequest();
        itemReq.setProductId("p1");
        itemReq.setQty(2);

        createOrderRequest = new CreateOrderRequest();
        createOrderRequest.setItems(Arrays.asList(itemReq));

        // Order request with variant
        CreateOrderRequest.OrderItemRequest variantItemReq = new CreateOrderRequest.OrderItemRequest();
        variantItemReq.setProductId("p1");
        variantItemReq.setQty(2);
        variantItemReq.setVariantId("v1");

        variantOrderRequest = new CreateOrderRequest();
        variantOrderRequest.setItems(Arrays.asList(variantItemReq));
    }

    // ------------------------------------------------------------------
    // placeOrder — no variant (product-level stock)
    // ------------------------------------------------------------------

    @Test
    void placeOrder_NoVariant_ShouldSucceedAndDeductProductStock() {
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
        // Stock must be deducted from product (no variant path)
        verify(productRepository, times(1)).save(any(Product.class));
        verify(variantRepository, never()).save(any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_NoVariant_WithInsufficientStock_ThrowsException() {
        sampleProduct.setStock(1);
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));

        assertThrows(BusinessException.class,
                () -> orderService.placeOrder(createOrderRequest, "u1"));
        verify(orderRepository, never()).save(any());
    }

    // ------------------------------------------------------------------
    // placeOrder — with variant (variant-level stock)
    // ------------------------------------------------------------------

    @Test
    void placeOrder_WithVariantId_DeductsVariantStock() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById("v1")).thenReturn(Optional.of(sampleVariant));

        Order savedOrder = new Order();
        savedOrder.setId("o2");
        savedOrder.setUserId("u1");
        savedOrder.setSubtotal(new BigDecimal("240.00")); // 120 * 2
        savedOrder.setTax(new BigDecimal("19.20"));
        savedOrder.setTotal(new BigDecimal("259.20"));
        savedOrder.setItems(new ArrayList<>());

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderDto result = orderService.placeOrder(variantOrderRequest, "u1");

        assertNotNull(result);
        assertEquals("o2", result.getId());
        // Variant stock must be deducted, NOT product stock
        verify(variantRepository, times(1)).save(any(ProductVariant.class));
        verify(productRepository, never()).save(any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void placeOrder_WithVariantId_InsufficientVariantStock_ThrowsException() {
        // Variant has stock=5, but request asks for qty=10
        sampleVariant.setStock(4);
        CreateOrderRequest.OrderItemRequest highQtyReq = new CreateOrderRequest.OrderItemRequest();
        highQtyReq.setProductId("p1");
        highQtyReq.setQty(10);
        highQtyReq.setVariantId("v1");
        CreateOrderRequest highQtyOrder = new CreateOrderRequest();
        highQtyOrder.setItems(Arrays.asList(highQtyReq));

        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById("v1")).thenReturn(Optional.of(sampleVariant));

        assertThrows(BusinessException.class,
                () -> orderService.placeOrder(highQtyOrder, "u1"));
        verify(variantRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_WithVariantId_SnapshotsVariantLabelAndPrice() {
        // sampleVariant has price=120.00 and attributes {color=Red, size=M} → label "Red / M"
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById("v1")).thenReturn(Optional.of(sampleVariant));

        Order savedOrder = new Order();
        savedOrder.setId("o3");
        savedOrder.setUserId("u1");
        savedOrder.setSubtotal(new BigDecimal("240.00"));
        savedOrder.setTax(new BigDecimal("19.20"));
        savedOrder.setTotal(new BigDecimal("259.20"));

        // Capture the order to verify its items contain variantId and label
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order order = inv.getArgument(0);
            order.setId("o3");
            return order;
        });

        OrderDto result = orderService.placeOrder(variantOrderRequest, "u1");

        assertNotNull(result);
        assertFalse(result.getItems().isEmpty());
        assertEquals("v1", result.getItems().get(0).getVariantId());
        // Variant label must be non-blank (derived from attributes)
        assertNotNull(result.getItems().get(0).getVariantLabel());
        assertFalse(result.getItems().get(0).getVariantLabel().isBlank());
        // Price must be variant price (120.00), not product price (100.00)
        assertEquals(new BigDecimal("120.00"), result.getItems().get(0).getPrice());
    }

    @Test
    void placeOrder_WithVariantId_VariantNotFound_ThrowsResourceNotFoundException() {
        when(productRepository.findById("p1")).thenReturn(Optional.of(sampleProduct));
        when(variantRepository.findById("v_missing")).thenReturn(Optional.empty());

        CreateOrderRequest.OrderItemRequest req = new CreateOrderRequest.OrderItemRequest();
        req.setProductId("p1");
        req.setQty(1);
        req.setVariantId("v_missing");
        CreateOrderRequest order = new CreateOrderRequest();
        order.setItems(Arrays.asList(req));

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.placeOrder(order, "u1"));
    }

    // ------------------------------------------------------------------
    // getUserOrders / updateOrderStatus
    // ------------------------------------------------------------------

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
