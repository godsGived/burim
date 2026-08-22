package com.burim.cart_service.controller;

import com.burim.cart_service.dto.CartItemRequest;
import com.burim.cart_service.dto.CartItemResponse;
import com.burim.cart_service.dto.CartResponse;
import com.burim.cart_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@RequestHeader("X-User-Id") String userId) {
        return cartService.getCart(userId);
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse addItem(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartService.addItem(userId, request);
    }

    @PutMapping("/items/{productId}")
    public CartItemResponse updateItemQuantity(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId,
            @RequestParam Integer quantity
    ) {
        return cartService.updateItemQuantity(userId, productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long productId
    ) {
        cartService.removeItem(userId, productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@RequestHeader("X-User-Id") String userId) {
        cartService.clearCart(userId);
    }
}