package com.testly.controller;

import com.testly.dto.AttemptDtos.AttemptSummaryForTeacher;
import com.testly.dto.TestDtos.*;
import com.testly.service.AttemptService;
import com.testly.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;
    private final AttemptService attemptService;

    @PostMapping
    public ResponseEntity<TestDetailResponse> create(@Valid @RequestBody CreateTestRequest request) {
        return ResponseEntity.ok(testService.createTest(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<TestSummaryResponse>> myTests() {
        return ResponseEntity.ok(testService.myTests());
    }

    @GetMapping("/available")
    public ResponseEntity<List<TestSummaryResponse>> available() {
        return ResponseEntity.ok(testService.availableForStudent());
    }

    // Teacher's own detailed view: includes correct answers + tags.
    @GetMapping("/{id}")
    public ResponseEntity<TestDetailResponse> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(testService.getOwnedTestDetail(id));
    }

    @PutMapping("/{id}/publish")
    public ResponseEntity<TestDetailResponse> publish(@PathVariable Long id) {
        return ResponseEntity.ok(testService.publish(id, true));
    }

    @PutMapping("/{id}/unpublish")
    public ResponseEntity<TestDetailResponse> unpublish(@PathVariable Long id) {
        return ResponseEntity.ok(testService.publish(id, false));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<List<AttemptSummaryForTeacher>> results(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.resultsForTest(id));
    }

    // Student starts (or resumes) an attempt -- questions come back with no answer key, no tags.
    @PostMapping("/{id}/attempt")
    public ResponseEntity<?> startAttempt(@PathVariable Long id) {
        return ResponseEntity.ok(attemptService.startAttempt(id));
    }
}
