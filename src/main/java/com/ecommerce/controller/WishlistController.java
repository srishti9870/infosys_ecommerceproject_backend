package com.ecommerce.controller;

import com.ecommerce.model.Wishlist;
import com.ecommerce.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {

    @Autowired
    private WishlistRepository wishlistRepository;

    // ADD TO WISHLIST
    @PostMapping
    public ResponseEntity<?> addToWishlist(@RequestBody Wishlist wishlist) {
        if (wishlistRepository.existsByUserIdAndProductId(wishlist.getUserId(), wishlist.getProduct().getId())) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Already in wishlist");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        Wishlist saved = wishlistRepository.save(wishlist);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET WISHLIST
    @GetMapping("/{userId}")
    public ResponseEntity<List<Wishlist>> getWishlist(@PathVariable Long userId) {
        return new ResponseEntity<>(wishlistRepository.findByUserId(userId), HttpStatus.OK);
    }

    // REMOVE FROM WISHLIST
    @DeleteMapping("/{userId}/{productId}")
    public ResponseEntity<?> removeFromWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        wishlistRepository.deleteByUserIdAndProductId(userId, productId);
        return new ResponseEntity<>("Removed", HttpStatus.OK);
    }

    // CHECK IF IN WISHLIST
    @GetMapping("/check/{userId}/{productId}")
    public ResponseEntity<Boolean> checkWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        return new ResponseEntity<>(wishlistRepository.existsByUserIdAndProductId(userId, productId), HttpStatus.OK);
    }
}