package com.testly.service;

import com.testly.dto.ClassroomDtos.*;
import com.testly.entity.Classroom;
import com.testly.entity.User;
import com.testly.exception.ApiException;
import com.testly.repository.ClassroomRepository;
import com.testly.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomService {

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no O/0/I/1 -- easy to read aloud
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ClassroomRepository classroomRepository;
    private final CurrentUserProvider currentUserProvider;

    public ClassroomTeacherView create(CreateClassroomRequest request) {
        User teacher = currentUserProvider.getCurrentUser();

        Classroom classroom = Classroom.builder()
                .name(request.getName())
                .teacher(teacher)
                .joinCode(generateUniqueCode())
                .build();

        classroom = classroomRepository.save(classroom);
        return toTeacherView(classroom);
    }

    public List<ClassroomTeacherView> myClassrooms() {
        User teacher = currentUserProvider.getCurrentUser();
        return classroomRepository.findByTeacher(teacher).stream()
                .map(this::toTeacherView)
                .collect(Collectors.toList());
    }

    public List<ClassroomStudentView> joinedClassrooms() {
        User student = currentUserProvider.getCurrentUser();
        return classroomRepository.findByStudents_Id(student.getId()).stream()
                .map(c -> ClassroomStudentView.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .teacherName(c.getTeacher().getName())
                        .build())
                .collect(Collectors.toList());
    }

    public ClassroomStudentView join(JoinClassroomRequest request) {
        User student = currentUserProvider.getCurrentUser();
        Classroom classroom = classroomRepository.findByJoinCode(request.getJoinCode().trim().toUpperCase())
                .orElseThrow(() -> new ApiException("Invalid join code", HttpStatus.NOT_FOUND));

        classroom.getStudents().add(student); // Set -- adding an existing member is a no-op
        classroomRepository.save(classroom);

        return ClassroomStudentView.builder()
                .id(classroom.getId())
                .name(classroom.getName())
                .teacherName(classroom.getTeacher().getName())
                .build();
    }

    /** Used by TestService to validate/attach an optional classroom restriction when creating a test. */
    public Classroom getOwnedClassroom(Long classroomId) {
        User teacher = currentUserProvider.getCurrentUser();
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ApiException("Classroom not found", HttpStatus.NOT_FOUND));
        if (!classroom.getTeacher().getId().equals(teacher.getId())) {
            throw new ApiException("You do not own this classroom", HttpStatus.FORBIDDEN);
        }
        return classroom;
    }

    private String generateUniqueCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder(CODE_LENGTH);
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
            }
            code = sb.toString();
        } while (classroomRepository.existsByJoinCode(code));
        return code;
    }

    private ClassroomTeacherView toTeacherView(Classroom c) {
        return ClassroomTeacherView.builder()
                .id(c.getId())
                .name(c.getName())
                .joinCode(c.getJoinCode())
                .studentCount(c.getStudents().size())
                .build();
    }
}
