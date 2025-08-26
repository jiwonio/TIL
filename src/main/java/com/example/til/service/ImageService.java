package com.example.til.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public String saveProfileImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (!isImage(file)) {
            throw new IllegalArgumentException("Invalid image type");
        }
        // Create date-based directory
        Path base = Paths.get(uploadDir, "profiles", LocalDate.now().toString());
        Files.createDirectories(base);

        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replaceAll("-", "") + (ext.isBlank() ? ".jpg" : (ext.startsWith(".") ? ext : "." + ext));
        Path destination = base.resolve(filename);

        // Read and resize to max 512x512, compress to ~80% quality
        Thumbnails.of(file.getInputStream())
            .size(512, 512)
            .outputQuality(0.8)
            .toFile(destination.toFile());

        return destination.toString().replace('\\', '/');
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return false;
        return contentType.startsWith("image/") && !contentType.contains("svg"); // avoid SVG
    }

    private String getExtension(String name) {
        if (!StringUtils.hasText(name)) return "";
        int i = name.lastIndexOf('.');
        return i >= 0 ? name.substring(i) : "";
    }
}
