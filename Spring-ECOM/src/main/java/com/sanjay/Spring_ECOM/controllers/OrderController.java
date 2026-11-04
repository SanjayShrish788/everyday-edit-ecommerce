
package com.sanjay.Spring_ECOM.controllers;

import com.sanjay.Spring_ECOM.model.dto.OrderRequest;
import com.sanjay.Spring_ECOM.model.dto.OrderResponse;
import com.sanjay.Spring_ECOM.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP adapters for order creation, history, and deletion. */
@RestController
@RequestMapping("/api")

public class OrderController {

    @Autowired
    private OrderService orderService;

    /** Delegates inventory validation and order persistence to the transactional service. */
    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequest orderRequest, org.springframework.security.core.Authentication auth) {

        OrderResponse orderResponse =
                orderService.placeOrder(orderRequest, auth.getName());

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    /** Lists response DTOs instead of serializing bidirectional JPA relationships. */
    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders(org.springframework.security.core.Authentication auth) {

        List<OrderResponse> orderResponseList =
                orderService.getAllOrderResponses(auth.getName(), auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));

        return new ResponseEntity<>(
                orderResponseList, HttpStatus.OK);
    }

    /** Uses the public order reference, returning HTTP 204 or HTTP 404. */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable String orderId) {

        boolean deleted = orderService.deleteOrder(orderId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.notFound().build();
    }
}
