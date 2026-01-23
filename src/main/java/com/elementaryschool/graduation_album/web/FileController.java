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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;

    // 학생 개인 사진 조회
    @GetMapping("/api/files/students/{id}/personal-photo")
    public ResponseEntity<byte[]> getStudentPersonalPhoto(@PathVariable Long id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty() || studentOpt.get().getPersonalPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(student.getPersonalPhoto());
    }

    // 학생 손편지 사진 조회
    @GetMapping("/api/files/students/{id}/hand-letter-photo")
    public ResponseEntity<byte[]> getStudentHandLetterPhoto(@PathVariable Long id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty() || studentOpt.get().getHandLetterPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(student.getHandLetterPhoto());
    }

    // 선생님 사진 조회
    @GetMapping("/api/files/teachers/{id}/photo")
    public ResponseEntity<byte[]> getTeacherPhoto(@PathVariable Long id) {
        Optional<Teacher> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty() || teacherOpt.get().getTeacherPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = teacherOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(teacher.getTeacherPhoto());
    }

    // 반 단체 사진 조회
    @GetMapping("/api/files/classrooms/{id}/group-photo")
    public ResponseEntity<byte[]> getClassroomGroupPhoto(@PathVariable Long id) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty() || classroomOpt.get().getClassGroupPhoto() == null) {
            return ResponseEntity.notFound().build();
        }
        Classroom classroom = classroomOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(classroom.getClassGroupPhoto());
    }

    // 반 별 담임선생님 영상편지 조회 (시킹 가능)
    @GetMapping("/api/files/classrooms/{id}/video-letter")
    public ResponseEntity<byte[]> getClassroomVideoLetter(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty() || classroomOpt.get().getClassVideoLetter() == null) {
            return ResponseEntity.notFound().build();
        }

        Classroom classroom = classroomOpt.get();
        byte[] videoBytes = classroom.getClassVideoLetter();
        long videoLength = videoBytes.length;

        long start = 0;
        long end = videoLength - 1;

        // ⭐ Range 요청 처리
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        }

        long contentLength = end - start + 1;
        byte[] data = Arrays.copyOfRange(videoBytes, (int) start, (int) end + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        headers.set("Accept-Ranges", "bytes");
        headers.set("Content-Range",
                "bytes " + start + "-" + end + "/" + videoLength);
        headers.setContentLength(contentLength);
        headers.setContentDispositionFormData("attachment", "video-letter.mp4");

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT) // ⭐ 핵심
                .headers(headers)
                .body(data);
    }


    // 반 별 학생 영상편지 조회 (시킹 가능)
    @GetMapping("/api/files/classrooms/{id}/student-video-letter")
    public ResponseEntity<byte[]> getStudentVideoLetter(
            @PathVariable Long id,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty() || classroomOpt.get().getStudentVideoLetter() == null) {
            return ResponseEntity.notFound().build();
        }

        Classroom classroom = classroomOpt.get();
        byte[] videoBytes = classroom.getStudentVideoLetter();
        long videoLength = videoBytes.length;

        long start = 0;
        long end = videoLength - 1;

        // ⭐ Range 처리
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.replace("bytes=", "").split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                end = Long.parseLong(ranges[1]);
            }
        }

        long contentLength = end - start + 1;
        byte[] data = Arrays.copyOfRange(videoBytes, (int) start, (int) end + 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        headers.set("Accept-Ranges", "bytes");
        headers.set("Content-Range",
                "bytes " + start + "-" + end + "/" + videoLength);
        headers.setContentLength(contentLength);
        headers.setContentDispositionFormData("attachment", "student-video-letter.mp4");

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT) // ⭐ 핵심
                .headers(headers)
                .body(data);
    }

    // 학생 개인 사진 업로드 (관리자용)
    @PostMapping("/api/files/students/{id}/personal-photo")
    @ResponseBody
    public ResponseEntity<String> uploadStudentPersonalPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Student> studentOpt = studentRepository.findById(id);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Student student = studentOpt.get();
            student.setPersonalPhoto(file.getBytes());
            studentRepository.save(student);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 학생 손편지 사진 업로드 (관리자용)
    @PostMapping("/api/files/students/{id}/hand-letter-photo")
    @ResponseBody
    public ResponseEntity<String> uploadStudentHandLetterPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Student> studentOpt = studentRepository.findById(id);
            if (studentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Student student = studentOpt.get();
            student.setHandLetterPhoto(file.getBytes());
            studentRepository.save(student);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 선생님 사진 업로드 (관리자용)
    @PostMapping("/api/files/teachers/{id}/photo")
    @ResponseBody
    public ResponseEntity<String> uploadTeacherPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Teacher> teacherOpt = teacherRepository.findById(id);
            if (teacherOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Teacher teacher = teacherOpt.get();
            teacher.setTeacherPhoto(file.getBytes());
            teacherRepository.save(teacher);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 반 단체 사진 업로드 (관리자용)
    @PostMapping("/api/files/classrooms/{id}/group-photo")
    @ResponseBody
    public ResponseEntity<String> uploadClassroomGroupPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(id);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Classroom classroom = classroomOpt.get();
            classroom.setClassGroupPhoto(file.getBytes());
            classroomRepository.save(classroom);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 선생님 영상편지 업로드 (관리자용)
    @PostMapping("/api/files/classrooms/{id}/video-letter")
    @ResponseBody
    public ResponseEntity<String> uploadClassroomVideoLetter(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(id);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Classroom classroom = classroomOpt.get();
            classroom.setClassVideoLetter(file.getBytes());
            classroomRepository.save(classroom);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }
    // 반 별 학생 영상편지 업로드 (관리자용)
    @PostMapping("/api/files/classrooms/{id}/student-video-letter")
    @ResponseBody
    public ResponseEntity<String> uploadStudentVideoLetter(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Optional<Classroom> classroomOpt = classroomRepository.findById(id);
            if (classroomOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Classroom classroom = classroomOpt.get();
            classroom.setStudentVideoLetter(file.getBytes());
            classroomRepository.save(classroom);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }


}

