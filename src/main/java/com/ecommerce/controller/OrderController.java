package com.ecommerce.controller;

import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // CHECKOUT - With Transaction Logic
    @PostMapping("/checkout/{userId}")
    @Transactional  // ← TRANSACTION - sab ya kuch nahi
    public ResponseEntity<?> checkout(@PathVariable Long userId, @RequestBody Map<String, String> body) {
        
        List<Cart> cartItems = cartRepository.findByUserId(userId);

        // VALIDATION 1: Cart empty
        if (cartItems.isEmpty()) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Cart is empty");
            error.put("status", "FAILED");
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }

        // VALIDATION 2: Check stock for all items
        for (Cart item : cartItems) {
            if (item.getProduct().getStockQuantity() < item.getQuantity()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Insufficient stock for: " + item.getProduct().getName());
                error.put("status", "FAILED");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
        }

        // Calculate total with tax & shipping
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.18")); // 18% GST
        BigDecimal shipping = subtotal.compareTo(new BigDecimal("500")) > 0 ? 
                              BigDecimal.ZERO : new BigDecimal("50");
        BigDecimal total = subtotal.add(tax).add(shipping);

        // Create Order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(total);
        order.setShippingAddress(body.getOrDefault("shippingAddress", "Default Address"));
        order.setPaymentMethod(body.getOrDefault("paymentMethod", "COD"));
        order.setStatus("CONFIRMED");
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        // Create Order Items & Update Stock
        for (Cart cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getProduct().getPrice());
            orderItemRepository.save(orderItem);

            // Update stock
            Product product = cartItem.getProduct();
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        // Clear cart
        cartRepository.deleteByUserId(userId);

        // Response with details
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order placed successfully!");
        response.put("orderId", savedOrder.getId());
        response.put("status", "CONFIRMED");
        response.put("subtotal", subtotal);
        response.put("tax", tax);
        response.put("shipping", shipping);
        response.put("totalAmount", total);
        response.put("items", cartItems.size());
        response.put("savedAmount", subtotal.compareTo(new BigDecimal("500")) > 0 ? "₹50 (Free Shipping)" : "₹0");

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // GET ALL ORDERS (Admin)
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return new ResponseEntity<>(orderRepository.findAll(), HttpStatus.OK);
    }

    // GET USER ORDERS with details
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        
        if (orders.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "No orders found");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    // GET ORDER DETAILS
    @GetMapping("/detail/{id}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(order -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("order", order);
                    detail.put("items", order.getItems());
                    return new ResponseEntity<>(detail, HttpStatus.OK);
                })
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE ORDER STATUS (Admin)
    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return orderRepository.findById(id).map(order -> {
            String newStatus = body.get("status");
            
            // Transaction validation
            List<String> validStatuses = Arrays.asList("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");
            if (!validStatuses.contains(newStatus)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid status: " + newStatus);
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            
            order.setStatus(newStatus);
            orderRepository.save(order);
            return new ResponseEntity<>(order, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}