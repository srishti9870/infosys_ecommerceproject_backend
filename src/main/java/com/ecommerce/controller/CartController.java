package com.ecommerce.controller;

import com.ecommerce.model.Cart;
import com.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    // ADD TO CART
    @PostMapping
    public ResponseEntity<?> addToCart(@RequestBody Cart cart) {
        try {
            Cart savedCart = cartRepository.save(cart);
            return new ResponseEntity<>(savedCart, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // GET CART BY USER ID
    @GetMapping("/{userId}")
    public ResponseEntity<List<Cart>> getCartByUserId(@PathVariable Long userId) {
        List<Cart> cartItems = cartRepository.findByUserId(userId);
        return new ResponseEntity<>(cartItems, HttpStatus.OK);
    }

    // DELETE CART ITEM
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id) {
        if (cartRepository.existsById(id)) {
            cartRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // CLEAR CART BY USER ID
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        List<Cart> items = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(items);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}