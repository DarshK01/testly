package com.testly.controller;

import com.testly.dto.ClassroomDtos.*;
import com.testly.service.ClassroomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/classrooms")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<ClassroomTeacherView> create(@Valid @RequestBody CreateClassroomRequest request) {
        return ResponseEntity.ok(classroomService.create(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<ClassroomTeacherView>> myClassrooms() {
        return ResponseEntity.ok(classroomService.myClassrooms());
    }

    @GetMapping("/joined")
    public ResponseEntity<List<ClassroomStudentView>> joinedClassrooms() {
        return ResponseEntity.ok(classroomService.joinedClassrooms());
    }

    @PostMapping("/join")
    public ResponseEntity<ClassroomStudentView> join(@Valid @RequestBody JoinClassroomRequest request) {
        return ResponseEntity.ok(classroomService.join(request));
    }
}
