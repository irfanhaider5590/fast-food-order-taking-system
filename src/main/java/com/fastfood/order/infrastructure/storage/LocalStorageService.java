package com.fastfood.order.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path rootDirectory;

    public LocalStorageService(@Value("${app.upload.directory:uploads}") String uploadDirectory) {
        this.rootDirectory = Paths.get(uploadDirectory).toAbsolutePath().normalize();
    }

    @Override
    public StoredObject store(MultipartFile file, String folder) {
        try {
            String safeFolder = (folder == null || folder.isBlank()) ? "general" : folder.replaceAll("[^a-zA-Z0-9_-]", "");
            Path folderPath = rootDirectory.resolve(safeFolder);
            Files.createDirectories(folderPath);

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
            }

            String uniqueFilename = UUID.randomUUID() + extension;
            Path filePath = folderPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String relativePath = safeFolder + "/" + uniqueFilename;
            String publicUrl = "/api/files/serve?path=" + relativePath;
            log.info("Stored file locally at {}", filePath);
            return new StoredObject(relativePath, publicUrl, uniqueFilename);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String relativePath) {
        try {
            Path fullPath = resolveSafe(relativePath);
            if (Files.exists(fullPath)) {
                Files.delete(fullPath);
                return true;
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Resource> loadAsResource(String relativePath) {
        Path fullPath = resolveSafe(relativePath);
        if (!Files.exists(fullPath) || !Files.isRegularFile(fullPath)) {
            return Optional.empty();
        }
        return Optional.of(new FileSystemResource(fullPath));
    }

    private Path resolveSafe(String relativePath) {
        String cleaned = relativePath == null ? "" : relativePath
                .replace("/assets/images/", "")
                .replace("assets/images/", "")
                .replace("\\", "/");
        while (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        Path resolved = rootDirectory.resolve(cleaned).normalize();
        if (!resolved.startsWith(rootDirectory)) {
            throw new RuntimeException("Invalid file path");
        }
        return resolved;
    }
}
