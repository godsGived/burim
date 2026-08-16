package com.burim.review_service.controller;


import com.burim.review_service.BaseIntegrationTest;
import com.burim.review_service.dto.CreateReviewRequest;
import com.burim.review_service.dto.ReviewResponse;
import com.burim.review_service.enitiy.Review;
import com.burim.review_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ReviewControllerIT extends BaseIntegrationTest {

    @Autowired
    protected ReviewRepository reviewRepository;

    @BeforeEach
    void setUp(){
        reviewRepository.deleteAll();
    }

    @Test
    void createReview_whenRequestIsValid_ShouldReturn201AndSaveToDB() throws Exception{
        // Arrange
        CreateReviewRequest reviewRequest = new CreateReviewRequest(
                1L,
                5,
                "Everything's good!",
                "I use this stuff and it's perfect. I recommend it.",
                "price",
                "none"
        );

        // Act
        var jsonResponse = mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Everything's good!"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Assert
        ReviewResponse response = objectMapper.readValue(jsonResponse, ReviewResponse.class);
        assertThat(response.id()).isNotNull();

        var review = reviewRepository.findById(response.id());

        assertThat(review).isPresent();
        assertThat(review.get()).satisfies(r -> {
                    assertThat(r.getProductId()).isEqualTo(1L);
                    assertThat(r.getUserId()).isEqualTo(12L);
                    assertThat(r.getTitle()).isEqualTo("Everything's good!");
                    assertThat(r.getCreatedAt()).isNotNull();
                    assertThat(r.getUpdatedAt()).isNotNull();
                }
        );
    }

    @Test
    void createOneMoreReview_whenRequestIsValid_ShouldReturn409AndDoesNotSave() throws Exception{
        // Arrange
        Review review = Review.builder()
                .userId(12L)
                .productId(1L)
                .rating(3)
                .title("Test title")
                .description("Test description")
                .build();

        reviewRepository.save(review);

        CreateReviewRequest reviewRequest = new CreateReviewRequest(
                1L,
                5,
                "Everything's good!",
                "I use this stuff and it's perfect. I recommend it.",
                "price",
                "none"
        );

        // Act
        mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Review already exists for product: 1"));

        // Assert
        var reviews = reviewRepository.findAllByProductId(1L);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.getFirst().getRating()).isEqualTo(3);
    }

    @Test
    void deleteReview_whenUserIsNotOwner_ShouldReturn403_AndDoesNotDelete() throws Exception{
        // Arrange
        Review review = Review.builder()
                .userId(12L)
                .productId(1L)
                .rating(3)
                .title("Test title")
                .description("Test description")
                .build();

        var saverReview = reviewRepository.saveAndFlush(review);

        // Act
        mockMvc.perform(delete("/api/v1/reviews/" + saverReview.getId())
                        .header("X-User-Id", 13L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not allowed to delete this review"));

        // Assert
        var userReview = reviewRepository.findAllByUserId(12L);

        assertThat(userReview).hasSize(1);
    }

    @Test
    void createReview_withNotValidRating_ShouldReturn400_AndDoesNotSave() throws Exception{
        // Arrange
        CreateReviewRequest reviewRequest = new CreateReviewRequest(
                1L,
                6,
                "Everything's good!",
                "I use this stuff and it's perfect. I recommend it.",
                "price",
                "none"
        );

        // Act
        var jsonResponse = mockMvc.perform(post("/api/v1/reviews")
                        .header("X-User-Id", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rating must be at most 5"));

        // Assert
        var review = reviewRepository.findAllByProductId(1L);

        assertThat(review).hasSize(0);
    }


}
