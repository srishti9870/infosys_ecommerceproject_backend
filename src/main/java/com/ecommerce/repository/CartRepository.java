package com.ecommerce.repository;

import com.ecommerce.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart items by user ID
    List<Cart> findByUserId(Long userId);

    // Delete cart item by user ID and product ID
    void deleteByUserIdAndProductId(Long userId, Long productId);
}