
package com.sanjay.Spring_ECOM.controllers;

import com.sanjay.Spring_ECOM.model.dto.OrderRequest;
import com.sanjay.Spring_ECOM.model.dto.OrderResponse;
import com.sanjay.Spring_ECOM.service.OrderService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestBody OrderRequest orderRequest) {

        OrderResponse orderResponse =
                orderService.placeOrder(orderRequest);

        return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        List<OrderResponse> orderResponseList =
                orderService.getAllOrderResponses();

        return new ResponseEntity<>(
                orderResponseList, HttpStatus.OK);
    }

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
