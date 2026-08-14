package com.burim.product_service.mapper;

import com.burim.product_service.dto.ProductRequest;
import com.burim.product_service.dto.ProductResponse;
import com.burim.product_service.entity.Product;
import org.springframework.stereotype.Component;

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
                product.getCreatedAt()
        );
    }

    public Product toProduct(ProductRequest request){
        return Product.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stock(request.stock())
                .build();
    }
}
