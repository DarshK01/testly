package com.testly.controller;

import com.testly.dto.TagDtos.*;
import com.testly.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    // Public read: useful for a teacher's "existing tags" autocomplete when tagging a question.
    @GetMapping
    public ResponseEntity<List<TagResponse>> listAll() {
        return ResponseEntity.ok(tagService.listAll());
    }

    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.ok(tagService.create(request));
    }
}
