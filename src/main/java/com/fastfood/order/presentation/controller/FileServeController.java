package com.fastfood.order.presentation.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileServeController {
    
    @Value("${app.upload.directory:../frontend/src/assets/images}")
    private String uploadDirectory;
    
    @GetMapping("/serve")
    public ResponseEntity<Resource> serveFile(@RequestParam("path") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                log.warn("File serve request with empty path");
                return ResponseEntity.badRequest().build();
            }
            
            // Remove /assets/images/ prefix if present
            String relativePath = filePath.replace("/assets/images/", "").replace("assets/images/", "");
            
            // Build full file path
            Path fullPath = Paths.get(uploadDirectory, relativePath);
            File file = fullPath.toFile();
            
            log.debug("Serving file - Request path: {}, Relative path: {}, Full path: {}, Exists: {}", 
                    filePath, relativePath, fullPath, file.exists());
            
            if (!file.exists() || !file.isFile()) {
                log.warn("File not found: {} (resolved from: {})", fullPath, filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = Files.probeContentType(fullPath);
            if (contentType == null) {
                // Fallback to common image types based on extension
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".png")) contentType = "image/png";
                else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (fileName.endsWith(".gif")) contentType = "image/gif";
                else if (fileName.endsWith(".webp")) contentType = "image/webp";
                else contentType = "application/octet-stream";
            }
            
            Resource resource = new FileSystemResource(file);
            
            log.info("Serving file successfully: {} (Content-Type: {})", fullPath, contentType);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error serving file: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

