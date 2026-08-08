package com.fastfood.order.infrastructure.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Placeholder for cloud object storage (S3/R2/MinIO).
 * Enable with app.storage.type=s3 and wire AWS SDK when deploying multi-instance cloud.
 */
@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    @Override
    public StoredObject store(MultipartFile file, String folder) {
        throw new UnsupportedOperationException("S3 storage is not configured yet. Set app.storage.type=local or implement AWS SDK wiring.");
    }

    @Override
    public boolean delete(String relativePath) {
        throw new UnsupportedOperationException("S3 storage is not configured yet.");
    }

    @Override
    public Optional<Resource> loadAsResource(String relativePath) {
        throw new UnsupportedOperationException("S3 storage is not configured yet.");
    }
}
