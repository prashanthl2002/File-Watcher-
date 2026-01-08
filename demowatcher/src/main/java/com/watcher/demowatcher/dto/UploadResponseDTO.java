package com.watcher.demowatcher.dto;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResponseDTO {

	private Long id;
    private String fileName;
    private String originalName;
    private FileType fileType;
    private Long fileSize;
    private ProcessingStatus status;
    private String message;
}
