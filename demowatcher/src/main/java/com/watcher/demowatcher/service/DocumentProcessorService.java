package com.watcher.demowatcher.service;

import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.model.Document;
import com.watcher.demowatcher.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessorService {

    private final DocumentRepository documentRepository;
    private final CsvProcessorService csvProcessorService;
    private final PdfProcessorService pdfProcessorService;
    private final ExcelProcessorService excelProcessorService;
    private final ImageProcessorService imageProcessorService;
    
    @Value("${app.processed.dir}")
    private String processedDir;
    
    @Value("${app.failed.dir}")
    private String failedDir;

    @Async
    public void processDocument(Long documentId) {
        log.info("Starting to process document ID: {}", documentId);
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        try {
            // Update status to PROCESSING
            document.setProcessingStatus(ProcessingStatus.PROCESSING);
            documentRepository.save(document);
            
            // Process based on file type
            String metadata = null;
            switch (document.getFileType()) {
                case CSV:
                    log.info("Processing CSV file: {}", document.getFileName());
                    metadata = csvProcessorService.processCsv(document);
                    break;
                    
                case PDF:
                    log.info("Processing PDF file: {}", document.getFileName());
                    metadata = pdfProcessorService.processPdf(document);
                    break;
                    
                case EXCEL:
                    log.info("Processing Excel file: {}", document.getFileName());
                    metadata = "Excel processing not yet implemented";
                    break;
                    
                case IMAGE:
                    log.info("Processing Image file: {}", document.getFileName());
                    metadata = "Image processing not yet implemented";
                    break;
                    
                default:
                    log.warn("Unknown file type: {}", document.getFileType());
                    metadata = "File type not supported for processing";
            }
            
            // Mark as completed
            document.setProcessingStatus(ProcessingStatus.COMPLETED);
            document.setProcessedAt(LocalDateTime.now());
            document.setMetadata(metadata);
            documentRepository.save(document);
            
            // Move file to processed folder
            moveToProcessed(document);
            
            log.info("Successfully processed document ID: {}", documentId);
            
        } catch (Exception e) {
            log.error("Error processing document ID {}: {}", documentId, e.getMessage(), e);
            
            // Mark as failed
            document.setProcessingStatus(ProcessingStatus.FAILED);
            document.setErrorMessage(e.getMessage());
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);
            
            // Move file to failed folder
            moveToFailed(document);
        }
    }

    private void moveToProcessed(Document document) {
        try {
            Path sourcePath = Paths.get(document.getFilePath());
            Path targetPath = Paths.get(processedDir, document.getFileName());
            
            // Create directory if not exists
            Files.createDirectories(targetPath.getParent());
            
            // Move file
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update file path in database
            document.setFilePath(targetPath.toString());
            documentRepository.save(document);
            
            log.info("Moved file to processed folder: {}", targetPath);
            
        } catch (IOException e) {
            log.error("Error moving file to processed folder: {}", e.getMessage(), e);
        }
    }

    private void moveToFailed(Document document) {
        try {
            Path sourcePath = Paths.get(document.getFilePath());
            Path targetPath = Paths.get(failedDir, document.getFileName());
            
            // Create directory if not exists
            Files.createDirectories(targetPath.getParent());
            
            // Move file
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Update file path in database
            document.setFilePath(targetPath.toString());
            documentRepository.save(document);
            
            log.info("Moved file to failed folder: {}", targetPath);
            
        } catch (IOException e) {
            log.error("Error moving file to failed folder: {}", e.getMessage(), e);
        }
    }
}