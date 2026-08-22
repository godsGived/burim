package com.burim.cart_service.service;

import com.burim.cart_service.dto.CartItemRequest;
import com.burim.cart_service.dto.CartItemResponse;
import com.burim.cart_service.dto.CartResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final StringRedisTemplate redisTemplate;

    private String getKey(String userId){
        return "cart:" + userId;
    }

    public CartResponse getCart(String userId){
        String key = getKey(userId);
        List<CartItemResponse> items = redisTemplate.opsForHash().entries(key).entrySet().stream()
                .map(entry -> new CartItemResponse(
                        Long.valueOf((String) entry.getKey()),
                        Integer.valueOf((String) entry.getValue())
                )).toList();
        return new CartResponse(userId, items);
    }

    public CartItemResponse addItem(String userId, CartItemRequest request){
        String key = getKey(userId);
        Long newQuantity = redisTemplate.opsForHash().increment(
                key,
                request.productId().toString(),
                request.quantity()
        );
        return new CartItemResponse(request.productId(), newQuantity.intValue());
    }

    public CartItemResponse updateItemQuantity(String userId, Long productId, Integer quantity) {
        String key = getKey(userId);
        redisTemplate.opsForHash().put(key, productId.toString(), quantity.toString());
        return new CartItemResponse(productId, quantity);
    }

    public void removeItem(String userId, Long productId){
        String key = getKey(userId);
        redisTemplate.opsForHash().delete(key, productId.toString());
    }

    public void clearCart(String userId){
        String key = getKey(userId);
        redisTemplate.delete(key);
    }

}
