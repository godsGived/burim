package com.burim.product_service.specification;

import com.burim.product_service.dto.ProductFilter;
import com.burim.product_service.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> withFilter(ProductFilter filter) {
        return (root, query, cb) -> {
            if (filter == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (filter.query() != null && !filter.query().isBlank()) {
                var pattern = "%" + filter.query().trim().toLowerCase() + "%";
                var nameLike = cb.like(cb.lower(root.get("name")), pattern);
                var descLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameLike, descLike));
            }

            if (filter.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.categoryId()));
            }

            if (filter.brandId() != null) {
                predicates.add(cb.equal(root.get("brand").get("id"), filter.brandId()));
            }

            if (filter.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), filter.minPrice()));
            }

            if (filter.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), filter.maxPrice()));
            }

            if (filter.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), filter.minRating()));
            }

            if (Boolean.TRUE.equals(filter.inStock())) {
                predicates.add(cb.greaterThan(root.get("stock"), 0));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
