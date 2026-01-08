package com.watcher.demowatcher.dto;

import java.time.LocalDateTime;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponseDTO {
	
	private Long id;
    private String fileName;
    private String originalName;
    private FileType fileType;
    private Long fileSize;
    private String filePath;
    private ProcessingStatus processingStatus;
    private LocalDateTime uploadedAt;
    private LocalDateTime processedAt;
    private String errorMessage;
    private String metadata;
}
