package com.watcher.demowatcher.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.watcher.demowatcher.constants.FileType;
import com.watcher.demowatcher.constants.ProcessingStatus;
import com.watcher.demowatcher.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>{


    // Find by status
    List<Document> findByProcessingStatus(ProcessingStatus status);
    
    // Find by file type
    List<Document> findByFileType(FileType fileType);
    
    // Find by date range
    List<Document> findByUploadedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find failed documents
    List<Document> findByProcessingStatusOrderByUploadedAtDesc(ProcessingStatus status);
    
    // Count by status
    long countByProcessingStatus(ProcessingStatus status);
    
    // Count by file type
    long countByFileType(FileType fileType);
    
    // Get recent documents
    List<Document> findTop10ByOrderByUploadedAtDesc();
    
    // Custom query for statistics
    @Query("SELECT d.fileType, COUNT(d) FROM Document d GROUP BY d.fileType")
    List<Object[]> countByFileTypeGrouped();
    
    @Query("SELECT d.processingStatus, COUNT(d) FROM Document d GROUP BY d.processingStatus")
    List<Object[]> countByStatusGrouped();
}
