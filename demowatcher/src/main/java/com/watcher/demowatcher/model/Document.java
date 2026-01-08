package com.watcher.demowatcher.model;

import java.time.LocalDateTime;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Document {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String originalName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false, length = 500)
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
    
    private LocalDateTime processedAt;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(columnDefinition = "TEXT")
    private String metadata;
    
    
    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        if (processingStatus == null) {
            processingStatus = ProcessingStatus.PENDING;
        }
    }
    
	
}
