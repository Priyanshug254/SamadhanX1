package com.samadhanx.module.storage.service;

import com.samadhanx.common.exception.BadRequestException;
import com.samadhanx.common.exception.ResourceNotFoundException;
import com.samadhanx.module.challenge.entity.enums.MediaType;
import com.samadhanx.module.storage.dto.FileUploadResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public LocalFileStorageServiceImpl(@Value("${samadhanx.storage.upload-dir:./uploads}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public FileUploadResponse storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }

        String storedFileName = UUID.randomUUID() + extension;

        try {
            if (storedFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence: " + originalFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String mimeType = file.getContentType();
            MediaType mediaType = determineMediaType(mimeType, extension);

            return FileUploadResponse.builder()
                    .fileName(storedFileName)
                    .originalFileName(originalFileName)
                    .fileUrl("/api/v1/files/" + storedFileName)
                    .mediaType(mediaType)
                    .mimeType(mimeType)
                    .fileSizeBytes(file.getSize())
                    .build();
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("File", "fileName", fileName);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("File", "fileName", fileName);
        }
    }

    private MediaType determineMediaType(String mimeType, String extension) {
        if (mimeType != null) {
            if (mimeType.startsWith("image/")) return MediaType.IMAGE;
            if (mimeType.startsWith("video/")) return MediaType.VIDEO;
            if (mimeType.startsWith("audio/")) return MediaType.AUDIO;
            if (mimeType.contains("pdf") || mimeType.contains("word") || mimeType.contains("document") || mimeType.contains("text")) {
                return MediaType.DOCUMENT;
            }
        }

        String lowerExt = extension.toLowerCase();
        if (lowerExt.endsWith(".jpg") || lowerExt.endsWith(".jpeg") || lowerExt.endsWith(".png") || lowerExt.endsWith(".webp")) {
            return MediaType.IMAGE;
        }
        if (lowerExt.endsWith(".mp4") || lowerExt.endsWith(".mkv") || lowerExt.endsWith(".mov")) {
            return MediaType.VIDEO;
        }
        if (lowerExt.endsWith(".mp3") || lowerExt.endsWith(".wav") || lowerExt.endsWith(".m4a")) {
            return MediaType.AUDIO;
        }
        return MediaType.DOCUMENT;
    }
}
