package com.testly.service;

import com.testly.dto.QuestionDtos.*;
import com.testly.dto.TestDtos.*;
import com.testly.entity.*;
import com.testly.exception.ApiException;
import com.testly.repository.ClassroomRepository;
import com.testly.repository.TestRepository;
import com.testly.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TestService {

    private final TestRepository testRepository;
    private final ClassroomRepository classroomRepository;
    private final ClassroomService classroomService;
    private final CurrentUserProvider currentUserProvider;

    public TestDetailResponse createTest(CreateTestRequest request) {
        User teacher = currentUserProvider.getCurrentUser();

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new ApiException("End time must be after start time", HttpStatus.BAD_REQUEST);
        }

        // Optional -- null/omitted classroomId means the test is open to every student (unchanged default).
        Classroom classroom = request.getClassroomId() != null
                ? classroomService.getOwnedClassroom(request.getClassroomId())
                : null;

        Test test = Test.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .teacher(teacher)
                .durationMinutes(request.getDurationMinutes())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .allowReview(request.getAllowReview() == null || request.getAllowReview())
                .classroom(classroom)
                .published(false)
                .build();

        test = testRepository.save(test);
        return toDetail(test);
    }

    public List<TestSummaryResponse> myTests() {
        User teacher = currentUserProvider.getCurrentUser();
        return testRepository.findByTeacher(teacher).stream()
                .sorted(Comparator.comparing(Test::getStartTime).reversed())
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public List<TestSummaryResponse> availableForStudent() {
        User student = currentUserProvider.getCurrentUser();
        LocalDateTime now = LocalDateTime.now();
        List<Classroom> joined = classroomRepository.findByStudents_Id(student.getId());
        return testRepository.findAvailableForStudent(now, joined).stream()
                .sorted(Comparator.comparing(Test::getEndTime))
                .map(this::toSummary)
                .collect(Collectors.toList());
    }

    public TestDetailResponse getOwnedTestDetail(Long testId) {
        Test test = getOwnedTest(testId);
        return toDetail(test);
    }

    public TestDetailResponse publish(Long testId, boolean publish) {
        Test test = getOwnedTest(testId);
        if (publish && test.getQuestions().isEmpty()) {
            throw new ApiException("Cannot publish a test with no questions", HttpStatus.BAD_REQUEST);
        }
        test.setPublished(publish);
        test = testRepository.save(test);
        return toDetail(test);
    }

    /** Fetches a test and verifies the current user is the owning teacher. */
    public Test getOwnedTest(Long testId) {
        User teacher = currentUserProvider.getCurrentUser();
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ApiException("Test not found", HttpStatus.NOT_FOUND));
        if (!test.getTeacher().getId().equals(teacher.getId())) {
            throw new ApiException("You do not own this test", HttpStatus.FORBIDDEN);
        }
        return test;
    }

    public Test getById(Long testId) {
        return testRepository.findById(testId)
                .orElseThrow(() -> new ApiException("Test not found", HttpStatus.NOT_FOUND));
    }

    private TestSummaryResponse toSummary(Test test) {
        return TestSummaryResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .durationMinutes(test.getDurationMinutes())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .published(test.isPublished())
                .questionCount(test.getQuestions().size())
                .classroomName(test.getClassroom() != null ? test.getClassroom().getName() : null)
                .build();
    }

    private TestDetailResponse toDetail(Test test) {
        List<QuestionTeacherView> questions = test.getQuestions().stream()
                .map(this::toTeacherView)
                .collect(Collectors.toList());

        return TestDetailResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .description(test.getDescription())
                .durationMinutes(test.getDurationMinutes())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .published(test.isPublished())
                .allowReview(test.isAllowReview())
                .classroomName(test.getClassroom() != null ? test.getClassroom().getName() : null)
                .questions(questions)
                .build();
    }

    private QuestionTeacherView toTeacherView(Question q) {
        return QuestionTeacherView.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionImageUrl(q.getQuestionImageUrl())
                .marks(q.getMarks())
                .options(q.getOptions().stream()
                        .map(o -> OptionTeacherView.builder()
                                .id(o.getId())
                                .optionText(o.getOptionText())
                                .optionImageUrl(o.getOptionImageUrl())
                                .correct(o.isCorrect())
                                .build())
                        .collect(Collectors.toList()))
                .tags(q.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList()))
                .build();
    }
}
