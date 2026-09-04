package com.testly.repository;

import com.testly.entity.Answer;
import com.testly.entity.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByAttempt(TestAttempt attempt);
    Optional<Answer> findByAttemptAndQuestion_Id(TestAttempt attempt, Long questionId);
}
