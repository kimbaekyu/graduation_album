package com.elementaryschool.graduation_album.web.admin;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/admin/students")
public class AdminStudentController {

    private static final String PHOTO_DIR =
            "/volume1/docker/elementary_album/data/Photos/students";
    private static final String HANDLETTER_DIR =
            "/volume1/docker/elementary_album/data/Photos/handletters";

    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;

    public AdminStudentController(StudentRepository studentRepository,
                                  ClassroomRepository classroomRepository) {
        this.studentRepository = studentRepository;
        this.classroomRepository = classroomRepository;
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
                         Model model) {

        AdminGuard.requireAdmin(session);

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
            saveFiles(student, personalPhotoFile, handLetterPhotoFile);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "파일 저장 실패", e);
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

        Files.createDirectories(Paths.get(PHOTO_DIR));
        Files.createDirectories(Paths.get(HANDLETTER_DIR));

        if (personalPhotoFile != null && !personalPhotoFile.isEmpty()) {
            Path path = Paths.get(PHOTO_DIR, student.getId() + ".jpg");
            Files.write(path, personalPhotoFile.getBytes());
            student.setPersonalPhoto(path.toString());
        }

        if (handLetterPhotoFile != null && !handLetterPhotoFile.isEmpty()) {
            Path path = Paths.get(HANDLETTER_DIR, student.getId() + ".jpg");
            Files.write(path, handLetterPhotoFile.getBytes());
            student.setHandLetterPhoto(path.toString());
        }
    }
}
