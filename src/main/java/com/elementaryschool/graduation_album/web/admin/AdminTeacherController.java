package com.elementaryschool.graduation_album.web.admin;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Teacher;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
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

    public AdminTeacherController(TeacherRepository teacherRepository,
                                  ClassroomRepository classroomRepository) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("teachers", teacherRepository.findAll());
        return "admin/teachers";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("teacher", new Teacher());
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
        return "admin/teacher-form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @Valid @ModelAttribute("teacher") Teacher teacher,
                         BindingResult bindingResult,
                         @RequestParam("classroomId") Long classroomId,
                         @RequestParam(value = "teacherPhotoFile", required = false) MultipartFile teacherPhotoFile,
                         @RequestParam(value = "teacherVideoLetterFile", required = false) MultipartFile teacherVideoLetterFile,
                         Model model) {
        AdminGuard.requireAdmin(session);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 반입니다."));
        teacher.setClassroom(classroom);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/teacher-form";
        }
        try {
            if (teacherPhotoFile != null && !teacherPhotoFile.isEmpty()) {
                teacher.setTeacherPhoto(teacherPhotoFile.getBytes());
            }
            if (teacherVideoLetterFile != null && !teacherVideoLetterFile.isEmpty()) {
                teacher.setTeacherVideoLetter(teacherVideoLetterFile.getBytes());
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/teacher-form";
        }
        teacherRepository.save(teacher);
        return "redirect:/admin/teachers";
    }

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("teacher", teacher);
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
        return "admin/teacher-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(HttpSession session,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("teacher") Teacher form,
                       BindingResult bindingResult,
                       @RequestParam("classroomId") Long classroomId,
                       @RequestParam(value = "teacherPhotoFile", required = false) MultipartFile teacherPhotoFile,
                       @RequestParam(value = "teacherVideoLetterFile", required = false) MultipartFile teacherVideoLetterFile,
                       Model model) {
        AdminGuard.requireAdmin(session);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 반입니다."));
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/teacher-form";
        }

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        teacher.setClassroom(classroom);
        teacher.setName(form.getName());
        teacher.setClassMotto(form.getClassMotto());
        teacher.setTalkTo(form.getTalkTo());
        teacher.setPromise(form.getPromise());
        teacher.setTeacherPhotoUrl(form.getTeacherPhotoUrl());
        teacher.setTeacherVideoLetterUrl(form.getTeacherVideoLetterUrl());
        teacher.setGender(form.getGender());
        
        try {
            if (teacherPhotoFile != null && !teacherPhotoFile.isEmpty()) {
                teacher.setTeacherPhoto(teacherPhotoFile.getBytes());
            }
            if (teacherVideoLetterFile != null && !teacherVideoLetterFile.isEmpty()) {
                teacher.setTeacherVideoLetter(teacherVideoLetterFile.getBytes());
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/teacher-form";
        }
        
        teacherRepository.save(teacher);
        return "redirect:/admin/teachers";
    }

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable Long id) {
        AdminGuard.requireAdmin(session);
        teacherRepository.deleteById(id);
        return "redirect:/admin/teachers";
    }
}


