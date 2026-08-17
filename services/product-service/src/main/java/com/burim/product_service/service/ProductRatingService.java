package com.burim.product_service.service;

import com.burim.product_service.dto.event.ReviewEvent;
import com.burim.product_service.entity.Product;
import com.burim.product_service.entity.ProductReviewState;
import com.burim.product_service.repository.ProductRepository;
import com.burim.product_service.repository.ProductReviewStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRatingService {

    private final ProductReviewStateRepository productReviewStateRepository;
    private final ProductRepository productRepository;

    @Transactional
    public void processReviewEvent(ReviewEvent event){
        var product = productRepository.findById(event.productId())
                .orElse(null);
        if (product == null){
            log.warn("Product not found for event: {}", event);
            return;
        }

        if (product.getRating() == null) {
            product.setRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
        if (product.getReviewsCount() == null) {
            product.setReviewsCount(0);
        }

        var state = productReviewStateRepository.findById(event.reviewId())
                .orElse(null);

        if (state != null && event.version() <= state.getLastVersion()){
            log.info("Skipping stale/duplicate event: reviewId={}, eventVersion={}, lastVersion={}",
                    event.reviewId(), event.version(), state.getLastVersion());
            return;
        }
        
        switch (event.eventType()){
            case REVIEW_CREATED -> handleCreate(product, event);
            case REVIEW_DELETED -> handleDelete(product, state, event);
            case REVIEW_UPDATED -> handleUpdate(product, state, event);
        }
    }

    private void handleUpdate(Product product, ProductReviewState state, ReviewEvent event) {
        if (state == null){
            state = ProductReviewState.builder()
                    .reviewId(event.reviewId())
                    .productId(event.productId())
                    .rating(event.rating())
                    .lastVersion(event.version())
                    .build();

            recalculateRatingOnAdd(product, event.rating());
        } else if (state.getRating() == null) {
            recalculateRatingOnAdd(product, event.rating());
            state.setRating(event.rating());
            state.setLastVersion(event.version());
        }
        else {
            recalculateRatingOnUpdate(product, state.getRating(), event.rating());
            state.setRating(event.rating());
            state.setLastVersion(event.version());
        }

        productReviewStateRepository.save(state);
        productRepository.save(product);
    }



    private void handleDelete(Product product, ProductReviewState state, ReviewEvent event) {
        if (state == null){
            state = ProductReviewState.builder()
                    .reviewId(event.reviewId())
                    .productId(event.productId())
                    .rating(event.rating())
                    .lastVersion(event.version())
                    .build();
        }
        else {
            if (state.getRating() != null) {
                recalculateRatingOnDelete(product, state.getRating());
                state.setRating(null);
                productRepository.save(product);
            }
            state.setLastVersion(event.version());
        }

        productReviewStateRepository.save(state);
    }

    private void handleCreate(Product product, ReviewEvent event) {
        var state = ProductReviewState.builder()
                .reviewId(event.reviewId())
                .productId(event.productId())
                .rating(event.rating())
                .lastVersion(event.version())
                .build();

        recalculateRatingOnAdd(product, event.rating());

        productReviewStateRepository.save(state);
        productRepository.save(product);

    }

    private void recalculateRatingOnAdd(Product product, Integer rating) {
        int oldCount = product.getReviewsCount();
        int newCount = oldCount + 1;

        BigDecimal totalSum = product.getRating().multiply(BigDecimal.valueOf(oldCount));
        BigDecimal newTotalSum = totalSum.add(BigDecimal.valueOf(rating));
        BigDecimal newAvg = newTotalSum.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        product.setReviewsCount(newCount);
        product.setRating(newAvg);
    }

    private void recalculateRatingOnUpdate(Product product, Integer oldRating, Integer newRating) {
        int count = product.getReviewsCount();

        BigDecimal totalSum = product.getRating().multiply(BigDecimal.valueOf(count));

        BigDecimal newTotalSum = totalSum
                .subtract(BigDecimal.valueOf(oldRating))
                .add(BigDecimal.valueOf(newRating));

        BigDecimal newAvg = newTotalSum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        product.setRating(newAvg);
    }

    private void recalculateRatingOnDelete(Product product, Integer rating) {
        int oldCount = product.getReviewsCount();
        int newCount = oldCount - 1;

        if (newCount <= 0) {
            product.setReviewsCount(0);
            product.setRating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            return;
        }

        BigDecimal totalSum = product.getRating().multiply(BigDecimal.valueOf(oldCount));

        BigDecimal newTotalSum = totalSum.subtract(BigDecimal.valueOf(rating));

        BigDecimal newAvg = newTotalSum.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        product.setReviewsCount(newCount);
        product.setRating(newAvg);
    }



}
