package com.testly.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class TestDtos {

    @Data
    public static class CreateTestRequest {
        @NotBlank
        private String title;

        private String description;

        @NotNull @Min(1)
        private Integer durationMinutes;

        @NotNull
        private LocalDateTime startTime;

        @NotNull
        private LocalDateTime endTime;

        private Boolean allowReview = true;

        // Optional. Leave null/omit for a test open to every student (original behavior).
        // If set, must be a classroom this teacher owns.
        private Long classroomId;
    }

    // Summary shown in list views (teacher's own tests, student's available tests).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestSummaryResponse {
        private Long id;
        private String title;
        private String description;
        private Integer durationMinutes;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean published;
        private int questionCount;
        private String classroomName; // null if open to everyone
    }

    // Full detail for the teacher managing a test (includes correct answers + tags).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestDetailResponse {
        private Long id;
        private String title;
        private String description;
        private Integer durationMinutes;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private boolean published;
        private boolean allowReview;
        private String classroomName; // null if open to everyone
        private List<QuestionDtos.QuestionTeacherView> questions;
    }
}
