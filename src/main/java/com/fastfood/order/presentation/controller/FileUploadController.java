package com.fastfood.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileUploadController {
    
    @Value("${app.upload.directory:../frontend/src/assets/images}")
    private String uploadDirectory;
    
    // Allowed image file extensions
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
                log.warn("Upload attempt with empty file");
                return ResponseEntity.badRequest().body(createErrorResponse("File is empty"));
            }
            
            // Get file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            
            // Validate file type by extension (more reliable than content type)
            boolean isValidExtension = !extension.isEmpty() && ALLOWED_EXTENSIONS.contains(extension);
            String contentType = file.getContentType();
            boolean isValidContentType = contentType != null && contentType.startsWith("image/");
            
            log.debug("File validation - filename: {}, extension: {}, contentType: {}, isValidExtension: {}, isValidContentType: {}", 
                    originalFilename, extension, contentType, isValidExtension, isValidContentType);
            
            if (!isValidExtension && !isValidContentType) {
                log.warn("File rejected - extension: {}, contentType: {}", extension, contentType);
                return ResponseEntity.badRequest().body(createErrorResponse(
                    "Only image files are allowed. Supported formats: " + String.join(", ", ALLOWED_EXTENSIONS)));
            }
            
            // Create upload directory if it doesn't exist
            String folderPath = uploadDirectory + "/" + folder;
            Path uploadPath = Paths.get(folderPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Created upload directory: {}", uploadPath);
            }
            
            // Generate unique filename with original extension
            String uniqueFilename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(uniqueFilename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved successfully - Path: {}, Size: {} bytes, ContentType: {}", 
                    filePath, file.getSize(), contentType);
            
            // Return relative path for frontend
            // Use backend API endpoint to serve images instead of direct assets path
            String relativePath = "/assets/images/" + folder + "/" + uniqueFilename;
            String apiPath = "/api/files/serve?path=" + relativePath;
            
            Map<String, String> response = new HashMap<>();
            response.put("url", apiPath); // Use API endpoint instead of direct path
            response.put("path", relativePath); // Keep original path for reference
            response.put("filename", uniqueFilename);
            
            log.info("File upload successful - API URL: {}, Original path: {}", apiPath, relativePath);
            
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
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
            // Remove /assets/images/ prefix if present
            String relativePath = filePath.replace("/assets/images/", "");
            Path fullPath = Paths.get(uploadDirectory, relativePath);
            
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                log.info("File deleted successfully: {}", fullPath);
                return ResponseEntity.ok(createSuccessResponse("File deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (IOException e) {
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

