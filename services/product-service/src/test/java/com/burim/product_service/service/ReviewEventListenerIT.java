package com.burim.product_service.service;

import com.burim.product_service.BaseIntegrationTest;
import com.burim.product_service.dto.event.ReviewEvent;
import com.burim.product_service.dto.event.ReviewEventType;
import com.burim.product_service.entity.Brand;
import com.burim.product_service.entity.Category;
import com.burim.product_service.entity.Product;
import com.burim.product_service.repository.BrandRepository;
import com.burim.product_service.repository.CategoryRepository;
import com.burim.product_service.repository.ProductRepository;
import com.burim.product_service.repository.ProductReviewStateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class ReviewEventListenerIT extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductReviewStateRepository stateRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Value("${app.kafka.topics.reviews}")
    private String reviewsTopic;

    private Product product;

    @BeforeEach
    void setUp() {
        stateRepository.deleteAll();
        productRepository.deleteAll();

        Category category = categoryRepository.findById(1L).orElseGet(() ->
                categoryRepository.save(Category.builder().name("Gaming").description("Gaming gear").build()));

        Brand brand = brandRepository.findById(1L).orElseGet(() ->
                brandRepository.save(Brand.builder().name("Logitech").build()));

        product = productRepository.save(Product.builder()
                .name("G Pro Wireless")
                .description("Super light mouse")
                .price(new BigDecimal("149.99"))
                .category(category)
                .brand(brand)
                .stock(10)
                .rating(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .reviewsCount(0)
                .build());
    }

    private void sendEvent(ReviewEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(reviewsTopic, String.valueOf(event.productId()), json);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldCalculateRatingOnReviewCreatedEvent() {
        // Arrange
        ReviewEvent event = new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_CREATED,
                100L,
                product.getId(),
                5,
                0L,
                Instant.now()
        );

        // Act
        sendEvent(event);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Product updated = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updated.getReviewsCount()).isEqualTo(1);
                    assertThat(updated.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));
                });
    }

    @Test
    void shouldRecalculateRatingOnReviewDeletedEvent() {
        // Arrange
        ReviewEvent createEvent = new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_CREATED,
                101L,
                product.getId(),
                5,
                0L,
                Instant.now()
        );

        // Act
        sendEvent(createEvent);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Product updated = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updated.getReviewsCount()).isEqualTo(1);
                });

        // Arrange
        ReviewEvent deleteEvent = new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_DELETED,
                101L,
                product.getId(),
                null,
                1L,
                Instant.now()
        );

        // Act
        sendEvent(deleteEvent);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Product updated = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updated.getReviewsCount()).isZero();
                    assertThat(updated.getRating()).isEqualByComparingTo(BigDecimal.ZERO);
                });
    }

    @Test
    void shouldRecalculateRatingOnReviewUpdatedEvent() {
        // Arrange
        ReviewEvent createEvent = new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_CREATED,
                101L,
                product.getId(),
                5,
                0L,
                Instant.now()
        );

        // Act
        sendEvent(createEvent);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Product updated = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updated.getReviewsCount()).isEqualTo(1);
                    assertThat(updated.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));
                });

        // Arrange
        ReviewEvent updateEvent = new ReviewEvent(
                UUID.randomUUID(),
                ReviewEventType.REVIEW_UPDATED,
                101L,
                product.getId(),
                1,
                1L,
                Instant.now()
        );

        // Act
        sendEvent(updateEvent);

        // Assert
        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> {
                    Product updated = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(updated.getReviewsCount()).isEqualTo(1);
                    assertThat(updated.getRating()).isEqualByComparingTo(new BigDecimal("1.00"));
                });
    }

    @Test
    void shouldCorrectlyAggregateMultipleReviewsLifecycle() {
        // Arrange & Act: Review A (5)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_CREATED, 101L, product.getId(), 5, 0L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getReviewsCount()).isEqualTo(1);
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));
        });

        // Arrange & Act: Review B (3)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_CREATED, 102L, product.getId(), 3, 0L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getReviewsCount()).isEqualTo(2);
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("4.00"));
        });

        // Arrange & Act: Review B (3 -> 1)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_UPDATED, 102L, product.getId(), 1, 1L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getReviewsCount()).isEqualTo(2);
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("3.00"));
        });

        // Arrange & Act: Review A deleted
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_DELETED, 101L, product.getId(), null, 1L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getReviewsCount()).isEqualTo(1);
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("1.00"));
        });
    }

    @Test
    void shouldIgnoreDuplicateAndStaleVersionEvents() {
        // Arrange & Act: create review (v0, rating=5)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_CREATED, 201L, product.getId(), 5, 0L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getReviewsCount()).isEqualTo(1);
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));
        });

        // Arrange & Act: duplicate same event (v0, rating=5)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_CREATED, 201L, product.getId(), 5, 0L, Instant.now()));

        // Assert
        await().during(Duration.ofMillis(400))
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    Product p = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(p.getReviewsCount()).isEqualTo(1);
                    assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("5.00"));
                });

        // Arrange & Act: update to v1 (v1, rating=2)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_UPDATED, 201L, product.getId(), 2, 1L, Instant.now()));

        // Assert
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            Product p = productRepository.findById(product.getId()).orElseThrow();
            assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("2.00"));
        });

        // Arrange & Act: get old event (v0, rating=5)
        sendEvent(new ReviewEvent(UUID.randomUUID(), ReviewEventType.REVIEW_UPDATED, 201L, product.getId(), 5, 0L, Instant.now()));

        // Assert
        await().during(Duration.ofMillis(400))
                .atMost(Duration.ofSeconds(2))
                .untilAsserted(() -> {
                    Product p = productRepository.findById(product.getId()).orElseThrow();
                    assertThat(p.getReviewsCount()).isEqualTo(1);
                    assertThat(p.getRating()).isEqualByComparingTo(new BigDecimal("2.00"));
                });
    }
}