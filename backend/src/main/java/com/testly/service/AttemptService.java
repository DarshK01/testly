package com.testly.service;

import com.testly.dto.AttemptDtos.*;
import com.testly.dto.QuestionDtos.*;
import com.testly.entity.*;
import com.testly.exception.ApiException;
import com.testly.repository.AnswerRepository;
import com.testly.repository.TestAttemptRepository;
import com.testly.security.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttemptService {

    // Small grace window so a student mid-click when the timer hits zero isn't unfairly blocked.
    private static final long SUBMIT_GRACE_SECONDS = 30;

    private final TestAttemptRepository attemptRepository;
    private final AnswerRepository answerRepository;
    private final TestService testService;
    private final CurrentUserProvider currentUserProvider;

    public AttemptStartResponse startAttempt(Long testId) {
        User student = currentUserProvider.getCurrentUser();
        Test test = testService.getById(testId);
        LocalDateTime now = LocalDateTime.now();

        if (!test.isPublished()) {
            throw new ApiException("Test is not available", HttpStatus.FORBIDDEN);
        }
        if (now.isBefore(test.getStartTime()) || now.isAfter(test.getEndTime())) {
            throw new ApiException("Test is not open right now", HttpStatus.FORBIDDEN);
        }

        TestAttempt attempt = attemptRepository.findByStudentAndTest(student, test)
                .orElse(null);

        if (attempt != null && attempt.getSubmittedTime() != null) {
            throw new ApiException("You have already submitted this test", HttpStatus.CONFLICT);
        }

        if (attempt == null) {
            attempt = TestAttempt.builder()
                    .student(student)
                    .test(test)
                    .startTime(now)
                    .build();
            attempt = attemptRepository.save(attempt);
        }
        // else: resuming an in-progress attempt -- startTime stays as originally recorded.

        LocalDateTime hardDeadline = attempt.getStartTime()
                .plusMinutes(test.getDurationMinutes());
        if (hardDeadline.isAfter(test.getEndTime())) {
            hardDeadline = test.getEndTime();
        }

        List<QuestionAttemptView> questions = test.getQuestions().stream()
                .map(this::toAttemptView)
                .collect(Collectors.toList());

        return AttemptStartResponse.builder()
                .attemptId(attempt.getId())
                .testId(test.getId())
                .testTitle(test.getTitle())
                .durationMinutes(test.getDurationMinutes())
                .startTime(attempt.getStartTime())
                .hardDeadline(hardDeadline)
                .questions(questions)
                .build();
    }

    public void saveAnswer(Long attemptId, SaveAnswerRequest request) {
        TestAttempt attempt = getOwnedInProgressAttempt(attemptId);
        assertWithinDeadline(attempt);

        Question question = attempt.getTest().getQuestions().stream()
                .filter(q -> q.getId().equals(request.getQuestionId()))
                .findFirst()
                .orElseThrow(() -> new ApiException("Question does not belong to this test", HttpStatus.BAD_REQUEST));

        QuestionOption selected = null;
        if (request.getSelectedOptionId() != null) {
            selected = question.getOptions().stream()
                    .filter(o -> o.getId().equals(request.getSelectedOptionId()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException("Option does not belong to this question", HttpStatus.BAD_REQUEST));
        }

        Answer answer = answerRepository.findByAttemptAndQuestion_Id(attempt, question.getId())
                .orElseGet(() -> Answer.builder().attempt(attempt).question(question).build());
        answer.setSelectedOption(selected);
        answerRepository.save(answer);
    }

    public AttemptResultResponse submit(Long attemptId) {
        TestAttempt attempt = getOwnedInProgressAttempt(attemptId);
        // Allow submission slightly past the deadline (grace window) but never reject a
        // student trying to submit on time due to network lag.
        LocalDateTime deadline = effectiveDeadline(attempt).plusSeconds(SUBMIT_GRACE_SECONDS);
        LocalDateTime now = LocalDateTime.now();
        attempt.setSubmittedTime(now.isBefore(deadline) ? now : deadline);

        double totalScore = grade(attempt);
        attempt.setScore(totalScore);
        attemptRepository.save(attempt);

        return buildResult(attempt);
    }

    public AttemptResultResponse getResult(Long attemptId) {
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException("Attempt not found", HttpStatus.NOT_FOUND));
        User student = currentUserProvider.getCurrentUser();
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new ApiException("Not your attempt", HttpStatus.FORBIDDEN);
        }
        if (attempt.getSubmittedTime() == null) {
            throw new ApiException("Test has not been submitted yet", HttpStatus.BAD_REQUEST);
        }
        return buildResult(attempt);
    }

    public List<AttemptSummaryForTeacher> resultsForTest(Long testId) {
        Test test = testService.getOwnedTest(testId); // ownership check
        return attemptRepository.findByTest(test).stream()
                .map(a -> AttemptSummaryForTeacher.builder()
                        .attemptId(a.getId())
                        .studentId(a.getStudent().getId())
                        .studentName(a.getStudent().getName())
                        .startTime(a.getStartTime())
                        .submittedTime(a.getSubmittedTime())
                        .score(a.getScore())
                        .build())
                .collect(Collectors.toList());
    }

    // --- helpers ---

    private TestAttempt getOwnedInProgressAttempt(Long attemptId) {
        User student = currentUserProvider.getCurrentUser();
        TestAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ApiException("Attempt not found", HttpStatus.NOT_FOUND));
        if (!attempt.getStudent().getId().equals(student.getId())) {
            throw new ApiException("Not your attempt", HttpStatus.FORBIDDEN);
        }
        if (attempt.getSubmittedTime() != null) {
            throw new ApiException("Attempt already submitted", HttpStatus.CONFLICT);
        }
        return attempt;
    }

    private LocalDateTime effectiveDeadline(TestAttempt attempt) {
        LocalDateTime byDuration = attempt.getStartTime().plusMinutes(attempt.getTest().getDurationMinutes());
        LocalDateTime testEnd = attempt.getTest().getEndTime();
        return byDuration.isBefore(testEnd) ? byDuration : testEnd;
    }

    private void assertWithinDeadline(TestAttempt attempt) {
        LocalDateTime deadline = effectiveDeadline(attempt).plusSeconds(SUBMIT_GRACE_SECONDS);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new ApiException("Time is up for this test", HttpStatus.FORBIDDEN);
        }
    }

    private double grade(TestAttempt attempt) {
        List<Answer> answers = answerRepository.findByAttempt(attempt);
        Map<Long, Answer> byQuestion = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        double total = 0;
        for (Question q : attempt.getTest().getQuestions()) {
            Answer a = byQuestion.get(q.getId());
            if (a != null && a.getSelectedOption() != null && a.getSelectedOption().isCorrect()) {
                total += q.getMarks();
            }
        }
        return total;
    }

    private AttemptResultResponse buildResult(TestAttempt attempt) {
        Test test = attempt.getTest();
        List<Answer> answers = answerRepository.findByAttempt(attempt);
        Map<Long, Answer> byQuestion = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a));

        double maxScore = test.getQuestions().stream().mapToInt(Question::getMarks).sum();

        List<QuestionResultView> questionViews = new ArrayList<>();
        Map<String, TagScore> tagWise = new TreeMap<>();

        if (test.isAllowReview()) {
            for (Question q : test.getQuestions()) {
                Answer a = byQuestion.get(q.getId());
                Long selectedId = (a != null && a.getSelectedOption() != null) ? a.getSelectedOption().getId() : null;
                QuestionOption correctOption = q.getOptions().stream()
                        .filter(QuestionOption::isCorrect).findFirst().orElse(null);
                boolean isCorrect = correctOption != null && correctOption.getId().equals(selectedId);

                List<String> tagNames = q.getTags().stream().map(Tag::getName).sorted().collect(Collectors.toList());

                questionViews.add(QuestionResultView.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .questionImageUrl(q.getQuestionImageUrl())
                        .marks(q.getMarks())
                        .marksAwarded(isCorrect ? q.getMarks() : 0)
                        .selectedOptionId(selectedId)
                        .correctOptionId(correctOption != null ? correctOption.getId() : null)
                        .correct(isCorrect)
                        .options(q.getOptions().stream()
                                .map(o -> OptionTeacherView.builder()
                                        .id(o.getId())
                                        .optionText(o.getOptionText())
                                        .optionImageUrl(o.getOptionImageUrl())
                                        .correct(o.isCorrect())
                                        .build())
                                .collect(Collectors.toList()))
                        .tags(tagNames) // tags revealed here, in the post-submit result
                        .build());

                // Aggregate topic-wise score for any tag(s) this question carries.
                for (String tag : tagNames) {
                    TagScore existing = tagWise.getOrDefault(tag, TagScore.builder().correct(0).total(0).build());
                    tagWise.put(tag, TagScore.builder()
                            .correct(existing.getCorrect() + (isCorrect ? 1 : 0))
                            .total(existing.getTotal() + 1)
                            .build());
                }
            }
        }

        return AttemptResultResponse.builder()
                .attemptId(attempt.getId())
                .testId(test.getId())
                .testTitle(test.getTitle())
                .submittedTime(attempt.getSubmittedTime())
                .score(attempt.getScore() == null ? 0 : attempt.getScore())
                .maxScore(maxScore)
                .questions(questionViews)
                .tagWiseScore(tagWise)
                .build();
    }

    private QuestionAttemptView toAttemptView(Question q) {
        return QuestionAttemptView.builder()
                .id(q.getId())
                .questionText(q.getQuestionText())
                .questionImageUrl(q.getQuestionImageUrl())
                .marks(q.getMarks())
                .options(q.getOptions().stream()
                        .map(o -> OptionAttemptView.builder()
                                .id(o.getId())
                                .optionText(o.getOptionText())
                                .optionImageUrl(o.getOptionImageUrl())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        // Note: no `correct` flag and no `tags` are ever included in this view --
        // that's what keeps the answer key and topic tags hidden during the attempt.
    }
}
