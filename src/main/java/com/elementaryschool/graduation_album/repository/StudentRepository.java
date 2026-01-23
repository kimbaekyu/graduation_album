package com.elementaryschool.graduation_album.repository;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    
    @Override
    @EntityGraph(attributePaths = {"classroom"})
    List<Student> findAll();
    
    @Override
    @EntityGraph(attributePaths = {"classroom"})
    Optional<Student> findById(Long id);
    
    @EntityGraph(attributePaths = {"classroom"})
    List<Student> findAllByClassroomOrderByNameAsc(Classroom classroom);

}


