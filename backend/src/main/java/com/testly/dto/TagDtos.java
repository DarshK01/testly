package com.testly.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TagDtos {

    @Data
    public static class TagRequest {
        @NotBlank
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagResponse {
        private Long id;
        private String name;
    }
}
