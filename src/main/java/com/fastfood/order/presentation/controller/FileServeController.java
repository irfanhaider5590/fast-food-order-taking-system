package com.fastfood.order.presentation.controller;

import com.fastfood.order.infrastructure.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileServeController {

    private final StorageService storageService;

    @GetMapping("/serve")
    public ResponseEntity<Resource> serveFile(@RequestParam("path") String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            Optional<Resource> resourceOpt = storageService.loadAsResource(filePath);
            if (resourceOpt.isEmpty()) {
                log.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = resourceOpt.get();
            String fileName = resource.getFilename() != null ? resource.getFilename().toLowerCase() : "";
            String contentType = "application/octet-stream";
            if (fileName.endsWith(".png")) contentType = "image/png";
            else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (fileName.endsWith(".gif")) contentType = "image/gif";
            else if (fileName.endsWith(".webp")) contentType = "image/webp";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                    .body(resource);
        } catch (Exception e) {
            log.error("Error serving file: {}", filePath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
