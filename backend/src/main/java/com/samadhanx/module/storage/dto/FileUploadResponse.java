package com.samadhanx.module.storage.dto;

import com.samadhanx.module.challenge.entity.enums.MediaType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String fileName;
    private String originalFileName;
    private String fileUrl;
    private MediaType mediaType;
    private String mimeType;
    private long fileSizeBytes;
}
