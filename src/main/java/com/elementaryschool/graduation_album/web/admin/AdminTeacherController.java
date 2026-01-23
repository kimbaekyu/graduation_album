package com.elementaryschool.graduation_album.web.admin;


import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Teacher;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
import com.elementaryschool.graduation_album.service.FileStorageService;
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
@RequestMapping("/admin/teachers")
public class AdminTeacherController {

    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService;

    public AdminTeacherController(TeacherRepository teacherRepository,
                                  ClassroomRepository classroomRepository,
                                  FileStorageService fileStorageService) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
        this.fileStorageService = fileStorageService;
    }

    /* ===================== 목록 ===================== */

    @GetMapping
    public String list(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("teachers", teacherRepository.findAll());
        return "admin/teachers";
    }

    /* ===================== 생성 ===================== */

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("teacher", new Teacher());
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "admin/teacher-form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @Valid @ModelAttribute Teacher teacher,
                         BindingResult bindingResult,
                         @RequestParam Long classroomId,
                         @RequestParam(required = false) MultipartFile teacherPhotoFile,
                         Model model) throws IOException {

        AdminGuard.requireAdmin(session);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        teacher.setClassroom(classroom);

        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "admin/teacher-form";
        }

        // 1️⃣ 먼저 저장해서 ID 확보
        teacherRepository.save(teacher);

        // 2️⃣ 사진 업로드 (있을 때만)
        if (teacherPhotoFile != null && !teacherPhotoFile.isEmpty()) {
            String path = fileStorageService.savePhoto(
                    teacherPhotoFile,
                    "photos/teacher",
                    teacher.getId()
            );
            teacher.setTeacherPhoto(path);
        }

        teacherRepository.save(teacher);
        return "redirect:/admin/teachers";
    }

    /* ===================== 수정 ===================== */

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        model.addAttribute("teacher", teacher);
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "admin/teacher-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(HttpSession session,
                       @PathVariable Long id,
                       @Valid @ModelAttribute Teacher form,
                       BindingResult bindingResult,
                       @RequestParam Long classroomId,
                       @RequestParam(required = false) MultipartFile teacherPhotoFile,
                       Model model) throws IOException {

        AdminGuard.requireAdmin(session);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "admin/teacher-form";
        }

        teacher.setName(form.getName());
        teacher.setGender(form.getGender());
        teacher.setClassroom(classroom);
        teacher.setClassMotto(form.getClassMotto());
        teacher.setTalkTo(form.getTalkTo());
        teacher.setPromise(form.getPromise());

        // 사진 교체
        if (teacherPhotoFile != null && !teacherPhotoFile.isEmpty()) {
            String path = fileStorageService.savePhoto(
                    teacherPhotoFile,
                    "photos/teacher",
                    teacher.getId()
            );
            teacher.setTeacherPhoto(path);
        }

        teacherRepository.save(teacher);
        return "redirect:/admin/teachers";
    }

    /* ===================== 삭제 ===================== */

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable Long id) {
        AdminGuard.requireAdmin(session);
        teacherRepository.deleteById(id);
        return "redirect:/admin/teachers";
    }
}

