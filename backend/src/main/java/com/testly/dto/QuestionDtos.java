package com.testly.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class QuestionDtos {

    // Sent by the teacher when creating/editing a question.
    // questionText and/or an uploaded image must be present (validated in the service layer).
    // tagNames is optional and may be empty -- tags are not required.
    @Data
    public static class CreateQuestionRequest {
        private String questionText;

        @NotNull
        private Integer marks;

        private List<OptionInput> options; // exactly 4, exactly one marked correct

        private List<String> tagNames; // optional, 0..n topic tags e.g. ["Arrays", "Recursion"]
    }

    @Data
    public static class OptionInput {
        private String optionText;
        private boolean correct;
    }

    // What the teacher sees: correct answers + tags visible.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionTeacherView {
        private Long id;
        private String questionText;
        private String questionImageUrl;
        private Integer marks;
        private List<OptionTeacherView> options;
        private List<String> tags;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionTeacherView {
        private Long id;
        private String optionText;
        private String optionImageUrl;
        private boolean correct;
    }

    // What the student sees WHILE ATTEMPTING the test: no correct flag, no tags.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionAttemptView {
        private Long id;
        private String questionText;
        private String questionImageUrl;
        private Integer marks;
        private List<OptionAttemptView> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionAttemptView {
        private Long id;
        private String optionText;
        private String optionImageUrl;
    }

    // What the student sees AFTER submitting (if the teacher allows review):
    // correct answers revealed AND tags revealed -- this is the only place tags reach the student.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionResultView {
        private Long id;
        private String questionText;
        private String questionImageUrl;
        private Integer marks;
        private Integer marksAwarded;
        private Long selectedOptionId;
        private Long correctOptionId;
        private boolean correct;
        private List<OptionTeacherView> options;
        private List<String> tags; // <-- tags revealed only here
    }
}
