
package com.sanjay.Spring_ECOM.service;

import com.sanjay.Spring_ECOM.model.Order;
import com.sanjay.Spring_ECOM.model.OrderItem;
import com.sanjay.Spring_ECOM.model.Product;
import com.sanjay.Spring_ECOM.model.dto.OrderItemRequest;
import com.sanjay.Spring_ECOM.model.dto.OrderItemResponse;
import com.sanjay.Spring_ECOM.model.dto.OrderRequest;
import com.sanjay.Spring_ECOM.model.dto.OrderResponse;
import com.sanjay.Spring_ECOM.repo.OrderRepo;
import com.sanjay.Spring_ECOM.repo.ProductRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Builds persisted orders and response DTOs without exposing entity relationships. */
@Service
public class OrderService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private OrderRepo orderRepo;

    /**
     * Validates quantities, decrements inventory, and saves the order in one transaction.
     * An exception rolls back earlier stock changes in this request. This transaction
     * does not lock inventory rows against concurrent checkout requests.
     */
    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {

        Order order = new Order();

        String orderId = "ORD" +
                UUID.randomUUID().toString()
                        .substring(0, 8).toUpperCase();

        order.setOrderId(orderId);
        order.setCustomerName(request.customerName());
        order.setEmail(request.email());
        order.setStatus("PLACED");
        order.setOrderDate(LocalDate.now());

        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.items()) {

            if (itemReq.quantity() <= 0) {
                throw new RuntimeException(
                        "Quantity must be greater than zero");
            }

            Product product = productRepo.findById(
                            (long) itemReq.productId())
                    .orElseThrow(() ->
                            new RuntimeException("Product not found"));

            if (product.getStockQuantity() < itemReq.quantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName());
            }

            product.setStockQuantity(
                    product.getStockQuantity() - itemReq.quantity());

            productRepo.save(product);

            // Snapshot the line total so later catalog price edits do not change this order.
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .totalPrice(
                            product.getPrice().multiply(
                                    BigDecimal.valueOf(itemReq.quantity())))
                    .order(order)
                    .build();

            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        Order savedOrder = orderRepo.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : savedOrder.getOrderItems()) {
            itemResponses.add(new OrderItemResponse(
                    item.getProduct().getName(),
                    item.getQuantity(),
                    getItemTotal(item)
            ));
        }

        return new OrderResponse(
                savedOrder.getOrderId(),
                savedOrder.getCustomerName(),
                savedOrder.getEmail(),
                savedOrder.getStatus(),
                savedOrder.getOrderDate(),
                itemResponses
        );
    }

    /** Maps order items while the read transaction keeps lazy relationships accessible. */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrderResponses() {

        List<Order> orders = orderRepo.findAll();
        List<OrderResponse> orderResponses = new ArrayList<>();

        for (Order order : orders) {

            List<OrderItemResponse> itemResponses = new ArrayList<>();

            for (OrderItem item : order.getOrderItems()) {
                itemResponses.add(new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        getItemTotal(item)
                ));
            }

            orderResponses.add(new OrderResponse(
                    order.getOrderId(),
                    order.getCustomerName(),
                    order.getEmail(),
                    order.getStatus(),
                    order.getOrderDate(),
                    itemResponses
            ));
        }

        return orderResponses;
    }

    /** Uses the saved line total, with a calculated fallback for older order rows. */
    private BigDecimal getItemTotal(OrderItem item) {

        if (item.getTotalPrice() != null) {
            return item.getTotalPrice();
        }

        if (item.getProduct() != null
                && item.getProduct().getPrice() != null) {
            return item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
        }

        return BigDecimal.ZERO;
    }

    /** Deletes an order if present. Deleting history does not restock its products. */
    @Transactional
    public boolean deleteOrder(String orderId) {

        Order order = orderRepo.findByOrderId(orderId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        orderRepo.delete(order);
        return true;
    }
}
