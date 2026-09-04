package com.testly.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class AttemptDtos {

    // Returned when a student starts an attempt: questions with NO correct answers, NO tags.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptStartResponse {
        private Long attemptId;
        private Long testId;
        private String testTitle;
        private Integer durationMinutes;
        private LocalDateTime startTime;
        private LocalDateTime hardDeadline; // startTime + duration, capped by test.endTime
        private List<QuestionDtos.QuestionAttemptView> questions;
    }

    // Autosave a single answer while the test is in progress.
    @Data
    public static class SaveAnswerRequest {
        private Long questionId;
        private Long selectedOptionId; // null = clear/skip
    }

    // Full result after submission, with tags revealed per question (if allowReview).
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptResultResponse {
        private Long attemptId;
        private Long testId;
        private String testTitle;
        private LocalDateTime submittedTime;
        private double score;
        private double maxScore;
        private List<QuestionDtos.QuestionResultView> questions; // empty if allowReview = false
        private Map<String, TagScore> tagWiseScore; // topic -> score breakdown, derived from tags
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagScore {
        private int correct;
        private int total;
    }

    // Teacher-facing: one row per student attempt for a test.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttemptSummaryForTeacher {
        private Long attemptId;
        private Long studentId;
        private String studentName;
        private LocalDateTime startTime;
        private LocalDateTime submittedTime;
        private Double score;
    }
}
