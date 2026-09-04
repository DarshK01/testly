package com.testly.service;

import com.testly.dto.QuestionDtos.*;
import com.testly.entity.Question;
import com.testly.entity.QuestionOption;
import com.testly.entity.Tag;
import com.testly.entity.Test;
import com.testly.exception.ApiException;
import com.testly.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final FileStorageService fileStorageService;
    private final TagService tagService;
    private final TestService testService;

    public QuestionTeacherView addQuestion(Long testId, CreateQuestionRequest request, MultipartFile image) {
        Test test = testService.getOwnedTest(testId);

        if (test.isPublished()) {
            throw new ApiException("Cannot modify questions after the test is published", HttpStatus.BAD_REQUEST);
        }

        boolean hasText = request.getQuestionText() != null && !request.getQuestionText().isBlank();
        boolean hasImage = image != null && !image.isEmpty();
        if (!hasText && !hasImage) {
            throw new ApiException("Question needs text and/or an image", HttpStatus.BAD_REQUEST);
        }

        if (request.getOptions() == null || request.getOptions().size() != 4) {
            throw new ApiException("Exactly 4 options are required", HttpStatus.BAD_REQUEST);
        }
        long correctCount = request.getOptions().stream().filter(OptionInput::isCorrect).count();
        if (correctCount != 1) {
            throw new ApiException("Exactly one option must be marked correct", HttpStatus.BAD_REQUEST);
        }

        String imageUrl = hasImage ? fileStorageService.store(image) : null;

        // Tags are entirely optional -- resolveOrCreate returns an empty set for null/empty input.
        Set<Tag> tags = tagService.resolveOrCreate(request.getTagNames());

        Question question = Question.builder()
                .test(test)
                .questionText(request.getQuestionText())
                .questionImageUrl(imageUrl)
                .marks(request.getMarks() == null ? 1 : request.getMarks())
                .tags(tags)
                .build();

        List<QuestionOption> options = request.getOptions().stream()
                .map(o -> QuestionOption.builder()
                        .question(question)
                        .optionText(o.getOptionText())
                        .correct(o.isCorrect())
                        .build())
                .collect(Collectors.toList());
        question.setOptions(options);

        Question saved = questionRepository.save(question);
        return toTeacherView(saved);
    }

    public void deleteQuestion(Long testId, Long questionId) {
        Test test = testService.getOwnedTest(testId);
        if (test.isPublished()) {
            throw new ApiException("Cannot modify questions after the test is published", HttpStatus.BAD_REQUEST);
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ApiException("Question not found", HttpStatus.NOT_FOUND));
        if (!question.getTest().getId().equals(test.getId())) {
            throw new ApiException("Question does not belong to this test", HttpStatus.BAD_REQUEST);
        }
        questionRepository.delete(question);
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
