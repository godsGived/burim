package com.burim.cart_service.controller;

import com.burim.cart_service.dto.CartItemRequest;
import com.burim.cart_service.dto.CartItemResponse;
import com.burim.cart_service.dto.CartResponse;
import com.burim.cart_service.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(jwt.getSubject());
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartItemResponse addItem(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartService.addItem(jwt.getSubject(), request);
    }

    @PutMapping("/items/{productId}")
    public CartItemResponse updateItemQuantity(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId,
            @RequestParam @Positive(message = "Quantity must be greater than 0") Integer quantity
    ) {
        return cartService.updateItemQuantity(jwt.getSubject(), productId, quantity);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long productId
    ) {
        cartService.removeItem(jwt.getSubject(), productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(@AuthenticationPrincipal Jwt jwt) {
        cartService.clearCart(jwt.getSubject());
    }
}