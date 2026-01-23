package com.elementaryschool.graduation_album.repository;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Teacher;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    @Override
    @EntityGraph(attributePaths = {"classroom"})
    java.util.List<Teacher> findAll();
    
    @EntityGraph(attributePaths = {"classroom"})
    Optional<Teacher> findByClassroom(Classroom classroom);
}


