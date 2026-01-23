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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin/teachers")
public class AdminTeacherController {

    private static final String PHOTO_DIR =
            "/volume1/docker/elementary_album/data/Photos/teachers";
    private static final String VIDEO_DIR =
            "/volume1/docker/elementary_album/data/Videos/teachers";

    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    public AdminTeacherController(TeacherRepository teacherRepository,
                                  ClassroomRepository classroomRepository) {
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
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
                         @RequestParam(required = false) MultipartFile teacherVideoLetterFile,
                         Model model) {

        AdminGuard.requireAdmin(session);

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        teacher.setClassroom(classroom);

        if (bindingResult.hasErrors()) {
            model.addAttribute("classrooms", classroomRepository.findAll());
            return "admin/teacher-form";
        }

        try {
            saveFiles(teacher, teacherPhotoFile, teacherVideoLetterFile);
        } catch (IOException e) {
            model.addAttribute("error", "파일 저장 실패");
            return "admin/teacher-form";
        }

        teacherRepository.save(teacher);
        return "redirect:/admin/teachers";
    }

    /* ===================== 수정 ===================== */

    @GetMapping("/{id}/edit")
    public String editForm(HttpSession session, @PathVariable Long id, Model model) {
        AdminGuard.requireAdmin(session);
        model.addAttribute("teacher", teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
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
                       @RequestParam(required = false) MultipartFile teacherVideoLetterFile,
                       Model model) {

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

        try {
            saveFiles(teacher, teacherPhotoFile, teacherVideoLetterFile);
        } catch (IOException e) {
            model.addAttribute("error", "파일 저장 실패");
            return "admin/teacher-form";
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

    /* ===================== 파일 저장 공통 ===================== */

    private void saveFiles(Teacher teacher,
                           MultipartFile photo,
                           MultipartFile video) throws IOException {

        if (photo != null && !photo.isEmpty()) {
            Files.createDirectories(Paths.get(PHOTO_DIR));
            String filename = UUID.randomUUID() + "_" + photo.getOriginalFilename();
            Path path = Paths.get(PHOTO_DIR, filename);
            photo.transferTo(path);
            teacher.setTeacherPhotoUrl(path.toString());
        }

        if (video != null && !video.isEmpty()) {
            Files.createDirectories(Paths.get(VIDEO_DIR));
            String filename = UUID.randomUUID() + "_" + video.getOriginalFilename();
            Path path = Paths.get(VIDEO_DIR, filename);
            video.transferTo(path);
            teacher.setTeacherVideoLetterUrl(path.toString());
        }
    }
}
