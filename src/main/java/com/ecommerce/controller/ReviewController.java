package com.ecommerce.controller;

import com.ecommerce.model.Review;
import com.ecommerce.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    // ADD REVIEW
    @PostMapping
    public ResponseEntity<Review> addReview(@RequestBody Review review) {
        Review saved = reviewRepository.save(review);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // GET REVIEWS BY PRODUCT
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getProductReviews(@PathVariable Long productId) {
        return new ResponseEntity<>(reviewRepository.findByProductId(productId), HttpStatus.OK);
    }

    // GET AVERAGE RATING
    @GetMapping("/average/{productId}")
    public ResponseEntity<Double> getAverageRating(@PathVariable Long productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        if (reviews.isEmpty()) return new ResponseEntity<>(0.0, HttpStatus.OK);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        return new ResponseEntity<>(Math.round(avg * 10) / 10.0, HttpStatus.OK);
    }
}