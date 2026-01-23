package com.elementaryschool.graduation_album.web.admin;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
import com.elementaryschool.graduation_album.service.FileStorageService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin/students")
public class AdminStudentController {

    /**
     * NAS/볼륨 마운트 경로 (컨테이너/호스트 환경에 따라 설정)
     * DB에는 상대경로만 저장한다.
     */
    @Value("${app.media.base-path:/data/}")
    private String basePath;

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService; // ✅ 주입

    // 생성자에서 주입
    public AdminStudentController(StudentRepository studentRepository,
                                  ClassroomRepository classroomRepository,
                                  FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
        this.fileStorageService = fileStorageService;
    }

    /* ================= 목록 ================= */

    @GetMapping
    public String list(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("students", studentRepository.findAll());
        return "admin/students";
    }

    /* ================= 생성 ================= */

    @GetMapping("/new")
    public String createForm(HttpSession session, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("student", new Student());
        model.addAttribute("classrooms",
                classroomRepository.findAll().stream()
                        .sorted((a, b) -> Integer.compare(a.getClassNum(), b.getClassNum()))
                        .toList());
        return "admin/student-form";
    }

    @PostMapping("/new")
    public String create(HttpSession session,
                         @Valid @ModelAttribute("student") Student student,
                         BindingResult bindingResult,
                         @RequestParam("classroomId") Long classroomId,
                         @RequestParam(value = "personalPhotoFile", required = false) MultipartFile personalPhotoFile,
                         @RequestParam(value = "handLetterPhotoFile", required = false) MultipartFile handLetterPhotoFile,
                         Model model) throws IOException {
        if (bindingResult.hasErrors()) return "admin/student-form";

        AdminGuard.requireAdmin(session);

        // 학급 설정
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        student.setClassroom(classroom);

        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "admin/student-form";
        }

        // 먼저 저장해서 ID 확보
        studentRepository.save(student);

        try {
            // ✅ saveFiles에서 UUID 기반 저장으로 수정
            saveFiles(student, personalPhotoFile, handLetterPhotoFile);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }

        studentRepository.save(student);
        return "redirect:/admin/students";
    }

    /* ================= 수정 ================= */

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        model.addAttribute("student", student);
        model.addAttribute("classrooms", classroomRepository.findAll());
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
                       Model model) {

        AdminGuard.requireAdmin(session);

        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "admin/student-form";
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));

        student.setClassroom(classroom);
        student.setName(form.getName());
        student.setMotto(form.getMotto());
        student.setTalk(form.getTalk());
        student.setGender(form.getGender());

        try {
            saveFiles(student, personalPhotoFile, handLetterPhotoFile);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
        }

        studentRepository.save(student);
        return "redirect:/admin/students";
    }

    /* ================= 삭제 ================= */

    @PostMapping("/{id}/delete")
    public String delete(HttpSession session, @PathVariable Long id) {
        AdminGuard.requireAdmin(session);
        studentRepository.deleteById(id);
        return "redirect:/admin/students";
    }

    /* ================= 파일 저장 로직 ================= */

    private void saveFiles(Student student,
                           MultipartFile personalPhotoFile,
                           MultipartFile handLetterPhotoFile) throws IOException {

        String classDir = String.format("%02d", student.getClassroom().getClassNum()); // 01, 02

        // 📸 개인 사진
        if (personalPhotoFile != null && !personalPhotoFile.isEmpty()) {
            String relativePath = fileStorageService.savePhoto(
                    personalPhotoFile,
                    "photos/students/" + classDir // UUID로 파일명 자동 생성
            );
            student.setPersonalPhoto(relativePath);
        }

        // ✍ 손글씨 사진
        if (handLetterPhotoFile != null && !handLetterPhotoFile.isEmpty()) {
            String relativePath = fileStorageService.savePhoto(
                    handLetterPhotoFile,
                    "photos/students/" + classDir // UUID로 파일명 자동 생성
            );
            student.setHandLetterPhoto(relativePath);
        }
    }
}
