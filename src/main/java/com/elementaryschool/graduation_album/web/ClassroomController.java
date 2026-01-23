package com.elementaryschool.graduation_album.web;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ClassroomController {

    private final ClassroomRepository classroomRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public ClassroomController(ClassroomRepository classroomRepository,
                              TeacherRepository teacherRepository,
                              StudentRepository studentRepository) {
        this.classroomRepository = classroomRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/classes/{classNum}")
    public String classPage(@PathVariable int classNum, Model model) {
        Classroom classroom = classroomRepository.findByClassNum(classNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("classroom", classroom);
        model.addAttribute("teacher", teacherRepository.findByClassroom(classroom).orElse(null));
        model.addAttribute("students", studentRepository.findAllByClassroomOrderByNameAsc(classroom));

        return "classroom";
    }
}


