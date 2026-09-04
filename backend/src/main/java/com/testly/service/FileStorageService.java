package com.testly.service;

import com.testly.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 2L * 1024 * 1024; // 2MB

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Validates and stores an uploaded image, returning a public URL path (e.g. "/uploads/xyz.png").
     * Never trust the client: re-check content type and size here even if the frontend already checked.
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("Uploaded file is empty", HttpStatus.BAD_REQUEST);
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ApiException("Only JPEG, PNG, or WEBP images are allowed", HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new ApiException("Image must be smaller than 2MB", HttpStatus.BAD_REQUEST);
        }

        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            String ext = switch (file.getContentType()) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
            String filename = UUID.randomUUID() + ext;
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);

            return "/uploads/" + filename;

            // NOTE: for production, consider resizing/compressing here (e.g. with Thumbnailator)
            // before writing to disk, and/or swap this for an S3/Cloudinary client.
        } catch (IOException e) {
            throw new ApiException("Failed to store image: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<String> allowedTypes() {
        return List.copyOf(ALLOWED_TYPES);
    }
}
