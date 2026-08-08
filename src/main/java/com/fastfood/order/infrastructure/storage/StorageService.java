package com.fastfood.order.infrastructure.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface StorageService {

    StoredObject store(MultipartFile file, String folder);

    boolean delete(String relativePath);

    Optional<Resource> loadAsResource(String relativePath);

    record StoredObject(String relativePath, String publicUrl, String filename) {}
}
