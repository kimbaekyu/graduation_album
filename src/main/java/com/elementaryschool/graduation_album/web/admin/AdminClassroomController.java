package com.elementaryschool.graduation_album.web.admin;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.storage.FileStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Controller
@RequestMapping("/admin/classes")
public class AdminClassroomController {

    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService;

    public AdminClassroomController(ClassroomRepository classroomRepository,
                                    FileStorageService fileStorageService) {
        this.classroomRepository = classroomRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum()))
                .toList());
        return "admin/classes";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("classroom", new Classroom());
        return "admin/class-form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @Valid @ModelAttribute("classroom") Classroom classroom,
                         BindingResult bindingResult,
                         @RequestParam(value = "groupPhotoFile", required = false) MultipartFile groupPhotoFile,
                         @RequestParam(value = "videoLetterFile", required = false) MultipartFile videoLetterFile,
                         Model model) {
        AdminGuard.requireAdmin(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력 오류가 있습니다.");
            return "admin/class-form";
        }
        classroomRepository.save(classroom);
        try {
            if (groupPhotoFile != null && !groupPhotoFile.isEmpty()) {
                String path = fileStorageService.save(groupPhotoFile, "Photos/classrooms/group", classroom.getId() + "_group");
                classroom.setClassGroupPhotoUrl(path);
                classroom.setClassGroupPhoto(null);
            }
            if (videoLetterFile != null && !videoLetterFile.isEmpty()) {
                String path = fileStorageService.save(videoLetterFile, "Videos/classrooms", classroom.getId() + "_video");
                classroom.setClassVideoLetterUrl(path);
                classroom.setClassVideoLetter(null);
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            return "admin/class-form";
        }
        classroomRepository.save(classroom);
        return "redirect:/admin/classes";
    }

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);
        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("classroom", classroom);
        return "admin/class-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(HttpSession session,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("classroom") Classroom form,
                       BindingResult bindingResult,
                       @RequestParam(value = "groupPhotoFile", required = false) MultipartFile groupPhotoFile,
                       @RequestParam(value = "videoLetterFile", required = false) MultipartFile videoLetterFile,
                       Model model) {
        AdminGuard.requireAdmin(session);
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "입력 오류가 있습니다.");
            return "admin/class-form";
        }

        Classroom classroom = classroomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        classroom.setClassNum(form.getClassNum());
        classroom.setHomeroomTeacher(form.getHomeroomTeacher());
        classroom.setClassGroupPhotoUrl(form.getClassGroupPhotoUrl());
        classroom.setClassVideoLetterUrl(form.getClassVideoLetterUrl());
        
        try {
            if (groupPhotoFile != null && !groupPhotoFile.isEmpty()) {
                String path = fileStorageService.save(groupPhotoFile, "Photos/classrooms/group", classroom.getId() + "_group");
                classroom.setClassGroupPhotoUrl(path);
                classroom.setClassGroupPhoto(null);
            }
            if (videoLetterFile != null && !videoLetterFile.isEmpty()) {
                String path = fileStorageService.save(videoLetterFile, "Videos/classrooms", classroom.getId() + "_video");
                classroom.setClassVideoLetterUrl(path);
                classroom.setClassVideoLetter(null);
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            return "admin/class-form";
        }
        
        classroomRepository.save(classroom);
        return "redirect:/admin/classes";
    }

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable Long id) {
        AdminGuard.requireAdmin(session);
        classroomRepository.deleteById(id);
        return "redirect:/admin/classes";
    }
}


