package com.testly.controller;

import com.testly.dto.QuestionDtos.*;
import com.testly.service.QuestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    private final ObjectMapper objectMapper;

    /**
     * Adds a question to a test. Accepts multipart/form-data so an image can be attached:
     *  - "question": JSON body matching CreateQuestionRequest (questionText, marks, options, tagNames)
     *  - "image": optional file part (jpg/png/webp, <=2MB)
     *
     * tagNames inside "question" is entirely optional -- omit it, or send an empty array,
     * for an untagged question.
     */
    @PostMapping(value = "/api/tests/{testId}/questions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionTeacherView> addQuestion(
            @PathVariable Long testId,
            @RequestPart("question") String questionJson,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws Exception {
        CreateQuestionRequest request = objectMapper.readValue(questionJson, CreateQuestionRequest.class);
        return ResponseEntity.ok(questionService.addQuestion(testId, request, image));
    }

    @DeleteMapping("/api/tests/{testId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long testId, @PathVariable Long questionId) {
        questionService.deleteQuestion(testId, questionId);
        return ResponseEntity.noContent().build();
    }
}
