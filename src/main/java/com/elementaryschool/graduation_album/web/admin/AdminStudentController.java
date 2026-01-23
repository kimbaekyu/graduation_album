package com.elementaryschool.graduation_album.web.admin;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
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
@RequestMapping("/admin/students")
public class AdminStudentController {

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService;

    public AdminStudentController(StudentRepository studentRepository,
                                  ClassroomRepository classroomRepository,
                                  FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("students", studentRepository.findAll());
        return "admin/students";
    }

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("student", new Student());
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
        return "admin/student-form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         @RequestParam("classroomId") Long classroomId,
                         @RequestParam(value = "personalPhotoFile", required = false) MultipartFile personalPhotoFile,
                         @RequestParam(value = "handLetterPhotoFile", required = false) MultipartFile handLetterPhotoFile,
                         @RequestParam(value = "studentVideoLetterFile", required = false) MultipartFile studentVideoLetterFile,
                         Model model) {
        AdminGuard.requireAdmin(session);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 반입니다."));
        student.setClassroom(classroom);
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/student-form";
        }
        // 먼저 저장해서 ID를 확보한다.
        studentRepository.save(student);
        try {
            if (personalPhotoFile != null && !personalPhotoFile.isEmpty()) {
                String path = fileStorageService.save(personalPhotoFile, "Photos/students/personal", student.getId() + "_personal");
                student.setPersonalPhotoUrl(path);
                student.setPersonalPhoto(null);
            }
            if (handLetterPhotoFile != null && !handLetterPhotoFile.isEmpty()) {
                String path = fileStorageService.save(handLetterPhotoFile, "Photos/students/hand-letter", student.getId() + "_hand");
                student.setHandLetterPhotoUrl(path);
                student.setHandLetterPhoto(null);
            }
            if (studentVideoLetterFile != null && !studentVideoLetterFile.isEmpty()) {
                String path = fileStorageService.save(studentVideoLetterFile, "Videos/students", student.getId() + "_video");
                student.setStudentVideoLetterUrl(path);
                student.setStudentVideoLetter(null);
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/student-form";
        }
        studentRepository.save(student);
        return "redirect:/admin/students";
    }

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("student", student);
        model.addAttribute("classrooms", classroomRepository.findAll()
                .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
        return "admin/student-form";
    }

    @PostMapping("/{id}/edit")
    public String edit(HttpSession session,
                       @PathVariable Long id,
                       @Valid @ModelAttribute("student") Student form,
                       BindingResult bindingResult,
                       @RequestParam("classroomId") Long classroomId,
                       @RequestParam(value = "personalPhotoFile", required = false) MultipartFile personalPhotoFile,
                       @RequestParam(value = "handLetterPhotoFile", required = false) MultipartFile handLetterPhotoFile,
                       @RequestParam(value = "studentVideoLetterFile", required = false) MultipartFile studentVideoLetterFile,
                       Model model) {
        AdminGuard.requireAdmin(session);
        
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "존재하지 않는 반입니다."));
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/student-form";
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        student.setClassroom(classroom);
        student.setName(form.getName());
        student.setMotto(form.getMotto());
        student.setTalk(form.getTalk());
        student.setPersonalPhotoUrl(form.getPersonalPhotoUrl());
        student.setHandLetterPhotoUrl(form.getHandLetterPhotoUrl());
        student.setStudentVideoLetterUrl(form.getStudentVideoLetterUrl());
        student.setGender(form.getGender());
        
        try {
            if (personalPhotoFile != null && !personalPhotoFile.isEmpty()) {
                String path = fileStorageService.save(personalPhotoFile, "Photos/students/personal", student.getId() + "_personal");
                student.setPersonalPhotoUrl(path);
                student.setPersonalPhoto(null);
            }
            if (handLetterPhotoFile != null && !handLetterPhotoFile.isEmpty()) {
                String path = fileStorageService.save(handLetterPhotoFile, "Photos/students/hand-letter", student.getId() + "_hand");
                student.setHandLetterPhotoUrl(path);
                student.setHandLetterPhoto(null);
            }
            if (studentVideoLetterFile != null && !studentVideoLetterFile.isEmpty()) {
                String path = fileStorageService.save(studentVideoLetterFile, "Videos/students", student.getId() + "_video");
                student.setStudentVideoLetterUrl(path);
                student.setStudentVideoLetter(null);
            }
        } catch (IOException e) {
            model.addAttribute("error", "파일 업로드 실패: " + e.getMessage());
            model.addAttribute("classrooms", classroomRepository.findAll()
                    .stream().sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum())).toList());
            return "admin/student-form";
        }
        
        studentRepository.save(student);
        return "redirect:/admin/students";
    }

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable Long id) {
        AdminGuard.requireAdmin(session);
        studentRepository.deleteById(id);
        return "redirect:/admin/students";
    }
}


