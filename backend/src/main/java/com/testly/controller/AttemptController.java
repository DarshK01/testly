package com.testly.controller;

import com.testly.dto.AttemptDtos.*;
import com.testly.service.AttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attempts")
@RequiredArgsConstructor
public class AttemptController {

    private final AttemptService attemptService;

    // Auto-save a single answer while the student is mid-test.
    @PostMapping("/{attemptId}/answer")
    public ResponseEntity<Void> saveAnswer(@PathVariable Long attemptId, @RequestBody SaveAnswerRequest request) {
        attemptService.saveAnswer(attemptId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<AttemptResultResponse> submit(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.submit(attemptId));
    }

    // Tags and correct answers are revealed here -- only after submission, only to this student.
    @GetMapping("/{attemptId}/result")
    public ResponseEntity<AttemptResultResponse> getResult(@PathVariable Long attemptId) {
        return ResponseEntity.ok(attemptService.getResult(attemptId));
    }
}
