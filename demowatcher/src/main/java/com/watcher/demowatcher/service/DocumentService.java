package com.watcher.demowatcher.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.dto.DocumentResponseDTO;
import com.watcher.demowatcher.dto.UploadResponseDTO;
import com.watcher.demowatcher.exception.FileProcessingException;
import com.watcher.demowatcher.model.Document;
import com.watcher.demowatcher.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
	
	@Autowired
	private DocumentRepository documentRepository;
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    public UploadResponseDTO uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileProcessingException("File is empty");
        }
        
        try {
            // Create upload directory if not exists
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir, uniqueFilename);
            
            // Save file to disk
            Files.copy(file.getInputStream(), filePath);
            
            // Create document record
            Document document = new Document();
            document.setFileName(uniqueFilename);
            document.setOriginalName(originalFilename);
            document.setFileType(FileType.fromFileName(originalFilename));
            document.setFileSize(file.getSize());
            document.setFilePath(filePath.toString());
            document.setProcessingStatus(ProcessingStatus.PENDING);
            
            // Save to database
            Document savedDocument = documentRepository.save(document);
            
            log.info("File uploaded successfully: {}", uniqueFilename);
            
            // Return response
            UploadResponseDTO response = new UploadResponseDTO();
            response.setId(savedDocument.getId());
            response.setFileName(savedDocument.getFileName());
            response.setOriginalName(savedDocument.getOriginalName());
            response.setFileType(savedDocument.getFileType());
            response.setFileSize(savedDocument.getFileSize());
            response.setStatus(savedDocument.getProcessingStatus());
            response.setMessage("File uploaded successfully");
            
            return response;
            
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new FileProcessingException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    public List<DocumentResponseDTO> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public DocumentResponseDTO getDocumentById(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileProcessingException("Document not found with id: " + id));
        return convertToDTO(document);
    }
    
    public List<DocumentResponseDTO> getDocumentsByStatus(ProcessingStatus status) {
        return documentRepository.findByProcessingStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<DocumentResponseDTO> getDocumentsByType(FileType fileType) {
        return documentRepository.findByFileType(fileType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public void deleteDocument(Long id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new FileProcessingException("Document not found with id: " + id));
        
        // Delete physical file
        try {
            Path filePath = Paths.get(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
        }
        
        // Delete from database
        documentRepository.delete(document);
        log.info("Document deleted: {}", document.getFileName());
    }
    
    private DocumentResponseDTO convertToDTO(Document document) {
        DocumentResponseDTO dto = new DocumentResponseDTO();
        dto.setId(document.getId());
        dto.setFileName(document.getFileName());
        dto.setOriginalName(document.getOriginalName());
        dto.setFileType(document.getFileType());
        dto.setFileSize(document.getFileSize());
        dto.setFilePath(document.getFilePath());
        dto.setProcessingStatus(document.getProcessingStatus());
        dto.setUploadedAt(document.getUploadedAt());
        dto.setProcessedAt(document.getProcessedAt());
        dto.setErrorMessage(document.getErrorMessage());
        dto.setMetadata(document.getMetadata());
        return dto;
    }

}
