package com.burim.review_service.service;

import com.burim.review_service.BaseIntegrationTest;
import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.UpdateReviewRequest;
import com.burim.review_service.dto.event.ReviewEvent;
import com.burim.review_service.dto.event.ReviewEventType;
import com.burim.review_service.enitiy.Review;
import com.burim.review_service.repository.ReviewRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ReviewEventProducerIT extends BaseIntegrationTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Value("${app.kafka.topics.reviews}")
    private String reviewsTopic;

    private Consumer<String, String> testConsumer;

    @BeforeEach
    void setUp() {
        reviewRepository.deleteAll();

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-verify-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        testConsumer = new KafkaConsumer<>(props);

        TopicPartition partition = new TopicPartition(reviewsTopic, 0);

        testConsumer.assign(Collections.singletonList(partition));
        testConsumer.seekToEnd(Collections.singletonList(partition));
        testConsumer.position(partition);
    }

    @AfterEach
    void tearDown() {
        if (testConsumer != null) {
            testConsumer.close();
        }
    }

    @Test
    void createReview_ShouldSaveReviewAndPublishCreatedEventToKafka() throws Exception {
        // Arrange
        CreateReviewRequest request = new CreateReviewRequest(
                1L,
                5,
                "Excellent Product",
                "Really loved using this item everyday!",
                "Battery life, build quality",
                "A bit pricey"
        );

        // Act
        mockMvc.perform(post("/api/v1/reviews")
                        .with(jwt().jwt(builder -> builder.subject("user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Assert
        assertThat(reviewRepository.count()).isEqualTo(1);

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                testConsumer,
                reviewsTopic,
                Duration.ofSeconds(5)
        );

        ReviewEvent event = objectMapper.readValue(record.value(), ReviewEvent.class);

        assertThat(record.key()).isEqualTo("1");
        assertThat(event.eventType()).isEqualTo(ReviewEventType.REVIEW_CREATED);
        assertThat(event.productId()).isEqualTo(1L);
        assertThat(event.rating()).isEqualTo(5);
        assertThat(event.version()).isZero();
    }

    @Test
    void updateReview_ShouldUpdateReviewAndPublishUpdatedEventToKafka() throws Exception {
        // Arrange
        Review review = reviewRepository.save(Review.builder()
                .userId("user-123")
                .productId(1L)
                .rating(5)
                .title("Initial Title")
                .description("Initial Description")
                .advantages("None")
                .disadvantages("None")
                .build());

        UpdateReviewRequest request = new UpdateReviewRequest(
                2,
                "Updated Title",
                "Updated Description",
                "Battery life",
                "Price"
        );

        // Act
        mockMvc.perform(put("/api/v1/reviews/{id}", review.getId())
                        .with(jwt().jwt(builder -> builder.subject("user-123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                testConsumer,
                reviewsTopic,
                Duration.ofSeconds(5)
        );

        ReviewEvent event = objectMapper.readValue(record.value(), ReviewEvent.class);

        assertThat(record.key()).isEqualTo("1");
        assertThat(event.eventType()).isEqualTo(ReviewEventType.REVIEW_UPDATED);
        assertThat(event.reviewId()).isEqualTo(review.getId());
        assertThat(event.productId()).isEqualTo(1L);
        assertThat(event.rating()).isEqualTo(2);
        assertThat(event.version()).isEqualTo(1L);
    }

    @Test
    void deleteReview_ShouldDeleteReviewAndPublishDeletedEventToKafka() throws Exception {
        // Arrange
        Review review = reviewRepository.save(Review.builder()
                .userId("user-123")
                .productId(1L)
                .rating(4)
                .title("Delete me")
                .description("Going to be deleted")
                .build());

        // Act
        mockMvc.perform(delete("/api/v1/reviews/{id}", review.getId())
                        .with(jwt().jwt(builder -> builder.subject("user-123"))))
                .andExpect(status().isNoContent());

        // Assert
        assertThat(reviewRepository.count()).isZero();

        ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                testConsumer,
                reviewsTopic,
                Duration.ofSeconds(5)
        );

        ReviewEvent event = objectMapper.readValue(record.value(), ReviewEvent.class);

        assertThat(record.key()).isEqualTo("1");
        assertThat(event.eventType()).isEqualTo(ReviewEventType.REVIEW_DELETED);
        assertThat(event.reviewId()).isEqualTo(review.getId());
        assertThat(event.productId()).isEqualTo(1L);
        assertThat(event.rating()).isNull();
        assertThat(event.version()).isEqualTo(1L);
    }
}