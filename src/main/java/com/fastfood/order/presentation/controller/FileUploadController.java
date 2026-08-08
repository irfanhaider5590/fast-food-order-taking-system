package com.fastfood.order.presentation.controller;

import com.fastfood.order.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {

    private final StorageService storageService;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".ico", ".tiff", ".tif"
    );

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "general") String folder,
            Authentication authentication) {
        log.info("POST /api/files/upload - Uploading file: {}, folder: {}", file.getOriginalFilename(), folder);

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }

            boolean isValidExtension = !extension.isEmpty() && ALLOWED_EXTENSIONS.contains(extension);
            String contentType = file.getContentType();
            boolean isValidContentType = contentType != null && contentType.startsWith("image/");

            if (!isValidExtension && !isValidContentType) {
                return ResponseEntity.badRequest().body(createErrorResponse(
                    "Only image files are allowed. Supported formats: " + String.join(", ", ALLOWED_EXTENSIONS)));
            }

            StorageService.StoredObject stored = storageService.store(file, folder);
            Map<String, String> response = new HashMap<>();
            response.put("url", stored.publicUrl());
            response.put("path", stored.relativePath());
            response.put("filename", stored.filename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to upload file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(
            @RequestParam("path") String filePath,
            Authentication authentication) {
        log.info("DELETE /api/files/delete - Deleting file: {}", filePath);
        try {
            if (storageService.delete(filePath)) {
                return ResponseEntity.ok(createSuccessResponse("File deleted successfully"));
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to delete file: " + e.getMessage()));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}
