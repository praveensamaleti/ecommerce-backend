package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.CreateOrderRequest;
import com.ecommerce.backend.dto.OrderDto;
import com.ecommerce.backend.enums.OrderStatus;
import com.ecommerce.backend.security.UserDetailsImpl;
import com.ecommerce.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Orders", description = "Endpoints for placing and managing orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Place a new order", description = "Creates a new order for the currently authenticated user")
    public ResponseEntity<OrderDto> placeOrder(@RequestBody CreateOrderRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return new ResponseEntity<>(orderService.placeOrder(request, userDetails.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List user orders", description = "Retrieves all orders placed by the currently authenticated user")
    public ResponseEntity<List<OrderDto>> getUserOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(orderService.getUserOrders(userDetails.getId()));
    }

    @PatchMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Update order status (Admin only)", description = "Changes the status of an existing order")
    public ResponseEntity<OrderDto> updateOrderStatus(@Parameter(description = "ID of the order to update") @PathVariable String id, @RequestBody Map<String, OrderStatus> statusMap) {
        OrderStatus status = statusMap.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
