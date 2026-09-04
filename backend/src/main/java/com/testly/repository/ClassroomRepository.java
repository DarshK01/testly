package com.testly.repository;

import com.testly.entity.Classroom;
import com.testly.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByJoinCode(String joinCode);
    boolean existsByJoinCode(String joinCode);
    List<Classroom> findByTeacher(User teacher);

    // Classrooms a given student has joined.
    List<Classroom> findByStudents_Id(Long studentId);
}
