package com.elementaryschool.graduation_album.web;


import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.domain.Teacher;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/media")
public class MediaController {

    @Value("${app.media.base-path}")
    private String basePath;

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    // ✅ 생성자 주입
    public MediaController(StudentRepository studentRepository,
                           TeacherRepository teacherRepository,
                           ClassroomRepository classroomRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.classroomRepository = classroomRepository;
    }

    /* ================= 공통 ================= */
    private ResponseEntity<Resource> serve(Path path, MediaType mediaType) {
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.noCache())
                .body(new FileSystemResource(path));
    }

    /* ================= 사진 ================= */

    // 1️⃣ 선생님 개인 사진
    @GetMapping("/photo/teachers/{teacherId}")
    public ResponseEntity<Resource> teacherPhoto(@PathVariable Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (teacher.getTeacherPhoto() == null || teacher.getTeacherPhoto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(basePath, teacher.getTeacherPhoto());
        return serve(path, MediaType.IMAGE_JPEG);
    }

    //학생 개인 사진
    @GetMapping("/photo/students/{classNum}/{studentId}")
    public ResponseEntity<Resource> studentPhoto(
            @PathVariable Integer classNum,
            @PathVariable Long studentId) {

            // DB에서 학생 정보 조회
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            // 사진 경로가 없으면 404
            if (student.getPersonalPhoto() == null || student.getPersonalPhoto().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            // DB에 저장된 경로를 그대로 사용
            Path path = Paths.get(basePath, student.getPersonalPhoto());
            return serve(path, MediaType.IMAGE_JPEG);
    }

    //학생 개인 손글씨 사진
    @GetMapping("/photo/students/hand-letter/{classNum}/{studentId}")
    public ResponseEntity<Resource> studentHandLetter(
            @PathVariable Integer classNum,
            @PathVariable Long studentId) {

            // DB에서 학생 정보 조회
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

            // 사진 경로가 없으면 404
            if (student.getHandLetterPhoto() == null || student.getHandLetterPhoto().isBlank()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }

            Path path = Paths.get(basePath, student.getHandLetterPhoto());
            return serve(path, MediaType.IMAGE_JPEG);
    }

    //반 별 학생 단체 사진
    @GetMapping("/photo/classrooms/{classNum}")
    public ResponseEntity<Resource> classroomPhoto(@PathVariable Integer classNum) {
        Classroom classroom = classroomRepository.findByClassNum(classNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (classroom.getClassGroupPhoto() == null || classroom.getClassGroupPhoto().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(basePath, classroom.getClassGroupPhoto());
        return serve(path, MediaType.IMAGE_JPEG);
    }

    /* ================= 영상 ================= */

    // 선생님 편지 영상
    @GetMapping("/video/teachers/{classNum}")
    public ResponseEntity<Resource> teacherVideo(@PathVariable Integer classNum) {
        Classroom classroom = classroomRepository.findByClassNum(classNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (classroom.getClassVideoLetter() == null || classroom.getClassVideoLetter().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(basePath, classroom.getClassVideoLetter());
        return serve(path, MediaType.valueOf("video/mp4"));
    }

    // 6️⃣ 학생 단체 편지 영상
    @GetMapping("/video/students/{classNum}")
    public ResponseEntity<Resource> classroomVideo(@PathVariable Integer classNum) {
        Classroom classroom = classroomRepository.findByClassNum(classNum)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (classroom.getStudentVideoLetter() == null || classroom.getStudentVideoLetter().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Path path = Paths.get(basePath, classroom.getStudentVideoLetter());
        return serve(path, MediaType.valueOf("video/mp4"));
    }
}
