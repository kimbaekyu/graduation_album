package com.elementaryschool.graduation_album.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageService {

    private final Path basePath;

    public FileStorageService(@Value("${app.storage.base-path:./storage}") String basePath) {
        this.basePath = Path.of(basePath).toAbsolutePath().normalize();
    }

    public String save(MultipartFile file, String subdir, String prefix) throws IOException {
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null) {
            int idx = original.lastIndexOf('.');
            if (idx >= 0 && idx < original.length() - 1) {
                ext = original.substring(idx);
            }
        }

        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        String safePrefix = (prefix == null || prefix.isBlank()) ? "file" : prefix.replaceAll("[^a-zA-Z0-9_-]", "_");
        String filename = safePrefix + "_" + ts + ext;

        Path dir = basePath.resolve(subdir == null ? "" : subdir).normalize();
        Files.createDirectories(dir);

        Path target = dir.resolve(filename).normalize();
        if (!target.startsWith(dir)) {
            throw new IOException("잘못된 경로");
        }

        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        // DB에는 basePath 기준 상대경로로 저장 (OS별 경로 구분자 차이 최소화)
        return basePath.relativize(target).toString().replace("\\", "/");
    }

    public Path getBasePath() {
        return basePath;
    }
}

