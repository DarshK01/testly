package com.testly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class ClassroomDtos {

    @Data
    public static class CreateClassroomRequest {
        @NotBlank
        private String name;
    }

    @Data
    public static class JoinClassroomRequest {
        @NotBlank
        private String joinCode;
    }

    // Teacher's own view: includes the join code to share with students.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomTeacherView {
        private Long id;
        private String name;
        private String joinCode;
        private int studentCount;
    }

    // Student's view: no join code needed once already a member.
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClassroomStudentView {
        private Long id;
        private String name;
        private String teacherName;
    }
}
