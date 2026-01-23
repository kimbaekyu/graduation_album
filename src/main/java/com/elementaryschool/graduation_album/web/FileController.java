package com.elementaryschool.graduation_album.web;

import com.elementaryschool.graduation_album.domain.Classroom;
import com.elementaryschool.graduation_album.domain.Student;
import com.elementaryschool.graduation_album.domain.Teacher;
import com.elementaryschool.graduation_album.repository.ClassroomRepository;
import com.elementaryschool.graduation_album.repository.StudentRepository;
import com.elementaryschool.graduation_album.repository.TeacherRepository;
import com.elementaryschool.graduation_album.storage.FileStorageService;
import com.elementaryschool.graduation_album.web.admin.AdminGuard;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class FileController {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final FileStorageService fileStorageService;

    private ResponseEntity<byte[]> readImageFromPathOrBlob(String path, byte[] blob) {
        try {
            if (path != null && !path.isBlank()) {
                Path filePath = resolveStoragePath(path);
                if (Files.exists(filePath)) {
                    byte[] data = Files.readAllBytes(filePath);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.IMAGE_JPEG);
                    return ResponseEntity.ok().headers(headers).body(data);
                }
            }
        } catch (IOException ignored) {
        }
        if (blob == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(blob);
    }

    private ResponseEntity<byte[]> readBinaryFromPathOrBlob(String path, byte[] blob, String downloadName) {
        try {
            if (path != null && !path.isBlank()) {
                Path filePath = resolveStoragePath(path);
                if (Files.exists(filePath)) {
                    byte[] data = Files.readAllBytes(filePath);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                    headers.setContentDispositionFormData("attachment", downloadName);
                    return ResponseEntity.ok().headers(headers).body(data);
                }
            }
        } catch (IOException ignored) {
        }
        if (blob == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", downloadName);
        return ResponseEntity.ok().headers(headers).body(blob);
    }

    private Path resolveStoragePath(String storedPath) {
        Path base = fileStorageService.getBasePath();
        Path target = base.resolve(storedPath).normalize();
        if (!target.startsWith(base)) {
            throw new IllegalArgumentException("잘못된 파일 경로");
        }
        return target;
    }

    // 학생 개인 사진 조회
    @GetMapping("/api/files/students/{id}/personal-photo")
    public ResponseEntity<byte[]> getStudentPersonalPhoto(@PathVariable Long id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();
        return readImageFromPathOrBlob(student.getPersonalPhotoUrl(), student.getPersonalPhoto());
    }

    // 학생 손편지 사진 조회
    @GetMapping("/api/files/students/{id}/hand-letter-photo")
    public ResponseEntity<byte[]> getStudentHandLetterPhoto(@PathVariable Long id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();
        return readImageFromPathOrBlob(student.getHandLetterPhotoUrl(), student.getHandLetterPhoto());
    }

    // 선생님 사진 조회
    @GetMapping("/api/files/teachers/{id}/photo")
    public ResponseEntity<byte[]> getTeacherPhoto(@PathVariable Long id) {
        Optional<Teacher> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = teacherOpt.get();
        return readImageFromPathOrBlob(teacher.getTeacherPhotoUrl(), teacher.getTeacherPhoto());
    }

    // 반 단체 사진 조회
    @GetMapping("/api/files/classrooms/{id}/group-photo")
    public ResponseEntity<byte[]> getClassroomGroupPhoto(@PathVariable Long id) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Classroom classroom = classroomOpt.get();
        return readImageFromPathOrBlob(classroom.getClassGroupPhotoUrl(), classroom.getClassGroupPhoto());
    }

    // 반 영상편지 조회
    @GetMapping("/api/files/classrooms/{id}/video-letter")
    public ResponseEntity<byte[]> getClassroomVideoLetter(@PathVariable Long id) {
        Optional<Classroom> classroomOpt = classroomRepository.findById(id);
        if (classroomOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Classroom classroom = classroomOpt.get();
        return readBinaryFromPathOrBlob(classroom.getClassVideoLetterUrl(), classroom.getClassVideoLetter(), "video-letter.mp4");
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
            Student student = studentRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Photos/students/personal", id + "_personal");
            student.setPersonalPhotoUrl(path);
            student.setPersonalPhoto(null);
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
            Student student = studentRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Photos/students/hand-letter", id + "_hand");
            student.setHandLetterPhotoUrl(path);
            student.setHandLetterPhoto(null);
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
            Teacher teacher = teacherRepository.findById(id)
                    .orElseThrow();
            // NAS/로컬 디스크에 저장하고, 경로만 DB에 저장
            String path = fileStorageService.save(file, "Photos/teachers", id + "_photo");
            teacher.setTeacherPhotoUrl(path);
            teacher.setTeacherPhoto(null); // 기존 BLOB 필드는 비워서 메모리 사용 최소화
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
            Classroom classroom = classroomRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Photos/classrooms/group", id + "_group");
            classroom.setClassGroupPhotoUrl(path);
            classroom.setClassGroupPhoto(null);
            classroomRepository.save(classroom);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 반 영상편지 업로드 (관리자용)
    @PostMapping("/api/files/classrooms/{id}/video-letter")
    @ResponseBody
    public ResponseEntity<String> uploadClassroomVideoLetter(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Classroom classroom = classroomRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Videos/classrooms", id + "_video");
            classroom.setClassVideoLetterUrl(path);
            classroom.setClassVideoLetter(null);
            classroomRepository.save(classroom);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 선생님 영상편지 조회
    @GetMapping("/api/files/teachers/{id}/video-letter")
    public ResponseEntity<byte[]> getTeacherVideoLetter(@PathVariable Long id) {
        Optional<Teacher> teacherOpt = teacherRepository.findById(id);
        if (teacherOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = teacherOpt.get();
        return readBinaryFromPathOrBlob(teacher.getTeacherVideoLetterUrl(), teacher.getTeacherVideoLetter(), "teacher-video-letter.mp4");
    }

    // 학생 영상편지 조회
    @GetMapping("/api/files/students/{id}/video-letter")
    public ResponseEntity<byte[]> getStudentVideoLetter(@PathVariable Long id) {
        Optional<Student> studentOpt = studentRepository.findById(id);
        if (studentOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentOpt.get();
        return readBinaryFromPathOrBlob(student.getStudentVideoLetterUrl(), student.getStudentVideoLetter(), "student-video-letter.mp4");
    }

    // 선생님 영상편지 업로드 (관리자용)
    @PostMapping("/api/files/teachers/{id}/video-letter")
    @ResponseBody
    public ResponseEntity<String> uploadTeacherVideoLetter(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Teacher teacher = teacherRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Videos/teachers", id + "_video");
            teacher.setTeacherVideoLetterUrl(path);
            teacher.setTeacherVideoLetter(null);
            teacherRepository.save(teacher);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }

    // 학생 영상편지 업로드 (관리자용)
    @PostMapping("/api/files/students/{id}/video-letter")
    @ResponseBody
    public ResponseEntity<String> uploadStudentVideoLetter(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        AdminGuard.requireAdmin(session);
        try {
            Student student = studentRepository.findById(id)
                    .orElseThrow();
            String path = fileStorageService.save(file, "Videos/students", id + "_video");
            student.setStudentVideoLetterUrl(path);
            student.setStudentVideoLetter(null);
            studentRepository.save(student);
            return ResponseEntity.ok("파일이 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패: " + e.getMessage());
        }
    }
}

