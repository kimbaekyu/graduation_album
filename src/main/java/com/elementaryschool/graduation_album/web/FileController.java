package com.elementaryschool.graduation_album.web;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.domain.Teacher;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
import com.elementaryschool.graduation_album.web.admin.AdminGuard;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {

    private static final String BASE_PATH =
            "/volume1/docker/elementary_album/data/";

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    /* =====================================================
     * 📸 사진 조회
     * ===================================================== */

    @GetMapping("/students/{id}/personal-photo")
    public ResponseEntity<Resource> studentPhoto(@PathVariable Long id) {
        return servePhoto(
                studentRepository.findById(id).map(Student::getPersonalPhoto)
        );
    }

    @GetMapping("/students/{id}/hand-letter")
    public ResponseEntity<Resource> studentHandLetter(@PathVariable Long id) {
        return servePhoto(
                studentRepository.findById(id).map(Student::getHandLetterPhoto)
        );
    }

    @GetMapping("/teachers/{id}/photo")
    public ResponseEntity<Resource> teacherPhoto(@PathVariable Long id) {
        return servePhoto(
                teacherRepository.findById(id).map(Teacher::getTeacherPhoto)
        );
    }

    @GetMapping("/classrooms/{id}/group-photo")
    public ResponseEntity<Resource> classroomGroupPhoto(@PathVariable Long id) {
        return servePhoto(
                classroomRepository.findById(id).map(Classroom::getClassGroupPhoto)
        );
    }

    /* =====================================================
     * 🎥 영상 조회 (Range 지원)
     * ===================================================== */

    @GetMapping("/teachers/{id}/video")
    public ResponseEntity<Resource> teacherVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        return serveVideo(
                teacherRepository.findById(id).map(Teacher::getTeacherVideoLetter),
                range
        );
    }

    @GetMapping("/classrooms/{id}/student-video")
    public ResponseEntity<Resource> studentVideo(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        return serveVideo(
                classroomRepository.findById(id).map(Classroom::getStudentVideoLetter),
                range
        );
    }

    /* =====================================================
     * ⬆️ 업로드 (관리자)
     * ===================================================== */

    @PostMapping("/students/{id}/personal-photo")
    @ResponseBody
    public ResponseEntity<?> uploadStudentPhoto(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            HttpSession session
    ) throws IOException {
        AdminGuard.requireAdmin(session);

        Student student = studentRepository.findById(id).orElseThrow();
        String path = saveFile(file, "Photos/students", id + ".jpg");
        student.setPersonalPhoto(path);
        studentRepository.save(student);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/students/{id}/hand-letter")
    @ResponseBody
    public ResponseEntity<?> uploadHandLetter(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            HttpSession session
    ) throws IOException {
        AdminGuard.requireAdmin(session);

        Student student = studentRepository.findById(id).orElseThrow();
        String path = saveFile(file, "Photos/students", id + "_hand.jpg");
        student.setHandLetterPhoto(path);
        studentRepository.save(student);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/teachers/{id}/photo")
    @ResponseBody
    public ResponseEntity<?> uploadTeacherPhoto(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            HttpSession session
    ) throws IOException {
        AdminGuard.requireAdmin(session);

        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        String path = saveFile(file, "Photos/teachers", id + ".jpg");
        teacher.setTeacherPhoto(path);
        teacherRepository.save(teacher);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/teachers/{id}/video")
    @ResponseBody
    public ResponseEntity<?> uploadTeacherVideo(
            @PathVariable Long id,
            @RequestParam MultipartFile file,
            HttpSession session
    ) throws IOException {
        AdminGuard.requireAdmin(session);

        Teacher teacher = teacherRepository.findById(id).orElseThrow();
        String path = saveFile(file, "Videos/teachers", id + ".mp4");
        teacher.setTeacherVideoLetter(path);
        teacherRepository.save(teacher);

        return ResponseEntity.ok().build();
    }

    /* =====================================================
     * 🔧 공통 메서드
     * ===================================================== */

    private ResponseEntity<Resource> servePhoto(Optional<String> pathOpt) {
        if (pathOpt.isEmpty()) return ResponseEntity.notFound().build();

        File file = new File(BASE_PATH + pathOpt.get());
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    private ResponseEntity<Resource> serveVideo(Optional<String> pathOpt, String range)
    {
        if (pathOpt.isEmpty()) return ResponseEntity.notFound().build();

        File file = new File(BASE_PATH + pathOpt.get());
        if (!file.exists()) return ResponseEntity.notFound().build();

        try {
            long fileLength = file.length();
            long start = 0;
            long end = fileLength - 1;

            if (range != null && range.startsWith("bytes=")) {
                String[] parts = range.replace("bytes=", "").split("-");
                start = Long.parseLong(parts[0]);
                if (parts.length > 1 && !parts[1].isEmpty()) {
                    end = Long.parseLong(parts[1]);
                }
            }

            Resource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.valueOf("video/mp4"));
            headers.set("Accept-Ranges", "bytes");
            headers.set("Content-Range",
                    "bytes " + start + "-" + end + "/" + fileLength);
            headers.setContentLength(end - start + 1);

            return new ResponseEntity<>(resource, headers, HttpStatus.PARTIAL_CONTENT);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String saveFile(MultipartFile file, String dir, String filename)
            throws IOException {

        File targetDir = new File(BASE_PATH + dir);
        if (!targetDir.exists()) {
            Files.createDirectories(targetDir.toPath());
        }

        File target = new File(targetDir, filename);
        file.transferTo(target);

        return dir + "/" + filename; // DB에 저장될 상대경로
    }
}
