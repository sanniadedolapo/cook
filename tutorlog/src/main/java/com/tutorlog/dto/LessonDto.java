package com.tutorlog.dto;

import com.tutorlog.model.Lesson;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

public class LessonDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Title is required")
        private String title;

        @NotBlank(message = "Subject is required")
        private String subject;

        private String description;

        @NotBlank(message = "Content is required")
        private String content;

        @Min(value = 1, message = "Duration must be at least 1 minute")
        private int durationMinutes;

        @NotNull(message = "Access type is required")
        private Lesson.AccessType accessType;
    }

    @Data
    public static class UpdateRequest {
        private String title;
        private String subject;
        private String description;
        private String content;
        private Integer durationMinutes;
        private Lesson.AccessType accessType;
    }

    @Data
    public static class Response {
        private String id;
        private String title;
        private String subject;
        private String description;
        private String content;
        private int durationMinutes;
        private String tutorId;
        private String tutorName;
        private Lesson.AccessType accessType;
        private int viewCount;
        private double rating;
        private int ratingCount;
        private LocalDateTime createdAt;

        public static Response from(Lesson lesson) {
            Response r = new Response();
            r.setId(lesson.getId());
            r.setTitle(lesson.getTitle());
            r.setSubject(lesson.getSubject());
            r.setDescription(lesson.getDescription());
            r.setContent(lesson.getContent());
            r.setDurationMinutes(lesson.getDurationMinutes());
            r.setTutorId(lesson.getTutorId());
            r.setTutorName(lesson.getTutorName());
            r.setAccessType(lesson.getAccessType());
            r.setViewCount(lesson.getViewCount());
            r.setRating(lesson.getRating());
            r.setRatingCount(lesson.getRatingCount());
            r.setCreatedAt(lesson.getCreatedAt());
            return r;
        }

        // Returns lesson without content for listing endpoints (lighter payload)
        public static Response fromSummary(Lesson lesson) {
            Response r = from(lesson);
            r.setContent(null);
            return r;
        }
    }
}
