package com.burim.review_service.enitiy;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "reviews")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String userId;
    Long productId;
    Integer rating;
    String title;
    String description;
    String advantages;
    String disadvantages;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    OffsetDateTime createdAt;

    @Column(nullable = false)
    @UpdateTimestamp
    OffsetDateTime updatedAt;

    public void updateContent(Integer rating, String title, String description, String advantages, String disadvantages) {
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.advantages = advantages;
        this.disadvantages = disadvantages;
    }

    public boolean isOwnedBy(String userId) {
        return Objects.equals(this.userId, userId);
    }

}
