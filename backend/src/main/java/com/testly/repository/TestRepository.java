package com.testly.repository;

import com.testly.entity.Classroom;
import com.testly.entity.Test;
import com.testly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByTeacher(User teacher);
    List<Test> findByPublishedTrueAndStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);

    // Open (classroom IS NULL) tests, plus tests restricted to one of the student's joined classrooms.
    @Query("SELECT t FROM Test t WHERE t.published = true AND t.startTime <= :now AND t.endTime >= :now " +
           "AND (t.classroom IS NULL OR t.classroom IN :classrooms)")
    List<Test> findAvailableForStudent(@Param("now") LocalDateTime now, @Param("classrooms") List<Classroom> classrooms);
}
