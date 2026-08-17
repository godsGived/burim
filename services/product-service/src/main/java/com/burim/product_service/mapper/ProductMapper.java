package com.burim.product_service.mapper;

import com.burim.product_service.dto.ProductRequest;
import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product){
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getBrand().getId(),
                product.getStock(),
                product.getRating(),
                product.getReviewsCount(),
                product.getCreatedAt()
        );
    }

    public Product toProduct(ProductRequest request){
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .rating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .reviewsCount(0)
                .build();
    }
}
