package com.elementaryschool.graduation_album.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.media.base-path}")
    private String basePath;

    /** ---------------- 일반 저장 ---------------- */
    public String save(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 디렉토리 생성
        Path dirPath = Paths.get(basePath, dir);
        Files.createDirectories(dirPath);

        // UUID + 원본 확장자
        String ext = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);

        Path filePath = dirPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // DB에는 상대경로 저장
        return dir + "/" + fileName;
    }

    /** 디렉토리 + 파일명 지정 */
    public String save(MultipartFile file, String dir, String filename) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 디렉토리 생성
        Path dirPath = Paths.get(basePath, dir);
        Files.createDirectories(dirPath);

        // UUID + 원래 확장자
        String originalExt = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + "." + originalExt;

        Path filePath = dirPath.resolve(filename);
        file.transferTo(filePath.toFile());

        // DB에는 상대경로 저장
        return dir + "/" + filename;
    }

    /** ---------------- 사진 저장 ---------------- */
    public String savePhoto(MultipartFile file, String dir) throws IOException {
        Path dirPath = Paths.get(basePath, dir);
        Files.createDirectories(dirPath);

        String ext = getFileExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + "." + ext;

        Path filePath = dirPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return dir + "/" + filename;
    }

    /** 사진 저장 (자동 jpg) */
    public String savePhoto(MultipartFile file, String dir, Long id) throws IOException {
        return save(file, dir, id + ".jpg");
    }

    /** 사진 저장 (자동 jpg) */
    public String savePhoto(MultipartFile file, String dir, String filename) throws IOException {
        return save(file, dir, filename);
    }

    /** 영상 저장 (자동 mp4) */
    public String saveVideo(MultipartFile file, String dir, String name) throws IOException {
        return save(file, dir, name + ".mp4");
    }

    /** ---------------- 영상 저장 ---------------- */
    public String saveVideo(MultipartFile file, String dir) throws IOException {
        if (file == null || file.isEmpty()) return null;

        // 디렉토리 생성
        Path dirPath = Paths.get(basePath, dir);
        Files.createDirectories(dirPath);

        // UUID + mp4 확장자 강제
        String fileName = UUID.randomUUID().toString() + ".mp4";

        Path filePath = dirPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        return dir + "/" + fileName;
    }

    /** 파일 로드 */
    public Resource load(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;

        Path path = Paths.get(basePath).resolve(relativePath);
        if (!Files.exists(path)) return null;

        return new FileSystemResource(path);
    }

    /** ---------------- 확장자 추출 ---------------- */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : "";
    }
}
