package com.testly.repository;

import com.testly.entity.Test;
import com.testly.entity.TestAttempt;
import com.testly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {
    Optional<TestAttempt> findByStudentAndTest(User student, Test test);
    List<TestAttempt> findByTest(Test test);
    List<TestAttempt> findByStudent(User student);
}
