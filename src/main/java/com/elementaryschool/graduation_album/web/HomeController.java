package com.elementaryschool.graduation_album.web;

import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ClassroomRepository classroomRepository;

    public HomeController(ClassroomRepository classroomRepository) {
        this.classroomRepository = classroomRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum()))
                .toList());
        return "home";
    }
}


