package com.samadhanx.module.storage.service;

import com.samadhanx.module.storage.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileUploadResponse storeFile(MultipartFile file);
    Resource loadFileAsResource(String fileName);
}
