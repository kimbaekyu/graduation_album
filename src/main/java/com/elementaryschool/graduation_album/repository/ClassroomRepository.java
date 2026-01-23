package com.elementaryschool.graduation_album.repository;

import com.elementaryschool.graduation_album.domain.Classroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Optional<Classroom> findByClassNum(int classNum);
}


