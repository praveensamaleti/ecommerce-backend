package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderDto;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto sampleOrderDto;
    private UserDetailsImpl sampleUserDetails;
    private UserDetailsImpl adminUserDetails;

    @BeforeEach
    void setUp() {
        sampleOrderDto = OrderDto.builder()
                .id("o1")
                .userId("u1")
                .status(OrderStatus.pending)
                .build();

        sampleUserDetails = UserDetailsImpl.builder()
                .id("u1")
                .email("user@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_user")))
                .build();

        adminUserDetails = UserDetailsImpl.builder()
                .id("admin")
                .email("admin@example.com")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_admin")))
                .build();
    }

    @Test
    void placeOrder_ShouldReturnCreated() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setItems(new ArrayList<>());
        
        when(orderService.placeOrder(any(CreateOrderRequest.class), anyString())).thenReturn(sampleOrderDto);

        mockMvc.perform(post("/api/orders")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("o1"));
    }

    @Test
    void getUserOrders_ShouldReturnList() throws Exception {
        when(orderService.getUserOrders(anyString())).thenReturn(Arrays.asList(sampleOrderDto));

        mockMvc.perform(get("/api/orders")
                .with(SecurityMockMvcRequestPostProcessors.user(sampleUserDetails))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("o1"));
    }

    @Test
    void updateOrderStatus_ShouldReturnOk() throws Exception {
        when(orderService.updateOrderStatus("o1", OrderStatus.delivered)).thenReturn(sampleOrderDto);

        Map<String, OrderStatus> statusMap = Collections.singletonMap("status", OrderStatus.delivered);

        mockMvc.perform(patch("/api/admin/orders/o1/status")
                .with(SecurityMockMvcRequestPostProcessors.user(adminUserDetails))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusMap)))
                .andExpect(status().isOk());
    }
}
