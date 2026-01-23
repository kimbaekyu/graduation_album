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

@Service
public class FileStorageService {

    @Value("${app.media.base-path}")
    private String basePath;

    /** 일반 파일 저장 */
    public String save(MultipartFile file, String relativePath) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path path = Paths.get(basePath).resolve(relativePath);
        Files.createDirectories(path.getParent());
        file.transferTo(path.toFile());

        return relativePath.replace("\\", "/");
    }

    /** 디렉토리 + 파일명 지정 */
    public String save(MultipartFile file, String dir, String filename) throws IOException {
        if (file == null || file.isEmpty()) return null;

        dir = dir.replaceAll("/+$", "");
        Path dirPath = Paths.get(basePath).resolve(dir);
        Files.createDirectories(dirPath);

        Path filePath = dirPath.resolve(filename);
        file.transferTo(filePath.toFile());

        return dir + "/" + filename;
    }

    /** 사진 저장 (자동 jpg) */
    public String savePhoto(MultipartFile file, String dir, Long id) throws IOException {
        return save(file, dir, id + ".jpg");
    }

    /** 영상 저장 (자동 mp4) */
    public String saveVideo(MultipartFile file, String dir, String name) throws IOException {
        return save(file, dir, name + ".mp4");
    }

    /** 파일 로드 */
    public Resource load(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return null;

        Path path = Paths.get(basePath).resolve(relativePath);
        if (!Files.exists(path)) return null;

        return new FileSystemResource(path);
    }
}
