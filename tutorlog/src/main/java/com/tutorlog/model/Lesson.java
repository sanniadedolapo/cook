package com.tutorlog.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "lessons")
public class Lesson {

    @Id
    private String id;

    private String title;
    private String subject;
    private String description;
    private String content;
    private int durationMinutes;

    private String tutorId;
    private String tutorName;

    private AccessType accessType;

    @Builder.Default
    private int viewCount = 0;

    @Builder.Default
    private double rating = 0.0;

    @Builder.Default
    private int ratingCount = 0;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum AccessType {
        FREE, PREMIUM
    }
}
